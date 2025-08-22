package com.example.demo.auth.dto.response;

import com.example.demo.common.ResponseCode;
import com.example.demo.common.ResponseDto;
import com.example.demo.common.ResponseMessage;
import com.example.demo.survey.dto.response.SurveySimpleResponseDto;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Getter
public class GetMyProfileResponseDto extends ResponseDto {

    private String nickname;
    private List<SurveySimpleResponseDto> surveys;
    private boolean hasNext;
    private Integer nextCursor;

    private GetMyProfileResponseDto(String nickname, List<SurveySimpleResponseDto> surveys, boolean hasNext, Integer nextCursor) {
        super(ResponseCode.SUCCESS, ResponseMessage.SUCCESS);
        this.nickname = nickname;
        this.surveys = surveys;
        this.hasNext = hasNext;
        this.nextCursor = nextCursor;
    }

    public static ResponseEntity<GetMyProfileResponseDto> success(String nickname, List<SurveySimpleResponseDto> surveys, boolean hasNext, Integer nextCursor){
        GetMyProfileResponseDto result = new GetMyProfileResponseDto(nickname, surveys, hasNext, nextCursor);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    public static ResponseEntity<ResponseDto> notExistedUser(){
        ResponseDto result = new ResponseDto(ResponseCode.NOT_EXISTED_USER, ResponseMessage.NOT_EXISTED_USER);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
    }
}
