package hu.bme.aut.server.domain;

import hu.bme.aut.server.domain.database.LightData;
import hu.bme.aut.server.domain.database.MeasurementDay;
import hu.bme.aut.server.domain.restapi.LightSettingsBody;
import hu.bme.aut.server.repository.MeasurementDayRepository;
import hu.bme.aut.server.repository.ServerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log=LoggerFactory.getLogger(LightModel.class);

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
    private final boolean RASPI_MODE = true;

    /**
     * Light model for singleton scheme
     */
    private static LightModel lightModel;

    /**
     * Validation boundaries
     */
    private static final int microsecHigh = 1500;        // 2000 ms
    private static final int measurementPeriod = 10*60000; // 15 min

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
        log.debug("LightModel c'tor begin at " + LocalDateTime.now().toString());
        switchLights(false);
        changeSystemSettings(LocalTime.parse("10:00"), LocalTime.parse("18:00"), 50);
        log.debug("LightModel c'tor end at " + LocalDateTime.now().toString());
    }

    /**
     * Initialize (start) timers after constructor
     */
    @PostConstruct
    private void initializeTimers() {
        log.debug("initializeTimers() call at " + LocalDateTime.now().toString());

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
        log.debug("exportSettings() call at " + LocalDateTime.now().toString());
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
        log.info("Settings update to {" + from + ", " + until + ", " + sensitivity + "}");
    }

    /**
     * Restart whole system with given settings
     * Reset settings and reinitialize timers
     * @param from          measure from
     * @param until         measure until
     * @param sensitivity   sensitivity for switch
     */
    public void restartWithSettings(LocalTime from, LocalTime until, int sensitivity) {
        log.debug("restartWithSettings( " + from + ", " + until + ", " + sensitivity + " ) call at " + LocalDateTime.now().toString());
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
        log.info("SystemTurnOnTimer to " + turnOnDateTime.toString() + " at " + LocalDateTime.now().toString());
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
        log.info("setSystemTurnOffTimer to " + turnOffDateTime.toString() + " at " + LocalDateTime.now().toString());
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
        log.info("turnSystemOn call at " + LocalDateTime.now().toString());
    }

    /**
     * Stop measurement timer
     */
    private void turnSystemOff() {
        if(measureWriteTimer!=null) { // turning the periodic measurements off
            measureWriteTimer.cancel();
        }
        switchLights(false);
        log.info("turnSystemOff call at " + LocalDateTime.now().toString());
    }

    /**
     * Take a measurement, adjusts lights based on that and record measurement in the DB
     */
    private void measureAndSetLights() {
        log.debug("measureAndSetLights call at " + LocalDateTime.now().toString());

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
        }
    }

    /**
     * Measure light data (more measurements to filter out outliers and get average)
     *
     * @return  average of the measurements
     */
    private int takeMeasurement() {
        int avg = 0;
            // we'll take 51 separate measurements and work on that
            int[] measurements = new int[50];
            for(int i = 0; i < 50; i++) {
                measurements[i] = readFromDevice();
            }

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


        log.info("Measurement taken at " + LocalDateTime.now().toString() + ", value is " + avg);
        return avg;
    }

    /**
     * Adjust the light emitter value (turn on/off)
     *
     * @param measurement   measured light data to calculate if needed to change
     * @return              if lights turned on or not
     */
    private boolean adjustLights(int measurement) {
        log.debug("Called adjustLights(" + measurement + ") at " + LocalDateTime.now().toString());
        boolean turnLightsOn = false;
        if(percentageToMicrosec(sensitivity) < measurement) {
            turnLightsOn = true;
        } else {
            turnLightsOn = false;
        }
        switchLights(turnLightsOn);
        return turnLightsOn;
    }

    /**
     * Set the light on/off based on previous state
     *
     * @param lightsOn  true - switch lights on; false - switch lights off
     */
    public void switchLights(boolean lightsOn) {
        log.info("Switching lights " + (lightsOn ? "on" : "off") + " at " + LocalDateTime.now().toString());
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
                // we send an ASCII char
                bufferedWriter.write(msg + 48);
                bufferedWriter.flush();
                bufferedWriter.close();
            }
            log.debug("stringSentToDriver: " + (char)(msg+48));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Communicate with kernel module, read from it
     *
     * @return                  received value
     */
    private Integer readFromDevice() {
        int dataReceived = 0;
        try {
            BufferedReader bufferedReader;
            if (RASPI_MODE) {
                bufferedReader = new BufferedReader(new FileReader("/dev/ldrchar"));
                String stringReceived = bufferedReader.readLine();
                log.debug("stringReceived: " + stringReceived);
                dataReceived = Integer.parseInt(stringReceived);
                log.debug("dataReceived: " + dataReceived);
                bufferedReader.close();
            } else {
                dataReceived = 300;
            }

        } catch (IOException ioException) {
            log.error("Cannot read from file: " + ioException.getMessage());
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
        // mapping 0-100% to ms low and high
        if(percentageValue>100 || percentageValue<0) {
            throw new IndexOutOfBoundsException("Sensitivity should be between 0 and 100");
        }

        // similar curve to sensor characteristics
        double expValue = Math.pow(1.03, percentageValue)/Math.pow(1.03, 100)*microsecHigh;
        return (int) Math.round(expValue);
    }

    /**
     * Convert microsec to percentage (used when sending data to frontend)
     *
     * @param microsec   to be converted microsec
     * @return                  converted percentages
     */
    private static int microsecToPercentage(int microsec) {
        if(microsec>microsecHigh) {
            microsec = microsecHigh;
        }

        double percentage = Math.log((double)microsec/(double)microsecHigh*Math.pow(1.03, 100))/Math.log(1.03);
        return (int) Math.round(percentage);
    }
}
