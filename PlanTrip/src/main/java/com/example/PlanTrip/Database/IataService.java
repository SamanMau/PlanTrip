package com.example.PlanTrip.Database;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Service
public class IataService {

    @Autowired
    private IataRepository repo;

    public IataService(IataRepository repo) {
        this.repo = repo;
    }

    public void saveIata(String code, String country) {
        Iata i = new Iata();
        i.setIataCode(code);
        i.setCountry(country);
        repo.save(i);
    }

    public String checkIfIataCodeExists(String country) {
        Iata iata = repo.findByCountry(country);
        if (iata != null) {
            return iata.getIataCode();
        }
        return "";
    }

    public List<Iata> getAllIata() {
        return repo.findAll();
    }

}