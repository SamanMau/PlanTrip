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
        try (Connection conn = DriverManager.getConnection("jdbc:derby:planTripDB;create=true");
             Statement st = conn.createStatement()) {

            String table1 = "CREATE TABLE Iata (" +
                         "id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, " +
                         "iataCode VARCHAR(10), " +
                         "country VARCHAR(50))";

            st.execute(table1);

        } catch (SQLException e) {
            if (!"X0Y32".equals(e.getSQLState())) {
                e.printStackTrace();  // skriv bara ut fel om det är något annat
                }
        }
    }
}
