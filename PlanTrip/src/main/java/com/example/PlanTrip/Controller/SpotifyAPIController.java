package com.example.PlanTrip.Controller;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.OkHttpClient;

public class SpotifyAPIController {

    public Map<String, Object> getMusicAndPodcastInformation(String duration, String accessToken){
        OkHttpClient client = new OkHttpClient(); //This object is used to send HTTP requests and receive responses.
        ObjectMapper mapper = new ObjectMapper(); //This object is used to convert Java objects to JSON and vice versa.

        return null;
    }
    
}
