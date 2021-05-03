package hu.bme.aut.server.controller;

import hu.bme.aut.server.domain.database.LightData;
import hu.bme.aut.server.domain.LightModel;
import hu.bme.aut.server.domain.restapi.LightSettingsBody;
import hu.bme.aut.server.repository.MeasurementDayRepository;
import hu.bme.aut.server.repository.ServerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RestController
public class ServerController {

    @Autowired
    private ServerRepository serverRepository;

    @Autowired
    private MeasurementDayRepository measurementDayRepository;

    @Autowired
    private LightModel lightModel;

    @RequestMapping(value = "/data")
    @ResponseBody
    public ResponseEntity<List<LightData>> getAllData() {
        return new ResponseEntity<List<LightData>>(serverRepository.findAll(), HttpStatus.OK);
    }

    @RequestMapping(value = "/data", method = RequestMethod.POST)
    public LightData saveData(@RequestBody @Valid LightData lightData, Model model) {
        serverRepository.saveAndFlush(lightData);
        Pageable pageable = PageRequest.of(0, (int)serverRepository.count());
        Page<LightData> page = serverRepository.findAll(pageable);
        model.addAttribute("lightData", page.getContent());
        return lightData;
    }

    // POST { "sensitivity": <0-100> sensorSensitivity, "from": "15:00", "to":"19:00" } /settings
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

    // GET { "sensitivity": <0-100> sensorSensitivity, "from": "15:00", "to":"19:00" }  /settings
    @RequestMapping(value = "/settings",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<LightSettingsBody> getThreshold() {
        System.out.println("Settings GET");
        return new ResponseEntity<LightSettingsBody>(lightModel.exportSettings(), HttpStatus.OK);
    }

    // GET days /dates
    @RequestMapping(value = "/dates")
    public ResponseEntity<List<LocalDate>> getDays() {
        return new ResponseEntity<>(measurementDayRepository.findMeasurementDates(), HttpStatus.OK);
    }

    // GET all data per day /data/2021-05-30T15:13:48.934496
    @RequestMapping(value = "/data/{measure_date}")
    public ResponseEntity<List<LightData>> getDataByMeasureDate(@PathVariable("measure_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime measureDate) {
        return new ResponseEntity<>(serverRepository.findLightDataMeasuredOnDay(measureDate), HttpStatus.OK);
    }


}
