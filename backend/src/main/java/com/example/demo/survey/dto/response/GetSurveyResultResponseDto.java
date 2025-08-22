package com.example.demo.survey.dto.response;

import com.example.demo.auth.dto.response.GetMyProfileResponseDto;
import com.example.demo.common.ResponseCode;
import com.example.demo.common.ResponseDto;
import com.example.demo.common.ResponseMessage;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Getter
public class GetSurveyResultResponseDto extends ResponseDto {

    private SurveyResultResponseDto survey;

    private GetSurveyResultResponseDto(SurveyResultResponseDto survey){
        super(ResponseCode.SUCCESS, ResponseMessage.SUCCESS);
        this.survey = survey;
    }

    public static ResponseEntity<GetSurveyResultResponseDto> success(SurveyResultResponseDto survey){
        GetSurveyResultResponseDto result = new GetSurveyResultResponseDto(survey);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    public static ResponseEntity<ResponseDto> notExistedSurvey(){
        ResponseDto result = new ResponseDto(ResponseCode.NOT_EXISTED_SURVEY, ResponseMessage.NOT_EXISTED_SURVEY);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
    }

    public static ResponseEntity<ResponseDto> notExistedUser(){
        ResponseDto result = new ResponseDto(ResponseCode.NOT_EXISTED_USER, ResponseMessage.NOT_EXISTED_USER);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
    }
}
