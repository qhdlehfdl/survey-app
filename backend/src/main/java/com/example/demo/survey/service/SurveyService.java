package com.example.demo.survey.service;

import com.example.demo.survey.dto.request.AnswerSubmitRequestDto;
import com.example.demo.survey.dto.request.SurveyRegisterRequestDto;
import com.example.demo.survey.dto.response.*;
import org.hibernate.sql.Delete;
import org.springframework.http.ResponseEntity;

import java.util.List;


public interface SurveyService {

    ResponseEntity<? super SurveyRegisterResponseDto> surveyRegister(SurveyRegisterRequestDto dto, Integer id);
    ResponseEntity<? super SurveyDetailResponseDto> getSurveyDetail(Integer userId, Integer surveyId);
    ResponseEntity<? super SurveyListResponseDto> getSurveyList(Integer page, Integer size);
    ResponseEntity<? super AnswerSubmitResponseDto> submitAnswer(Integer userId, Integer surveyId, AnswerSubmitRequestDto dto);
    List<SurveySimpleResponseDto> getSurveyListByUser(Integer userId, Integer lastId, Integer size);
    ResponseEntity<? super GetSurveyResultResponseDto> getSurveyResult(Integer userId, Integer surveyId);
    ResponseEntity<? super SurveyUpdateResponseDto> updateSurvey(Integer userId, Integer surveyID, SurveyRegisterRequestDto dto);
    ResponseEntity<? super DeleteSurveyResponseDto> deleteSurvey(Integer userId, Integer surveyId);

}
