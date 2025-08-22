package com.example.demo.survey.dto.response;

import com.example.demo.survey.entity.Survey;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SurveyBasicResponseDto {

    private Integer id;
    private String title;
    private Integer writerId;
    private Integer participantsNum;

    public SurveyBasicResponseDto(Survey survey) {
        this.id = survey.getId();
        this.title = survey.getTitle();
        this.writerId = survey.getWriter().getId();
        this.participantsNum = survey.getParticipantsNum();
    }
}
