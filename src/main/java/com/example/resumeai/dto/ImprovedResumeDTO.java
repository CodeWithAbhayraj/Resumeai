package com.example.resumeai.dto;

import lombok.Data;

import java.util.List;

@Data
public class ImprovedResumeDTO {

    // Personal Information
    private String fullName;

    private String email;

    private String github;

    private String linkedin;

    // Professional Summary
    private String professionalSummary;


    // Skills
    private SkillsDTO skills;

    // Projects
    private List<ProjectDTO> projects;

    // Education
    private List<EducationDTO> education;

    // Experience
    private List<ExperienceDTO> experience;

    // Certifications
    private List<String> certifications;
}