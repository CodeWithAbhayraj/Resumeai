package com.example.resumeai.dto;

import lombok.Data;

import java.util.List;

@Data
public class JDResponseDTO {

    private String jobTitle;

    private List<String> requiredSkills;

    private List<String> preferredSkills;

    private String experienceRequired;

    private String education;

    private List<String> responsibilities;
}