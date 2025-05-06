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

    public ArrayList<String> getFlightInformation(String from, String to, String date, String budget, String apiKey, String apiSecret, String adults, String children, String infants, String travelClass, String currency) {
        OkHttpClient client = new OkHttpClient(); //This object is used to send HTTP requests and receive responses.
        ObjectMapper mapper = new ObjectMapper(); //This object is used to convert Java objects to JSON and vice versa.
        ArrayList<HashMap<String, Object>> flightList = new ArrayList<>(); //Create an ArrayList to store the flight information
        ArrayList<String> displayedList = new ArrayList<>(); //Create an ArrayList to store the flight information


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
                String count = meta.get("count").toString();

                if(count.equals("0")) {
                    return null; //Return null if no flights are found
                }

                int convertedCount = Integer.parseInt(count);

                List<Map<String, Object>> dataList = (List<Map<String, Object>>) responseMap.get("data");

                int index = 1;

                //iteration for each itineraries
                for(int i = 0; i < convertedCount; i++) {
                    List<Map<String, Object>> departureList = getDepartureOrArrivalList(i, dataList, "departure");
                    List<Map<String, Object>> arrivalList = getDepartureOrArrivalList(i, dataList, "arrival");
                    ArrayList<String> flightNumberList = getFlightNumber(i, dataList);
                    ArrayList<String> carrierCodeList = getCarrierCode(i, dataList);
                    ArrayList<HashMap<String, Object>> tempFlightList = manageFlightList(departureList, arrivalList, flightNumberList, carrierCodeList);
                    flightList.addAll(tempFlightList);
                }
                
                displayedList = organizeFlightList(flightList);
                for (String flight : displayedList) {
                    System.out.println(flight); //Print the organized flight information
                }


            }
           
    } catch (IOException e) {
        e.printStackTrace();
    }

    return displayedList;
    }

    //This method organizes the flight list into a more readable format to be displayed on the frontend.
    public ArrayList<String> organizeFlightList(ArrayList<HashMap<String, Object>> flightList) {
        
        for (HashMap<String, Object> flight : flightList) {
            System.out.println(flight);
        }

        System.out.println("\n \n \n \n \n");
        
        
        ArrayList<String> organizedFlightList = new ArrayList<>(); //Create an ArrayList to store the flight information
        String departureIATA = " ";
        String departureAt = " ";
        String departureTerminal = " ";
        String arrivalIATA = " ";
        String arrivalAt = " ";
        String arrivalTerminal = " ";
        String flightNumber = " ";
        String carrierCode = " ";

        for(int i = 0; i < flightList.size(); i++){
            HashMap<String, Object> flight = flightList.get(i); //Get the flight information from the flight list
            
            departureIATA = (String) flight.get("departureIATA"); 
            departureAt = (String) flight.get("departureAt"); 
            departureTerminal = (String) flight.get("departureTerminal");
            
            flightList.remove(i); //Remove the flight information from the list after it has been processed

            
            for(int j = 0; j< flightList.size(); j++){
                HashMap<String, Object> flight2 = flightList.get(j);
                
                if(flight2.containsKey("arrivalIATA")) { 
                    arrivalIATA = (String) flight2.get("arrivalIATA");
                    arrivalAt = (String) flight2.get("arrivalAt"); 
                    arrivalTerminal = (String) flight2.get("arrivalTerminal");
                    flightList.remove(j); //Remove the flight information from the list after it has been processed

                    for(int k = 0; k < flightList.size(); k++){
                        HashMap<String, Object> flight3 = flightList.get(k); 
                       
                        if(flight3.containsKey("flightNumber")) { 
                            flightNumber = (String) flight3.get("flightNumber");
                            flightList.remove(k);

                            for(int l = 0; l < flightList.size(); l++){
                                HashMap<String, Object> flight4 = flightList.get(l);
                                
                                if(flight4.containsKey("carrierCode")) {
                                    carrierCode = (String) flight4.get("carrierCode");
                                    flightList.remove(l);
                                    break;
                                }
                        }

                        break;
                    }
                }

                break;
            }
        }

        String flightString = "Departure IATA: " + departureIATA + " Arrival IATA: " + arrivalIATA + " Departure time: " + departureAt + " Arrival time: " + arrivalAt + " Departure terminal: " + departureTerminal + " Arrival terminal: " + arrivalTerminal + " Flight number: " + flightNumber + " Carrier code: " + carrierCode; //Create a string to store the flight information
        organizedFlightList.add(flightString); //Add the flight information to the organized flight list
    }

    return organizedFlightList; //Return the organized flight list
}

    public ArrayList<HashMap<String, Object>> manageFlightList(List<Map<String, Object>> departureList, List<Map<String, Object>> arrivalList, ArrayList<String> flightNumberList, ArrayList<String> carrierCodeList) {
        ArrayList<HashMap<String, Object>> flightList = new ArrayList<>(); //Create an ArrayList to store the flight information

        for(int i = 0; i < departureList.size(); i++) {
            HashMap<String, Object> flight = new HashMap<>(); //Create a HashMap to store the flight information

            try{
                Map<String, Object> departure = (Map<String, Object>) departureList.get(i);

                String departureIATA = (String) departure.get("iataCode"); //Get the IATA code for the departure airport
                
                String departureAt = (String) departure.get("at"); //Get the departure time
                
                String departureTerminal = "Unknown"; //Initialize the departure terminal
                if(departure.containsKey("terminal")) { //Check if the terminal information is available
                    departureTerminal = (String) departure.get("terminal"); //Get the departure terminal
                } else {
                    departureTerminal = departureTerminal; //Set the terminal to unknown if not available
                }
    
                flight.put("departureIATA", departureIATA); //Add the departure IATA code to the flight HashMap
                flight.put("departureAt", departureAt); //Add the departure time to the flight HashMap
                flight.put("departureTerminal", departureTerminal); //Add the departure terminal to the flight HashMap
                flightList.add(flight); //Add the flight HashMap to the flight list

            } catch (Exception e) {
                e.getMessage(); //Print the error message
            }
            
        }

        for(int i = 0; i < arrivalList.size(); i++){
            HashMap<String, Object> flight = new HashMap<>(); //Create a HashMap to store the flight information
            try{
                Map<String, Object> arrival = (Map<String, Object>) arrivalList.get(i);

                String arrivalIATA = (String) arrival.get("iataCode"); //Get the IATA code for the arrival airport
                String arrivalTerminal = "Unknown"; //Initialize the arrival terminal

                if(arrival.containsKey("terminal")) { //Check if the terminal information is available
                    arrivalTerminal = (String) arrival.get("terminal"); //Get the arrival terminal
                } else {
                    arrivalTerminal = arrivalTerminal; //Set the terminal to unknown if not available
                }
                String arrivalAt = (String) arrival.get("at"); //Get the arrival time

                flight.put("arrivalIATA", arrivalIATA); //Add the arrival IATA code to the flight HashMap
                flight.put("arrivalAt", arrivalAt); //Add the arrival time to the flight HashMap
                flight.put("arrivalTerminal", arrivalTerminal); //Add the arrival terminal to the flight HashMap
                flightList.add(flight); //Add the flight HashMap to the flight list

                
            } catch (Exception e) {
                e.getMessage();
            }
        }

        for(int i = 0; i < flightNumberList.size(); i++){
            HashMap<String, Object> flight = new HashMap<>(); //Create a HashMap to store the flight information
            String flightNumber = "Unknown"; //Initialize the flight number
            try {
                flightNumber = flightNumberList.get(i);
                flight.put("flightNumber", flightNumber); //Add the flight number to the flight HashMap
                flightList.add(flight); //Add the flight HashMap to the flight list
            } catch (Exception e) {
                System.out.println("No flight number information available.");
            }
        }

        for(int i = 0; i < carrierCodeList.size(); i++){
            HashMap<String, Object> flight = new HashMap<>(); //Create a HashMap to store the flight information
            try{
                String carrierCode = carrierCodeList.get(i);
                carrierCode = carrierCode; //Add the ID suffix to the carrier code
                flight.put("carrierCode", carrierCode); //Add the carrier code to the flight HashMap
                flightList.add(flight); //Add the flight HashMap to the flight list


            } catch (Exception e) {
                e.getMessage();
            }
        }

        return flightList; //Return the list of flights
    }

    public List<Map<String, Object>> getDepartureOrArrivalList(int index, List<Map<String, Object>> dataList, String key) {
        List<Map<String, Object>> list = new ArrayList<>(); //Create an ArrayList to store the flight information

        List<Map<String, Object>> itineraries = (List<Map<String, Object>>) dataList.get(index).get("itineraries"); 
        
        List<Map<String, Object>> segments = (List<Map<String, Object>>) itineraries.get(0).get("segments");
        
        //for each departure in the segments list, we will get the departure information.
        for(int i = 0; i < segments.size(); i++) {
            try{
                Map<String, Object> currentList = (Map<String, Object>) segments.get(i).get(key);

                if(currentList != null){
                    list.add(currentList);
                }

            } catch (Exception e) {
                e.getMessage();

            }
        }

        return list;
    }

    public ArrayList<String> getFlightNumber(int index, List<Map<String, Object>> dataList) {
        ArrayList<String> flightNumberList = new ArrayList<>(); //Create an ArrayList to store the flight information

        List<Map<String, Object>> itineraries = (List<Map<String, Object>>) dataList.get(index).get("itineraries"); 
        
        //always fetch index 0, since each itinerary has one segment, which is located at index 0.
        List<Map<String, Object>> segments = (List<Map<String, Object>>) itineraries.get(0).get("segments");
        
        //for each departure in the segments list, we will get the departure information.
        for(int i = 0; i < segments.size(); i++){
            try{
                String flightNumber = segments.get(i).get("number").toString();
                
                if(flightNumberList != null){
                    flightNumberList.add(flightNumber);
                }

            } catch (Exception e) {
                e.getMessage();
            }
        }

        return flightNumberList;
    }

    public ArrayList<String> getCarrierCode(int index, List<Map<String, Object>> dataList) {
        ArrayList<String> carrierCodeList = new ArrayList<>(); //Create an ArrayList to store the flight information

        List<Map<String, Object>> itineraries = (List<Map<String, Object>>) dataList.get(index).get("itineraries"); 
        
        //always fetch index 0, since each itinerary has one segment, which is located at index 0.
        List<Map<String, Object>> segments = (List<Map<String, Object>>) itineraries.get(0).get("segments");
        
        //for each departure in the segments list, we will get the departure information.
        for(int i = 0; i < segments.size(); i++){
            try{
                String carrierCode = segments.get(i).get("carrierCode").toString();
                
                if(carrierCode != null){
                    carrierCodeList.add(carrierCode);
                }

            } catch (Exception e) {
                e.getMessage();
            }
        }

        return carrierCodeList;
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