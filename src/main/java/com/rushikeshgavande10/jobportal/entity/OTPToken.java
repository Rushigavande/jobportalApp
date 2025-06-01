package com.rushikeshgavande10.jobportal.entity;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "otptoken", indexes = {
    @Index(name = "idx_otp", columnList = "otp"),
    @Index(name = "idx_user", columnList = "user_id")
})
public class OTPToken {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String otp;

    @OneToOne(targetEntity = Users.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private Users user;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date expiryDate;

    public OTPToken() {
    }

    public OTPToken(String otp, Users user, Date expiryDate) {
        this.otp = otp;
        this.user = user;
        this.expiryDate = expiryDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }
} 