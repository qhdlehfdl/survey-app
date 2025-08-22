package com.example.demo.survey.dto.response;

import com.example.demo.survey.entity.QuestionOption;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class QuestionOptionResponseDto {

    private Integer id;
    private String text;
    private Integer order;

    public QuestionOptionResponseDto(QuestionOption option){
        this.id = option.getId();
        this.text = option.getText();
        this.order = option.getOptionOrder();
    }
}
