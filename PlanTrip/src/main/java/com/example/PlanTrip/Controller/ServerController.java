package com.example.PlanTrip.Controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.springframework.boot.actuate.autoconfigure.metrics.MetricsProperties.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import com.example.PlanTrip.Database.IataService;
import com.example.PlanTrip.Entity.TokenManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;
import okhttp3.ConnectionPool;
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
    private TmdbAPIController tmdbAPIController = new TmdbAPIController();
    private PexelsAPIController pexelsAPIController = new PexelsAPIController();
    private TokenManager tokenManager;
    private String spotifyClientID;
    private String spotifyClientSecret;
    private String spotifyAccessToken;
    private static String country;
    private String TMDBAPI_KEY;
    private String destination;
    private String TMDB_READ_ACCESS_KEY;
    private String Pexels_API_KEY;
    private ArrayList<HashMap<String, String>> activities;
    private final IataService service;
    
    private static final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS).connectionPool(new ConnectionPool(10, 5, TimeUnit.MINUTES))
            .retryOnConnectionFailure(true).build();
    
    public ServerController(IataService service){
        this.tokenManager = new TokenManager();
        this.spotifyClientID = getInfoFromENV("SPOTIFY_CLIENTID");
        this.spotifyClientSecret = getInfoFromENV("SPOTIFY_CLIENTSECRET");
        this.TMDBAPI_KEY = getInfoFromENV("TMDB_API_KEY");
        this.TMDB_READ_ACCESS_KEY = getInfoFromENV("TMDB_READ_ACCESS_KEY");
        this.Pexels_API_KEY = getInfoFromENV("PEXELS_API_KEY");
        String spotifyToken = fetchAccessToken(spotifyClientID, spotifyClientSecret, "https://accounts.spotify.com/api/token");
        tokenManager.setAccessToken(spotifyToken);
        this.service = service;
    }

    public static OkHttpClient getClient(){
        return httpClient;
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
        country = toArray[1].trim();
        destination = to;
        
        String amadeusApiKey = getInfoFromENV("AMADEUS_API_KEY");
        String amadeusApiSecret = getInfoFromENV("AMADEUS_API_SECRET");
        String chatGptApiKey = getInfoFromENV("CHAT_KEY");

        String fromIATA = "";
        String toIATA = "";

        fromIATA = service.checkIfIataCodeExists(from);
        toIATA = service.checkIfIataCodeExists(to);

        HashMap<String, String> iataCodesList = null;

        if(fromIATA.isEmpty() && toIATA.isEmpty()) {
            iataCodesList = chatGPTController.getIATACode(from, to, chatGptApiKey, true);
            fromIATA = iataCodesList.get("from");
            toIATA = iataCodesList.get("to");
            service.saveIata(from, fromIATA);
            service.saveIata(to, toIATA);

        } else if(fromIATA.isEmpty()){
            iataCodesList = chatGPTController.getIATACode(from, null, chatGptApiKey, false);
            fromIATA = iataCodesList.get("from");
            service.saveIata(from, fromIATA);
            

        } else if(toIATA.isEmpty()){
            iataCodesList = chatGPTController.getIATACode(null, to, chatGptApiKey, false);
            toIATA = iataCodesList.get("to");
            service.saveIata(to, toIATA);
        }

        String accessToken = fetchAccessToken(amadeusApiKey, amadeusApiSecret, "https://test.api.amadeus.com/v1/security/oauth2/token");
        ArrayList<String> result = amadeusController.getFlightInformation(fromIATA.trim(), toIATA.trim(), date, maxPrice, adults, children, infants, travelClass, currency, accessToken);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
                activities = chatGPTController.getActivitySuggestions(destination, chatGptApiKey, pexelsAPIController, Pexels_API_KEY);
                setActivities(activities);
        });
        executor.shutdown();
        
        return result;
    }

    public static String getCountry(){
        return country;
    }

    public void setActivities(ArrayList<HashMap<String, String>> activities){
        this.activities = activities;
    }

    public ArrayList<HashMap<String, String>> getActivities(){
        return activities;
    }

    @GetMapping("/callback")
    public RedirectView handleSpotifyCallback(@RequestParam("code") String code) {
        this.spotifyAccessToken = code;

        return new RedirectView("/music.html");
    }

    public String getInfoFromENV(String input){
        Dotenv dotenv = Dotenv.configure()
        .ignoreIfMissing()
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

    @GetMapping("/getMovies")
    public List<String> getMovieRecomendations(@RequestParam String genre) throws Exception {
        List<String> list = new ArrayList<>();
        
        list = tmdbAPIController.getMovieRecomendationsBasedOnGenre(genre, TMDBAPI_KEY, TMDB_READ_ACCESS_KEY);

        if(list == null){
            throw new Exception("The recomendations list was null");
        }

       return list;
    }

    @GetMapping("/fetch-genre")
    public ResponseEntity<Map<String, String>> fetchGenre() {
        String countryGenre = getGenre(country);        
        Map<String, String> response = new HashMap<>();
        response.put("genre", putSpaceForGenres(countryGenre));

        return ResponseEntity.ok(response);
    }

    @GetMapping("/fetch-activities")
    public ResponseEntity<ArrayList<HashMap<String, String>>> fetchActivities() {
        activities = getActivities();

        return ResponseEntity.ok(activities);
    }

    public String putSpaceForGenres(String input){
        String correctInput = "";
        
            if(input.contains("%20")){
                String line = input.replace("%20", " ");
                correctInput = line;
                return correctInput;
            }

        return input;
    }

    public String getGenre(String destination){
        String genre_FileTxt = "";
        String countryGenre = "";

        try {
            genre_FileTxt = Files.readString(Paths.get("countries_genre.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        String[] lines = genre_FileTxt.split("\\r?\\n");

            for (String line : lines) {
                String[] parts = line.split(":");
                String country = parts[0].trim();
                String genre = parts[1].trim();

                if (country.equalsIgnoreCase(destination)) {
                    String[] genreArray = genre.split(",\\s*");
                    List<String> arr = new ArrayList();
                    countryGenre = genreArray[0];
                    return countryGenre;
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
