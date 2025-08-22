package com.example.demo.survey.dto.response;

import com.example.demo.survey.entity.QuestionOption;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuestionOptionResultResponseDto extends QuestionOptionResponseDto{

    private Integer answersCount;

    public QuestionOptionResultResponseDto(QuestionOption option) {
        super(option);
        this.answersCount = 0;
    }
}
