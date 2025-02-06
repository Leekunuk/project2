package com.team1.travel.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class MapController {

	@Value("${kakaomap.api.key}")
	private String kakaoApiKey;

	// 맵을 표시하는 페이지를 반환하는 메서드
	@GetMapping("/map")
	public String showMapPage(@RequestParam("location") String location, @RequestParam("lat") double lat,
			@RequestParam("lng") double lng, Model model) {
		model.addAttribute("kakaoMapsApiKey", kakaoApiKey);
		return "map/map"; // resources/templates/map/map.html을 반환
	}
}
