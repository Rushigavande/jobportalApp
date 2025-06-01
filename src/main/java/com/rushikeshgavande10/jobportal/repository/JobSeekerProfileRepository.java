package com.rushikeshgavande10.jobportal.repository;

import com.rushikeshgavande10.jobportal.entity.JobSeekerProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobSeekerProfileRepository extends JpaRepository<JobSeekerProfile, Integer> {
}
