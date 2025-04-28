package com.example.PlanTrip.Controller;

import java.util.Map;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api")
public class ServerController {
    private APIController apiController = new APIController();

    @RequestMapping("/trip")
    public Map<String, String> getFlightInformation(@RequestParam Map<String, String> map) {
        String from = map.get("from");
        String to = map.get("to");
        String date = map.get("date");
        String budget = map.get("budget");
    }
    
}
