package com.rushikeshgavande10.jobportal.services;

import com.rushikeshgavande10.jobportal.entity.JobPostActivity;
import com.rushikeshgavande10.jobportal.entity.JobSeekerProfile;
import com.rushikeshgavande10.jobportal.entity.JobSeekerSave;
import com.rushikeshgavande10.jobportal.repository.JobSeekerSaveRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobSeekerSaveService {

    private final JobSeekerSaveRepository jobSeekerSaveRepository;

    public JobSeekerSaveService(JobSeekerSaveRepository jobSeekerSaveRepository) {
        this.jobSeekerSaveRepository = jobSeekerSaveRepository;
    }

    public List<JobSeekerSave> getCandidatesJob(JobSeekerProfile userAccountId) {
        return jobSeekerSaveRepository.findByUserId(userAccountId);
    }

    public List<JobSeekerSave> getJobCandidates(JobPostActivity job) {
        return jobSeekerSaveRepository.findByJob(job);
    }

    public void addNew(JobSeekerSave jobSeekerSave) {
        jobSeekerSaveRepository.save(jobSeekerSave);
    }

    public void deleteAllByJob(JobPostActivity job) {
        List<JobSeekerSave> savedJobs = jobSeekerSaveRepository.findByJob(job);
        jobSeekerSaveRepository.deleteAll(savedJobs);
    }
}
