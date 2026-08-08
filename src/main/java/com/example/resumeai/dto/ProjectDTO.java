package com.example.resumeai.dto;

import lombok.Data;

import java.util.List;

@Data
public class ProjectDTO {

    private String title;

    private List<String> technologies;

    private List<String> highlights;

}