package com.rushikeshgavande10.jobportal.repository;

import com.rushikeshgavande10.jobportal.entity.OTPToken;
import com.rushikeshgavande10.jobportal.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OTPTokenRepository extends JpaRepository<OTPToken, Long> {
    
    @Query("SELECT o FROM OTPToken o WHERE o.otp = :otp")
    OTPToken findByOtp(@Param("otp") String otp);
    
    @Query("SELECT o FROM OTPToken o WHERE o.user = :user")
    OTPToken findByUser(@Param("user") Users user);
} 