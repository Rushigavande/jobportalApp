package com.rushikeshgavande10.jobportal.services;

import com.rushikeshgavande10.jobportal.entity.PasswordResetToken;
import com.rushikeshgavande10.jobportal.entity.Users;
import com.rushikeshgavande10.jobportal.repository.PasswordResetTokenRepository;
import com.rushikeshgavande10.jobportal.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetService {

    private final UsersRepository usersRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private static final int EXPIRATION = 60 * 24; // 24 hours

    @Autowired
    public PasswordResetService(UsersRepository usersRepository,
                              PasswordResetTokenRepository tokenRepository,
                              PasswordEncoder passwordEncoder) {
        this.usersRepository = usersRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public String createPasswordResetTokenForUser(Users user) {
        String token = UUID.randomUUID().toString();
        PasswordResetToken existingToken = tokenRepository.findByUser(user);
        
        if (existingToken != null) {
            existingToken.setToken(token);
            existingToken.setExpiryDate(calculateExpiryDate());
            tokenRepository.save(existingToken);
            return token;
        }

        PasswordResetToken myToken = new PasswordResetToken(token, user, calculateExpiryDate());
        tokenRepository.save(myToken);
        return token;
    }

    private Date calculateExpiryDate() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.MINUTE, EXPIRATION);
        return cal.getTime();
    }

    public Optional<Users> validatePasswordResetToken(String token) {
        final PasswordResetToken passToken = tokenRepository.findByToken(token);

        if (passToken == null) {
            return Optional.empty();
        }

        Calendar cal = Calendar.getInstance();
        if ((passToken.getExpiryDate().getTime() - cal.getTime().getTime()) <= 0) {
            tokenRepository.delete(passToken);
            return Optional.empty();
        }

        return Optional.of(passToken.getUser());
    }

    public void changeUserPassword(Users user, String password) {
        user.setPassword(passwordEncoder.encode(password));
        usersRepository.save(user);
        
        // Delete the used token
        PasswordResetToken token = tokenRepository.findByUser(user);
        if (token != null) {
            tokenRepository.delete(token);
        }
    }
} 