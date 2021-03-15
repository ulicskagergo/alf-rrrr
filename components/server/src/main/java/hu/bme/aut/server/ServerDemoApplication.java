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
        Pageable pageable = PageRequest.of(0,3, Sort.Direction.DESC, "id");
        Page<LightData> page = serverRepository.findAll(pageable);
        page.forEach(System.out::println);
        page.forEach(LightData::toJSON);

        LightData newData = new LightData(4, LocalDateTime.of(2021,3,14,20,9), true, 33);
        System.out.println(newData);
        //serverRepository.save(newData);
        /*newData = serverRepository.save(newData);

        pageable = PageRequest.of(0,4, Sort.Direction.DESC, "is_on");
        page = serverRepository.findAll(pageable);
        page.forEach(System.out::println);
        page.forEach(LightData::toJSON);*/
    }

}