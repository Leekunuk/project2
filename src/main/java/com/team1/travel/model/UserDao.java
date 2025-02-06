package com.team1.travel.model;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserDao {
    // 로그인 확인 (비밀번호는 조회하지 않음)
    @Select("SELECT userNo, userName, userEmail, userPhone FROM users WHERE userEmail = #{userEmail}")
    UserVo findByEmail(@Param("userEmail") String userEmail);
    
    // 비밀번호 확인을 위한 별도 메소드
    @Select("SELECT userPw FROM users WHERE userEmail = #{userEmail}")
    String getPasswordByEmail(@Param("userEmail") String userEmail);
    
    // 중복 이메일 확인
    @Select("SELECT COUNT(*) FROM users WHERE userEmail = #{userEmail}")
    int countByEmail(@Param("userEmail") String userEmail);
    
    // 회원가입
    @Insert("INSERT INTO users (userName, userEmail, userPw, userPhone) VALUES (#{userName}, #{userEmail}, #{userPw}, #{userPhone})")
    int addInfo(UserVo bean);
    
    // 회원 정보 수정
    @Update("UPDATE users SET userName = #{userName}, userPhone = #{userPhone} WHERE userNo = #{userNo}")
    int updateUserInfo(UserVo user);
    
    // 비밀번호 변경
    @Update("UPDATE users SET userPw = #{newPassword} WHERE userNo = #{userNo}")
    int updatePassword(@Param("userNo") int userNo, @Param("newPassword") String newPassword);
    
    // 회원 탈퇴
    @Delete("DELETE FROM users WHERE userNo = #{userNo}")
    int deleteUser(@Param("userNo") int userNo);
    
    // 회원번호로 사용자 조회
    @Select("SELECT userNo, userName, userEmail, userPhone FROM users WHERE userNo = #{userNo}")
    UserVo findByUserNo(@Param("userNo") int userNo);
}
