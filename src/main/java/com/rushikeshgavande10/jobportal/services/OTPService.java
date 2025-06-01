package com.rushikeshgavande10.jobportal.services;

import com.rushikeshgavande10.jobportal.entity.OTPToken;
import com.rushikeshgavande10.jobportal.entity.Users;
import com.rushikeshgavande10.jobportal.repository.OTPTokenRepository;
import com.rushikeshgavande10.jobportal.repository.UsersRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import java.util.Random;

@Service
public class OTPService {
    
    private static final Logger logger = LoggerFactory.getLogger(OTPService.class);
    private final UsersRepository usersRepository;
    private final OTPTokenRepository otpTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private static final int EXPIRATION = 10; // 10 minutes

    @Autowired
    public OTPService(UsersRepository usersRepository,
                     OTPTokenRepository otpTokenRepository,
                     PasswordEncoder passwordEncoder) {
        this.usersRepository = usersRepository;
        this.otpTokenRepository = otpTokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public String generateOTP(Users user) {
        String otp = String.format("%06d", new Random().nextInt(999999));
        logger.info("Generating OTP for user: {}", user.getEmail());
        
        OTPToken existingToken = otpTokenRepository.findByUser(user);
        
        if (existingToken != null) {
            logger.info("Updating existing OTP token for user: {}", user.getEmail());
            existingToken.setOtp(otp);
            existingToken.setExpiryDate(calculateExpiryDate());
            otpTokenRepository.save(existingToken);
            logger.info("Updated OTP: {} for user: {}", otp, user.getEmail());
            return otp;
        }

        logger.info("Creating new OTP token for user: {}", user.getEmail());
        OTPToken otpToken = new OTPToken(otp, user, calculateExpiryDate());
        otpTokenRepository.save(otpToken);
        logger.info("Created new OTP: {} for user: {}", otp, user.getEmail());
        return otp;
    }

    private Date calculateExpiryDate() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.MINUTE, EXPIRATION);
        return cal.getTime();
    }

    @Transactional(readOnly = true)
    public Optional<Users> validateOTP(String otp) {
        logger.info("Validating OTP: {}", otp);
        if (otp == null || otp.trim().isEmpty()) {
            logger.warn("OTP is null or empty");
            return Optional.empty();
        }

        final OTPToken otpToken = otpTokenRepository.findByOtp(otp.trim());

        if (otpToken == null) {
            logger.warn("No OTP token found for: {}", otp);
            return Optional.empty();
        }

        Calendar cal = Calendar.getInstance();
        if ((otpToken.getExpiryDate().getTime() - cal.getTime().getTime()) <= 0) {
            logger.warn("OTP expired for user: {}", otpToken.getUser().getEmail());
            otpTokenRepository.delete(otpToken);
            return Optional.empty();
        }

        logger.info("OTP validated successfully for user: {}", otpToken.getUser().getEmail());
        return Optional.of(otpToken.getUser());
    }

    @Transactional
    public void changeUserPassword(Users user, String password) {
        logger.info("Changing password for user: {}", user.getEmail());
        user.setPassword(passwordEncoder.encode(password));
        usersRepository.save(user);
        
        // Delete the used OTP
        OTPToken token = otpTokenRepository.findByUser(user);
        if (token != null) {
            logger.info("Deleting used OTP token for user: {}", user.getEmail());
            otpTokenRepository.delete(token);
        }
    }
} 