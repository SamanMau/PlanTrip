package com.example.PlanTrip.Database;

import java.sql.*;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

// Annotation makes sure that an object is automatically created when the app starts.
@Component
public class DerbyInit {

    /*
    Annotaiton makes sure that this method is called automatically 
    when this class's object is created by spring boot.
    */
    @PostConstruct
    public void init() {
        try (Connection conn =
                DriverManager.getConnection("jdbc:derby:planTripDB;create=true")) {
            System.out.println("Ansluten till Java DB!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
