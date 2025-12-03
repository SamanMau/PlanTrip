package com.example.PlanTrip.Database;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseController {
    
    public String checkIfIataInFile(String location) {
        
        try (Connection conn = DriverManager.getConnection("jdbc:derby:planTripDB");
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery("SELECT * FROM Iata")) {
            
            while (rs.next()) {
                String iataCode = rs.getString("iataCode");
                String country = rs.getString("country");

                if (country.equals(location)) {
                    return iataCode;
                }

        }

} catch (SQLException e) {
    e.printStackTrace();
}
        return "";
    }

    public void addIataToFile(String country, String iataCode) {
        try (Connection conn = DriverManager.getConnection("jdbc:derby:planTripDB");
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO Iata (iataCode, country) VALUES (?, ?)"
            )) {

            ps.setString(1, iataCode);
            ps.setString(2, country);

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}