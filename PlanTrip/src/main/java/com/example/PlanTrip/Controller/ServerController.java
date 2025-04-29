package com.example.PlanTrip.Controller;

import java.util.HashMap;
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
    private AmadeusAPIController amadeusController = new AmadeusAPIController();
    private ChatGPTAPIController chatGPTController = new ChatGPTAPIController();

    @GetMapping("/trip")
    public Map<String, String> getFlightInformation(@RequestParam Map<String, String> map) {
        String from = map.get("from");
        String to = map.get("to");
        String date = map.get("date");
        String adults = map.get("adults");
        String children = map.get("children");
        String infants = map.get("infants");
        String travelClass = map.get("travelClass");
        String maxPrice = map.get("maxPrice");
        String currency = map.get("currency");
        
        String amadeusApiKey = getInfoFromENV("AMADEUS_API_KEY");
        String amadeusApiSecret = getInfoFromENV("AMADEUS_API_SECRET");
        String chatGptApiKey = getInfoFromENV("CHAT_KEY");

        System.out.println("CHAT KEY: " + chatGptApiKey);
        HashMap<String, String> iataCodesList = chatGPTController.getIATACode(from, to, chatGptApiKey);
        String fromIATA = iataCodesList.get("from");
        String toIATA = iataCodesList.get("to");

        Map<String, String> result = amadeusController.getFlightInformation(fromIATA, toIATA, date, maxPrice, amadeusApiKey, amadeusApiSecret, adults, children, infants, travelClass, currency);
   
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
