package com.team1.travel.model;

import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FavoriteVo {
    private int favoriteId;
    private int userNo;
    private String placeName;
    private String address;
    private Timestamp createdAt;
}