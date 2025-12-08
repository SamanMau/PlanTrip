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
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import okhttp3.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.cdimascio.dotenv.Dotenv;

public class ChatGPTAPIController {
    private final OkHttpClient client = ServerController.getClient();
    private final String URL = "https://api.openai.com/v1/chat/completions";

    public HashMap<String, String> getIATACode(String cityFrom, String cityTo, String key, boolean bothIataCodes){
        ObjectMapper mapper = new ObjectMapper();
        HashMap<String, String> iataCodes = new HashMap<>(); // Skapa en ny HashMap för att lagra IATA-koderna

        String chatGPTInput = "";

        if(bothIataCodes){
            chatGPTInput =
                    "Return the IATA airport codes for the cities below in this exact format:\n" +
                    "from:XXX, to:YYY\n\n" +
                    "Cities:\n" +
                    "from = " + cityFrom + "\n" +
                    "to = " + cityTo + "\n\n" +
                    "Rules:\n" +
                    "- Uppercase IATA codes.\n" +
                    "- No thinking.\n" +
                    "- No explanation.\n" +
                    "- Only output the final formatted line.";
        } else if(cityFrom != null){
            chatGPTInput = getInputForOneIataCodes(cityFrom);
        } else if(cityTo != null){
            chatGPTInput = getInputForOneIataCodes(cityTo);
        }
        
        String jsonBody = structureBasicFormat(chatGPTInput, mapper, false);

        try {
            String outputMessage = manageRequest(URL, key, jsonBody, client);

            if(bothIataCodes){
                String[] parts = outputMessage.split(", ");
                String from = parts[0].split(":")[1].trim(); // XXX
                String to = parts[1].split(":")[1].trim(); // YYY

                char toRemove = '.';
                String resultFrom = from.replace(Character.toString(toRemove), "");
                String resultTo = to.replace(Character.toString(toRemove), "");

                iataCodes.put("from", resultFrom); // Lägg till IATA-koden för den första staden
                iataCodes.put("to", resultTo); // Lägg till IATA-koden för den andra staden
            } else{
                String[] parts = outputMessage.split(":");
                String codeIATA = parts[1];

                String keyValue = "";
                if(cityFrom != null){
                    keyValue = "from";
                } else{
                    keyValue = "to";
                }

                iataCodes.put(keyValue, codeIATA); // Lägg till IATA-koden för den andra staden                
            }
            
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return iataCodes;
    }

    public String getInputForOneIataCodes(String location){
        String chatGPTInput =
        "Return the IATA airport code for the city below in this exact format:\n" +
        "code:XXX\n\n" +
        "City:" + location + "\n" +
        "Rules:\n" +
        "- Uppercase IATA code.\n" +
        "- No thinking.\n" +
        "- No explanation.\n" +
        "- Only output the final formatted line.";

        return chatGPTInput;
    }

    public ArrayList<HashMap<String, String>> getActivitySuggestions(String destination, String key, PexelsAPIController pexelsAPIController, String Pexels_API_KEY){
        ObjectMapper mapper = new ObjectMapper();
        ArrayList<HashMap<String, String>> activityResponse = new ArrayList<>();
        
        String URL = "https://api.openai.com/v1/chat/completions";
        
        
        String prompt = """
        I am traveling to %s. Give me 6 tourist activities for the city.

        Return them ONLY in this format (one line per activity): Title: text... : text...

        For example, it may look like this: Title: Eiffel Tower : Experience the iconic symbol of Paris with breathtaking views of the city from its observation decks.
        """.formatted(destination);

        String jsonBody = structureBasicFormat(prompt, mapper, true);

        try {
            String outputMessage = manageRequest(URL, key, jsonBody, client);
            activityResponse = turnStringToArray(outputMessage, pexelsAPIController, Pexels_API_KEY);

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return activityResponse;
    }

    public ArrayList<HashMap<String, String>> turnStringToArray(String message, PexelsAPIController pexelsAPIController, String Pexels_API_KEY){
        String[] lines = message.split("\n");
        ArrayList<HashMap<String, String>> activities = new ArrayList<>();

        for(String line : lines){
            HashMap<String, String> activity = new HashMap<>();
            String[] lineArr = line.split(":");
            String title = lineArr[1].trim();
            String description = lineArr[2].trim();
            String picture = pexelsAPIController.getPictures(title, Pexels_API_KEY);
            activity.put("title", title);
            activity.put("description", description);
            activity.put("picture", picture);
            activities.add(activity);
            System.out.println(title);
        }
        System.out.println("klart och returnerat");

        return activities;
    }

    public String structureBasicFormat(String chatGPTInput, ObjectMapper mapper, boolean isActivity) {
        Map<String, Object> message = getMessageForJSONIput(chatGPTInput);

        Map<String, Object> body = new HashMap<>();

        if(isActivity) {
            body.put("model", "gpt-5-mini");
            body.put("max_completion_tokens", 1000);
            body.put("temperature", 1);
            body.put("top_p", 1.0);
        } else{
            body.put("model", "gpt-5-nano");
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

        Response response = null;

        try{
           response = client.newCall(request).execute();
           if(response.isSuccessful()){
            String responseBody = response.body().string();
            outputMessage = extractContent(responseBody);

        } else {
            System.err.println("Fel: " + response.code() + " - " + response.body().string());
        }

        } catch (IOException e) {
            e.printStackTrace();
        }

        if(response != null){
            response.close();
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


