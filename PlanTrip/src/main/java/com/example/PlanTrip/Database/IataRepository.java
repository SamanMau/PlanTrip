package com.example.PlanTrip.Database;

import org.springframework.data.jpa.repository.JpaRepository;

public interface IataRepository extends JpaRepository<Iata, Long> {
    Iata findByCountry(String country);
    
}
