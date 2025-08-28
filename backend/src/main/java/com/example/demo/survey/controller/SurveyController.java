package com.example.demo.survey.controller;

import com.example.demo.auth.dto.request.SignUpRequestDto;
import com.example.demo.auth.dto.response.SignUpResponseDto;
import com.example.demo.common.ResponseDto;
import com.example.demo.survey.dto.request.AnswerSubmitRequestDto;
import com.example.demo.survey.dto.request.SurveyRegisterRequestDto;
import com.example.demo.survey.dto.response.*;
import com.example.demo.survey.service.SurveyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name="Survey API", description = "설문지 API")
@RestController
@RequestMapping("/api/survey")
@RequiredArgsConstructor
public class SurveyController {

    private final SurveyService surveyService;

    @Operation(
            summary = "설문지 등록",
            description = "설문지 등록 형식에 맞춰 보내야함",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "설문지 등록 JSON body 데이터",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SurveyRegisterRequestDto.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "설문지 등록 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = SurveyRegisterResponseDto.class)
                            )
                    )  ,
                    @ApiResponse(
                            responseCode = "404",
                            description = "존재하지 않는 유저로 설문지 등록 실패",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ResponseDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "데이터베이스 오류",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ResponseDto.class)
                            )
                    )
            },
            security = @SecurityRequirement(name = "JWT")

    )
    @PostMapping("")
    public ResponseEntity<? super SurveyRegisterResponseDto> surveyRegister(@Valid @RequestBody SurveyRegisterRequestDto dto, @AuthenticationPrincipal Integer id){
        ResponseEntity<? super SurveyRegisterResponseDto> response = surveyService.surveyRegister(dto, id);
        return response;
    }

    @Operation(
            summary = "설문지 내용 가져옴",
            description = "pathVariable survey_id 필요",
            parameters = {
                    @Parameter(
                            name = "survey_id",
                            description = "설문지 id",
                            example = "10",
                            required = true
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = SurveyDetailResponseDto.class)
                            )
                    )  ,
                    @ApiResponse(
                            responseCode = "404",
                            description = "존재하지 않는 설문지",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ResponseDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "데이터베이스 오류",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ResponseDto.class)
                            )
                    )
            }

    )
    @GetMapping("{survey_id}")
    public ResponseEntity<? super SurveyDetailResponseDto> getSurveyDetail(@AuthenticationPrincipal Integer userId, @PathVariable("survey_id") Integer surveyId){
        ResponseEntity<? super SurveyDetailResponseDto> response = surveyService.getSurveyDetail(userId, surveyId);
        return response;
    }

    @Operation(
            summary = "설문지 리스트 가져옴",
            description = "메인페이지에서 사용",
            parameters = {
                    @Parameter(
                            name = "page",
                            description = "페이지 번호",
                            example = "1",
                            required = true
                    ),
                    @Parameter(
                            name = "size",
                            description = "가져올 개수",
                            example = "10",
                            required = true
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = SurveyListResponseDto.class)
                            )
                    )  ,
                    @ApiResponse(
                            responseCode = "500",
                            description = "데이터베이스 오류",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ResponseDto.class)
                            )
                    )
            }

    )
    @GetMapping("")
    public ResponseEntity<? super SurveyListResponseDto> getSurveyList(@RequestParam Integer page, @RequestParam Integer size) {
        ResponseEntity<? super SurveyListResponseDto> response = surveyService.getSurveyList(page, size);
        return response;
    }

    @Operation(
            summary = "설문지 응답",
            description = "설문지 응답",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "설문지 응답 JSON body 데이터",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AnswerSubmitRequestDto.class)
                    )
            ),
            parameters = {
                    @Parameter(
                            name = "survey_id",
                            description = "설문지 id",
                            example = "1",
                            required = true
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = AnswerSubmitResponseDto.class)
                            )
                    )  ,
                    @ApiResponse(
                            responseCode = "404",
                            description = "존재하지 않는 설문지, 유저",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ResponseDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "이미 응답, 필수 응답이지만 응답하지 않음, 유효하지않은 답변, 옵션",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ResponseDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "데이터베이스 오류",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ResponseDto.class)
                            )
                    )
            },
            security = @SecurityRequirement(name = "JWT")
    )
    @PostMapping("/{survey_id}/response")
    public ResponseEntity<? super AnswerSubmitResponseDto> submitAnswer(@AuthenticationPrincipal Integer userId, @PathVariable("survey_id") Integer surveyId, @Valid @RequestBody AnswerSubmitRequestDto dto) {
        ResponseEntity<? super AnswerSubmitResponseDto> response = surveyService.submitAnswer(userId, surveyId, dto);
        return response;
    }

    @Operation(
            summary = "설문지 결과",
            description = "설문지 결과",
            parameters = {
                    @Parameter(
                            name = "survey_id",
                            description = "설문지 id",
                            example = "1",
                            required = true
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = GetSurveyResultResponseDto.class)
                            )
                    )  ,
                    @ApiResponse(
                            responseCode = "404",
                            description = "존재하지 않는 설문지, 유저",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ResponseDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "데이터베이스 오류",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ResponseDto.class)
                            )
                    )
            },
            security = @SecurityRequirement(name = "JWT")
    )
    @GetMapping("/{survey_id}/result")
    public ResponseEntity<? super GetSurveyResultResponseDto> getSurveyResult(@AuthenticationPrincipal Integer userId, @PathVariable("survey_id") Integer surveyId){
        ResponseEntity<? super GetSurveyResultResponseDto> response = surveyService.getSurveyResult(userId, surveyId);
        return response;
    }

    @Operation(
            summary = "설문지 수정",
            description = "설문지 수정",
            parameters = {
                    @Parameter(
                            name = "survey_id",
                            description = "설문지 id",
                            example = "1",
                            required = true
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = SurveyUpdateResponseDto.class)
                            )
                    )  ,
                    @ApiResponse(
                            responseCode = "403",
                            description = "권한 없음",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ResponseDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "존재하지 않는 설문지",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ResponseDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "데이터베이스 오류",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ResponseDto.class)
                            )
                    )
            },
            security = @SecurityRequirement(name = "JWT")
    )
    @PatchMapping("/{survey_id}")
    public ResponseEntity<? super SurveyUpdateResponseDto> updateSurvey(@AuthenticationPrincipal Integer userId, @PathVariable("survey_id") Integer surveyId, @Valid @RequestBody SurveyRegisterRequestDto dto) {
        ResponseEntity<? super SurveyUpdateResponseDto> response = surveyService.updateSurvey(userId, surveyId, dto);
        return response;
    }

    @Operation(
            summary = "설문지 삭제",
            description = "설문지 삭제",
            parameters = {
                    @Parameter(
                            name = "survey_id",
                            description = "설문지 id",
                            example = "1",
                            required = true
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = DeleteSurveyResponseDto.class)
                            )
                    )  ,
                    @ApiResponse(
                            responseCode = "403",
                            description = "권한 없음",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ResponseDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "존재하지 않는 설문지, 유저",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ResponseDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "데이터베이스 오류",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ResponseDto.class)
                            )
                    )
            },
            security = @SecurityRequirement(name = "JWT")
    )
    @DeleteMapping("/{survey_id}")
    public ResponseEntity<? super DeleteSurveyResponseDto> deleteSurvey(@AuthenticationPrincipal Integer userId, @PathVariable("survey_id") Integer surveyId) {
        ResponseEntity<? super DeleteSurveyResponseDto> response = surveyService.deleteSurvey(userId, surveyId);
        return response;
    }
}
