package com.example.PlanTrip.Controller;
import java.io.IOException;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;

@Service
public class APIController {

    public Map<String, String> getFlightInformation(String from, String to, String date, String budget, String apiKey, String apiSecret) {
        String accessToken = getAccessToken(apiKey, apiSecret);

    }

    public String getAccessToken(String apiKey, String apiSecret) {
        OkHttpClient client = new OkHttpClient(); //This object is used to send HTTP requests and receive responses.
        ObjectMapper mapper = new ObjectMapper(); //This object is used to convert Java objects to JSON and vice versa.
        
        //Definies what type of data we are going to send and receive.
        MediaType JSON = MediaType.get("application/json; charset=utf-8");

        // Skapa ett POST-anrop med headers Content-Type: application/x-www-form-urlencoded
        // och med body grant_type=client_credentials&client_id=xxx&client_secret=yyy
        // Sedan plockar du ut "access_token" från svaret (JSON).

        String postURL = "https://test.api.amadeus.com/v1/security/oauth2/token";
        String postInput = "grant_type=client_credentials&client_id=" + apiKey + "&client_secret=" + apiSecret;
    
        //Create a request body with the JSON data
        RequestBody body = RequestBody.create(postInput, JSON);

        //Create a request with the URL and the request body
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(postURL)
                .post(body) //Define the request method as POST
                .build();

       
        Response response = null; //Initialize the response variable        
        
        try {
            response = client.newCall(request).execute();

            if(response.isSuccessful()) {
                String responseBody = response.body().string(); //Get the response body as a string
                
                //This line converts the JSON response to a Map object
                //The Map object contains the key-value pairs of the JSON response
                Map<String, String> responseMap = mapper.readValue(responseBody, Map.class);
                String accessToken = responseMap.get("access_token"); //Get the access token from the Map object
                return accessToken;
            }  else { 
                return null;
            }

    } catch (IOException e) {
        e.printStackTrace();
    }

    return null;

    }
}