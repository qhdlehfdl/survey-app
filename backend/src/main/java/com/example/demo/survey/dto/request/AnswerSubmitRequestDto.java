package com.example.demo.survey.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AnswerSubmitRequestDto {

    @NotEmpty(message = "답변이 있어야합니다.")
    private List<AnswerRequestDto>answers;
}
