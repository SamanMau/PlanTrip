package com.example.PlanTrip.Controller;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
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
        String from_Code_IATA = getIATA(from, accessToken);
        String to_Code_IATA = getIATA(to, accessToken);
        URL url = null;
        String adults = "2";
        String children = "0";


        
        try {
            url = new URL("https://test.api.amadeus.com/v2/shopping/flight-offers?originLocationCode=" + from_Code_IATA + "&destinationLocationCode=" + to_Code_IATA + "&departureDate=" + date +"&adults=" + adults + "&children=" + children + "&travelClass=BUSINESS&nonStop=false&max=1");

        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }

    public String getIATA(String from, String accessToken) {
        OkHttpClient client = new OkHttpClient(); //This object is used to send HTTP requests and receive responses.
        ObjectMapper mapper = new ObjectMapper(); //This object is used to convert Java objects to JSON and vice versa.
       
        //If the city contains special charachters, such as space in "New York", we need to encode the value before putting it in the URL.
        String encodedFromCity = URLEncoder.encode(from, StandardCharsets.UTF_8);

        String url = "https://test.api.amadeus.com/v1/reference-data/locations?keyword=" + encodedFromCity + "&subType=AIRPORT,CITY";
   
        //Create a request body with the JSON data
        okhttp3.Request request = new okhttp3.Request.Builder()
        .url(url)
        .addHeader("Authorization", "Bearer " + accessToken) // Lägg till access token här
        .get()
        .build();

       
        Response response = null; //Initialize the response variable        
       
        try {
            response = client.newCall(request).execute();

            if(response.isSuccessful()) {
                String responseBody = response.body().string(); //Get the response body as a string
               
                //This line converts the JSON response to a Map object
                //The Map object contains the key-value pairs of the JSON response
                Map<String, Object> responseMap = mapper.readValue(responseBody, Map.class);
               
                List<Map<String, Object>> dataList = (List<Map<String, Object>>) responseMap.get("data"); //Get the data list from the Map object
                String result = dataList.get(0).get("iataCode").toString(); //Get the IATA code from the first element of the data list
               

                return result;
            }
           
    } catch (IOException e) {
        e.printStackTrace();
    }

    return null;
    }


    public String getAccessToken(String apiKey, String apiSecret) {
        OkHttpClient client = new OkHttpClient();

        String url = "https://test.api.amadeus.com/v1/security/oauth2/token";
        String body = "grant_type=client_credentials&client_id=" + apiKey + "&client_secret=" + apiSecret;

        RequestBody requestBody = RequestBody.create(body, MediaType.parse("application/x-www-form-urlencoded"));
        
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .post(requestBody)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, String> responseMap = objectMapper.readValue(responseBody, Map.class);
                return responseMap.get("access_token");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }   
        
        return null;
    
    }
}