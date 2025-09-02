package com.example.demo.auth.controller;

import com.example.demo.auth.dto.request.SignInRequestDto;
import com.example.demo.auth.dto.request.SignUpRequestDto;
import com.example.demo.auth.dto.response.*;
import com.example.demo.auth.service.AuthService;
import com.example.demo.common.ResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name="Auth API", description = "계정 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthCotroller {

    private final AuthService authService;

    @Operation(
            summary = "회원가입",
            description = "회원가입 형식에 맞춰 보내야함",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "회원가입 JSON body 데이터",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SignUpRequestDto.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "회원가입 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = SignUpResponseDto.class)
                            )
                    )  ,
                    @ApiResponse(
                            responseCode = "400",
                            description = "아이디, 이메일, 닉네임 중복 등으로 회원가입 실패",
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
    @PostMapping("/sign-up")
    public ResponseEntity<? super SignUpResponseDto> signUp(@Valid @RequestBody SignUpRequestDto dto) {
        ResponseEntity<? super SignUpResponseDto> response = authService.signUp(dto);
        return response;
    }

//    @Operation(
//            summary = "로그인",
//            description = "로그인 형식에 맞춰 보내야함",
//            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
//                    description = "로그인 JSON body 데이터",
//                    required = true,
//                    content = @Content(
//                            mediaType = "application/json",
//                            schema = @Schema(implementation = SignInRequestDto.class)
//                    )
//            ),
//            responses = {
//                    @ApiResponse(
//                            responseCode = "200",
//                            description = "로그인 성공",
//                            content = @Content(
//                                    mediaType = "application/json",
//                                    schema = @Schema(implementation = SignInResponseDto.class)
//                            )
//                    )  ,
//                    @ApiResponse(
//                            responseCode = "401",
//                            description = "로그인 실패",
//                            content = @Content(
//                                    mediaType = "application/json",
//                                    schema = @Schema(implementation = ResponseDto.class)
//                            )
//                    ),
//                    @ApiResponse(
//                            responseCode = "500",
//                            description = "데이터베이스 오류",
//                            content = @Content(
//                                    mediaType = "application/json",
//                                    schema = @Schema(implementation = ResponseDto.class)
//                            )
//                    )
//            }
//
//    )
//    @PostMapping("/sign-in")
//    public ResponseEntity<? super SignInResponseDto> signIn(@Valid @RequestBody SignInRequestDto dto) {
//        ResponseEntity<? super SignInResponseDto> response = authService.signIn(dto);
//        return response;
//    }

    @Operation(
            summary = "토큰 리프레쉬",
            description = "새로운 access token 발급",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "토큰 발급 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = RefreshTokenResponseDto.class)
                            )
                    )  ,
                    @ApiResponse(
                            responseCode = "401",
                            description = "refresh token 유효하지 않아서 토큰 발급 실패",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ResponseDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "refresh token 유효하지 않아서 토큰 발급 실패",
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
    @PostMapping("/refresh")
    public ResponseEntity<? super RefreshTokenResponseDto> refreshToken(HttpServletRequest request) {
        ResponseEntity<? super RefreshTokenResponseDto> response = authService.refreshToken(request);
        return response;
    }

//    @Operation(
//            summary = "로그아웃",
//            description = "refresh token 삭제",
//            responses = {
//                    @ApiResponse(
//                            responseCode = "200",
//                            description = "로그아웃 성공",
//                            content = @Content(
//                                    mediaType = "application/json",
//                                    schema = @Schema(implementation = LogOutResponseDto.class)
//                            )
//                    )  ,
//                    @ApiResponse(
//                            responseCode = "403",
//                            description = "refresh token 유효하지 않아서 로그아웃 실패지만 프론트에서는 로그아웃처리",
//                            content = @Content(
//                                    mediaType = "application/json",
//                                    schema = @Schema(implementation = ResponseDto.class)
//                            )
//                    ),
//                    @ApiResponse(
//                            responseCode = "500",
//                            description = "데이터베이스 오류",
//                            content = @Content(
//                                    mediaType = "application/json",
//                                    schema = @Schema(implementation = ResponseDto.class)
//                            )
//                    )
//            }
//    )
//    @PostMapping("/logout")
//    public ResponseEntity<? super LogOutResponseDto> logout(HttpServletRequest request) {
//        ResponseEntity<? super LogOutResponseDto> result = authService.logout(request);
//        return result;
//    }

    @Operation(
            summary = "간단한 계정 정보, 자신이 작성한 설문지, 무한스크롤 정보 제공",
            description = "간단한 계정 정보, 자신이 작성한 설문지, 무한스크롤 정보 제공",
            parameters = {
                    @Parameter(
                            name = "lastId",
                            description = "마지막으로 가져온 설문 ID (무한스크롤용)",
                            example = "10",
                            required = false
                    ),
                    @Parameter(
                            name = "size",
                            description = "한 번에 가져올 설문 개수 (기본값: 5)",
                            example = "5",
                            required = false
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = GetMyProfileResponseDto.class)
                            )
                    )  ,
                    @ApiResponse(
                            responseCode = "404",
                            description = "존재하지 않는 유저",
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
    @GetMapping("/profile")
    public ResponseEntity<? super GetMyProfileResponseDto> getMyProfile(@AuthenticationPrincipal Integer userId, @RequestParam(required = false) Integer lastId, @RequestParam(required = false, defaultValue = "5") Integer size){
        ResponseEntity<? super GetMyProfileResponseDto> result = authService.getMyProfile(userId, lastId, size);
        return result;
    }
}
