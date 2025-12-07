package com.example.PlanTrip.Controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Response;

public class PexelsAPIController {
    private String apiKey;
    private final OkHttpClient client = ServerController.getClient();

    public void getPictures(String apiKey, String query){
        this.apiKey = apiKey;
        List<String> pictures = new ArrayList<>();

        String URL = HttpUrl.parse("https://api.pexels.com/v1/search")
        .newBuilder()
        .addQueryParameter("query", query)
        .addQueryParameter("orientation", "landscape")
        .addQueryParameter("size", "medium")
        .build()
        .toString();

        okhttp3.Request request = new okhttp3.Request.Builder()
        .url(URL)
        .addHeader("Authorization", apiKey)
        .build();

        Response response = null; //Initialize the response variable

        try{
            response = client.newCall(request).execute();

            if(response.isSuccessful()){
                String responseBody = response.body().string();
                pictures = manageJSONFile(responseBody);
            } else{
                System.out.println("response blev inte sucess");
            }

        } catch(IOException e){
            response.close();
            e.printStackTrace();
        }

        response.close();
    }
    
    public List<String> manageJSONFile(String responseBody){
        ObjectMapper mapper = new ObjectMapper();

        try {
            JsonNode root = mapper.readTree(responseBody);
            JsonNode items = root.path("src");

            List<String> arr = new ArrayList<>();

            for (JsonNode n : items) {
                if (!n.isNull()) {
                    String original = n.path("original").asText();
                    arr.add(original);
                }
            }

            return arr;

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return null;
    }

}
