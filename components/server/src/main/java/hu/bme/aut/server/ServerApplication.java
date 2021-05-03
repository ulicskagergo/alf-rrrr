package hu.bme.aut.server;

import hu.bme.aut.server.domain.LightModel;
import hu.bme.aut.server.repository.MeasurementDayRepository;
import hu.bme.aut.server.repository.ServerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class
 */
@SpringBootApplication
public class ServerApplication implements CommandLineRunner {

    /**
     * Repository for light data
     */
    @Autowired
    ServerRepository serverRepository;

    /**
     * Repository for measurement dates
     */
    @Autowired
    MeasurementDayRepository measurementDayRepository;

    /**
     * Main function
     * @param args  cli arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception { }
}
