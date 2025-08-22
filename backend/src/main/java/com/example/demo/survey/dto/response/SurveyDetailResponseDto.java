package com.example.demo.survey.dto.response;

import com.example.demo.common.ResponseCode;
import com.example.demo.common.ResponseDto;
import com.example.demo.common.ResponseMessage;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Getter
public class SurveyDetailResponseDto extends ResponseDto {

    private SurveyResponseDto survey;

    private SurveyDetailResponseDto(SurveyResponseDto dto){
        super(ResponseCode.SUCCESS, ResponseMessage.SUCCESS);
        this.survey = dto;
    }

    public static ResponseEntity<SurveyDetailResponseDto> success(SurveyResponseDto dto){
        SurveyDetailResponseDto result = new SurveyDetailResponseDto(dto);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    public static ResponseEntity<ResponseDto> notExistedSurvey(){
        ResponseDto result = new ResponseDto(ResponseCode.NOT_EXISTED_SURVEY, ResponseMessage.NOT_EXISTED_SURVEY);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
    }
}
