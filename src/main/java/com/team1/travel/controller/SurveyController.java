package com.team1.travel.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.team1.travel.model.UserVo;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class SurveyController {

	@Value("${kakaomap.api.key}")
	private String kakaoApiKey;

	@GetMapping("/survey/form")
	public String surveyform(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
		UserVo user = (UserVo) session.getAttribute("loggedInUser");
		if (user == null) {
			redirectAttributes.addFlashAttribute("message", "로그인이 필요한 서비스입니다.");
			return "redirect:/"; // 홈으로 리다이렉트
		}
		model.addAttribute("user", user);
		return "survey/surveyform";
	}

	@GetMapping("/survey/results")
	public String showResults(HttpSession session, Model model) {
		UserVo user = (UserVo) session.getAttribute("loggedInUser");
		model.addAttribute("user", user);
	    model.addAttribute("kakaoMapsApiKey", kakaoApiKey);
		return "survey/surveyResult";
	}

}
