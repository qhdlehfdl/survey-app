package com.example.demo.survey.dto.response;

import com.example.demo.survey.entity.Question;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class QuestionBasicResponseDto {

    private Integer id;
    private String questionId;
    private String text;
    private String type;
    private boolean required;
    private Integer order;

    public QuestionBasicResponseDto(Question q) {
        this.id = q.getId();
        this.questionId = q.getQuestionId().toString();
        this.text = q.getText();
        this.type = q.getType().toString();
        this.required = q.isRequired();
        this.order = q.getQuestionOrder();
    }
}
