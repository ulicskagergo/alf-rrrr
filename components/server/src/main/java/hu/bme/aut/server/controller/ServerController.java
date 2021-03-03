package hu.bme.aut.server.controller;

import hu.bme.aut.server.domain.Server;
import hu.bme.aut.server.repository.ServerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@Controller
public class ServerController {

    @Autowired
    private ServerRepository serverRepository;

    @GetMapping("/")
    public String createServer(Model model) {
        model.addAttribute("server", new Server());
        return "server";
    }

    @PostMapping("/")
    public String saveServer(@Valid Server server, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            List<String> messages = new ArrayList<>();
            for (ObjectError error : bindingResult.getAllErrors()) {
                if (error instanceof FieldError) {
                    FieldError fe = (FieldError) error;
                    StringBuilder sb = new StringBuilder();
                    sb.append(fe.getField());
                    sb.append(": ");
                    sb.append(fe.getDefaultMessage());
                    messages.add(sb.toString());
                }
            }

            model.addAttribute("msgs", messages);
            return "server";
        }
        model.addAttribute("msgs", List.of("Lights set."));
        serverRepository.save(server);
        return "server";
    }
}