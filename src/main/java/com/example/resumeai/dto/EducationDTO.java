package com.example.resumeai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EducationDTO {

    private String institution;

    private String degree;

    private String major;

    private String duration;

    private String startDate;

    private String endDate;

}