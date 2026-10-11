package com.jobconnect.jobportal.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"}, allowCredentials = "true")
public class AuthController {
    private final JdbcTemplate jdbc;
    private final PasswordEncoder encoder;
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    public AuthController(JdbcTemplate jdbc, PasswordEncoder encoder) {
        this.jdbc = jdbc;
        this.encoder = encoder;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        String name = clean(body.get("fullName"));
        String email = clean(body.get("email")).toLowerCase();
        String password = body.getOrDefault("password", "");
        if (name.isBlank() || !EMAIL.matcher(email).matches() || password.length() < 8) {
            return ResponseEntity.badRequest().body(Map.of("message", "Enter a name, valid email, and password with at least 8 characters."));
        }
        Integer exists = jdbc.queryForObject("SELECT COUNT(*) FROM users WHERE email=?", Integer.class, email);
        if (exists != null && exists > 0) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "An account with this email already exists."));
        }
        jdbc.update("INSERT INTO users(full_name,email,password_hash,role) VALUES(?,?,?,'USER')",
            name, email, encoder.encode(password));
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Registration successful. Please log in."));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body, HttpSession session) {
        String email = clean(body.get("email")).toLowerCase();
        String password = body.getOrDefault("password", "");
        var rows = jdbc.query("SELECT id, full_name, email, password_hash, role FROM users WHERE email=?",
            (rs, n) -> Map.<String,Object>of(
                "id", rs.getLong("id"), "fullName", rs.getString("full_name"),
                "email", rs.getString("email"), "passwordHash", rs.getString("password_hash"),
                "role", rs.getString("role")), email);
        if (rows.isEmpty() || !encoder.matches(password, (String) rows.get(0).get("passwordHash"))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Invalid email or password."));
        }
        Map<String,Object> user = rows.get(0);
        session.setAttribute("userId", user.get("id"));
        session.setAttribute("role", user.get("role"));
        session.setAttribute("fullName", user.get("fullName"));
        session.setAttribute("email", user.get("email"));
        return ResponseEntity.ok(publicUser(session));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(HttpSession session) {
        if (session.getAttribute("userId") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Not logged in."));
        }
        return ResponseEntity.ok(publicUser(session));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(Map.of("message", "Logged out."));
    }

    private Map<String,Object> publicUser(HttpSession session) {
        return Map.of("id", session.getAttribute("userId"), "fullName", session.getAttribute("fullName"),
            "email", session.getAttribute("email"), "role", session.getAttribute("role"));
    }

    private String clean(String value) { return value == null ? "" : value.trim(); }
}
