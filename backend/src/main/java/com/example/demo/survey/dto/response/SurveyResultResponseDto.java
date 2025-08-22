package com.example.demo.survey.dto.response;

import com.example.demo.survey.entity.Survey;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Getter @Setter
public class SurveyResultResponseDto extends SurveyBasicResponseDto{

    private List<QuestionResultResponseDto> questions;

    public SurveyResultResponseDto(Survey survey, List<QuestionResultResponseDto> questions){
        super(survey);
        this.questions = questions;
    }
}
