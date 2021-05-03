package hu.bme.aut.server.domain;

import hu.bme.aut.server.domain.database.LightData;
import hu.bme.aut.server.domain.database.MeasurementDay;
import hu.bme.aut.server.domain.restapi.LightSettingsBody;
import hu.bme.aut.server.repository.MeasurementDayRepository;
import hu.bme.aut.server.repository.ServerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Singleton class for main logic in domain
 */
@Component
@Scope("singleton")
public final class LightModel {

    /**
     * Repository for light data
     */
    @Autowired
    private ServerRepository serverRepository;

    /**
     * Repository for measurement days
     */
    @Autowired
    private MeasurementDayRepository measurementDayRepository;

    /**
     * Mode switch: if kernel module does not exist it must be false
     */
    private final boolean RASPI_MODE = false;
    /**
     * Mode switch for debug messages
     */
    private final boolean DEBUG_MODE = false;

    /**
     * Light model for singleton scheme
     */
    private static LightModel lightModel;

    /**
     * Validation boundaries
     */
    private static final double microsecLow = 50;           // 50 ms
    private static final double microsecHigh = 5000;        // 5000 ms
    private static final int measurementPeriod = 2*60000;   // TODO 15 min (most 2 min)

    /**
     * Timer for light switch
     */
    private Timer systemOnOffTimer;
    /**
     * Timer for measurement write purposes
     */
    private Timer measureWriteTimer;

    /**
     * Sensor sensitivity (stored in percentage)
     */
    private Integer sensitivity;
    /**
     * Times for system switch (from and until boundaries)
     */
    private LocalTime systemOnFrom;
    private LocalTime systemOnUntil;

    /**
     * Constructor with default system settings
     */
    private LightModel() {
        if (DEBUG_MODE) {
            System.out.println(" [ DEBUG ] LightModel c'tor begin at " + LocalDateTime.now().toString());
        }
        changeSystemSettings(LocalTime.parse("10:00"), LocalTime.parse("18:00"), 50);
        if (DEBUG_MODE) {
            System.out.println(" [ DEBUG ] LightModel c'tor end at " + LocalDateTime.now().toString());
        }
    }

    /**
     * Initialize (start) timers after constructor
     */
    @PostConstruct
    private void initializeTimers() {
        if (DEBUG_MODE) {
            System.out.println(" [ DEBUG ] initializeTimers() call at " + LocalDateTime.now().toString());
        }
        // only if system is on
        if(LocalTime.now().compareTo(systemOnFrom) >= 0 && LocalTime.now().compareTo(systemOnUntil) < 0) {
            setSystemTurnOffTimer();
            turnSystemOn();
        } else {
            turnSystemOff();
            setSystemTurnOnTimer();
        }
    }

    /**
     * Sets sensitivity, from and until settings
     *
     * @return  wrapped settings in LightSettingsBody
     */
    public LightSettingsBody exportSettings() {
        if (DEBUG_MODE) {
            System.out.println(" [ DEBUG ] exportSettings() call at " + LocalDateTime.now().toString());
        }
        return new LightSettingsBody(sensitivity, systemOnFrom, systemOnUntil);
    }

    /**
     * Change system setting to given parameters
     * Stops both timers before reset
     * Used by initialization and settings changing
     *
     * @param from          measure from
     * @param until         measure until
     * @param sensitivity   sensitivity for switch
     */
    private void changeSystemSettings(LocalTime from, LocalTime until, int sensitivity) {
        if(systemOnOffTimer!=null) { systemOnOffTimer.cancel(); }
        if(measureWriteTimer!=null) { measureWriteTimer.cancel(); }

        systemOnFrom = from;
        systemOnUntil = until;
        this.sensitivity = sensitivity;
    }

    /**
     * Restart whole system with given settings
     * Reset settings and reinitialize timers
     * @param from          measure from
     * @param until         measure until
     * @param sensitivity   sensitivity for switch
     */
    public void restartWithSettings(LocalTime from, LocalTime until, int sensitivity) {
        if (DEBUG_MODE) {
            System.out.println(" [ DEBUG ] restartWithSettings( " + from + ", " + until + ", " + sensitivity + " ) call at " + LocalDateTime.now().toString());
        }
        changeSystemSettings(from, until, sensitivity);
        initializeTimers();
    }

