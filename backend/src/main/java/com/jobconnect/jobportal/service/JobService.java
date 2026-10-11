package com.jobconnect.jobportal.service;

import com.jobconnect.jobportal.dao.JobDAO;
import com.jobconnect.jobportal.model.Job;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class JobService {
    private final JobDAO dao;
    public JobService(JobDAO dao) { this.dao = dao; }
    public List<Job> getAll() { return dao.findAll(); }
    public Optional<Job> getById(long id) { return dao.findById(id); }
    public Job create(Job job) { return dao.create(job); }
    public boolean update(long id, Job job) { return dao.update(id, job); }
    public boolean delete(long id) { return dao.delete(id); }
}
