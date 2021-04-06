package hu.bme.aut.server;

import hu.bme.aut.server.domain.LightData;
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
public class ServerDemoApplication implements CommandLineRunner {

    @Autowired
    ServerRepository serverRepository;

    public static void main(String[] args) {
        SpringApplication.run(ServerDemoApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        int dataToSend, dataReceived;
        System.out.println("Starting device test code example...");
        Scanner scanner = new Scanner(System.in);
        /*BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("/dev/ldrchar"));
        BufferedReader bufferedReader = new BufferedReader(new FileReader("/dev/ldchar"));*/
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("tmp.txt"));
        BufferedReader bufferedReader = new BufferedReader(new FileReader("tmp.txt"));

        while (true) {
            LightData lightData = new LightData();

            // read actual value
            System.out.println("Type in a short string to send to the kernel module:");
            dataReceived = readFromDevice(bufferedReader);
            lightData.setActualValue((dataReceived != -1) ? dataReceived : 0);
            lightData.setIsOn(dataReceived != -1);

            // set threshold
            dataToSend = scanner.nextInt();
            if (dataToSend == -1) {
                break;
            }
            writeToDevice(bufferedWriter, dataToSend);
            lightData.setThreshold(dataToSend);

            lightData.setMeasureDate(LocalDateTime.now());
            lightData = saveAndFlushLightData(lightData);

            printLightDataBase(0, (int)serverRepository.count());
        }
        bufferedReader.close();
        bufferedWriter.close();
        System.out.println("End of the program");
    }

    private void writeToDevice(BufferedWriter bufferedWriter, Integer dataToSend) {
        try {
            bufferedWriter.write(dataToSend);
            bufferedWriter.flush();
        } catch (IOException ioException) {
            System.err.println("Error while writing to file: " + ioException.getMessage());
        }
    }

    private Integer readFromDevice(BufferedReader bufferedReader) {
        Integer dataReceived = null;
        try {
            dataReceived = bufferedReader.read();
        } catch (IOException ioException) {
            System.err.println("Error while reading from file: " + ioException.getMessage());
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
        //page.forEach(LightData::toJSON);
    }

    // TODO print with sort and direction

    // TODO all CRUD
}