    /**
     * Start switch timer
     * Should only be called, when time is past (or equals) turn off (and before turn on)
     */
    private void setSystemTurnOnTimer() {
        // sanity check
        if(systemOnOffTimer!=null) { systemOnOffTimer.cancel(); }
        TimerTask turnOn = new TimerTask() {
            @Override
            public void run() {
                setSystemTurnOffTimer();
                turnSystemOn();
            }
        };
        // time of turning on can be today or tomorrow (as the user can update the settings arbitrarily)
        LocalDateTime turnOnDateTime = LocalDateTime.now().with(systemOnFrom);
        if(turnOnDateTime.isBefore(LocalDateTime.now())) {
            // we know, that right now the system should be turned off, so if we are past the turn On time
            // then we are past the whole truned on interval and should turn on next tomorrow
            turnOnDateTime = turnOnDateTime.plusDays(1);
        }
        Date turnOnDate = Date.from(turnOnDateTime.atZone(ZoneId.systemDefault()).toInstant());

        systemOnOffTimer = new Timer();
        systemOnOffTimer.schedule(turnOn, turnOnDate);
        if (DEBUG_MODE) {
            System.out.println(" [ DEBUG ] setSystemTurnOnTimer call to " + turnOnDateTime.toString() + " at " + LocalDateTime.now().toString());
        }
    }

    /**
     * Stop switch timer
     * Should only be called, when time is past (or equals) turn on (and before turn off)
     */
    private void setSystemTurnOffTimer() {
        // sanity check
        if(systemOnOffTimer!=null) systemOnOffTimer.cancel();
        TimerTask turnOff = new TimerTask() {
            @Override
            public void run() {
                turnSystemOff();
                setSystemTurnOnTimer();
            }
        };
        // time of turning off must be today
        LocalDateTime turnOffDateTime = LocalDateTime.now().with(systemOnUntil);
        Date turnOffDate = Date.from(turnOffDateTime.atZone(ZoneId.systemDefault()).toInstant());

        systemOnOffTimer = new Timer();
        systemOnOffTimer.schedule(turnOff, turnOffDate);
        if (DEBUG_MODE) {
            System.out.println(" [ DEBUG ] setSystemTurnOffTimer call " + turnOffDateTime.toString() + " at " + LocalDateTime.now().toString());
        }
    }

    /**
     * Start measurement timer
     */
    private void turnSystemOn() {
        // sanity check
        if(measureWriteTimer!=null) {
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
        if (DEBUG_MODE) {
            System.out.println(" [ DEBUG ] turnSystemOn call at " + LocalDateTime.now().toString());
        }
    }

    /**
     * Stop measurement timer
     */
    private void turnSystemOff() {
        if(measureWriteTimer!=null) { // turning the periodic measurements off
            measureWriteTimer.cancel();
        }
        switchLights(false);
        if (DEBUG_MODE) {
            System.out.println(" [ DEBUG ] turnSystemOff call at " + LocalDateTime.now().toString());
        }
    }

    /**
     * Take a measurement, adjusts lights based on that and record measurement in the DB
     */
    private void measureAndSetLights() {
        if (DEBUG_MODE) {
            System.out.println(" [ DEBUG ] measureAndSetLights call at " + LocalDateTime.now().toString());
        }

        LightData record = new LightData();
        record.setMeasureDate(LocalDateTime.now());
        int measurement = takeMeasurement();

        // turn lights on or off, based on measurement
        record.setIsOn(adjustLights(measurement));
        record.setActualValue(measurement);

        // we only record data into the DB in microsec, never in %
        record.setThreshold(percentageToMicrosec(sensitivity));
        record = serverRepository.saveAndFlush(record);

        // record measurement date into table
        MeasurementDay measurementDay = new MeasurementDay(record.getMeasureDate().toLocalDate());
        if (measurementDayRepository.findExistingMeasureDate(measurementDay.getMeasureDate()) == 0) {
            measurementDayRepository.saveAndFlush(measurementDay);
            System.out.println(measurement);
        }
    }

    /**
     * Measure light data (more measurements to filter out outliers and get average)
     *
     * @return  average of the measurements
     */
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
        System.out.println(" [ DEBUG ] Measurement taken at " + LocalDateTime.now().toString() + ", avg is " + avg);
        return avg;
    }

