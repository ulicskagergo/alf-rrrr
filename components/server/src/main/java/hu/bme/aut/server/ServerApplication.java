package hu.bme.aut.server;

import hu.bme.aut.server.domain.LightModel;
import hu.bme.aut.server.domain.database.LightData;
import hu.bme.aut.server.repository.ServerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.io.*;
import java.time.LocalDateTime;
import java.util.Scanner;

@SpringBootApplication
public class ServerApplication implements CommandLineRunner {

    private final boolean RASPI_MODE = false;

    @Autowired
    ServerRepository serverRepository;

    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        LightModel.getInstance(); // starts the logic after creating a light model
    }

    private void writeToDevice(BufferedWriter bufferedWriter, Integer dataToSend) {
        try {
            bufferedWriter.write(dataToSend + 48);
            bufferedWriter.flush();
        } catch (IOException ioException) {
            System.err.println("[ ERROR ] Cannot write to file: " + ioException.getMessage());
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

    public LightData saveAndFlushLightData(LightData lightData) {
        return serverRepository.saveAndFlush(lightData);
    }

    public void printLightDataBase(int pageNum, int size) {
        Pageable pageable = PageRequest.of(pageNum, size);
        Page<LightData> page = serverRepository.findAll(pageable);
        page.forEach(System.out::println);
    }

    // TODO all CRUD
}
