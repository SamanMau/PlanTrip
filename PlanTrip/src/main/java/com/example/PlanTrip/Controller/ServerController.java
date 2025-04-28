package com.example.PlanTrip.Controller;

import java.util.Map;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.github.cdimascio.dotenv.Dotenv;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api")
public class ServerController {
    private APIController apiController = new APIController();

    @GetMapping("/trip")
    public Map<String, String> getFlightInformation(@RequestParam Map<String, String> map) {
        System.out.println("helloooo");
        String from = map.get("from");
        String to = map.get("to");
        String date = map.get("date");
        String adults = map.get("adults");
        String children = map.get("children");
        String infants = map.get("infants");
        String travelClass = map.get("travelClass");
        String maxPrice = map.get("maxPrice");
        String currency = map.get("currency");

        String apiKey = getInfoFromENV("AMADEUS_API_KEY");
        String apiSecret = getInfoFromENV("AMADEUS_API_SECRET");

        Map<String, String> result = apiController.getFlightInformation(from, to, date, maxPrice, apiKey, apiSecret, adults, children, infants, travelClass, currency);
   
        return result;
    }


    public String getInfoFromENV(String input){
        Dotenv dotenv = Dotenv.configure()
        .directory(System.getProperty("user.dir"))
        .filename("PlanTrip\\.env")
        .load();

    String info = dotenv.get(input);
    return info;
    }
    
}