    /**
     * Adjust the light emitter value (turn on/off)
     *
     * @param measurement   measured light data to calculate if needed to change
     * @return              if lights turned on or not
     */
    private boolean adjustLights(int measurement) {
        if (DEBUG_MODE) {
            System.out.println(" [ DEBUG ] Called adjustLights(" + measurement + ") at " + LocalDateTime.now().toString());
        }
        boolean turnLightsOn = false;
        if(percentageToMicrosec(sensitivity) < measurement) {
            turnLightsOn = false;
        } else {
            turnLightsOn = true;
        }
        switchLights(turnLightsOn);
        return turnLightsOn;
    }

    /**
     * Set the light on/off based on previous state
     *
     * @param lightsOn  previous state of light (if on then turn off and the other way
     */
    private void switchLights(boolean lightsOn) {
        if (DEBUG_MODE) {
            System.out.println(" [ DEBUG ] Called switchLights(" + lightsOn + ") at " + LocalDateTime.now().toString());
        }
        if(lightsOn) {
            writeToDevice(1);
        } else {
            writeToDevice(0);
        }
    }

    /**
     * Communicate with kernel module, write on it
     *
     * @param msg   to be written message
     */
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
            // we send an ASCII char
            bufferedWriter.write(msg + 48);
            bufferedWriter.flush();
            if (DEBUG_MODE) {
                System.out.println("[ DEBUG ] stringSentToDriver: " + (char)(msg+48));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Communicate with kernel module, read from it
     *
     * @param bufferedReader    reader device
     * @return                  received value
     */
    private Integer readFromDevice(BufferedReader bufferedReader) {
        int dataReceived = 0;
        try {
            if (RASPI_MODE) {
                String stringReceived = bufferedReader.readLine();
                if (DEBUG_MODE) {
                    System.out.println("[ DEBUG ] stringReceived: " + stringReceived);
                }
                dataReceived = Integer.parseInt(stringReceived);
                if (DEBUG_MODE) {
                    System.out.println("[ DEBUG ] dataReceived: " + dataReceived);
                }
            } else {
                String stringReceived = bufferedReader.readLine();
                if (stringReceived != null) {
                    dataReceived = Integer.parseInt(stringReceived.trim());
                    if (DEBUG_MODE) {
                        System.out.println("[ DEBUG ] dataReceived: " + dataReceived);
                    }
                }
            }
        } catch (IOException ioException) {
            System.err.println("[ ERROR ] Cannot read from file: " + ioException.getMessage());
        }
        return dataReceived;
    }

    /**
     * Convert percentage to microseconds (used for sensitivity)
     *
     * @param percentageValue   to be converted percentage
     * @return                  converted microseconds
     */
    private static int percentageToMicrosec(int percentageValue) {
        // mapping 0-100% to 50-5000ms
        if(percentageValue>100 || percentageValue<0) {
            throw new IndexOutOfBoundsException("Sensitivity should be between 0 and 100");
        }
        // remapping formula (from 1 to 2): "low2 + (value - low1) * (high2 - low2) / (high1 - low1)"
        double value = microsecLow + ((double)percentageValue-0.0) * (microsecHigh - microsecLow) / (100.0 - 0.0);
        return (int) Math.round(value);
    }

    /**
     * Convert microseconds to percentage (used for sensitivity)
     *
     * @param microsecValue     to be converted microseconds
     * @return                  converted percentage
     */
    private static int microsecToPercentage(int microsecValue) {
        // mapping 50-5000ms to 0-100%
        if(microsecValue<50) microsecValue = 50;
        else if(microsecValue>5000) microsecValue = 5000;

        // remapping formula (from 1 to 2): "low2 + (value - low1) * (high2 - low2) / (high1 - low1)"
        double value = 0.0 + ((double)microsecValue- microsecLow) * (100.0-0.0) / (microsecHigh - microsecLow);

        return (int) Math.round(value);
    }
}
