package com.team1.travel.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder	
public class WeatherData {
    private String temperature;
    private String sky;
    private String precipitation;
    private String rainProbability;
    private String location;
}
