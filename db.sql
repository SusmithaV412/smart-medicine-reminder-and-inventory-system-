-- Create Database
CREATE DATABASE IF NOT EXISTS smart_medicine;
USE smart_medicine;

-- Drop old tables if they exist (for clean setup)
DROP TABLE IF EXISTS reminder;
DROP TABLE IF EXISTS medicine;
DROP TABLE IF EXISTS family_member;

-- Table: family_member
CREATE TABLE family_member (
    member_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    age INT,
    relation VARCHAR(50) -- Father, Mother, Self, Child, etc.
);

-- Table: medicine
CREATE TABLE medicine (
    medicine_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50), -- Tablet, Syrup, Capsule, Injection
    stock INT NOT NULL,
    expiry_date DATE
);

-- Table: reminder
CREATE TABLE reminder (
    reminder_id INT AUTO_INCREMENT PRIMARY KEY,
    member_id INT,
    medicine_id INT,
    dosage VARCHAR(50), -- e.g., "1 Tablet", "5 ml"
    time_of_day TIME,   -- e.g., "08:00:00"
    frequency VARCHAR(50), -- Daily, Weekly
    FOREIGN KEY (member_id) REFERENCES family_member(member_id),
    FOREIGN KEY (medicine_id) REFERENCES medicine(medicine_id)
);

-- Insert Data into family_member
INSERT INTO family_member (name, age, relation) VALUES
('Ramesh', 45, 'Father'),
('Sita', 42, 'Mother'),
('Arjun', 20, 'Son'),
('Priya', 16, 'Daughter');

-- Insert Data into medicine
INSERT INTO medicine (name, type, stock, expiry_date) VALUES
('Paracetamol', 'Tablet', 20, '2025-12-31'),
('Amoxicillin', 'Capsule', 10, '2025-06-15'),
('Cough Syrup', 'Syrup', 2, '2024-11-30'),
('Vitamin C', 'Tablet', 30, '2026-01-10'),
('Insulin', 'Injection', 5, '2024-12-20');

-- Insert Data into reminder
INSERT INTO reminder (member_id, medicine_id, dosage, time_of_day, frequency) VALUES
(1, 1, '1 Tablet', '08:00:00', 'Daily'),
(1, 4, '1 Tablet', '20:00:00', 'Daily'),
(2, 2, '1 Capsule', '09:00:00', 'Daily'),
(2, 3, '10 ml', '22:00:00', 'Daily'),
(3, 4, '1 Tablet', '07:30:00', 'Daily'),
(4, 1, '1 Tablet', '08:30:00', 'If Fever'),
(4, 3, '5 ml', '21:00:00', 'If Cough'),
(1, 5, '5 Units', '07:00:00', 'Daily');
