package com.team1.travel.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.team1.travel.model.UserVo;
import com.team1.travel.service.UserService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Controller
public class UserController {

	 @Value("${kakaomap.api.key}")
	 private String kakaoApiKey;
	
    @Autowired
    private UserService userService;

    // 회원가입 페이지
    @GetMapping("/user/signup")
    public String signupPage() {
        return "user/signup";
    }

    // 로그인 페이지
    @GetMapping("/user/login")
    public String loginPage() {
        return "user/login";
    }
    
    // 마이 페이지
    @GetMapping("/user/mypage")
    public String mypage(HttpSession session,Model model) {
        UserVo user = (UserVo) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/";
        }
        model.addAttribute("user", user);
        return "user/mypage";
    }
    
    //회원 탈퇴 모달
    @GetMapping("/user/mypage/delete")
    public String deleteAccountModal(HttpSession session, Model model) {
        UserVo user = (UserVo) session.getAttribute("loggedInUser");
        model.addAttribute("user", user);
        return "user/deleteAccount";
    }

    @PostMapping("/mypage/delete")
    @ResponseBody  
    public Map<String, Object> delete(@RequestParam String password, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        UserVo currentUser = (UserVo) session.getAttribute("loggedInUser");
        
        if (currentUser != null) {
            boolean result = userService.deleteUser(currentUser.getUserNo(), password);
            
            if (result) {
                session.invalidate();
                response.put("success", true);
                response.put("message", "회원 탈퇴가 완료되었습니다.");
            } else {
                response.put("success", false);
                response.put("message", "비밀번호가 일치하지 않습니다.");
            }
        } else {
            response.put("success", false);
            response.put("message", "로그인 상태가 아닙니다.");
        }
        
        return response;
    }
    
    @PostMapping("/user/mypage/update")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateProfile(
        @ModelAttribute UserVo updatedUser,
        @RequestParam String currentPassword,
        @RequestParam(required = false) String newPassword,
        HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        UserVo currentUser = (UserVo) session.getAttribute("loggedInUser");
        
        if (currentUser == null) {
            response.put("success", false);
            response.put("message", "로그인이 필요합니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        
        // 현재 사용자의 번호 설정
        updatedUser.setUserNo(currentUser.getUserNo());
        
        // 현재 비밀번호 확인
        if (!userService.checkPassword(currentUser.getUserNo(), currentPassword)) {
            response.put("success", false);
            response.put("message", "현재 비밀번호가 일치하지 않습니다.");
            return ResponseEntity.ok(response);
        }
        
        try {
            // 새 비밀번호가 있는 경우
            if (newPassword != null && !newPassword.trim().isEmpty()) {
                boolean passwordUpdated = userService.updatePassword(
                    currentUser.getUserNo(), 
                    currentPassword, 
                    newPassword
                );
                if (!passwordUpdated) {
                    response.put("success", false);
                    response.put("message", "비밀번호 변경에 실패했습니다.");
                    return ResponseEntity.ok(response);
                }
            }
            
            // 기본 정보 업데이트
            boolean profileUpdated = userService.updateUserInfo(updatedUser);
            
            if (profileUpdated) {
                // 세션 업데이트
                UserVo updatedUserInfo = userService.getUserByNo(currentUser.getUserNo());
                session.setAttribute("loggedInUser", updatedUserInfo);
                
                response.put("success", true);
                response.put("message", "회원정보가 성공적으로 수정되었습니다.");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "회원정보 수정에 실패했습니다.");
                return ResponseEntity.ok(response);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "서버 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/user/mypage/check-password")
    @ResponseBody
    public Map<String, Boolean> checkCurrentPassword(
        @RequestParam String currentPassword, 
        HttpSession session) {
        
        Map<String, Boolean> response = new HashMap<>();
        UserVo currentUser = (UserVo) session.getAttribute("loggedInUser");
        
        if (currentUser != null) {
            boolean isValid = userService.checkPassword(
                currentUser.getUserNo(), 
                currentPassword
            );
            response.put("valid", isValid);
        } else {
            response.put("valid", false);
        }
        
        return response;
    }
    
    // 즐겨 찾기
	@GetMapping("/user/favorites")
	public String favorites(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
	    UserVo user = (UserVo) session.getAttribute("loggedInUser");
	    if (user == null) {
	        redirectAttributes.addFlashAttribute("message", "로그인이 필요한 서비스입니다.");
	        return "redirect:/"; // 홈으로 리다이렉트
	    }
	    model.addAttribute("user", user);
	    model.addAttribute("kakaoMapsApiKey", kakaoApiKey);
	    return "user/favorites";
	}

    
    // 회원가입
    @PostMapping("/user/signup")
    public String add(@ModelAttribute UserVo bean) {
        userService.add(bean);
        return "redirect:/";
    }

    // 이메일 중복 체크
    @PostMapping("/check-email")
    @ResponseBody
    public String checkEmail(@RequestParam String userEmail) {
        boolean isAvailable = userService.isEmailAvailable(userEmail);
        return isAvailable ? "available" : "exists";
    }

    // 로그인
    @PostMapping("/login")
    @ResponseBody
    public String login(@RequestParam String userEmail, @RequestParam String userPw, 
            HttpSession session) {
        UserVo user = userService.login(userEmail, userPw);
        if (user != null) {
            session.setAttribute("loggedInUser", user);
            return "success";
        } else {
            return "fail";
        }
    }

    // 로그아웃
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/";
    }
}
