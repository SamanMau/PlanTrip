package com.example.PlanTrip.Controller;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.PlanTrip.Controller.Entity.FlightBlock;
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
        ArrayList<String> listOfFlights = new ArrayList<>(); //Create an ArrayList to store the flight information
        ArrayList<String> displayedList = new ArrayList<>(); //Create an ArrayList to store the flight information

        String accessToken = getAccessToken(apiKey, apiSecret);

        String URL = getSpecificURL(from, to, date, budget, adults, children, infants, travelClass, currency);

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
                ArrayList<String> flightDurationList = getFlightDurations(dataList);
                ArrayList<String> travelerPricingList = getTravelerPricings(dataList, adults, children, infants, currency);

                //iteration for each itineraries
                for(int i = 0; i < convertedCount; i++) {
                    List<Map<String, Object>> departureList = getDepartureOrArrivalList(i, dataList, "departure");
                    List<Map<String, Object>> arrivalList = getDepartureOrArrivalList(i, dataList, "arrival");
                    ArrayList<String> flightNumberList = getFlightNumber(i, dataList);
                    ArrayList<String> carrierCodeList = getCarrierCode(i, dataList);
                    ArrayList<HashMap<String, Object>> tempFlightList = preprocessFlightData(departureList, arrivalList, flightNumberList, carrierCodeList, i);
                    flightList.addAll(tempFlightList);
                }

            int index = 0;

            while (index < flightList.size()) {
                FlightBlock block = getEachSeparateFlight(flightList.subList(index, flightList.size()));
                List<HashMap<String, Object>> oneFlight = block.flight;
                int stops = checkNumberOfStops(oneFlight);


                if (stops == 2) {
                    listOfFlights.addAll(prepareDisplayStrings(oneFlight, flightDurationList, travelerPricingList, 12, 3, 0, 3, 6, 9));
                } else if (stops == 3) {
                } else if (stops == 1) {
                    listOfFlights.addAll(prepareDisplayStrings(oneFlight, flightDurationList, travelerPricingList, 8, 2, 0, 2, 4, 6));
                } else {
                    listOfFlights.addAll(prepareDisplayStrings(oneFlight, flightDurationList, travelerPricingList, 4, 1, 0, 1, 2, 3));
                }

                index += block.nextIndex; // uppdatera index korrekt
            }

            displayedList = addFlightDurationAndPricingToList(listOfFlights, flightDurationList, travelerPricingList, convertedCount, children, infants);

        return displayedList;           
    }
    } catch (IOException e){

    }
    
    return displayedList;

}

    public ArrayList<String> addFlightDurationAndPricingToList(ArrayList<String> result, ArrayList<String> flightDurationList, ArrayList<String> travelerPricingList, int convertedCount, String children, String infants) {
        ArrayList<String> returnList = new ArrayList<>();
        int flight = 1; //Initialize the flight number
        System.out.println("flightDurationList: " + flightDurationList.size());
        System.out.println("travelerPricingList: " + travelerPricingList.size());

        try{
            for(int i = 0; i < convertedCount; i++){
                StringBuilder flightHeader_sb = new StringBuilder();
                StringBuilder pricing_sb = new StringBuilder();
                String duration = flightDurationList.get(i);
                flightHeader_sb.append("📄Flight number: " + flight).append("\n").append("⏳ Flight duration: ").append(duration).append("\n").append("\n");
                flight++;
                returnList.add(flightHeader_sb.toString());

                if(travelerPricingList.size() > 1) {
                    String pricing = travelerPricingList.get(0);
                    pricing_sb.append(pricing).append("\n").append("\n");
                    travelerPricingList.remove(0);
                    returnList.add(pricing);
                }

                else if(travelerPricingList.size() == 1) {
                    String pricing = travelerPricingList.get(0);
                    pricing_sb.append(pricing).append("\n").append("\n");
                    returnList.add(pricing);
                }

                returnList.add(result.get(i));
            }

            return returnList;

        } catch (Exception e){
            e.printStackTrace();
        }

        return returnList;

    }

    public FlightBlock getEachSeparateFlight(List<HashMap<String, Object>> flightList) {
        List<HashMap<String, Object>> oneFlight = new ArrayList<>();

        for (int i = 0; i < flightList.size(); i++) {
            HashMap<String, Object> current = flightList.get(i);
            oneFlight.add(current);

            if (current.containsKey("carrierCode") && i + 1 < flightList.size()) {
                HashMap<String, Object> next = flightList.get(i + 1);
                if (next.containsKey("departureIATA")) {
                    return new FlightBlock(oneFlight, i + 1); // här
                }
            }


        }

        // Om vi når slutet utan att hitta ny departure
        return new FlightBlock(oneFlight, flightList.size());
    }

    public String getSpecificURL(String from, String to, String date, String budget, String adults, String children, String infants, String travelClass, String currency) {
        String URL = "";

        if(children == null || children.isEmpty()) {
            children = "0";
        }

        if(infants == null || infants.isEmpty()) {
            infants = "0";
        }

        if(budget == null || budget.isEmpty()) {
            URL = "https://test.api.amadeus.com/v2/shopping/flight-offers?originLocationCode=" + from + "&destinationLocationCode=" + to + "&departureDate=" + date + "&adults=" + adults + "&children=" + children + "&infants=" + infants + "&travelClass=" + travelClass + "&nonStop=false" + "&max=4";
        }
        
        else{
           URL = "https://test.api.amadeus.com/v2/shopping/flight-offers?originLocationCode=" + from + "&destinationLocationCode=" + to + "&departureDate=" + date + "&adults=" + adults + "&children=" + children + "&infants=" + infants + "&travelClass=" + travelClass + "&nonStop=false&currencyCode=" + currency + "&maxPrice=" + budget + "&max=4";
        }

        return URL;

    }

    public int checkNumberOfStops(List<HashMap<String, Object>> oneFlight) {
        int stops = -1; // För att exkludera den första departure

        for (HashMap<String, Object> entry : oneFlight) {
            if (entry.containsKey("departureIATA")) {
                stops++;
            }
        }

        return stops;
    }

    public ArrayList<String> getTravelerPricings(List<Map<String, Object>> dataList, String adults, String children, String infants, String currency) {
            ArrayList<String> travelerPricingList = new ArrayList<>();
            int childrenAmount = 0;
            int infantsAmount = 0;
            //we dont need one "adultsAmount" variable for adults, as the API requires at least one adult to be present in the request.
            
            
            double totalPrice = 0;
            double totalAdultPrice = 0;
            double totalChildrenPrice = 0;
            double totalInfantPrice = 0;
            String oneAdultPrice = null;
            String oneChildPrice = null;
            String oneInfantPrice = null;

            if(children == null || children.isEmpty()) {
                childrenAmount = 0;
            } else {
                childrenAmount = Integer.parseInt(children.trim());
            }

            if(infants == null || infants.isEmpty()) {
                infantsAmount = 0;
            } else {
                infantsAmount = Integer.parseInt(infants.trim());
            }

            /*
             * The index for the child prices is determined by the number of adults.
             * This pattern was identified when testing the API.
             */
            int childIndex = Integer.parseInt(adults);

            //same goes for infants.
            int infantIndex = Integer.parseInt(adults) + childrenAmount;
            
            for(int i = 0; i < dataList.size(); i++){
                try{
                    List<Map<String, Object>> pricings = (List<Map<String, Object>>) dataList.get(i).get("travelerPricings");
                    
                    if(pricings != null) {
                        Map<String, Object> price_map_adult = (Map<String, Object>) pricings.get(0).get("price");
                        oneAdultPrice = (String) price_map_adult.get("total");

                        totalAdultPrice = Double.parseDouble(oneAdultPrice.trim()) * Double.parseDouble(adults.trim());
                        totalPrice += totalAdultPrice;

                        try{
                           Map<String, Object> price_map_children = (Map<String, Object>) pricings.get(childIndex).get("price");
                            if(price_map_children != null || price_map_children.size() > 0) {
                            oneChildPrice = (String) price_map_children.get("total");
                            totalChildrenPrice = Double.parseDouble(oneChildPrice.trim()) * childrenAmount;
                            totalPrice += totalChildrenPrice;
                        }

                        } catch (Exception e) {
                            
                        }

                        try{
                            Map<String, Object> price_map_infant = (Map<String, Object>) pricings.get(infantIndex).get("price");

                        if(price_map_infant != null || price_map_infant.size() > 0) {
                            oneInfantPrice = (String) price_map_infant.get("total");
                            totalInfantPrice = Double.parseDouble(oneInfantPrice.trim()) * infantsAmount;
                            totalPrice += totalInfantPrice;
                        }
                        } catch (Exception e) {
                            
                        }

                        DecimalFormat df = new DecimalFormat("#.##");
                        String formattedTotalPrice = df.format(totalPrice);

                        String element = "";

                        if(totalChildrenPrice != 0 && totalInfantPrice != 0){
                            element = "Total price: " + formattedTotalPrice + "(" + currency + ")" + "\n" + "Total adult price: " + totalAdultPrice + "(" + currency + ")" + "\n" + "Total children price: " + totalChildrenPrice + "(" + currency + ")" + "\n" + "Total nfant price: " + totalInfantPrice + "(" + currency + ")";
                        } else if(totalChildrenPrice != 0 && totalInfantPrice == 0) {
                            element = "Total price: " + formattedTotalPrice + "(" + currency + ")" + "\n" + "Total adult price: " + totalAdultPrice + "(" + currency + ")" + "\n" + "Total children price: " + totalChildrenPrice + "(" + currency + ")";
                        } else if(totalChildrenPrice == 0 && totalInfantPrice != 0) {
                            element = "Total price: " + formattedTotalPrice + "(" + currency + ")" + "\n" + "Total adult price: " + totalAdultPrice + "(" + currency + ")" + "\n" + "Total infant price: " + totalInfantPrice + "(" + currency + ")";
                        } else if (totalChildrenPrice == 0 && totalInfantPrice == 0){
                            element = "Total price: " + formattedTotalPrice + "(" + currency + ")"  + "\n"  +"Total adult price: "  + totalAdultPrice  +"("  + currency  + ")" ;
                        }

                        travelerPricingList.add(element);
                        totalPrice = 0; //Reset the total price for the next iteration
                        totalAdultPrice = 0; //Reset the total adult price for the next iteration
                        totalChildrenPrice = 0; //Reset the total children price for the next iteration
                        totalInfantPrice = 0; //Reset the total infant price for the next iteration
                    }
    
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
    
            return travelerPricingList;
    }


    public ArrayList<String> getFlightDurations(List<Map<String, Object>> dataList) {
        ArrayList<String> flightDurationList = new ArrayList<>();

        for(int i = 0; i < dataList.size(); i++){
            try{
                List<Map<String, Object>> itineraries = (List<Map<String, Object>>) dataList.get(i).get("itineraries");
                
                if(itineraries != null) {
                    String duration = itineraries.get(0).get("duration").toString();
                    flightDurationList.add(duration);
                }

            } catch (Exception e) {
                e.getMessage();
            }
        }


        return flightDurationList;
    }

    //This method prepares the display strings for the flight information.
    //It takes the flight list, flight duration list, and traveler pricing list as input and returns an ArrayList of strings for display.
            public ArrayList<String> prepareDisplayStrings(
                List<HashMap<String, Object>> flightList,
                List<String> flightDurationList,
                List<String> travelerPricingList,
                int outerLoopIncrement,
                int innerLoopCondition,
                int departureOffset,
                int arrivalOffset,
                int flightNumberOffset,
                int carrierCodeOffset
            ) {
                ArrayList<String> result = new ArrayList<>();

                try {
                    StringBuilder sb = new StringBuilder();
                    StringBuilder flightHeader_sb = new StringBuilder();
                    StringBuilder pricing_sb = new StringBuilder();
                    int flight = 1; //Initialize the flight number

                    for (int j = 0; j < innerLoopCondition; j++) {
                        HashMap<String, Object> departure = flightList.get(departureOffset + j);
                        HashMap<String, Object> arrival = flightList.get(arrivalOffset + j);
                        HashMap<String, Object> flightNumber = flightList.get(flightNumberOffset + j);
                        HashMap<String, Object> carrier = flightList.get(carrierCodeOffset + j);

                        String departureIATA = (String) departure.get("departureIATA");
                        String departureTime = (String) departure.get("departureAt");
                        String departureTerminal = (String) departure.get("departureTerminal");

                        String arrivalIATA = (String) arrival.get("arrivalIATA");
                        String arrivalTime = (String) arrival.get("arrivalAt");
                        String arrivalTerminal = (String) arrival.get("arrivalTerminal");

                        String fn = (String) flightNumber.get("flightNumber");
                        String cc = (String) carrier.get("carrierCode");

                        sb.append("\n")
                        .append("🛫 Departure: ").append(departureIATA).append("\n")
                        .append("🕐 Departure time: ").append(departureTime).append("\n")
                        .append("🛃 Departure terminal: ").append(departureTerminal).append("\n")
                        .append("✈ Flight number: ").append(fn).append("\n")
                        .append("🛩 Airline: ").append(cc).append("\n\n")
                        .append("🛬 Arrival: ").append(arrivalIATA).append("\n")
                        .append("🕐 Arrival time: ").append(arrivalTime).append("\n")
                        .append("🛃 Arrival terminal: ").append(arrivalTerminal).append("\n");
                    }

                    result.add(sb.toString());

                } catch (Exception e) {
                    e.printStackTrace();
                }

                return result;
            }


    //This method preprocesses the flight data and organizes it into a more readable format.
    public ArrayList<HashMap<String, Object>> preprocessFlightData(List<Map<String, Object>> departureList, List<Map<String, Object>> arrivalList, ArrayList<String> flightNumberList, ArrayList<String> carrierCodeList, int trycount) {
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
                e.printStackTrace();
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
                e.printStackTrace();
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
                e.printStackTrace();
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
                e.printStackTrace();
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
