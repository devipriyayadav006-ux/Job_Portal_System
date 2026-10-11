CREATE DATABASE IF NOT EXISTS job_portal;
USE job_portal;

CREATE TABLE IF NOT EXISTS users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  full_name VARCHAR(120) NOT NULL,
  email VARCHAR(190) NOT NULL UNIQUE,
  password_hash VARCHAR(100) NOT NULL,
  role ENUM('ADMIN','USER') NOT NULL DEFAULT 'USER',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS jobs (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(150) NOT NULL,
  company VARCHAR(150) NOT NULL,
  location VARCHAR(150) NOT NULL,
  job_type VARCHAR(50) NOT NULL,
  description TEXT NOT NULL,
  salary_min DECIMAL(12,2) NULL,
  salary_max DECIMAL(12,2) NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS applications (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  job_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  cover_letter TEXT NULL,
  status ENUM('APPLIED','SHORTLISTED','INTERVIEW_SCHEDULED','REJECTED','HIRED')
    NOT NULL DEFAULT 'APPLIED',
  interview_at DATETIME NULL,
  interview_notes VARCHAR(1000) NULL,
  applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_application_job FOREIGN KEY (job_id) REFERENCES jobs(id),
  CONSTRAINT fk_application_user FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT uq_user_job UNIQUE (user_id, job_id)
);

INSERT INTO jobs (title, company, location, job_type, description, salary_min, salary_max)
SELECT 'Java Developer', 'ABC Technologies', 'Hyderabad', 'Full-time',
       'Java, Spring Boot, REST APIs and MySQL.', 400000, 700000
WHERE NOT EXISTS (SELECT 1 FROM jobs WHERE title='Java Developer' AND company='ABC Technologies');

INSERT INTO jobs (title, company, location, job_type, description, salary_min, salary_max)
SELECT 'React Developer', 'XYZ Solutions', 'Bengaluru', 'Full-time',
       'React, JavaScript, HTML and CSS.', 500000, 900000
WHERE NOT EXISTS (SELECT 1 FROM jobs WHERE title='React Developer' AND company='XYZ Solutions');

INSERT INTO jobs (title, company, location, job_type, description, salary_min, salary_max)
SELECT 'SQL Developer Intern', 'Data Systems', 'Pune', 'Internship',
       'SQL queries and database development.', 200000, 350000
WHERE NOT EXISTS (SELECT 1 FROM jobs WHERE title='SQL Developer Intern' AND company='Data Systems');
