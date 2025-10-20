package com.example.PlanTrip.Controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Response;

public class SpotifyAPIController {

    public List<String> getMusicAndPodcastInformation(int duration, String accessToken, String genre){
        OkHttpClient client = new OkHttpClient(); //This object is used to send HTTP requests and receive responses.
        int songs = 17; //As default, we should have 17 songs per hour of duration.
        List<String> playLists = new ArrayList();

        if(duration < 1){
            songs = 17;
        } else if(duration > 1){
            songs = songs * duration;
        }

        if(songs > 100){
            songs = 100;
        }
          
        String URL = "https://api.spotify.com/v1/search?q=" + genre + "&type=playlist&limit="+String.valueOf(songs);
               
        okhttp3.Request request = new okhttp3.Request.Builder()
        .url(URL)
        .addHeader("Authorization", "Bearer " + accessToken)
        .build();

        Response response = null; //Initialize the response variable

        try{
            response = client.newCall(request).execute();

            if(response.isSuccessful()){
                String responseBody = response.body().string();
                playLists = manageJSONFile(responseBody);
                return playLists;
            } else{
                System.out.println("response blev inte sucess");
            }

        } catch(IOException e){
            e.printStackTrace();
        }

        return null;
    } 

    public List<String> manageJSONFile(String responseBody){
        
        ObjectMapper mapper = new ObjectMapper();

        try {
            JsonNode root = mapper.readTree(responseBody);
            JsonNode items = root.path("playlists").path("items");

            List<String> arr = new ArrayList<>();

            for (JsonNode n : items) {
                if (!n.isNull()) {
                    String name = n.path("name").asText();
                    String url = n.path("external_urls").path("spotify").asText();
                    String img = n.path("images").isArray() && n.path("images").size() > 0
                            ? n.path("images").get(0).path("url").asText()
                            : "";

                    String result = name + " | " + url + " | " + img;
                    arr.add(result);
                }
            }

            return arr;

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return null;

    }
    
}
