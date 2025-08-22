package com.example.demo.survey.dto.response;

import com.example.demo.common.ResponseCode;
import com.example.demo.common.ResponseDto;
import com.example.demo.common.ResponseMessage;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Getter
public class AnswerSubmitResponseDto extends ResponseDto {

    private AnswerSubmitResponseDto(){super(ResponseCode.SUCCESS, ResponseMessage.SUCCESS);}

    public static ResponseEntity<AnswerSubmitResponseDto> success(){
        AnswerSubmitResponseDto result = new AnswerSubmitResponseDto();
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

    public static ResponseEntity<ResponseDto> alreadyAnswered(){
        ResponseDto result = new ResponseDto(ResponseCode.ALREADY_ANSWER, ResponseMessage.ALREADY_ANSWER);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }

    public static ResponseEntity<ResponseDto> emptyAnswer(){
        ResponseDto result = new ResponseDto(ResponseCode.EMPTY_ANSWER, ResponseMessage.EMPTY_ANSWER);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }

    public static ResponseEntity<ResponseDto> invalidAnswer(){
        ResponseDto result = new ResponseDto(ResponseCode.INVALID_ANSWER, ResponseMessage.INVALID_ANSWER);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }

    public static ResponseEntity<ResponseDto> invalidQuestionOption(){
        ResponseDto result = new ResponseDto(ResponseCode.INVALID_QUESTION_OPTION,ResponseMessage.INVALID_QUESTION_OPTION);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }

}
