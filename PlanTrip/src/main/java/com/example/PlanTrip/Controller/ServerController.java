package com.example.PlanTrip.Controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api")
public class ServerController {
    private AmadeusAPIController amadeusController = new AmadeusAPIController();
    private ChatGPTAPIController chatGPTController = new ChatGPTAPIController();
    private SpotifyAPIController spotifyAPIController = new SpotifyAPIController();
    private TokenManager tokenManager;
    private String spotifyClientID;
    private String spotifyClientSecret;
    private String spotifyAccessToken;
    private String destination;

    public ServerController(){
        this.tokenManager = new TokenManager();
        this.spotifyClientID = getInfoFromENV("SPOTIFY_CLIENTID");
        this.spotifyClientSecret = getInfoFromENV("SPOTIFY_CLIENTSECRET");
        String spotifyToken = fetchAccessToken(spotifyClientID, spotifyClientSecret, "https://accounts.spotify.com/api/token");
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
        
        String[] toArray = to.split(",");
        destination = toArray[1].trim();
        
        String amadeusApiKey = getInfoFromENV("AMADEUS_API_KEY");
        String amadeusApiSecret = getInfoFromENV("AMADEUS_API_SECRET");
        String chatGptApiKey = getInfoFromENV("CHAT_KEY");

        HashMap<String, String> iataCodesList = chatGPTController.getIATACode(from, to, chatGptApiKey);
        String fromIATA = iataCodesList.get("from");
        String toIATA = iataCodesList.get("to");

        String accessToken = fetchAccessToken(amadeusApiKey, amadeusApiSecret, "https://test.api.amadeus.com/v1/security/oauth2/token");

        ArrayList<String> result = amadeusController.getFlightInformation(fromIATA, toIATA, date, maxPrice, adults, children, infants, travelClass, currency, accessToken);

        return result;
    }

    @GetMapping("/callback")
    public RedirectView handleSpotifyCallback(@RequestParam("code") String code) {
        this.spotifyAccessToken = code;

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

    @GetMapping("/musicRecommendations")
    public List<String> getMusicRecommendations(@RequestParam String genre) throws Exception {
        if(tokenManager.isTokenExpired()){
            String accessToken = fetchAccessToken(spotifyClientID, spotifyClientSecret, "https://accounts.spotify.com/api/token");
            tokenManager.setAccessToken(accessToken);
        }
        
        String tokenToUse = tokenManager.getAccessToken();

        List<String> recommendations = spotifyAPIController.getMusicAndPodcastInformation(amadeusController.getShortestFlightDuration(), tokenToUse, genre);

        if(recommendations == null){
            throw new Exception("The recomendations list was null");
        }

       return recommendations;
    }

    @GetMapping("/fetch-genre")
    public ResponseEntity<List<String>> fetchGenre() {
        List<String> genres = getGenre(destination);
        
        return ResponseEntity.ok(putSpaceForGenres(genres));
    }

    public List<String> putSpaceForGenres(List<String> arr){
        List<String> newList = new ArrayList<>();

        for(String line : arr){
            if(line.contains("%20")){
                line.replace("%20", " ");
                newList.add(line);
            }
        }

        return newList;
    }

    public List<String> getGenre(String destination){
        String countries_genre = "";
        
        try {
            countries_genre = Files.readString(Paths.get("countries_genre.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        String[] lines = countries_genre.split("\\r?\\n");

            for (String line : lines) {
                String[] parts = line.split(":");
                String country = parts[0].trim();
                String genres = parts[1].trim();

                if (country.equalsIgnoreCase(destination)) {
                    String[] genreArray = genres.split(",\\s*");
                    List<String> arr = new ArrayList();

                    for(int i = 0; i < genreArray.length; i++){
                        arr.add(genreArray[i]);
                    }
                    return arr;
                }
            }

        return null;    
    }



    public String fetchAccessToken(String apiKey, String apiSecret, String APIURL) {
        OkHttpClient client = new OkHttpClient();

        String url = APIURL;
        String body = "grant_type=client_credentials";

        RequestBody requestBody = RequestBody.create(body,MediaType.parse("application/x-www-form-urlencoded"));        
        
        String basicAuth = okhttp3.Credentials.basic(apiKey, apiSecret);

        okhttp3.Request request = new okhttp3.Request.Builder()
        .url(url)
        .post(requestBody)
        .addHeader("Authorization", basicAuth)
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
