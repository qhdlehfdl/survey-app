# 📝 Survey Project

사용자가 설문지를 작성할 수 있고 간단한 결과를 볼 수 있는 웹서비스.
Typescript + SpringBoot 기반으로 제작하였고 JWT 인증을 사용하였다.

---

## 🛠 기술 스택
**Frontend**
- React, TypeScript
- Styled-components / CSS Modules

**Backend**
- Java 17, Spring Boot
- Spring Security, JWT
- JPA, MySQL, Redis(refresh 토큰 관리(blacklist))

---
## 로그인 구현
기존에는 컨트롤러에서 로그인 요청을 받아 처리하는 식으로 구성했지만 스프링 시큐리티를 조금 더 사용하기 위해 로그인 요청이 들어오면 필터단에서 요청을 가로채 처리하였다.

- ### 로그인 플로우
1. 요청이 들어오면 먼저 JwtAuthentication 필터를 만남. 로그인 요청이므로 jwt 없음 -> 그 다음 필터 진행  
2. 로그인 필터 만남. 필터에서 authenticationManager.authenticate() 실행.
3. authenticate()에서 UserService의 loadUserByUsername 함수 실행.
4. 성공 후 authenticate()에서 Authentication 리턴.
5. LoginSuccessHandler 실행.

- ### JwtAuthenticationFilter
1. 토큰이 없다면 그 다음 필터로 보냄
2. 토큰 유효한지 확인. 유효하지 않다면 유효하지 않다는 응답 보냄.
3. 토큰에서 role, userId(db에서 pk값) 가져오고 UsernamePasswordAuthentication()으로 userId를 principal로 지정.

- ### 소셜 로그인

1. 프론트에서 백엔드로 리디렉션(fetch X). 
2. 카카오 개발자에 설정해둔 리다이렉트 URI에 의해 카카오 서버가 로그인 창 띄워줌. 카카오 서버에서 로그인 과정(코드로 토큰 받고 토큰으로 유저정보 받음, 스프링부트가 알아서 수행) 진행
3. 유저 정보로 로그인. access, refresh token 발급

-  #### 3번의 로그인 과정에 대한 설명
자체 로그인처럼 로그인 성공에 대한 응답으로 access, refresh(httpOnly) 토큰을 바로 발급하면 좋지만 1번에서 프론트가 백엔드로 리디렉션시켰기때문에 백엔드에서 응답을 보내도 프론트에서는 응답을 받았는지 알 수가 없음.

왜? 요청(fetch)하지 않았기때문. 

해결방법으로 

1. 로그인 성공 후 백엔드에서 짧은 생명주기를 가진 refresh token을 httpOnly로 쿠키 설정해주고 프론트의  OAuthCookieHandler.tsx 페이지로 리디렉션 시킨다.
2. 해당 페이지에서 httpOnly로 설정된 refresh 토큰을 넣어서 useEffect로 /refresh 요청을 보낸다.
3. 새로운 access, refresh 토큰 발급. 로그인 완료

---
## 📜 API 명세서
- Swagger를 통한 api 문서 자동화

[index.html](https://github.com/user-attachments/files/22019524/index.html)


## ⚙️ERD
<img width="1170" height="912" alt="Copy of Copy of survey ERD" src="https://github.com/user-attachments/assets/953ff274-a467-4301-95c6-db56347598e0" />

---

## 🖥️ 실행 화면
<details>
  <summary>메인 페이지</summary>
  <img width="1527" height="852" alt="image" src="https://github.com/user-attachments/assets/e4730498-877f-4706-88f6-2c1b9f278c45" />
</details>

<details>
  <summary>개인 페이지</summary>
  <img width="1198" height="801" alt="image" src="https://github.com/user-attachments/assets/12b2ec6c-d4cd-4d1b-86f2-43ac25b6b660" />
</details>

<details>
  <summary>설문지 결과 페이지</summary>
  <img width="1290" height="683" alt="image" src="https://github.com/user-attachments/assets/60ba0a0f-621b-497d-879c-ab4a32639c65" />
</details>

<details>
  <summary>설문지 수정 페이지</summary>
  응답자가 있는 질문은 수정 불가능. 선택지(옵션)추가, 필수 수정만 가능
  <img width="1313" height="833" alt="image" src="https://github.com/user-attachments/assets/6e5bcc8c-5d4b-4a16-b8d3-e4805758c8c5" />
</details>

<details>
  <summary>설문지 작성 페이지</summary>
  <img width="1230" height="443" alt="image" src="https://github.com/user-attachments/assets/2355c67b-4954-4bd5-a698-0dce97511c8a" />

</details>

## 배포
url: http://52.63.0.110/main

aws-ec2, aws-rds 사용
<details>
  <img width="1812" height="990" alt="image" src="https://github.com/user-attachments/assets/9ffd3c15-4fc1-4c0d-aa5a-4d3601db1b34" />
</details>
