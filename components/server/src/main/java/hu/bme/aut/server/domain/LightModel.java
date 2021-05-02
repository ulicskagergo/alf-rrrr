package hu.bme.aut.server.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.Entity;
import java.io.*;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LightModel {

    private final boolean RASPI_MODE = false;

    private static LightModel lightModel;
    private final Timer timer;
    private TimerTask timerTask;
    private Integer sensitivity;
    private String from;
    private String to;

    public static LightModel getInstance() {
        return (lightModel == null) ? new LightModel() : lightModel;
    }

    protected LightModel() {
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                measure();
            }
        };
        //start();
    }

    public void start() {
        // schedule with fixed delay
        long fromL = Long.parseLong(from.substring(0, 2)) * 1000 * 60 + Long.parseLong(from.substring(3, 5)) * 1000;
        long toL = Long.parseLong(to.substring(0, 2)) * 1000 * 60 + Long.parseLong(to.substring(3, 5)) * 1000;
        long delay = toL - fromL;
        long dailyPeriod = 1000L * 60L * 60L * 24L;
        timer.schedule(timerTask, delay, dailyPeriod);
    }

    public void setSensitivity(Integer sensitivity) {
        this.sensitivity = sensitivity;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public Integer getSensitivity() {
        return sensitivity;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    private void measure() {
        // TODO more measurement at time
        try {
            BufferedReader bufferedReader;
            if (RASPI_MODE) {
                bufferedReader = new BufferedReader(new FileReader("/dev/ldrchar"));
            } else {
                bufferedReader = new BufferedReader(new FileReader("tmp.txt"));
            }
            LightData lightData = new LightData();
            lightData.setThreshold(sensitivity);
            lightData.setIsOn(true);
            if (RASPI_MODE) {
                lightData.setActualValue(readFromDevice(bufferedReader));
            } else {
                int dataReceived = readFromDevice(bufferedReader);
                lightData.setActualValue((dataReceived != -1) ? dataReceived : 0);
            }
        } catch (FileNotFoundException e) {
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
}
