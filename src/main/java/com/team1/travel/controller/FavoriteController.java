package com.team1.travel.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.team1.travel.model.FavoriteVo;
import com.team1.travel.model.UserVo;
import com.team1.travel.service.FavoriteService;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    @Autowired
    private HttpSession session;

    @GetMapping("/{userNo}")
    public ResponseEntity<?> getUserFavorites(@PathVariable int userNo) {
        try {
            UserVo loggedInUser = (UserVo) session.getAttribute("loggedInUser");
            
            // 로그인하지 않았거나 다른 사용자의 즐겨찾기에 접근 시도할 경우
            if (loggedInUser == null || loggedInUser.getUserNo() != userNo) {
                return ResponseEntity.status(302).body(Map.of(
                    "redirect", "/"
                ));
            }

            List<FavoriteVo> favorites = favoriteService.getUserFavorites(userNo);

            if (favorites.isEmpty()) {
                return ResponseEntity.status(204).body(Map.of(
                    "status", "success",
                    "message", "즐겨찾기 목록이 없습니다."
                ));
            }

            return ResponseEntity.ok(Map.of(
                "status", "success",
                "favorites", favorites
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "message", "즐겨찾기 조회 중 오류가 발생했습니다: " + e.getMessage()
            ));
        }
    }

    @DeleteMapping("/{userNo}/{favoriteId}")
    public ResponseEntity<?> deleteFavorite(@PathVariable int userNo, @PathVariable int favoriteId) {
        try {
            UserVo loggedInUser = (UserVo) session.getAttribute("loggedInUser");
            
            // 로그인하지 않았거나 다른 사용자의 즐겨찾기를 삭제 시도할 경우
            if (loggedInUser == null || loggedInUser.getUserNo() != userNo) {
                return ResponseEntity.status(302).body(Map.of(
                    "redirect", "/"
                ));
            }

            int result = favoriteService.deleteFavorite(userNo, favoriteId);

            if (result > 0) {
                return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "즐겨찾기가 삭제되었습니다."
                ));
            } else {
                return ResponseEntity.status(404).body(Map.of(
                    "status", "error",
                    "message", "해당 즐겨찾기를 찾을 수 없습니다."
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "message", "즐겨찾기 삭제 중 오류가 발생했습니다: " + e.getMessage()
            ));
        }
    }
}