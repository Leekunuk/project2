package com.team1.travel.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.team1.travel.model.UserDao;
import com.team1.travel.model.UserVo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;

    public UserVo login(String userEmail, String userPw) {
        UserVo user = userDao.findByEmail(userEmail);
        if (user != null) {
            String storedPassword = userDao.getPasswordByEmail(userEmail);
            if (passwordEncoder.matches(userPw, storedPassword)) {
                return user; // 로그인 성공
            }
        }
        return null; // 로그인 실패
    }
    
    public void add(UserVo bean) {
        bean.setUserPw(passwordEncoder.encode(bean.getUserPw())); // 비밀번호 암호화
        userDao.addInfo(bean);
    }
    
    // 중복 이메일 확인
    public boolean isEmailAvailable(String userEmail) {
        int count = userDao.countByEmail(userEmail);
        return count == 0; // 이메일이 존재하지 않으면 true 반환
    }
    
    // 회원 정보 수정
    @Transactional
    public boolean updateUserInfo(UserVo user) {
        return userDao.updateUserInfo(user) > 0;
    }

    // 비밀번호 변경
    @Transactional
    public boolean updatePassword(int userNo, String currentPassword, String newPassword) {
        UserVo user = userDao.findByUserNo(userNo);
        if (user != null) {
            String storedPassword = userDao.getPasswordByEmail(user.getUserEmail());
            if (passwordEncoder.matches(currentPassword, storedPassword)) {
                String encodedNewPassword = passwordEncoder.encode(newPassword);
                return userDao.updatePassword(userNo, encodedNewPassword) > 0;
            }
        }
        return false;
    }

    // 회원 탈퇴
    @Transactional
    public boolean deleteUser(int userNo, String password) {
        UserVo user = userDao.findByUserNo(userNo);
        if (user != null) {
            String storedPassword = userDao.getPasswordByEmail(user.getUserEmail());
            if (passwordEncoder.matches(password, storedPassword)) {
                return userDao.deleteUser(userNo) > 0;
            }
        }
        return false;
    }
    
    //비밀번호 검증
    public boolean checkPassword(int userNo, String password) {
        UserVo user = userDao.findByUserNo(userNo);
        if (user != null) {
            String storedPassword = userDao.getPasswordByEmail(user.getUserEmail());
            return passwordEncoder.matches(password, storedPassword);
        }
        return false;
    }
    
    public UserVo getUserByNo(int userNo) {
        return userDao.findByUserNo(userNo);
    }
}
