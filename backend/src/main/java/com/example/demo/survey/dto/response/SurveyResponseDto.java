package com.example.demo.survey.dto.response;

import com.example.demo.survey.entity.Survey;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SurveyResponseDto extends SurveyBasicResponseDto{

    private List<QuestionResponseDto> questions;

    public SurveyResponseDto(Survey survey, List<QuestionResponseDto> questions){
        super(survey);
        this.questions = questions;
    }
}
