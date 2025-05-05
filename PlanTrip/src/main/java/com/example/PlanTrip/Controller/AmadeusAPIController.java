package com.example.PlanTrip.Controller;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;

@Service
public class AmadeusAPIController {

    public ArrayList<HashMap<String, Object>> getFlightInformation(String from, String to, String date, String budget, String apiKey, String apiSecret, String adults, String children, String infants, String travelClass, String currency) {
        OkHttpClient client = new OkHttpClient(); //This object is used to send HTTP requests and receive responses.
        ObjectMapper mapper = new ObjectMapper(); //This object is used to convert Java objects to JSON and vice versa.
        ArrayList<HashMap<String, Object>> flightList = new ArrayList<>(); //Create an ArrayList to store the flight information


        String accessToken = getAccessToken(apiKey, apiSecret);

        String URL = "https://test.api.amadeus.com/v2/shopping/flight-offers?originLocationCode=" + from + "&destinationLocationCode=" + to + "&departureDate=" + date + "&adults=" + adults + "&children=" + children + "&infants=" + infants + "&travelClass=" + travelClass + "&nonStop=false&currencyCode=" + currency + "&maxPrice=" + budget + "&max=4";
    
        okhttp3.Request request = new okhttp3.Request.Builder()
        .url(URL)
        .addHeader("Authorization", "Bearer " + accessToken) // Lägg till access token här
        .get()
        .build();

        Response response = null; //Initialize the response variable        
       
        try {
            response = client.newCall(request).execute();

            if(response.isSuccessful()) {
                System.out.println("HEEEJ JAG BEFINNER MIG HÄR");
                String responseBody = response.body().string(); //Get the response body as a string
               
                //This line converts the JSON response to a Map object
                //The Map object contains the key-value pairs of the JSON response                              
                Map<String, Object> responseMap = mapper.readValue(responseBody, Map.class);

                Map<String, Object> meta = (Map<String, Object>) responseMap.get("meta");
                String count = meta.get("count").toString();

                if(count.equals("0")) {
                    return null; //Return null if no flights are found
                }

                int convertedCount = Integer.parseInt(count);
                System.out.println("COUNT " + convertedCount);

                List<Map<String, Object>> dataList = (List<Map<String, Object>>) responseMap.get("data");

                int index = 1;

                //iteration for each itineraries
                for(int i = 0; i < convertedCount; i++) {
                    HashMap<String, Object> flight = extractFlightFromJSON(i, dataList, index);
                    flightList.add(flight);
                    index++; 
                }
            }
           
    } catch (IOException e) {
        System.out.println("blev fel");
        e.printStackTrace();
    }

    return flightList;
    }

