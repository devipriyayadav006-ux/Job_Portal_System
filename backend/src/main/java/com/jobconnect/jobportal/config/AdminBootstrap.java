package com.jobconnect.jobportal.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminBootstrap implements CommandLineRunner {
    private final JdbcTemplate jdbc;
    private final PasswordEncoder encoder;
    @Value("${jobportal.admin.email}") private String adminEmail;
    @Value("${jobportal.admin.password}") private String adminPassword;

    public AdminBootstrap(JdbcTemplate jdbc, PasswordEncoder encoder) {
        this.jdbc = jdbc;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        Integer count = jdbc.queryForObject(
            "SELECT COUNT(*) FROM users WHERE role = 'ADMIN'", Integer.class);
        if (count != null && count == 0) {
            jdbc.update("INSERT INTO users(full_name,email,password_hash,role) VALUES(?,?,?,'ADMIN')",
                "JobConnect Admin", adminEmail.toLowerCase(), encoder.encode(adminPassword));
            System.out.println("Demo admin created. Change ADMIN_EMAIL and ADMIN_PASSWORD before deployment.");
        }
    }
}
