package com.example.demo.survey.dto.response;

import com.example.demo.common.ResponseCode;
import com.example.demo.common.ResponseDto;
import com.example.demo.common.ResponseMessage;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Getter
public class SurveyRegisterResponseDto extends ResponseDto {

    private SurveyRegisterResponseDto(){super(ResponseCode.SUCCESS, ResponseMessage.SUCCESS);}

    public static ResponseEntity<SurveyRegisterResponseDto> success(){
        SurveyRegisterResponseDto result = new SurveyRegisterResponseDto();
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    public static ResponseEntity<ResponseDto> notExistedUser(){
        ResponseDto result = new ResponseDto(ResponseCode.NOT_EXISTED_USER, ResponseMessage.NOT_EXISTED_USER);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
    }

}
