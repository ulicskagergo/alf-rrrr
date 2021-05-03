package hu.bme.aut.server.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import hu.bme.aut.server.ServerApplication;
import hu.bme.aut.server.domain.database.LightData;
import hu.bme.aut.server.domain.database.MeasurementDay;
import hu.bme.aut.server.domain.restapi.LightSettingsBody;
import hu.bme.aut.server.repository.MeasurementDayRepository;
import hu.bme.aut.server.repository.ServerRepository;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.io.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public final class LightModel {

    @Autowired
    private ServerRepository serverRepository;

    @Autowired
    private MeasurementDayRepository measurementDayRepository;

    private final boolean RASPI_MODE = false;

    private static LightModel lightModel;

    private static final double microsecLow = 50; // 50 ms
    private static final double microsecHigh = 5000; // 5000 ms
    private static final int measurementPeriod = 15000; // 15 min

    private Timer systemOnOffTimer;
    private Timer measureWriteTimer;

    private Integer sensitivity; // sensor sensitivity in %
    private LocalTime systemOnFrom;
    private LocalTime systemOnUntil;

    public static LightModel getInstance() {
        if(lightModel==null) {
            lightModel = new LightModel();
        }
        return lightModel;
    }

    private LightModel() {
        // set default settings
        changeSystemSettings(LocalTime.parse("10:00"), LocalTime.parse("18:00"), 50);
    }

    @PostConstruct
    private void initializeTimers() {
        // start the appropriate timers
        if(LocalTime.now().compareTo(systemOnFrom) >= 0 && LocalTime.now().compareTo(systemOnUntil) < 0) { // system should be on right now
            setSystemTurnOffTimer();
            turnSystemOn(); // sets the measurement timer (in later cases turnSystemOn() will be called by setSystemTurnOnTimer)
        } else { // system is off
            turnSystemOff();
            setSystemTurnOnTimer();
        }
    }

    public LightSettingsBody exportSettings() {
        return new LightSettingsBody(sensitivity, systemOnFrom, systemOnUntil);
    }

    // TODO timer restart hol?
    public void changeSystemSettings(LocalTime from, LocalTime until, int sensitivity) { // both for initialization and when changing settings
        if(systemOnOffTimer!=null) { systemOnOffTimer.cancel(); }
        if(measureWriteTimer!=null) { measureWriteTimer.cancel(); }

        systemOnFrom = from;
        systemOnUntil = until;
        this.sensitivity = sensitivity;
    }

    // should only be called, when time is past (or equals) turn off (and before turn on)
    private void setSystemTurnOnTimer() {
        if(systemOnOffTimer!=null) { systemOnOffTimer.cancel(); } // should never be the case?
        TimerTask turnOn = new TimerTask() {
            @Override
            public void run() {
                setSystemTurnOffTimer();
                turnSystemOn();
            }
        };
        LocalDateTime turnOnDateTime = LocalDateTime.now().with(systemOnFrom);
        Date turnOnDate = Date.from(turnOnDateTime.atZone(ZoneId.systemDefault()).toInstant());

        systemOnOffTimer = new Timer();
        systemOnOffTimer.schedule(turnOn, turnOnDate);
    }

    // should only be called, when time is past (or equals) turn on (and before turn off)
    private void setSystemTurnOffTimer() {
        if(systemOnOffTimer!=null) systemOnOffTimer.cancel(); // should never be the case?
        TimerTask turnOff = new TimerTask() {
            @Override
            public void run() {
                turnSystemOff();
                setSystemTurnOnTimer();
            }
        };
        LocalDateTime turnOffDateTime = LocalDateTime.now().with(systemOnUntil);
        Date turnOffDate = Date.from(turnOffDateTime.atZone(ZoneId.systemDefault()).toInstant());

        systemOnOffTimer = new Timer();
        systemOnOffTimer.schedule(turnOff, turnOffDate);
    }

    private void turnSystemOn() {
        if(measureWriteTimer!=null) { // just to be sure, but should never happen
            measureWriteTimer.cancel();
        }

        TimerTask periodicMeasurement = new TimerTask() {
            @Override
            public void run() {
                measureAndSetLights();
            }
        };

        measureWriteTimer = new Timer();
        measureWriteTimer.schedule(periodicMeasurement, 0, measurementPeriod);
    }

    private void turnSystemOff() {
        if(measureWriteTimer!=null) { // turning the periodic measurements off
            measureWriteTimer.cancel();
        }
        switchLights(false);
    }

    // takes a measurement, adjusts lights based on that and records measurement in the DB
    private void measureAndSetLights() {
        LightData record = new LightData();
        record.setMeasureDate(LocalDateTime.now());
        int measurement = takeMeasurement();
        record.setIsOn(adjustLights(measurement)); // turn lights on or off, based on measurement
        record.setActualValue(measurement);
        record.setThreshold(percentageToMicrosec(sensitivity)); // we only record data into the DB in microsec, never in %
        record = serverRepository.saveAndFlush(record);

        // record measurement date into table
        MeasurementDay measurementDay = new MeasurementDay(record.getMeasureDate().toLocalDate());
        if (measurementDayRepository.findExistingMeasureDate(measurementDay.getMeasureDate()) == 0) {
            measurementDayRepository.saveAndFlush(measurementDay);
            System.out.println(measurement);
        }
    }

    private int takeMeasurement() {
        int avg = 0;
        try {
            BufferedReader bufferedReader;
            if (RASPI_MODE) {
                bufferedReader = new BufferedReader(new FileReader("/dev/ldrchar"));
            } else {
                bufferedReader = new BufferedReader(new FileReader("tmp.txt"));
            }

            // we'll take 51 separate measurements and work on that
            int[] measurements = new int[50];
            for(int i = 0; i < 50; i++) {
                measurements[i] = readFromDevice(bufferedReader);
            }
            bufferedReader.close();

            // https://miro.medium.com/max/8000/1*0MPDTLn8KoLApoFvI0P2vQ.png
            // filter outliers and get the avg of the rest
            Arrays.sort(measurements);
            int Q1 = measurements[12];
            int Q3 = measurements[37];
            int IQR = Q3-Q1;
            int min = (int) Math.round( Q1 - 1.5*IQR ); // below this value the measurements are outliers
            int max = (int) Math.round( Q3 + 1.5*IQR ); // over this value the measurements are outliers

            int validValueCount = 0;
            for(int i = 0; i < 50; i++) {
                if(measurements[i] >= min && measurements[i] <= max) {
                    avg += measurements[i];
                    validValueCount++;
                }
            }

            avg = (int) Math.round((double)avg / (double)validValueCount);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return avg;
    }

    private boolean adjustLights(int measurement) {
        boolean turnLightsOn = false;
        if(percentageToMicrosec(sensitivity) < measurement) {
            turnLightsOn = false;
        } else {
            turnLightsOn = true;
        }
        switchLights(turnLightsOn);
        return turnLightsOn;
    }

    // turns lights on or off, based on the boolean
    private void switchLights(boolean lightsOn) {
        if(lightsOn) {
            writeToDevice(1);
        } else {
            writeToDevice(0);
        }
    }

    private void writeToDevice(int msg) {
        if(msg!=0 && msg!=1) {
            throw new RuntimeException("Sending wrong message to kernel module (should be 0 or 1");
        }

        try {
            BufferedWriter bufferedWriter;
            if (RASPI_MODE) {
                    bufferedWriter = new BufferedWriter(new FileWriter("/dev/ldrchar"));

            } else {
                bufferedWriter = new BufferedWriter(new FileWriter("tmp.txt"));
            }
            bufferedWriter.write(msg + 48); // we send an ASCII char here
            bufferedWriter.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Integer readFromDevice(BufferedReader bufferedReader) {
        int dataReceived = 0;
        try {
            if (RASPI_MODE) {
                String stringReceived = bufferedReader.readLine();
                System.out.println("[ DEBUG ] stringReceived: " + stringReceived);
                dataReceived = Integer.parseInt(stringReceived);
                System.out.println("[ DEBUG ] dataReceived: " + dataReceived);
            } else {
                String stringReceived = bufferedReader.readLine();
                if (stringReceived != null) {
                    dataReceived = Integer.parseInt(stringReceived.trim());
                    System.out.println("[ DEBUG ] dataReceived: " + dataReceived);
                }
            }
        } catch (IOException ioException) {
            System.err.println("[ ERROR ] Cannot read from file: " + ioException.getMessage());
        }
        return dataReceived;
    }

    private static int percentageToMicrosec(int percentageValue) {
        // mapping 0-100% to 50-5000ms
        if(percentageValue>100 || percentageValue<0) {
            throw new IndexOutOfBoundsException("Sensitivity should be between 0 and 100");
        }
        // remapping formula (from 1 to 2): "low2 + (value - low1) * (high2 - low2) / (high1 - low1)"
        double value = microsecLow + ((double)percentageValue-0.0) * (microsecHigh - microsecLow) / (100.0 - 0.0);
        return (int) Math.round(value);
    }

    // TODO használni, ahol kell
    private static int microsecToPercentage(int microsecValue) {
        // mapping 50-5000ms to 0-100%
        if(microsecValue<50) microsecValue = 50;
        else if(microsecValue>5000) microsecValue = 5000;

        // remapping formula (from 1 to 2): "low2 + (value - low1) * (high2 - low2) / (high1 - low1)"
        double value = 0.0 + ((double)microsecValue- microsecLow) * (100.0-0.0) / (microsecHigh - microsecLow);

        return (int) Math.round(value);
    }

}
