package com.example.demo.survey.dto.response;

import com.example.demo.common.ResponseCode;
import com.example.demo.common.ResponseDto;
import com.example.demo.common.ResponseMessage;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Getter
public class SurveyUpdateResponseDto extends ResponseDto {

    private SurveyUpdateResponseDto(){super(ResponseCode.SUCCESS, ResponseMessage.SUCCESS);}

    public static ResponseEntity<SurveyUpdateResponseDto> success(){
        SurveyUpdateResponseDto result = new SurveyUpdateResponseDto();
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    public static ResponseEntity<ResponseDto> notExistedSurvey(){
        ResponseDto result = new ResponseDto(ResponseCode.NOT_EXISTED_SURVEY, ResponseMessage.NOT_EXISTED_SURVEY);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
    }

    public static ResponseEntity<ResponseDto> authorizationFail(){
        ResponseDto result = new ResponseDto(ResponseCode.AUTHORIZATION_FAIL, ResponseMessage.AUTHORIZATION_FAIL);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result);
    }
}
