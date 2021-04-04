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
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

@SpringBootApplication
public class ServerDemoApplication implements CommandLineRunner {

    @Autowired
    ServerRepository serverRepository;

    public static void main(String[] args) {
        SpringApplication.run(ServerDemoApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

        LightData lightData1 = saveAndFlushLightData("2021-03-14 20:09", true, 33, 30);
        LightData lightData2 = saveAndFlushLightData("2021-03-15 17:19", false, 0, 0);
        printLightDataBase(0, (int)serverRepository.count());
    }

    public LightData saveAndFlushLightData(LightData lightData) {
        return serverRepository.saveAndFlush(lightData);
    }

    public LightData saveAndFlushLightData(LocalDateTime localDateTime, boolean isOn, Integer threshold, Integer actualValue) {
        return serverRepository.saveAndFlush(new LightData(localDateTime, isOn, threshold, actualValue));
    }

    public LightData saveAndFlushLightData(String localDateTime, boolean isOn, Integer threshold, Integer actualValue) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return serverRepository.saveAndFlush(new LightData(LocalDateTime.parse(localDateTime, dateTimeFormatter), isOn, threshold, actualValue));
    }

    public void printLightDataBase(int pageNum, int size) {
        Pageable pageable = PageRequest.of(pageNum, size);
        Page<LightData> page = serverRepository.findAll(pageable);
        page.forEach(System.out::println);
        page.forEach(LightData::toJSON);
    }

    // TODO print with sort and direction

    // TODO all CRUD
}