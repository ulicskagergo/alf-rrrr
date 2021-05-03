package hu.bme.aut.server.controller;

import hu.bme.aut.server.domain.database.LightData;
import hu.bme.aut.server.domain.LightModel;
import hu.bme.aut.server.domain.restapi.LightSettingsBody;
import hu.bme.aut.server.repository.MeasurementDayRepository;
import hu.bme.aut.server.repository.ServerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Controller for server application
 */
@RestController
public class ServerController {

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
     * Light model to take measurements
     */
    @Autowired
    private LightModel lightModel;

    /**
     * Object for synchronize
     */
    private final Object monitor = new Object();

    /**
     * Jdbc template for database dump
     */
    private final JdbcTemplate jdbcTemplate;

    /**
     * Constructor to initialize jdbc template
     *
     * @param jdbcTemplate  give jdbc template
     */
    public ServerController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Dump whole database into file with GET on /dumpdb
     *
     * @throws IOException  when there is a problem with dump file
     */
    @RequestMapping(value = "/dumpdb")
    @ResponseBody
    public void dumpdb() throws IOException {
        synchronized (this.monitor) {
            File file = new File("dump.sql");
            if (file.exists()) {
                file.delete();
            }
            this.jdbcTemplate.execute("script '" + file.getAbsolutePath() + "'");
        }
    }

    /**
     * Set sensitivity value and time range for measurements on /settings
     * Pseudo: POST { "sensitivity": <0-100>, "from": "hh:mm", "to":"hh:mm" } /settings
     *
     * @param postBody  received POST message
     */
    @RequestMapping(value = "/settings"
            , method = RequestMethod.POST,
            consumes = {MediaType.APPLICATION_JSON_VALUE})
    public void setThreshold(@RequestBody @Valid LightSettingsBody postBody) {
        System.out.println("Settings POST");
        lightModel.restartWithSettings(
                LocalTime.parse(postBody.getFrom()),
                LocalTime.parse(postBody.getTo()),
                postBody.getSensitivity()
        );
    }

    /**
     * Get set sensitivity value and time range for measurements on /settings
     * Pesudo: GET { "sensitivity": <0-100>, "from": "hh:mm", "to":"hh:mm" }  /settings
     *
     * @return  response entity with http status sign and data
     */
    @RequestMapping(value = "/settings",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<LightSettingsBody> getThreshold() {
        System.out.println("Settings GET");
        return new ResponseEntity<LightSettingsBody>(lightModel.exportSettings(), HttpStatus.OK);
    }

    /**
     * Get days on which measurement is taken on /dates
     * Pseudo: GET ["2021-03-14","2021-03-15","2021-03-17"] /dates
     *
     * @return  response entity with http status sign and days
     */
    @RequestMapping(value = "/dates")
    public ResponseEntity<List<LocalDate>> getDays() {
        return new ResponseEntity<>(measurementDayRepository.findMeasurementDates(), HttpStatus.OK);
    }

    /**
     * Get measurement values on given day on /data/{specified_measure_day}
     * Pseudo: GET [{"id":1001,"measureDate":"2021-03-14T15:43:11","isOn":true,"threshold":22,"actualValue":20},{..}] /data/2021-05-30
     *
     * @param measureDate
     * @return
     */
    @RequestMapping(value = "/data/{measure_date}")
    public ResponseEntity<List<LightData>> getDataByMeasureDate(@PathVariable("measure_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate measureDate) {
        LocalDateTime measureDayTime = measureDate.atTime(0,0,0);
        return new ResponseEntity<>(serverRepository.findLightDataMeasuredOnDay(measureDayTime), HttpStatus.OK);
    }


}
