package com.team1.travel.controller;


import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.team1.travel.model.Grid;
import com.team1.travel.model.UserVo;
import com.team1.travel.model.WeatherData;
import com.team1.travel.service.WeatherService;

import jakarta.servlet.http.HttpSession;

@Controller
public class MainController {

	@Value("${popular.api.key}")
	private String popularApiKey;
	
	 @Value("${weather.api.key}")
	 private String weatherApiKey;
	 
	 @Value("${kakaomap.api.key}")
	 private String kakaoApiKey;
	 
	// index page
	@GetMapping("/")
	public String index(Model model, HttpSession session) {
		UserVo user = (UserVo) session.getAttribute("loggedInUser");
		model.addAttribute("user", user);
		return "index";
	}
	
	//랜덤 장소 추천
	@GetMapping("/fatra")
    public String showFatraPage(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        UserVo user = (UserVo) session.getAttribute("loggedInUser");
		if (user == null) {
			redirectAttributes.addFlashAttribute("message", "로그인후 여행지를 확인하실 수 있습니다!");
			return "redirect:/"; // 홈으로 리다이렉트
		}
        model.addAttribute("user", user);
		model.addAttribute("popularApiKey", popularApiKey);

       
        return "fatra";
	}
	
	@GetMapping("/weather")
	@ResponseBody
	public Map<String, String> getWeatherInfo(
	        @RequestParam("lat") double latitude,
	        @RequestParam("lon") double longitude) {

	    Map<String, String> response = new HashMap<>();

	    try {
	        WeatherService weather = new WeatherService(weatherApiKey);
	        Optional<WeatherData> weatherData = weather.fetchCurrentWeather(latitude, longitude);

	        if (weatherData.isPresent()) {
	            WeatherData data = weatherData.get();
	            response.put("date", ZonedDateTime.now(ZoneId.of("Asia/Seoul"))
	                    .format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 EEEE", Locale.KOREAN)));

	            // 날씨 상태와 강수 형태를 결합
	            String weatherStatus;
	            if (data.getPrecipitation().equals("없음")) {
	                weatherStatus = data.getSky(); // 강수가 없으면 하늘 상태만 표시
	            } else {
	                weatherStatus = data.getSky() + "(" + data.getPrecipitation() + ")"; // 강수가 있으면 둘 다 표시
	            }

	            response.put("weather", weatherStatus);
	            response.put("temperature", data.getTemperature());
	            response.put("rainProbability", data.getRainProbability());
	            
	            Grid nearestGrid = weather.findNearestGrid(latitude, longitude);
	            // subRegion이 있다면 함께 포함
	            String location = nearestGrid.getSubRegion() != null 
	                ? nearestGrid.getRegion() + " " + nearestGrid.getSubRegion() 
	                : nearestGrid.getRegion();
	            response.put("location", location);
	        } else {
	            response.put("error", "날씨 정보를 가져오지 못했습니다.");
	        }
	    } catch (Exception e) {
	        response.put("error", "날씨 정보를 처리하는 중 오류가 발생했습니다: " + e.getMessage());
	    }

	    return response;
	}
    
    // AI 추천 페이지
	@GetMapping("/recommendation")
	public String recommendation(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
		UserVo user = (UserVo) session.getAttribute("loggedInUser");
		if (user == null) {
			redirectAttributes.addFlashAttribute("message", "로그인이 필요한 서비스입니다.");
			return "redirect:/"; // 홈으로 리다이렉트
		}
		model.addAttribute("user", user);
		return "recommend";
	}


	

}