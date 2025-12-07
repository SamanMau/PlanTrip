package com.example.PlanTrip.Database;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;


@Entity
public class Iata {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String iataCode;
    private String country;

    public Iata() {}

    public void setIataCode(String iataCode) {
        this.iataCode = iataCode;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getIataCode() {
        return iataCode;
    }

    public String getCountry() {
        return country;
    }
}
