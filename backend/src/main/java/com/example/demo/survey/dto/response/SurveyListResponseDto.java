package com.example.demo.survey.dto.response;

import com.example.demo.common.ResponseCode;
import com.example.demo.common.ResponseDto;
import com.example.demo.common.ResponseMessage;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Getter
public class SurveyListResponseDto extends ResponseDto {

    private List<SurveySimpleResponseDto> surveys;
    private Integer totalPages;
    private Integer totalElements;


    private SurveyListResponseDto(List<SurveySimpleResponseDto> surveys, Integer totalPages, Integer totalElements){
        super(ResponseCode.SUCCESS, ResponseMessage.SUCCESS);
        this.surveys = surveys;
        this.totalPages = totalPages;
        this.totalElements = totalElements;
    }

    public static ResponseEntity<SurveyListResponseDto> success(List<SurveySimpleResponseDto> surveys, Integer totalPages, Integer totalElements){
        SurveyListResponseDto result = new SurveyListResponseDto(surveys, totalPages, totalElements);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

}
