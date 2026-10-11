package com.jobconnect.jobportal.controller;

import com.jobconnect.jobportal.model.Job;
import com.jobconnect.jobportal.service.JobService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/jobs")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"}, allowCredentials = "true")
public class JobController {
    private final JobService service;
    public JobController(JobService service) { this.service = service; }

    @GetMapping public List<Job> getAll() { return service.getAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<Job> getById(@PathVariable long id) {
        return service.getById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Job job, HttpSession session) {
        if (!isAdmin(session)) return forbidden();
        if (!isValid(job)) return ResponseEntity.badRequest().body(Map.of("message", "Required fields or salary values are invalid."));
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(job));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable long id, @RequestBody Job job, HttpSession session) {
        if (!isAdmin(session)) return forbidden();
        if (!isValid(job)) return ResponseEntity.badRequest().body(Map.of("message", "Required fields or salary values are invalid."));
        if (!service.update(id, job)) return ResponseEntity.notFound().build();
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable long id, HttpSession session) {
        if (!isAdmin(session)) return forbidden();
        if (!service.delete(id)) return ResponseEntity.notFound().build();
        return ResponseEntity.noContent().build();
    }

    private boolean isAdmin(HttpSession s) { return "ADMIN".equals(s.getAttribute("role")); }
    private ResponseEntity<Map<String,String>> forbidden() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Admin login is required for this action."));
    }
    private boolean isValid(Job j) {
        return j != null && text(j.getTitle()) && text(j.getCompany()) && text(j.getLocation())
            && text(j.getJobType()) && text(j.getDescription())
            && (j.getSalaryMin() == null || j.getSalaryMin().signum() >= 0)
            && (j.getSalaryMax() == null || j.getSalaryMax().signum() >= 0)
            && (j.getSalaryMin() == null || j.getSalaryMax() == null || j.getSalaryMin().compareTo(j.getSalaryMax()) <= 0);
    }
    private boolean text(String s) { return s != null && !s.isBlank(); }
}
