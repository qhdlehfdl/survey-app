package com.example.demo.survey.controller;

import com.example.demo.survey.dto.request.AnswerSubmitRequestDto;
import com.example.demo.survey.dto.request.SurveyRegisterRequestDto;
import com.example.demo.survey.dto.response.*;
import com.example.demo.survey.service.SurveyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/survey")
@RequiredArgsConstructor
public class SurveyController {

    private final SurveyService surveyService;

    @PostMapping("")
    public ResponseEntity<? super SurveyRegisterResponseDto> surveyRegister(@Valid @RequestBody SurveyRegisterRequestDto dto, @AuthenticationPrincipal Integer id){
        ResponseEntity<? super SurveyRegisterResponseDto> response = surveyService.surveyRegister(dto, id);
        return response;
    }

    @GetMapping("{survey_id}")
    public ResponseEntity<? super SurveyDetailResponseDto> getSurveyDetail(@AuthenticationPrincipal Integer userId, @PathVariable("survey_id") Integer surveyId){
        ResponseEntity<? super SurveyDetailResponseDto> response = surveyService.getSurveyDetail(userId, surveyId);
        return response;
    }

    @GetMapping("")
    public ResponseEntity<? super SurveyListResponseDto> getSurveyList(@RequestParam Integer page, @RequestParam Integer size) {
        ResponseEntity<? super SurveyListResponseDto> response = surveyService.getSurveyList(page, size);
        return response;
    }

    @PostMapping("/{survey_id}/response")
    public ResponseEntity<? super AnswerSubmitResponseDto> submitAnswer(@AuthenticationPrincipal Integer userId, @PathVariable("survey_id") Integer surveyId, @Valid @RequestBody AnswerSubmitRequestDto dto) {
        ResponseEntity<? super AnswerSubmitResponseDto> response = surveyService.submitAnswer(userId, surveyId, dto);
        return response;
    }

    @GetMapping("/{survey_id}/result")
    public ResponseEntity<? super GetSurveyResultResponseDto> getSurveyResult(@AuthenticationPrincipal Integer userId, @PathVariable("survey_id") Integer surveyId){
        ResponseEntity<? super GetSurveyResultResponseDto> response = surveyService.getSurveyResult(userId, surveyId);
        return response;
    }

    @PatchMapping("/{survey_id}")
    public ResponseEntity<? super SurveyUpdateResponseDto> surveyUpdate(@AuthenticationPrincipal Integer userId, @PathVariable("survey_id") Integer surveyId, @Valid @RequestBody SurveyRegisterRequestDto dto) {
        ResponseEntity<? super SurveyUpdateResponseDto> response = surveyService.updateSurvey(userId, surveyId, dto);
        return response;
    }
}
