package com.example.PlanTrip.Controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import io.github.cdimascio.dotenv.Dotenv;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;

import okhttp3.OkHttpClient;

public class TmdbAPIController {

    public List<String> getMovieRecomendationsBasedOnGenre(String genre, String apiKey, String TMDB_READ_ACCESS_KEY){
        String genreID = getGenreID(genre);
        List<String> list = new ArrayList<>();

        OkHttpClient client = new OkHttpClient();

        okhttp3.Request request = new okhttp3.Request.Builder()
        .url("https://api.themoviedb.org/3/discover/movie?include_adult=true&include_video=false&language=en-US&page=1&sort_by=popularity.desc&with_genres="+genreID)
        .get()
        .addHeader("accept", "application/json")
        .addHeader("Authorization", "Bearer " + TMDB_READ_ACCESS_KEY)
        .build();

        Response response = null;

        try {
            response = client.newCall(request).execute();

            if(response.isSuccessful()){
                String responseBody = new String(response.body().bytes(), StandardCharsets.UTF_8);
                list = manageJSONFile(responseBody);
                return list;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<String> manageJSONFile(String responseBody){
        List<String> movieList = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();

        JsonNode root;
        try {
            root = mapper.readTree(responseBody);
            JsonNode items = root.path("results");

            for(JsonNode n : items){
                StringBuilder sb = new StringBuilder();
                String title = n.path("original_title").asText();
                String description = n.path("overview").asText();
                String poster_path = n.path("poster_path").asText();
                String date = n.path("release_date").asText();
                String language = n.path("original_language").asText();
                String genre = n.path("genre_ids").toString();
                sb.append(title + " | ");
                sb.append(description + " | ");
                sb.append("https://image.tmdb.org/t/p/w500/" + poster_path + " | ");
                sb.append("Release Date| " + date + " | ");
                sb.append("Language| " + language + " | ");

                JsonNode genreNode = n.path("genre_ids");
                StringBuilder genreNames = new StringBuilder();

                for(int i = 0; i < genreNode.size(); i++){
                    String genreName = getGenreNameFromID(genreNode.get(i).asText());
                    genreNames.append(genreName);
                    if(i < genreNode.size() - 1){
                        genreNames.append(", ");
                    }
                }

                sb.append(" Genres| " + genreNames.toString());
                movieList.add(sb.toString());
            }
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return movieList;
    }

    public String getGenreNameFromID(String id){
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

                if (id.equalsIgnoreCase(genre_IDString)) {
                    return genre_type;
                }
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
