package com.example.resumeai.dto;

import lombok.Data;

import java.util.List;

@Data
public class SkillsDTO {

    private List<String> languages;
    private List<String> frameworks;
    private List<String> databases;
    private List<String> devopsAndTools;
    private List<String> concepts;

}