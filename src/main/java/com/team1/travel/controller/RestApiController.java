package com.team1.travel.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.team1.travel.model.FavoriteVo;
import com.team1.travel.service.FavoriteService;

@RestController
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5000"})
@RequestMapping("/api/survey")
public class RestApiController {
    
    private static final Logger logger = LoggerFactory.getLogger(RestApiController.class);
    private Map<String, Object> latestSurveyData;
    private List<Map<String, Object>> latestPredictions;
    
    @Autowired
    private FavoriteService favoriteService;
    
    @PostMapping("/submit")
    public ResponseEntity<?> submitSurvey(@RequestBody Map<String, Object> requestBody) {
        try {
            logger.info("Received survey data: {}", requestBody);
            
            // null 체크
            if (requestBody == null || !requestBody.containsKey("inputs") || !requestBody.containsKey("modelType")) {
                return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "필수 데이터가 누락되었습니다."
                ));
            }

            @SuppressWarnings("unchecked")
            List<Object> surveyAnswers = (List<Object>) requestBody.get("inputs");
            String modelType = (String) requestBody.get("modelType");
            
            if (surveyAnswers == null || surveyAnswers.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "설문 응답이 비어있습니다."
                ));
            }

            // 데이터 형식 변환
            Map<String, Object> formattedData = formatSurveyData(surveyAnswers);
            formattedData.put("model_type", modelType);
            
            // 최신 데이터 저장
            this.latestSurveyData = formattedData;
            this.latestPredictions = null;
            
            logger.info("Processed survey data: {}", this.latestSurveyData);
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "설문이 성공적으로 처리되었습니다.",
                "data", formattedData
            ));
            
        } catch (Exception e) {
            logger.error("Error processing survey: ", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "error",
                "message", "서버 처리 중 오류가 발생했습니다: " + e.getMessage()
            ));
        }
    }
    
    private Map<String, Object> formatSurveyData(List<Object> inputs) {
        Map<String, Object> formatted = new HashMap<>();
        try {
            formatted.put("SIDO", String.valueOf(inputs.get(0)));
            formatted.put("gender", String.valueOf(inputs.get(1)).equals("남성") ? "M" : "F");
            
            // 연령대 처리
            String ageGroup = String.valueOf(inputs.get(2));
            int age;
            if (ageGroup.contains("이상")) {
                age = 60;  // "60대 이상"인 경우 60으로 설정
            } else {
                age = Integer.parseInt(ageGroup.replace("대", ""));
            }
            formatted.put("age_group", age);
            
            // 동반자 수 처리
            formatted.put("companion_count", String.valueOf(inputs.get(3)).equals("혼자") ? 0 : 
                Integer.parseInt(String.valueOf(inputs.get(3)).replace("명", "")));
            
            // 동반자 유형
            formatted.put("companion_type", String.valueOf(inputs.get(4)).toLowerCase());
            
            // 여행 스타일
            formatted.put("travel_motive_primary", String.valueOf(inputs.get(5)).toLowerCase().replace("/", "_"));
            formatted.put("travel_motive_secondary", String.valueOf(inputs.get(6)).toLowerCase().replace("/", "_"));
            
            // 이동수단
            formatted.put("transport_primary", String.valueOf(inputs.get(8)).toLowerCase());
            formatted.put("transport_secondary", String.valueOf(inputs.get(9)).toLowerCase());
            
            // 선호도 점수
            formatted.put("nature_rating", convertRatingToNumber(String.valueOf(inputs.get(10))));
            formatted.put("culture_rating", convertRatingToNumber(String.valueOf(inputs.get(11))));
            formatted.put("activity_rating", convertRatingToNumber(String.valueOf(inputs.get(12))));
            
            // 예산 정보
            if (inputs.size() > 13) {
                formatted.put("budget", String.valueOf(inputs.get(13)));
            }
            
        } catch (IndexOutOfBoundsException e) {
            logger.error("Invalid input array length: ", e);
            throw new IllegalArgumentException("입력 데이터 형식이 올바르지 않습니다.");
        } catch (Exception e) {
            logger.error("Error formatting survey data: ", e);
            throw new IllegalArgumentException("데이터 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
        return formatted;
    }
    
    private int convertRatingToNumber(String rating) {
        Map<String, Integer> ratings = Map.of(
            "매우 선호", 5,
            "선호", 4,
            "보통", 3,
            "비선호", 2,
            "매우 비선호", 1
        );
        return ratings.getOrDefault(rating, 3);  // 기본값 3 (보통)
    }
    
    @PostMapping("/predictions")
    public ResponseEntity<?> savePredictions(@RequestBody Map<String, Object> requestBody) {
        try {
            logger.info("Received prediction results: {}", requestBody);
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> predictions = (List<Map<String, Object>>) requestBody.get("predictions");
            this.latestPredictions = predictions;
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "예측 결과가 성공적으로 저장되었습니다.",
                "predictions", predictions
            ));
            
        } catch (Exception e) {
            logger.error("Error saving predictions: ", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }
    
    @GetMapping("/latest")
    public ResponseEntity<?> getLatestSurvey() {
        if (latestSurveyData == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(Map.of(
            "inputs", latestSurveyData,
            "predictions", latestPredictions != null ? latestPredictions : List.of()
        ));
    }
    
    @GetMapping("/results")
    public ResponseEntity<?> getLatestResults() {
        if (latestPredictions == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Map.of("predictions", latestPredictions));
    }
    
    // 즐겨찾기 추가 메서드
    @PostMapping("/favorites/add")
    public ResponseEntity<?> addFavorite(@RequestBody Map<String, Object> requestBody) {
        try {
            // 요청 본문에서 필요한 데이터 추출
            int userNo = Integer.parseInt(String.valueOf(requestBody.get("userNo")));
            String placeName = String.valueOf(requestBody.get("placeName"));
            String address = String.valueOf(requestBody.get("address"));

            // 중복 즐겨찾기 체크
            if (favoriteService.checkDuplicateFavorite(userNo, placeName, address)) {
                return ResponseEntity.ok(Map.of(
                    "status", "error",
                    "message", "이미 추가된 즐겨찾기 입니다."
                ));
            }

            // FavoriteVO 객체 생성
            FavoriteVo favoriteVo = FavoriteVo.builder()
                .userNo(userNo)
                .placeName(placeName)
                .address(address)
                .build();

            // 서비스를 통해 즐겨찾기 추가
            boolean result = favoriteService.addFavorite(favoriteVo);

            if (result) {
                return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "즐겨찾기에 추가되었습니다."
                ));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "status", "error",
                    "message", "즐겨찾기 추가에 실패했습니다."
                ));
            }
            
        } catch (Exception e) {
            logger.error("Error adding favorite: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "status", "error",
                "message", "즐겨찾기 추가 중 오류가 발생했습니다: " + e.getMessage()
            ));
        }
    }

    // 사용자의 최근 즐겨찾기 목록 조회 메서드
    @GetMapping("/favorites/{userNo}")
    public ResponseEntity<?> getUserFavorites(@PathVariable int userNo) {
        try {
            List<FavoriteVo> favorites = favoriteService.getUserFavorites(userNo);
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "favorites", favorites
            ));
        } catch (Exception e) {
            logger.error("Error retrieving user favorites: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "status", "error",
                "message", "즐겨찾기 조회 중 오류가 발생했습니다: " + e.getMessage()
            ));
        }
    }
}