package com.example.PlanTrip.Controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.HttpUrl;
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

        if(songs > 100){
            songs = 100;
        }

      //  String[] genres = getGenre(destination);

            /* 
                String URL = "https://api.spotify.com/v1/recommendations?seed_genres=" 
               + genres[0] + "," + genres[1] + "," + genres[2] + "&limit=" + String.valueOf(songs);
               */

                 HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host("api.spotify.com")
                .addPathSegments("v1/recommendations")
                .addQueryParameter("seed_genres", "pop")
                .addQueryParameter("limit", String.valueOf(songs))
                .addQueryParameter("market", "SE")
                .build();

        okhttp3.Request request = new okhttp3.Request.Builder()
        .url(url)
        .addHeader("Authorization", "Bearer " + accessToken)
        .build();

        Response response = null; //Initialize the response variable

        try{
            response = client.newCall(request).execute();

            if(response.isSuccessful()){
                String responseBody = response.body().string();
                System.out.println(responseBody);
            } else{
                System.out.println("response blev inte sucess");
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