    public HashMap<String, Object> extractFlightFromJSON(int index, List<Map<String, Object>> dataList, int counter) {
        System.out.println("jag befinner mig i extract metoden");
        HashMap<String, Object> flight = new HashMap<>();

        List<Map<String, Object>> itineraries = (List<Map<String, Object>>) dataList.get(index).get("itineraries"); 
        
        //duration, this is outside the segments map. 
        String duration = itineraries.get(0).get("duration").toString();

        System.out.println("duration: " + duration);

        //always fetch index 0, since each itinerary has one segment, which is located at index 0.
        List<Map<String, Object>> segments = (List<Map<String, Object>>) itineraries.get(0).get("segments");
        
        //Each departure list shows where one should depart from. Important info to know how many departures (and arrivals) a customer has to make.
        int  amountOfDepartures = 0;

        // Unique ID for each departure and its related information.
        // Used with another ID called "ID_Arrival" to match departures with their corresponding arrivals.
        int ID_Departure = 0;
        String idSuffix_Departure = "_ID" + ID_Departure;


        //for each departure in the segments list, we will get the departure information.
        for(int i = 0; i < segments.size(); i++) {
            try{
                Map<String, Object> departure = (Map<String, Object>) segments.get(i).get("departure");

                if(departure != null){
                    String departureIATA = departure.get("iataCode") + idSuffix_Departure;
                    System.out.println("departureIATA: " + departureIATA);

                    String departureAt = departure.get("at")+ idSuffix_Departure;
                    System.out.println("departureAt: " + departureAt);

                    String departureTerminal = "Unknown";

                    if(departure.containsKey("terminal")){
                        departureTerminal = departure.get("terminal").toString() + idSuffix_Departure;
                    } else {
                        departureTerminal = "Unknown_ID" + ID_Departure;
                    }

                    System.out.println("departureTerminal: " + departureTerminal);

                    ID_Departure++;
                }

            } catch (Exception e) {
                //System.out.println("Kunde inte läsa departure för segment " + i + ": " + e.getMessage());
            }
        }

        // Unique ID for each arrival and its related information.
        // Used with "ID_Departure" to match departure-arrival pairs
        int ID_Arrival = 0;
        String idSuffix_Arrival = "_ID" + ID_Arrival;

        //for each arrival in the segments list, we will get the arrival information.
        for(int i = 0; i < segments.size(); i++){
            try{
                Map<String, Object> arrival = (Map<String, Object>) segments.get(i).get("arrival");
                
                if(arrival != null){
                    String arrivalIATA = arrival.get("iataCode") + idSuffix_Arrival;
                    System.out.println("arrivalIATA: " + arrivalIATA);

                    String arrivalTerminal = "Unknown";

                    if(arrival.containsKey("terminal")){
                        arrivalTerminal = arrival.get("terminal") + idSuffix_Arrival;
                    } else {
                        arrivalTerminal = "Unknown_ID" + idSuffix_Arrival;
                    }
                    System.out.println("arrivalTerminal: " + arrivalTerminal);
    
                    String arrivalAt = arrival.get("at") + idSuffix_Arrival;
                    System.out.println("arrivalAt: " + arrivalAt);
                        
                    String flightNumber = "Unknown";
                    try {
                        flightNumber = segments.get(0).get("number").toString();
                        System.out.println("flightNumber: " + flightNumber);
                    } catch (Exception e) {
                        System.out.println("No flight number information available.");
                    }

                    ID_Arrival++;
                }
            } catch (Exception e) {
                System.out.println("Kunde inte läsa departure för segment " + i + ": " + e.getMessage());
            }
        }

        // Unique ID for each flight number
        int ID_FlightNumber = 0;
        String idSuffix_FlightNumber = "_ID" + ID_FlightNumber;

        //Get all flight numbers
        for(int i = 0; i < segments.size(); i++){
            try{
                String carrierCode = segments.get(i).get("number").toString();
                
                if(carrierCode != null){
                    carrierCode = carrierCode + idSuffix_FlightNumber;
                    ID_FlightNumber++;
                } else {
                    carrierCode = "Unknown_ID" + ID_FlightNumber;
                }

            } catch (Exception e) {
                System.out.println("Kunde inte läsa departure för segment " + i + ": " + e.getMessage());
            }
        }

        // Unique ID for each carrier code
        int ID_CarrierCode = 0;
        String idSuffix_CarrierCode = "_ID" + ID_CarrierCode;

        //Get all carrier codes
        for(int i = 0; i < segments.size(); i++){
            try{
                String carrierCode = segments.get(i).get("carrierCode").toString();

                if(carrierCode != null){
                    carrierCode = carrierCode + idSuffix_CarrierCode;
                    ID_CarrierCode++;
                } else {
                    carrierCode = "Unknown_ID" + ID_CarrierCode;
                } 
            } catch (Exception e) {
                System.out.println("Kunde inte läsa departure för segment " + i + ": " + e.getMessage());
            }
        }

        return flight; //Return the flight HashMap


    }

    public String getFirst3Letters(String input){
        if (input.length() <= 3) {
            return input;
        }

        return input.substring(0, 3);
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