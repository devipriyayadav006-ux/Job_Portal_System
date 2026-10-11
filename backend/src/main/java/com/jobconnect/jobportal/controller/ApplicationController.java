package com.jobconnect.jobportal.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/applications")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"}, allowCredentials = "true")
public class ApplicationController {
    private final JdbcTemplate jdbc;
    public ApplicationController(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @PostMapping
    public ResponseEntity<?> apply(@RequestBody Map<String,String> body, HttpSession session) {
        if (session.getAttribute("userId") == null) return unauthorized();
        if (!"USER".equals(session.getAttribute("role"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Only job-seeker accounts can apply."));
        }
        long jobId;
        try { jobId = Long.parseLong(body.getOrDefault("jobId", "")); }
        catch (NumberFormatException e) { return ResponseEntity.badRequest().body(Map.of("message", "Invalid job ID.")); }

        Integer jobCount = jdbc.queryForObject("SELECT COUNT(*) FROM jobs WHERE id=?", Integer.class, jobId);
        if (jobCount == null || jobCount == 0) return ResponseEntity.badRequest().body(Map.of("message", "This job no longer exists."));
        long userId = ((Number) session.getAttribute("userId")).longValue();
        Integer already = jdbc.queryForObject("SELECT COUNT(*) FROM applications WHERE user_id=? AND job_id=?", Integer.class, userId, jobId);
        if (already != null && already > 0) return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "You have already applied for this job."));
        jdbc.update("INSERT INTO applications(job_id,user_id,cover_letter,status) VALUES(?,?,?,'APPLIED')",
            jobId, userId, body.getOrDefault("coverLetter", ""));
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Application submitted successfully."));
    }

    @GetMapping("/mine")
    public ResponseEntity<?> mine(HttpSession session) {
        if (session.getAttribute("userId") == null) return unauthorized();
        if (!"USER".equals(session.getAttribute("role"))) return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Job-seeker login required."));
        long userId = ((Number) session.getAttribute("userId")).longValue();
        List<Map<String,Object>> rows = jdbc.query("""
            SELECT a.id, a.job_id AS jobId, j.title, j.company, j.location,
                   a.status, a.cover_letter AS coverLetter, a.interview_at AS interviewAt,
                   a.interview_notes AS interviewNotes, a.applied_at AS appliedAt
            FROM applications a JOIN jobs j ON j.id=a.job_id
            WHERE a.user_id=? ORDER BY a.applied_at DESC
            """, (rs,n) -> {
                var m = new java.util.LinkedHashMap<String,Object>();
                m.put("id", rs.getLong("id")); m.put("jobId", rs.getLong("jobId"));
                m.put("title", rs.getString("title")); m.put("company", rs.getString("company"));
                m.put("location", rs.getString("location")); m.put("status", rs.getString("status"));
                m.put("coverLetter", rs.getString("coverLetter")); m.put("interviewAt", rs.getTimestamp("interviewAt"));
                m.put("interviewNotes", rs.getString("interviewNotes")); m.put("appliedAt", rs.getTimestamp("appliedAt"));
                return m;
            }, userId);
        return ResponseEntity.ok(rows);
    }

    @GetMapping
    public ResponseEntity<?> all(HttpSession session) {
        if (!"ADMIN".equals(session.getAttribute("role"))) return forbidden();
        List<Map<String,Object>> rows = jdbc.query("""
            SELECT a.id, a.job_id AS jobId, j.title, j.company, j.location,
                   u.id AS userId, u.full_name AS applicantName, u.email AS applicantEmail,
                   a.status, a.cover_letter AS coverLetter, a.interview_at AS interviewAt,
                   a.interview_notes AS interviewNotes, a.applied_at AS appliedAt
            FROM applications a JOIN jobs j ON j.id=a.job_id
            JOIN users u ON u.id=a.user_id
            ORDER BY a.applied_at DESC
            """, (rs,n) -> {
                var m = new java.util.LinkedHashMap<String,Object>();
                m.put("id", rs.getLong("id")); m.put("jobId", rs.getLong("jobId"));
                m.put("title", rs.getString("title")); m.put("company", rs.getString("company"));
                m.put("location", rs.getString("location")); m.put("userId", rs.getLong("userId"));
                m.put("applicantName", rs.getString("applicantName")); m.put("applicantEmail", rs.getString("applicantEmail"));
                m.put("status", rs.getString("status")); m.put("coverLetter", rs.getString("coverLetter"));
                m.put("interviewAt", rs.getTimestamp("interviewAt")); m.put("interviewNotes", rs.getString("interviewNotes"));
                m.put("appliedAt", rs.getTimestamp("appliedAt"));
                return m;
            });
        return ResponseEntity.ok(rows);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable long id, @RequestBody Map<String,String> body, HttpSession session) {
        if (!"ADMIN".equals(session.getAttribute("role"))) return forbidden();
        String status = body.getOrDefault("status", "");
        List<String> allowed = List.of("SHORTLISTED", "INTERVIEW_SCHEDULED", "REJECTED", "HIRED", "APPLIED");
        if (!allowed.contains(status)) return ResponseEntity.badRequest().body(Map.of("message", "Invalid application status."));
        String interviewText = body.get("interviewAt");
        Timestamp interviewAt = null;
        if (interviewText != null && !interviewText.isBlank()) {
            try { interviewAt = Timestamp.valueOf(LocalDateTime.parse(interviewText)); }
            catch (Exception e) { return ResponseEntity.badRequest().body(Map.of("message", "Interview date must be a valid local date/time.")); }
        }
        if ("INTERVIEW_SCHEDULED".equals(status) && interviewAt == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Set an interview date and time before scheduling."));
        }
        int count = jdbc.update("UPDATE applications SET status=?, interview_at=?, interview_notes=? WHERE id=?",
            status, interviewAt, body.getOrDefault("interviewNotes", ""), id);
        if (count == 0) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(Map.of("message", "Application updated."));
    }

    private ResponseEntity<Map<String,String>> unauthorized() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Please log in first."));
    }
    private ResponseEntity<Map<String,String>> forbidden() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Admin login is required."));
    }
}
