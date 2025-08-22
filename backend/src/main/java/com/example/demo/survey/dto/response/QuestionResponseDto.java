package com.example.demo.survey.dto.response;

import com.example.demo.survey.entity.Question;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class QuestionResponseDto extends QuestionBasicResponseDto{

    private List<QuestionOptionResponseDto> options;
    private Integer participantsNum;
    public QuestionResponseDto(Question q, List<QuestionOptionResponseDto> options, Integer participantsNum){
        super(q);
        this.options = options;
        this.participantsNum = participantsNum;
    }

}
