package com.example.PlanTrip.Controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.OkHttpClient;
import okhttp3.Response;

public class SpotifyAPIController {

    public Map<String, Object> getMusicAndPodcastInformation(int duration, String accessToken, String destination){
        OkHttpClient client = new OkHttpClient(); //This object is used to send HTTP requests and receive responses.
        ObjectMapper mapper = new ObjectMapper(); //This object is used to convert Java objects to JSON and vice versa.
        int songs = 17; //As default, we should have 17 songs per hour of duration.
        
        if(duration < 1){
            songs = 17;
        } else if(duration > 1){
            songs = songs * duration;
        }

        String[] genres = getGenre(destination);

        if(genres == null){
            return null;
        }
        
                String URL = "https://api.spotify.com/v1/search?q="
                + "genre:" + genres[0]
                + " OR genre:" + genres[1]
                + " OR genre:" + genres[2]
                + "&type=track&limit="+ String.valueOf(songs);

                okhttp3.Request request = new okhttp3.Request.Builder()
        .url(URL)
        .addHeader("Authorization", "Bearer " + accessToken) // Lägg till access token här
        .get()
        .build();

        Response response = null; //Initialize the response variable

        try{
            response = client.newCall(request).execute();

            if(response.isSuccessful()){
                String responseBody = response.body().string(); //Get the response body as a string
                System.out.println("------Here is the responsebody----------");
                System.out.println(responseBody);
            }

        } catch(IOException e){
            e.printStackTrace();
        }


        return null;
    } 
    
    public String[] getGenre(String destination){
        String countries_genre = "";
        
        try {
            countries_genre = Files.readString(Paths.get("countries_genre.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        String[] lines = countries_genre.split("\\r?\\n");

            for (String line : lines) {
                String[] parts = line.split(":");
                String country = parts[0].trim();
                String genres = parts[1].trim();

                if (country.equalsIgnoreCase(destination)) {
                    String[] genreArray = genres.split(",\\s*");
                    return genreArray; // eller gör något med det
                }
            }

        return null;    

    }
}
