package com.example.PlanTrip.Controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import okhttp3.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.cdimascio.dotenv.Dotenv;

public class ChatGPTAPIController {

    public HashMap<String, String> getIATACode(String cityFrom, String cityTo, String key){
        OkHttpClient client = new OkHttpClient(); 
        ObjectMapper mapper = new ObjectMapper();
        HashMap<String, String> iataCodes = new HashMap<>(); // Skapa en ny HashMap för att lagra IATA-koderna

        String URL = "https://api.openai.com/v1/chat/completions";

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
        
        String jsonBody = structureBasicFormat(chatGPTInput, mapper, false);

        try {
            String outputMessage = manageRequest(URL, key, jsonBody, client);
            
            String[] parts = outputMessage.split(", ");
            String from = parts[0].split(":")[1].trim(); // XXX
            String to = parts[1].split(":")[1].trim(); // YYY

            char toRemove = '.';
            String resultFrom = from.replace(Character.toString(toRemove), "");
            String resultTo = to.replace(Character.toString(toRemove), "");


            iataCodes.put("from", resultFrom); // Lägg till IATA-koden för den första staden
            iataCodes.put("to", resultTo); // Lägg till IATA-koden för den andra staden

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return iataCodes;
    }

    public Map<String, String> getActivitySuggestions(String destination, String key){
        OkHttpClient client = new OkHttpClient(); 
        ObjectMapper mapper = new ObjectMapper();
        HashMap<String, String> activityResponse = new HashMap<>();
        
        String URL = "https://api.openai.com/v1/chat/completions";
        String prompt = """
            I will be traveling to %s. 
            Give me a list of the most famous activities in that city.

            Return the result using this JSON structure:

            {
            "activities": [
                {
                "title": "",
                "description": "",
                "category": []
                }
            ]
            }

            Rules:
            - title: name of the activity
            - description: 2–3 sentences
            - category: one or more of these: "Sightseeing", "Adventure", "Relaxation", "Cultural"
            - No text outside the JSON.
            - Only valid JSON.
            """.formatted(destination);

        String jsonBody = structureBasicFormat(prompt, mapper, true);

        try {
            String outputMessage = manageRequest(URL, key, jsonBody, client);

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }





        return null;
    }

    public String structureBasicFormat(String chatGPTInput, ObjectMapper mapper, boolean isActivity) {
        Map<String, Object> message = getMessageForJSONIput(chatGPTInput);

        Map<String, Object> body = new HashMap<>();
        body.put("model", "gpt-5-mini");

        if(isActivity) {
            Map<String, Object> responseFormat = new HashMap<>();
            responseFormat.put("type", "json_object");
            body.put("response_format", responseFormat);
        }

        body.put("messages", List.of(message));

        String jsonBody = "";
        try {
            jsonBody = mapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return jsonBody;

    }


    //It sets the URL, headers (including the API key), and the request body (in JSON format).
    //The content-type header specifies that the request body is in JSON format.
    //The post method specifies that this is a POST request.
    public String manageRequest(String API_URL, String API_KEY, String jsonBody, OkHttpClient client) throws IOException {
        
        // Creates a RequestBody object with the JSON string and the media type "application/json".
        RequestBody body = RequestBody.create(jsonBody, MediaType.parse("application/json"));

        Request request = new Request.Builder()
        .url(API_URL)
        .header("Authorization", "Bearer " + API_KEY)
        .header("Content-Type", "application/json")
        .post(body)
        .build();

        String outputMessage = "";
 
    try (Response response = client.newCall(request).execute()) {
        if (response.isSuccessful()) {
            String responseBody = response.body().string();

            outputMessage = extractContent(responseBody);

        } else {
            System.err.println("Fel: " + response.code() + " - " + response.body().string());
        }
    }

    return outputMessage;
    }

    public String extractContent(String json) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
    
        // Parse the JSON response from ChatGPT into a Map<String, Object>.
        // The responseMap will represent the entire JSON response as a key-value structure.
        Map<String, Object> responseMap = objectMapper.readValue(json, Map.class);
    
        List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
    
        // Get the first element (index 0) from the "choices" array.
        // This represents the first response choice generated by the model.
        Map<String, Object> firstChoice = choices.get(0);
    
        // Extract the "message" object from the first choice.
        // The "message" key contains another Map with details about the response, including the role and content.
        Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");
    
        // Extract the "content" field from the "message" object.
        // The "content" key contains the actual text response generated by the model.
        String content = (String) message.get("content");
    
        return content;
    }

    // Create the message to send to ChatGPT
    //The message is a HashMap with two keys: "role" and "content".
    //The "role" key indicates the role of the sender (user or assistant).
    //The "content" key contains the message content.
    public Map<String, Object> getMessageForJSONIput(String content){
        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", content);

        return message;
    }
    
}


