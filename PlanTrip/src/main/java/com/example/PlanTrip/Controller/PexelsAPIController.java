package com.example.PlanTrip.Controller;

import okhttp3.OkHttpClient;

public class PexelsAPIController {
    private String apiKey;
    private final OkHttpClient client = ServerController.getClient();

    public void getPictures(String apiKey, String query){
        this.apiKey = apiKey;

        // Implement the logic to call Pexels API and retrieve pictures based on the query.
    }

}
