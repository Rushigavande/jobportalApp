package com.rushikeshgavande10.jobportal.services;

import com.rushikeshgavande10.jobportal.entity.JobPostActivity;
import com.rushikeshgavande10.jobportal.entity.JobSeekerApply;
import com.rushikeshgavande10.jobportal.entity.JobSeekerProfile;
import com.rushikeshgavande10.jobportal.repository.JobSeekerApplyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobSeekerApplyService {

    private final JobSeekerApplyRepository jobSeekerApplyRepository;

    @Autowired
    public JobSeekerApplyService(JobSeekerApplyRepository jobSeekerApplyRepository) {
        this.jobSeekerApplyRepository = jobSeekerApplyRepository;
    }

    public List<JobSeekerApply> getCandidatesJobs(JobSeekerProfile userAccountId) {
        return jobSeekerApplyRepository.findByUserId(userAccountId);
    }

    public List<JobSeekerApply> getJobCandidates(JobPostActivity job) {
        return jobSeekerApplyRepository.findByJob(job);
    }

    public void addNew(JobSeekerApply jobSeekerApply) {
        jobSeekerApplyRepository.save(jobSeekerApply);
    }

    public void deleteAllByJob(JobPostActivity job) {
        List<JobSeekerApply> applications = jobSeekerApplyRepository.findByJob(job);
        jobSeekerApplyRepository.deleteAll(applications);
    }
}
