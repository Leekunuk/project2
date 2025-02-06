package com.team1.travel.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LocalController {

    @GetMapping("/local/jeju")
    public String getJejuPage(Model model) {
        // 필요한 데이터 모델을 추가할 수 있습니다.
        return "local/jeju";  // templates/local/jeju.html로 렌더링
    }

    @GetMapping("/local/busan")
    public String getBusanPage(Model model) {
        return "local/busan";  // templates/local/busan.html로 렌더링
    }

    @GetMapping("/local/seoul")
    public String getSeoulPage(Model model) {
        return "local/seoul";  // templates/local/seoul.html로 렌더링
    }
}
