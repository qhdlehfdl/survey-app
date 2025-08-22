package com.example.demo.survey.dto.response;

import com.example.demo.survey.entity.Survey;
import lombok.Getter;

@Getter
public class SurveySimpleResponseDto {

    private Integer id;
    private String title;
    private String writerNickname;
    private Integer participantsNum;

    public SurveySimpleResponseDto(Survey survey) {
        this.id = survey.getId();
        this.title = survey.getTitle();
        this.writerNickname = survey.getWriter().getNickname();
        this.participantsNum = survey.getParticipantsNum();
    }
}
