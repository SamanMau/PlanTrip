package com.example.PlanTrip.Controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import com.example.PlanTrip.TokenManager;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.cdimascio.dotenv.Dotenv;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api")
public class ServerController {
    private AmadeusAPIController amadeusController = new AmadeusAPIController();
    private ChatGPTAPIController chatGPTController = new ChatGPTAPIController();
    private SpotifyAPIController spotifyAPIController = new SpotifyAPIController();
    private TokenManager tokenManager;
    private String spotifyClientID;
    private String spotifyClientSecret;
    private String userCode;

    public ServerController(){
        this.tokenManager = new TokenManager();
        this.spotifyClientID = getInfoFromENV("SPOTIFY_CLIENTID");
        this.spotifyClientSecret = getInfoFromENV("SPOTIFY_CLIENTSECRET");
        String spotifyToken = fetchAccessToken(spotifyClientID, spotifyClientSecret, "https://api.spotify.com/v1/me");

        tokenManager.setAccessToken(spotifyToken);

    }

    @GetMapping("/trip")
    public ArrayList<String> getFlightInformation(@RequestParam Map<String, String> map) {
        String from = map.get("from");
        String to = map.get("to");
        String date = map.get("date");
        String adults = map.get("adults");
        String children = map.get("children");
        String infants = map.get("infants");
        String travelClass = map.get("travelClass");
        String maxPrice = map.get("maxPrice");
        String currency = map.get("currency");
        
        String amadeusApiKey = getInfoFromENV("AMADEUS_API_KEY");
        String amadeusApiSecret = getInfoFromENV("AMADEUS_API_SECRET");
        String chatGptApiKey = getInfoFromENV("CHAT_KEY");

        HashMap<String, String> iataCodesList = chatGPTController.getIATACode(from, to, chatGptApiKey);
        String fromIATA = iataCodesList.get("from");
        String toIATA = iataCodesList.get("to");
        System.out.println("From IATA: " + fromIATA);
        System.out.println("To IATA: " + toIATA);

        String accessToken = fetchAccessToken(amadeusApiKey, amadeusApiSecret, "https://test.api.amadeus.com/v1/security/oauth2/token");

        ArrayList<String> result = amadeusController.getFlightInformation(fromIATA, toIATA, date, maxPrice, adults, children, infants, travelClass, currency, accessToken);

        return result;
    }

    @GetMapping("/callback")
    public RedirectView handleSpotifyCallback(@RequestParam("code") String code) {
        this.userCode = code;
        System.out.println("jag befinner mig inne i callback");

        return new RedirectView("/music.html");
    }


    public String getInfoFromENV(String input){
        Dotenv dotenv = Dotenv.configure()
        .directory(System.getProperty("user.dir"))
        .filename("PlanTrip\\.env")
        .load();

    String info = dotenv.get(input);
    return info;
    }

    @GetMapping("/api/music-recommendations")
    public Map<String, String> getMusicRecommendations(@RequestParam String duration) {
        if(tokenManager.isTokenExpired()){
            String accessToken = fetchAccessToken(spotifyClientID, spotifyClientSecret, "https://accounts.spotify.com/api/token");
            tokenManager.setAccessToken(accessToken);
        }

        String tokenToUse = tokenManager.getAccessToken();

        Map<String, Object> recommendations = spotifyAPIController.getMusicAndPodcastInformation(duration, tokenToUse);


        // Anropa Spotify API eller hårdkoda rekommendationer baserat på 'destination'
        recommendations.put("Genre", "Lo-fi Chill");
        recommendations.put("Playlist", "https://open.spotify.com/playlist/example");
       // return recommendations;
       return null;
    }

    public String fetchAccessToken(String apiKey, String apiSecret, String APIURL) {
        OkHttpClient client = new OkHttpClient();

        String url = APIURL;
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
