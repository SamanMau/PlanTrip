package com.example.PlanTrip.Controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.micrometer.core.ipc.http.HttpSender.Response;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

public class ChatGPTAPIController {

    public HashMap<String, String> getIATACode(String cityFrom, String cityTo, String key){
        OkHttpClient client = new OkHttpClient(); 
        ObjectMapper mapper = new ObjectMapper();
        HashMap<String, String> iataCodes = new HashMap<>(); // Skapa en ny HashMap för att lagra IATA-koderna

        String URL = "https://api.openai.com/v1/responses";

        String chatGPTInput = "You will receive two variable names: 'cityFrom' and 'cityTo'.\n" +
        "Insert their values into the following instruction:\n\n" +
        "I want you to give me the IATA codes for the following cities: " + cityFrom + " and " + cityTo + ".\n\n" +
        "RULES:\n" +
        "1. The IATA code for 'cityFrom' should be labeled 'from' and the IATA code for 'cityTo' should be labeled 'to'.\n" +
        "2. After 'from' and before the IATA code, and after 'to' and before the IATA code, use a colon ':' without any spaces.\n" +
        "3. The IATA codes must be written in uppercase letters.\n" +
        "4. Separate the two fields ('from' and 'to') with a comma and a single space ', '.\n" +
        "5. Your entire response must follow exactly this format: from:XXX, to:YYY.\n" +
        "6. Do not add any extra text, explanation, or decoration outside the format above.\n\n" +
        "Example (if cityFrom = Sydney and cityTo = Barcelona): from:SYD, to:BCN.";

        String jsonBody = """
            {
              "model": "gpt-4.1",
              "input": "%s"
            }
            """.formatted(chatGPTInput);

        RequestBody requestBody = RequestBody.create(jsonBody, MediaType.parse("application/json"));
  
        okhttp3.Request request = new okhttp3.Request.Builder()
        .url(URL)
        .post(requestBody)
        .addHeader("Authorization", "Bearer " + key)
        .build();

        okhttp3.Response response = null;
        
        try {
            response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                System.out.println("blev stor success");
                String responseBody = response.body().string();
                Map<String, Object> result = mapper.readValue(responseBody, Map.class);
                String contentString = (String) result.get("output");
                
                //'from:XXX, to:YYY'
                String[] parts = contentString.split(", ");
                String from = parts[0].split(":")[1].trim(); // XXX
                String to = parts[1].split(":")[1].trim(); // YYY
                System.out.println("From: " + from + ", To: " + to);
                iataCodes.put("from", from); // Lägg till IATA-koden för den första staden
                iataCodes.put("to", to); // Lägg till IATA-koden för den andra staden

                return iataCodes;
            } else {
                System.out.println("Error: " + response.code() + " " + response.message());
                return null;
            }
        
        
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return iataCodes;

    }
    
}
