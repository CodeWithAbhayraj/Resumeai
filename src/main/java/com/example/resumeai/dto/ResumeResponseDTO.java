package com.example.resumeai.dto;

import lombok.Data;

import java.util.List;

@Data
public class ResumeResponseDTO {

    private String fullName;

    private String email;

    private String phoneNumber;

    private SkillsDTO skills;

    private List<ProjectDTO> projects;

    private String professionalSummary;

    private List<EducationDTO> education;

    private List<String> experience;

    private List<String> certifications;

    private String github;

    private String linkedin;

}