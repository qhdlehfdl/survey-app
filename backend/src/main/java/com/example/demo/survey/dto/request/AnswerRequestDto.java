package com.example.demo.survey.dto.request;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class AnswerRequestDto {

    private String questionId;

    //주관식(String), 객관식(int...) , 배열 상관없이 받을 수 있음
    private JsonNode answer;
}
