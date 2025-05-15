package com.example.PlanTrip.Controller.Entity;

import java.util.HashMap;
import java.util.List;

public class FlightBlock {
    public List<HashMap<String, Object>> flight;
    public int nextIndex;

    public FlightBlock(List<HashMap<String, Object>> flight, int nextIndex) {
        this.flight = flight;
        this.nextIndex = nextIndex;
    }
}
