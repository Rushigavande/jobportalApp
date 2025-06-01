package com.rushikeshgavande10.jobportal.controller;

import com.rushikeshgavande10.jobportal.entity.Users;
import com.rushikeshgavande10.jobportal.services.EmailService;
import com.rushikeshgavande10.jobportal.services.OTPService;
import com.rushikeshgavande10.jobportal.services.UsersService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class PasswordResetController {
    
    private static final Logger logger = LoggerFactory.getLogger(PasswordResetController.class);

    private final UsersService usersService;
    private final OTPService otpService;
    private final EmailService emailService;

    @Autowired
    public PasswordResetController(UsersService usersService,
                                 OTPService otpService,
                                 EmailService emailService) {
        this.usersService = usersService;
        this.otpService = otpService;
        this.emailService = emailService;
    }

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String email, 
                                      RedirectAttributes redirectAttributes,
                                      HttpSession session,
                                      Model model) {
        logger.info("Processing forgot password request for email: {}", email);
        Optional<Users> user = usersService.getUserByEmail(email);
        
        if (!user.isPresent()) {
            logger.warn("No user found with email: {}", email);
            model.addAttribute("error", "No account found with that email address");
            return "forgot-password";
        }

        try {
            String otp = otpService.generateOTP(user.get());
            emailService.sendOTPEmail(email, otp);
            
            // Set both session and redirect attributes
            session.setAttribute("resetEmail", email);
            redirectAttributes.addFlashAttribute("email", email);
            redirectAttributes.addFlashAttribute("otpVerified", false);
            
            logger.info("OTP sent successfully to: {}. Redirecting to verify-otp page.", email);
            return "redirect:/verify-otp";
            
        } catch (Exception e) {
            logger.error("Error sending OTP email", e);
            model.addAttribute("error", "Error sending OTP email. Please try again later.");
            return "forgot-password";
        }
    }

    @GetMapping("/verify-otp")
    public String showVerifyOTPForm(HttpSession session, Model model) {
        String email = (String) session.getAttribute("resetEmail");
        logger.info("Showing verify OTP form. Email in session: {}", email);
        
        if (email == null) {
            logger.warn("No email found in session. Redirecting to forgot-password");
            return "redirect:/forgot-password";
        }
        
        // If email is not in the model (from a flash attribute), add it
        if (!model.containsAttribute("email")) {
            model.addAttribute("email", email);
        }
        
        // Check if OTP is already verified
        String validatedOtp = (String) session.getAttribute("validatedOtp");
        model.addAttribute("otpVerified", validatedOtp != null);
        if (validatedOtp != null) {
            model.addAttribute("validatedOtp", validatedOtp);
        }
        
        logger.info("Rendering verify-otp page. OTP verified: {}", validatedOtp != null);
        return "verify-otp";
    }

    @PostMapping("/verify-otp")
    public String verifyOTP(@RequestParam("otp") String otp,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        String email = (String) session.getAttribute("resetEmail");
        logger.info("Verifying OTP for email: {}", email);
        
        if (email == null) {
            logger.warn("No email in session. Redirecting to forgot-password");
            return "redirect:/forgot-password";
        }

        Optional<Users> user = otpService.validateOTP(otp);
        
        if (!user.isPresent()) {
            logger.warn("Invalid or expired OTP: {}", otp);
            redirectAttributes.addFlashAttribute("error", "Invalid or expired OTP");
            redirectAttributes.addFlashAttribute("email", email);
            redirectAttributes.addFlashAttribute("otpVerified", false);
            return "redirect:/verify-otp";
        }

        // Store validated OTP and set attributes
        session.setAttribute("validatedOtp", otp);
        redirectAttributes.addFlashAttribute("email", email);
        redirectAttributes.addFlashAttribute("otpVerified", true);
        redirectAttributes.addFlashAttribute("validatedOtp", otp);
        
        logger.info("OTP verified successfully for: {}. Showing password reset form.", email);
        return "redirect:/verify-otp";
    }

    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam("password") String password,
                                     @RequestParam("confirmPassword") String confirmPassword,
                                     @RequestParam("otp") String otp,
                                     HttpSession session,
                                     RedirectAttributes redirectAttributes) {
        String email = (String) session.getAttribute("resetEmail");
        logger.info("Processing password reset for email: {}", email);
        
        if (email == null) {
            logger.warn("No email in session. Redirecting to forgot-password");
            return "redirect:/forgot-password";
        }

        Optional<Users> user = otpService.validateOTP(otp);
        if (!user.isPresent()) {
            logger.warn("Invalid or expired OTP during password reset");
            redirectAttributes.addFlashAttribute("error", "OTP has expired. Please start over.");
            return "redirect:/forgot-password";
        }

        if (!password.equals(confirmPassword)) {
            logger.warn("Passwords do not match during reset");
            redirectAttributes.addFlashAttribute("error", "Passwords do not match");
            redirectAttributes.addFlashAttribute("otpVerified", true);
            redirectAttributes.addFlashAttribute("validatedOtp", otp);
            return "redirect:/verify-otp";
        }

        try {
            otpService.changeUserPassword(user.get(), password);
            logger.info("Password reset successful for user: {}", email);
            
            // Clear all session attributes
            session.removeAttribute("validatedOtp");
            session.removeAttribute("resetEmail");
            
            redirectAttributes.addFlashAttribute("success", "Password has been reset successfully. Please login with your new password.");
            return "redirect:/login";
        } catch (Exception e) {
            logger.error("Error resetting password", e);
            redirectAttributes.addFlashAttribute("error", "Error resetting password. Please try again.");
            redirectAttributes.addFlashAttribute("otpVerified", true);
            redirectAttributes.addFlashAttribute("validatedOtp", otp);
            return "redirect:/verify-otp";
        }
    }
} 