package com.example.PlanTrip.Controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;

import okhttp3.OkHttpClient;

public class TmdbAPIController {

    public List<String> getMovieRecomendationsBasedOnGenre(String genre, String apiKey){
        String genreID = getGenreID(genre);
        List<String> list = new ArrayList<>();

        OkHttpClient client = new OkHttpClient();

        okhttp3.Request request = new okhttp3.Request.Builder()
        .url("https://api.themoviedb.org/3/discover/movie?include_adult=true&include_video=false&language=en-US&page=1&sort_by=popularity.desc&with_genres=28")
        .get()
        .addHeader("accept", "application/json")
        .addHeader("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiJjZTZhMTg5MTJjOWE2MjY0YjI5YjkzMzZkYjA1OTU0ZiIsIm5iZiI6MTc2MDk5NDc4OS43NzUsInN1YiI6IjY4ZjZhNWU1OWRhYmEwZjBkNzJkNmM1YSIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.Ji1NRPQDdJxdTP4VMK-R6_jUSdzf_Wgp677r1f3JKxg")
        .build();

        Response response = null;

        try {
            response = client.newCall(request).execute();

            if(response.isSuccessful()){
                String responseBody = response.body().string();
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }



        return null;
    }

    public String getGenreID(String genre){
        String genreList = "";

        try {
            genreList = Files.readString(Paths.get("movies_genre_ID.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        String[] lines = genreList.split("\\r?\\n");

            for (String line : lines) {
                String[] parts = line.split(":");
                String genre_type = parts[0].trim();
                String genre_IDString = parts[1].trim();

                if (genre_type.equalsIgnoreCase(genre)) {
                    return genre_IDString;
                }
            }

            return null;
    }
    
}
