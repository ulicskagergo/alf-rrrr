package hu.bme.aut.server;

import hu.bme.aut.server.domain.LightModel;
import hu.bme.aut.server.domain.database.LightData;
import hu.bme.aut.server.repository.MeasurementDayRepository;
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
import java.time.LocalTime;
import java.util.Scanner;

@SpringBootApplication
public class ServerApplication implements CommandLineRunner {

    private final boolean RASPI_MODE = false;

    @Autowired
    ServerRepository serverRepository;

    @Autowired
    MeasurementDayRepository measurementDayRepository;

    @Autowired
    private LightModel lightModel;

    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        // lightModel.restartWithSettings(LocalTime.parse("10:00"), LocalTime.parse("18:00"), 50);
        // LightModel.getInstance(); // starts the logic after creating a light model
    }
}
