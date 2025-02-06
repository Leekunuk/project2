package com.team1.travel.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import com.team1.travel.model.WeatherData;
import com.team1.travel.model.Grid;

import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import java.util.ArrayList;
import java.util.List;

@Service
public class WeatherService {
    private static final String API_URL = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst";
    private final String apiKey;
    private final List<Grid> gridList;
    private static final double EARTH_RADIUS = 6371.0; // 지구의 반지름 (km)

    public static final Map<String, String> STATUS_OF_SKY = Map.of(
        "1", "맑음",
        "3", "구름많음",
        "4", "흐림"
    );

    public static final Map<String, String> STATUS_OF_PRECIPITATION = Map.of(
        "0", "없음",
        "1", "비",
        "2", "비/눈",
        "3", "눈",
        "4", "소나기"
    );

    public WeatherService(@Value("${weather.api.key}") String apiKey) {
        this.apiKey = apiKey;
        this.gridList = loadGridData();
    }

    private List<Grid> loadGridData() {
        List<Grid> grids = new ArrayList<>();
        try {
            System.out.println("Starting to load grid data...");
            InputStream is = new ClassPathResource("static/xls/FinalWeatherXY.xlsx").getInputStream();
            Workbook workbook = WorkbookFactory.create(is);
            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    try {
                        // 1단계 (시/도) 읽기
                        Cell regionCell = row.getCell(2);
                        if (regionCell == null) continue;
                        String region = regionCell.getStringCellValue().trim();
                        if (region.isEmpty()) continue;
                        
                        // 2단계 (시/군/구) 읽기
                        Cell subRegionCell = row.getCell(3);
                        String subRegion = null;
                        if (subRegionCell != null && subRegionCell.getCellType() == CellType.STRING) {
                            String subRegionValue = subRegionCell.getStringCellValue().trim();
                            if (!subRegionValue.isEmpty()) {
                                subRegion = subRegionValue;
                            }
                        }
                        
                        // nx, ny 읽기
                        Cell nxCell = row.getCell(4);
                        Cell nyCell = row.getCell(5);
                        if (nxCell == null || nyCell == null) continue;
                        
                        int nx = (int) nxCell.getNumericCellValue();
                        int ny = (int) nyCell.getNumericCellValue();
                        
                        // nx나 ny가 0인 행은 건너뛰기
                        if (nx == 0 || ny == 0) continue;
                        
                        // 위도 경도 읽기
                        double longitude = row.getCell(6).getNumericCellValue();
                        double latitude = row.getCell(8).getNumericCellValue();
                        
                        Grid grid = Grid.builder()
                                .nx(nx)
                                .ny(ny)
                                .longitude(longitude)
                                .latitude(latitude)
                                .region(region)
                                .subRegion(subRegion)
                                .build();
                        
                        System.out.println(String.format("Loaded grid for %s %s: nx=%d, ny=%d, lat=%.2f, lon=%.2f",
                            region, 
                            subRegion != null ? subRegion : "",
                            nx, ny, latitude, longitude));
                        
                        grids.add(grid);
                        
                    } catch (Exception e) {
                        System.out.println("Error processing row " + i + ": " + e.getMessage());
                    }
                }
            }
            
