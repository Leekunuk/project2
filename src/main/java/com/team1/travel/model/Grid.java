package com.team1.travel.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Grid {
    private int nx;
    private int ny;
    private double longitude;
    private double latitude;
    private String region;
    private String subRegion;
}