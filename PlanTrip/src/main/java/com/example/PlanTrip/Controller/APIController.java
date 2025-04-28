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

    public Map<String, String> getFlightInformation(String from, String to, String date, String budget, String apiKey, String apiSecret, String adults, String children, String infants, String travelClass, String currency) {
        System.out.println("befinner mig här");
        OkHttpClient client = new OkHttpClient(); //This object is used to send HTTP requests and receive responses.
        ObjectMapper mapper = new ObjectMapper(); //This object is used to convert Java objects to JSON and vice versa.
        Map<String, String> result = null; //This variable will hold the result of the API call

        String accessToken = getAccessToken(apiKey, apiSecret);
        String from_Code_IATA = getIATA(from, accessToken);
        String to_Code_IATA = getIATA(to, accessToken);
        System.out.println("FROM: " + from_Code_IATA);
        System.out.println("TO: " + to_Code_IATA);

        String URL = "https://test.api.amadeus.com/v2/shopping/flight-offers?originLocationCode=" + from_Code_IATA + "&destinationLocationCode=" + to_Code_IATA + "&departureDate=" + date + "&adults=" + adults + "&children=" + children + "&infants=" + infants + "&travelClass=" + travelClass + "&nonStop=false&currencyCode=" + currency + "&maxPrice=" + budget;
    
        okhttp3.Request request = new okhttp3.Request.Builder()
        .url(URL)
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
                
                List<Map<String, Object>> dataList = (List<Map<String, Object>>) responseMap.get("data");
                List<Map<String, Object>> itineraries = (List<Map<String, Object>>) dataList.get(0).get("itineraries"); 
                


                String duration = itineraries.get(0).get("duration").toString();

                List<Map<String, Object>> segments = (List<Map<String, Object>>) itineraries.get(0).get("segments"); //Get the segments from the first element of the itineraries list
                
                
                Map<String, Object> departure = (Map<String, Object>) segments.get(0).get("departure"); //Get the first segment from the segments list
                String departureAt = departure.get("at").toString(); //Get the departure date from the first segment
                String departureTerminal = departure.get("terminal").toString(); //Get the departure terminal from the first segment
                String departureIATA = departure.get("iataCode").toString(); //Get the departure IATA code from the first segment
                
                Map<String, Object> arrival = (Map<String, Object>) segments.get(0).get("arrival"); //Get the first segment from the segments list
                String arrivalAt = arrival.get("at").toString(); //Get the arrival date from the first segment
                String arrivalTerminal = arrival.get("terminal").toString(); //Get the arrival terminal from the first segment
                String arrivalIATA = arrival.get("iataCode").toString(); //Get the arrival IATA code from the first segment

                String carrierCode = segments.get(0).get("carrierCode").toString(); //Get the carrier code from the first segment
                String flightNumber = segments.get(0).get("number").toString(); //Get the flight number from the first segment




                /* 

                Map<String, Object> departure2 = (Map<String, Object>) segments.get(1).get("departure"); //Get the first segment from the segments list
                String departureAt2 = departure2.get("at").toString(); //Get the departure date from the first segment
                String departureTerminal2 = departure2.get("terminal").toString(); //Get the departure terminal from the first segment
                String departureIATA2 = departure2.get("iataCode").toString(); //Get the departure IATA code from the first segment
                
                Map<String, Object> arrival2 = (Map<String, Object>) segments.get(1).get("arrival"); //Get the first segment from the segments list
                String arrivalAt2 = arrival2.get("at").toString(); //Get the arrival date from the first segment
                String arrivalTerminal2 = arrival2.get("terminal").toString(); //Get the arrival terminal from the first segment
                String arrivalIATA2 = arrival2.get("iataCode").toString(); //Get the arrival IATA code from the first segment

                String carrierCode2 = segments.get(1).get("carrierCode").toString(); //Get the carrier code from the first segment
                String flightNumber2 = segments.get(1).get("number").toString(); //Get the flight number from the first segment
                */


                
                System.out.println("DEPARTURE AT: " + departureAt);
                System.out.println("DEPARTURE TERMINAL: " + departureTerminal);
                System.out.println("DEPARTURE IATA: " + departureIATA);
                System.out.println("ARRIVAL AT: " + arrivalAt);
                System.out.println("ARRIVAL TERMINAL: " + arrivalTerminal);
                System.out.println("ARRIVAL IATA: " + arrivalIATA);
                System.out.println("CARRIER CODE: " + carrierCode);
                System.out.println("FLIGHT NUMBER: " + flightNumber);
                System.out.println("DURATION: " + duration);

                /*
                System.out.println("DEPARTURE AT 2: " + departureAt2);
                System.out.println("DEPARTURE TERMINAL 2: " + departureTerminal2);
                System.out.println("DEPARTURE IATA 2: " + departureIATA2);
                System.out.println("ARRIVAL AT 2: " + arrivalAt2);
                System.out.println("ARRIVAL TERMINAL 2: " + arrivalTerminal2);
                System.out.println("ARRIVAL IATA 2: " + arrivalIATA2);
                System.out.println("CARRIER CODE 2: " + carrierCode2);
                System.out.println("FLIGHT NUMBER 2: " + flightNumber2);
                System.out.println("DURATION 2: " + duration);
                */

                return result;

            }
           
    } catch (IOException e) {
        e.printStackTrace();
    }

    return null;
    }

    public String getFirst3Letters(String input){
        if (input.length() <= 3) {
            return input;
        }

        return input.substring(0, 3);
    }

    public String getIATA(String from, String accessToken) {
        OkHttpClient client = new OkHttpClient(); //This object is used to send HTTP requests and receive responses.
        ObjectMapper mapper = new ObjectMapper(); //This object is used to convert Java objects to JSON and vice versa.
       
        String inputName = getFirst3Letters(from);

        String url = "https://test.api.amadeus.com/v1/reference-data/locations?keyword=" + inputName + "&subType=AIRPORT,CITY";
   
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
            } else{
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