            System.out.println("Total loaded grids: " + grids.size());
            workbook.close();
            is.close();
            
        } catch (Exception e) {
            System.out.println("Error loading grid data: " + e.getMessage());
            e.printStackTrace();
        }
        return grids;
    }

    public Grid findNearestGrid(double lat, double lon) {
        Grid nearest = null;
        double minDistance = Double.MAX_VALUE;

        System.out.println("\nFinding nearest grid for lat=" + lat + ", lon=" + lon);
        System.out.println("Available grids: " + gridList.size());

        for (Grid grid : gridList) {
            double distance = calculateDistance(lat, lon, grid.getLatitude(), grid.getLongitude());
            if (distance < minDistance) {
                minDistance = distance;
                nearest = grid;
                String location = grid.getRegion();
            if (grid.getSubRegion() != null) {
                location += " " + grid.getSubRegion();
            }
            System.out.println("New nearest found: " + location + 
                             " (nx=" + grid.getNx() + ", ny=" + grid.getNy() + 
                             ", distance=" + distance + "km)");
            }
        }
        
        if (nearest != null) {
            System.out.println("\nSelected Location:");
            System.out.println("- Input: lat=" + lat + ", lon=" + lon);
            String location = nearest.getRegion();
            if (nearest.getSubRegion() != null) {
                location += " " + nearest.getSubRegion();
            }
            System.out.println("- Nearest region: " + location);
            System.out.println("- Grid Point: nx=" + nearest.getNx() + ", ny=" + nearest.getNy());
            System.out.println("- Distance: " + minDistance + "km");
        }
        
        return nearest;
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // 지구의 반지름 (km)
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                 + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                 * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c;
    }

    public Optional<WeatherData> fetchCurrentWeather(double lat, double lon) {
        try {
            Grid nearestGrid = findNearestGrid(lat, lon);
            if (nearestGrid == null) {
                return Optional.empty();
            }

            ZonedDateTime currentTimeKst = ZonedDateTime.now();
            String baseDate;
            String baseTime;
            
            int hour = currentTimeKst.getHour();
            int[] availableTimes = {2, 5, 8, 11, 14, 17, 20, 23};
            
            int baseHour = -1;
            for (int i = availableTimes.length - 1; i >= 0; i--) {
                if (hour >= availableTimes[i]) {
                    baseHour = availableTimes[i];
                    break;
                }
            }
            
            if (baseHour == -1) {
                baseDate = currentTimeKst.minusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                baseTime = "2300";
            } else {
                baseDate = currentTimeKst.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                baseTime = String.format("%02d00", baseHour);
            }

            String urlString = buildUrl(nearestGrid.getNx(), nearestGrid.getNy(), baseDate, baseTime);
            String jsonResponse = fetchDataFromApi(urlString);
            
            if (jsonResponse == null) {
                System.out.println("No response received from API");
                return Optional.empty();
            }

            System.out.println("\nAPI Response received. Length: " + jsonResponse.length() + " characters");
            return parseWeatherData(jsonResponse, nearestGrid);

        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    private String buildUrl(int nx, int ny, String baseDate, String baseTime) throws Exception {
        StringBuilder urlBuilder = new StringBuilder(API_URL);
        urlBuilder.append("?").append(URLEncoder.encode("serviceKey", "UTF-8")).append("=").append(apiKey);
        urlBuilder.append("&").append(URLEncoder.encode("pageNo", "UTF-8")).append("=").append("1");
        urlBuilder.append("&").append(URLEncoder.encode("numOfRows", "UTF-8")).append("=").append("10");
        urlBuilder.append("&").append(URLEncoder.encode("dataType", "UTF-8")).append("=").append("JSON");
        urlBuilder.append("&").append(URLEncoder.encode("base_date", "UTF-8")).append("=").append(baseDate);
        urlBuilder.append("&").append(URLEncoder.encode("base_time", "UTF-8")).append("=").append(baseTime);
        urlBuilder.append("&").append(URLEncoder.encode("nx", "UTF-8")).append("=").append(String.valueOf(nx));
        urlBuilder.append("&").append(URLEncoder.encode("ny", "UTF-8")).append("=").append(String.valueOf(ny));
        
        System.out.println("Request URL: " + urlBuilder.toString());
        System.out.println("Parameters:");
        System.out.println("- base_date: " + baseDate);
        System.out.println("- base_time: " + baseTime);
        System.out.println("- nx: " + nx);
        System.out.println("- ny: " + ny);
        
        return urlBuilder.toString();
    }

    private String fetchDataFromApi(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        
        if (conn.getResponseCode() != 200) {
            return null;
        }

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        } finally {
            conn.disconnect();
        }
    }

    private Optional<WeatherData> parseWeatherData(String jsonStr, Grid grid) {
        System.out.println("\nParsing weather data...");
        System.out.println("JSON Response: " + jsonStr);

        try {
            JsonObject jsonObject = JsonParser.parseString(jsonStr).getAsJsonObject();
            if (!jsonObject.has("response")) {
                return Optional.empty();
            }
            
            JsonObject response = jsonObject.getAsJsonObject("response");
            if (response == null || !response.has("body")) {
                return Optional.empty();
            }

            JsonObject body = response.getAsJsonObject("body");
            if (body == null || !body.has("items")) {
                return Optional.empty();
            }

            JsonObject items = body.getAsJsonObject("items");
            if (items == null || !items.has("item")) {
                return Optional.empty();
            }

            JsonArray itemArray = items.getAsJsonArray("item");

            String temp = null;
            String sky = null;
            String pty = null;
            String pop = null;

            for (JsonElement item : itemArray) {
                JsonObject itemObj = item.getAsJsonObject();
                String category = itemObj.get("category").getAsString();
                String value = itemObj.get("fcstValue").getAsString();

                switch (category) {
                    case "TMP":
                        temp = value;
                        System.out.println("Temperature: " + value + "°C");
                        break;
                    case "SKY":
                        sky = STATUS_OF_SKY.getOrDefault(value, "알 수 없음");
                        System.out.println("Sky condition: " + sky + " (code: " + value + ")");
                        break;
                    case "PTY":
                        pty = STATUS_OF_PRECIPITATION.getOrDefault(value, "알 수 없음");
                        System.out.println("Precipitation: " + pty + " (code: " + value + ")");
                        break;
                    case "POP":
                        pop = value;
                        System.out.println("Precipitation probability: " + value + "%");
                        break;
                }
            }

            if (temp != null && sky != null && pty != null && pop != null) {
                String location = grid.getSubRegion() != null ? 
                               grid.getRegion() + " " + grid.getSubRegion() : 
                               grid.getRegion();
                               
                return Optional.of(WeatherData.builder()
                        .temperature(temp)
                        .sky(sky)
                        .precipitation(pty)
                        .rainProbability(pop)
                        .location(location)
                        .build());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }
}