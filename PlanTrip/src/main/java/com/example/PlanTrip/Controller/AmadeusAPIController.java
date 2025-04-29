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
                String responseBody = response.body().string(); //Get the response body as a string
               
                //This line converts the JSON response to a Map object
                //The Map object contains the key-value pairs of the JSON response                              
                Map<String, Object> responseMap = mapper.readValue(responseBody, Map.class);

                Map<String, Object> meta = (Map<String, Object>) responseMap.get("meta");
                String count = meta.get("count").toString(); //Get the count from the meta object

                if(count.equals("0")) {
                    return null; //Return null if no flights are found
                }

                int convertedCount = Integer.parseInt(count); //Convert the count to an integer
                System.out.println("COUNT " + convertedCount); //Print the count

                List<Map<String, Object>> dataList = (List<Map<String, Object>>) responseMap.get("data");

                int index = 1;
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
        HashMap<String, Object> flight = new HashMap<>(); //Create a new HashMap to store the flight information

        List<Map<String, Object>> itineraries = (List<Map<String, Object>>) dataList.get(index).get("itineraries"); 
                
        String duration = itineraries.get(index).get("duration").toString();
        System.out.println("duration: " + duration); //Print the duration

        List<Map<String, Object>> segments = (List<Map<String, Object>>) itineraries.get(index).get("segments"); //Get the segments from the first element of the itineraries list
        
        
        Map<String, Object> departure = (Map<String, Object>) segments.get(index).get("departure"); //Get the first segment from the segments list
        
        String departureAt = departure.get("at").toString(); //Get the departure date from the first segment
        System.out.println("departureAt: " + departureAt); //Print the departure date
        
        String departureTerminal = departure.get("terminal").toString(); //Get the departure terminal from the first segment
        System.out.println("departureTerminal: " + departureTerminal); //Print the departure terminal
        
        String departureIATA = departure.get("iataCode").toString(); //Get the departure IATA code from the first segment
        System.out.println("departureIATA: " + departureIATA); //Print the departure IATA code

        Map<String, Object> arrival = (Map<String, Object>) segments.get(index).get("arrival"); //Get the first segment from the segments list
        
        String arrivalAt = arrival.get("at").toString(); //Get the arrival date from the first segment
        System.out.println("arrivalAt: " + arrivalAt); //Print the arrival date
        
        String arrivalTerminal = "Unknown";

        if(arrival.containsKey("terminal")){
            arrivalTerminal = arrival.get("terminal").toString(); //Get the arrival terminal from the first segment
        }
        System.out.println("arrivalTerminal: " + arrivalTerminal); //Print the arrival terminal
        
        String arrivalIATA = arrival.get("iataCode").toString(); //Get the arrival IATA code from the first segment
        System.out.println("arrivalIATA: " + arrivalIATA); //Print the arrival IATA code
       
        String carrierCode = segments.get(index).get("carrierCode").toString(); //Get the carrier code from the first segment
        System.out.println("carrierCode: " + carrierCode); //Print the carrier code
        
        String flightNumber = segments.get(index).get("number").toString(); //Get the flight number from the first segment
        System.out.println("flightNumber: " + flightNumber); //Print the flight number

        flight.put("duration" + counter, duration); 
        flight.put("departureAt" + counter, departureAt);
        flight.put("departureTerminal" + counter, departureTerminal);
        flight.put("departureIATA" + counter, departureIATA);
        flight.put("arrivalAt" + counter, arrivalAt);
        flight.put("arrivalTerminal" + counter, arrivalTerminal);
        flight.put("arrivalIATA" + counter, arrivalIATA);
        flight.put("carrierCode" + counter, carrierCode);
        flight.put("flightNumber" + counter, flightNumber);

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