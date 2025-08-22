package com.example.demo.survey.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter @Setter
public class QuestionRequestDto {

    private UUID id;

    @NotBlank(message = "질문을 입력하세요")
    private String text;

    private String type;
    private List<String> options;
    private boolean required;
    private Integer order;
}
