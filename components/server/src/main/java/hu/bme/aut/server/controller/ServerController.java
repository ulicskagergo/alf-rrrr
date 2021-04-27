package hu.bme.aut.server.controller;

import hu.bme.aut.server.domain.LightData;
import hu.bme.aut.server.repository.ServerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
public class ServerController {

    @Autowired
    private ServerRepository serverRepository;

    @RequestMapping(value = "/data")
    @ResponseBody
    public ResponseEntity<List<LightData>> getAllData() {
        return new ResponseEntity<List<LightData>>(serverRepository.findAll(), HttpStatus.OK);
    }

    @RequestMapping(value = "/data/{id}")
    @ResponseBody
    public ResponseEntity<LightData> getDataById(@PathVariable("id") int id) {
        if(id <= (int)serverRepository.count()) {
            LightData lightData = new LightData();
            lightData.setThreshold(11);
            lightData.setActualValue(22);
            lightData.setIsOn(true);
            return new ResponseEntity<LightData>(lightData, HttpStatus.OK);
        }
        return new ResponseEntity<LightData>(HttpStatus.NOT_FOUND);
    }

    @RequestMapping("/")
    public String createLightData(Model model) {
        model.addAttribute("lightData", new LightData());
        return "input.html";
    }

    @PostMapping("/")
    public String saveLightData(@Valid LightData lightData, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            List<String> messages = new ArrayList<>();
            for (ObjectError objectError : bindingResult.getAllErrors()) {
                if (objectError instanceof FieldError) {
                    FieldError fieldError = (FieldError) objectError;
                    String stringBuilder = fieldError.getField() +
                            " : " +
                            fieldError.getDefaultMessage();
                    messages.add(stringBuilder);
                }
            }
            model.addAttribute("messages", messages);
            model.addAttribute("lightData", lightData);
            return "output";
        }

        serverRepository.saveAndFlush(lightData);
        Pageable pageable = PageRequest.of(0, (int)serverRepository.count());
        Page<LightData> page = serverRepository.findAll(pageable);
        model.addAttribute("lightData", page.getContent());
        return "output";
    }
}
