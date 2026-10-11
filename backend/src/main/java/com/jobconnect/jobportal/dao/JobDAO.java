package com.jobconnect.jobportal.dao;

import com.jobconnect.jobportal.model.Job;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class JobDAO {
    private final JdbcTemplate jdbc;

    public JobDAO(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Job> mapper = (rs, rowNum) -> {
        Job job = new Job();
        job.setId(rs.getLong("id"));
        job.setTitle(rs.getString("title"));
        job.setCompany(rs.getString("company"));
        job.setLocation(rs.getString("location"));
        job.setJobType(rs.getString("job_type"));
        job.setDescription(rs.getString("description"));
        job.setSalaryMin(rs.getBigDecimal("salary_min"));
        job.setSalaryMax(rs.getBigDecimal("salary_max"));
        return job;
    };

    public List<Job> findAll() {
        return jdbc.query("SELECT * FROM jobs ORDER BY created_at DESC, id DESC", mapper);
    }

    public Optional<Job> findById(long id) {
        return jdbc.query("SELECT * FROM jobs WHERE id = ?", mapper, id)
                .stream().findFirst();
    }

    public Job create(Job job) {
        String sql = "INSERT INTO jobs (title, company, location, job_type, description, salary_min, salary_max) VALUES (?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            var statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, job.getTitle());
            statement.setString(2, job.getCompany());
            statement.setString(3, job.getLocation());
            statement.setString(4, job.getJobType());
            statement.setString(5, job.getDescription());
            statement.setBigDecimal(6, job.getSalaryMin());
            statement.setBigDecimal(7, job.getSalaryMax());
            return statement;
        }, keyHolder);
        if (keyHolder.getKey() != null) {
            job.setId(keyHolder.getKey().longValue());
        }
        return job;
    }

    public boolean update(long id, Job job) {
        int updated = jdbc.update(
            "UPDATE jobs SET title = ?, company = ?, location = ?, job_type = ?, description = ?, salary_min = ?, salary_max = ? WHERE id = ?",
            job.getTitle(), job.getCompany(), job.getLocation(), job.getJobType(),
            job.getDescription(), job.getSalaryMin(), job.getSalaryMax(), id);
        return updated > 0;
    }

    public boolean delete(long id) {
        return jdbc.update("DELETE FROM jobs WHERE id = ?", id) > 0;
    }
}
