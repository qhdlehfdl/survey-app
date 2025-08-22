package com.example.demo.survey.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SurveyRegisterRequestDto {

    @NotBlank(message = "설문지 제목을 입력하세요.")
    private String title;

    @NotEmpty(message = "질문을 입력하세요.")
    private List<QuestionRequestDto> questions;
}
