package com.example.demo.survey.dto.response;

import com.example.demo.survey.entity.Question;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class QuestionResultResponseDto extends QuestionBasicResponseDto {

    private List<QuestionOptionResultResponseDto> options;
    private List<String> answers;

    public QuestionResultResponseDto(Question q, List<QuestionOptionResultResponseDto> options){
        super(q);
        this.options = options;
        this.answers = new ArrayList<>();
    }
}
