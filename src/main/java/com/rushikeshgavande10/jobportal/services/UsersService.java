package com.rushikeshgavande10.jobportal.services;

import com.rushikeshgavande10.jobportal.entity.JobSeekerProfile;
import com.rushikeshgavande10.jobportal.entity.RecruiterProfile;
import com.rushikeshgavande10.jobportal.entity.Users;
import com.rushikeshgavande10.jobportal.repository.JobSeekerProfileRepository;
import com.rushikeshgavande10.jobportal.repository.RecruiterProfileRepository;
import com.rushikeshgavande10.jobportal.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
public class UsersService {

    private final UsersRepository usersRepository;
    private final JobSeekerProfileRepository jobSeekerProfileRepository;
    private final RecruiterProfileRepository recruiterProfileRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UsersService(UsersRepository usersRepository, JobSeekerProfileRepository jobSeekerProfileRepository, RecruiterProfileRepository recruiterProfileRepository, PasswordEncoder passwordEncoder) {
        this.usersRepository = usersRepository;
        this.jobSeekerProfileRepository = jobSeekerProfileRepository;
        this.recruiterProfileRepository = recruiterProfileRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Users addNew(Users users) {
        users.setActive(true);
        users.setRegistrationDate(new Date(System.currentTimeMillis()));
        users.setPassword(passwordEncoder.encode(users.getPassword()));
        Users savedUser = usersRepository.save(users);
        int userTypeId = users.getUserTypeId().getUserTypeId();

        if (userTypeId == 1) {
            recruiterProfileRepository.save(new RecruiterProfile(savedUser));
        }
        else {
            jobSeekerProfileRepository.save(new JobSeekerProfile(savedUser));
        }

        return savedUser;
    }

    public Object getCurrentUserProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String username = authentication.getName();
            Users users = usersRepository.findByEmail(username).orElseThrow(()-> new UsernameNotFoundException("Could not found user"));
            int userId = users.getUserId();
            if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("Recruiter"))) {
                Optional<RecruiterProfile> recruiterProfile = recruiterProfileRepository.findById(userId);
                if (!recruiterProfile.isPresent()) {
                    RecruiterProfile newProfile = new RecruiterProfile(users);
                    return recruiterProfileRepository.save(newProfile);
                }
                return recruiterProfile.get();
            } else {
                Optional<JobSeekerProfile> jobSeekerProfile = jobSeekerProfileRepository.findById(userId);
                if (!jobSeekerProfile.isPresent()) {
                    JobSeekerProfile newProfile = new JobSeekerProfile(users);
                    return jobSeekerProfileRepository.save(newProfile);
                }
                return jobSeekerProfile.get();
            }
        }
        return null;
    }

    public Users getCurrentUser() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String username = authentication.getName();
            Users user = usersRepository.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException("Could not found " + "user"));
            return user;
        }

        return null;
    }

    public Users findByEmail(String currentUsername) {
        return usersRepository.findByEmail(currentUsername).orElseThrow(() -> new UsernameNotFoundException("User not " +
                "found"));
    }

    public Optional<Users> getUserByEmail(String email) {
        return usersRepository.findByEmail(email);
    }
    
}








