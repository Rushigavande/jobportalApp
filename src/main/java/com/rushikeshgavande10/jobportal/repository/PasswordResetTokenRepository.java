package com.rushikeshgavande10.jobportal.repository;

import com.rushikeshgavande10.jobportal.entity.PasswordResetToken;
import com.rushikeshgavande10.jobportal.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    PasswordResetToken findByToken(String token);
    PasswordResetToken findByUser(Users user);
} 