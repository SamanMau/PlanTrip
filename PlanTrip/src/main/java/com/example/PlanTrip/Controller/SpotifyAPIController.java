package com.example.PlanTrip.Controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Response;

public class SpotifyAPIController {
    private int duration;
    private String access_token;
    private final OkHttpClient client = ServerController.getClient();

    public List<String> getMusicAndPodcastInformation(int duration, String accessToken, String genre){
        int songs = 17; //As default, we should have 17 songs per hour of duration.
        List<String> playLists = new ArrayList();

        if(duration < 1){
            songs = 17;
        } else if(duration > 1){
            songs = songs * duration;
        }

        if(songs > 100){
            songs = 50;
        }

        this.duration = duration;
        this.access_token = accessToken;
        
        System.out.println("genre: " + genre);
        //String URL = "https://api.spotify.com/v1/search?q=" + genre + "&type=playlist&limit="+String.valueOf(songs);
        
        String URL = HttpUrl.parse("https://api.spotify.com/v1/search")
        .newBuilder()
        .addQueryParameter("q", genre)
        .addQueryParameter("type", "playlist")
        .addQueryParameter("limit", String.valueOf(songs))
        .build()
        .toString();

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

            if(items.size() == 0){
                System.out.println("inga items, return general playlist");
                return getGeneralPlaylist();
            }

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

    public List<String> getGeneralPlaylist(){
        String genre = getGenre(ServerController.getDestination());
        return getMusicAndPodcastInformation(duration, access_token, genre);
    }

    public String getGenre(String destination){
        String genre_FileTxt = "";

        try {
            genre_FileTxt = Files.readString(Paths.get("countries_general_genre.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        String[] lines = genre_FileTxt.split("\\r?\\n");

            for (String line : lines) {
                String[] parts = line.split(":");
                String general_genre = parts[0].trim();
                String country = parts[1].trim();

                if(country.contains(destination)){
                    return general_genre;
                }
   
            }

        return null;    
    }

}
