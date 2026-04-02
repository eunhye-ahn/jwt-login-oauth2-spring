# OAuth2 연동 정리
> 소셜 사용자 대신 내 서비스에게 유저 정보에 접근할 수 있는 권한을 주는 개방형 표준 인증 프로토콜

## 1. 사용자 흐름

구글 로그인 버튼 클릭 window.location.href -> 브라우저 직접 이동

구글 로그인 -> spring으로 리다이렉트 
: security가 인증코드와 AT를 자동 교환함 -> 이쪽은 구글에게 내 서비스를 등록으로 인증

(시큐리티 설정 OAuth2 흐름을 커스텀해주어야 시큐리티가 이동
인증완료 > 유저정보엔드포인트 : OAuth2UserService 구현체 > AuthenticationSuccessHandler 구현체)

구현해야할 클래스는 두개
- **OAuth2UserService** //구글에게 유저정보를 받아서 **db에 저장하는 로직 구현** - 없으면 조회만하고 DB에 아무런 처리 X
  loadUser() : 유저 정보 수신 > db저장 > OAuth2User 반환
- **AuthenticationSuccessHandler** //위에서 인증된 유저에게 JWT발급하고 **클라이언트에게 전달하는 로직 구현**
  onAuthenticationSuccess(...) 오버라이딩 필수 : DB조회 -> AT/RT 발급 -> 리다이렉트

## 2. URL 흐름

1. 버튼 클릭
   http://localhost:8080/oauth2/authorization/google

2. 구글 로그인 페이지로 리다이렉트 - 클라 설정
   https://accounts.google.com/o/oauth2/auth?...

3. 로그인 완료 → Spring으로 리다이렉트 (구글 콘솔에 등록한 리디렉션 URI) - 자동
   http://localhost:8080/login/oauth2/code/google?code=...

5. SuccessHandler 실행 → 리액트로 리다이렉트 - config 설정
   http://localhost:5173/callback?accessToken=eyJhbG...

6. **/callback에서 AT 저장** → /home으로 이동
   http://localhost:5173/home

---

## 3. 트러블슈팅

### 소셜 로그인 실패 401반환 문제

**문제**
> OAuth2 로그인으로 발급한 AT가 클라로 전달 안됨

**원인**
- AT를 헤더에 담아서 전달 : 리다이렉트는 헤더,바디없이 Location만 전달

**해결**
- **서버**
  - AT를 URL의 쿼리 파라미터로 전달

- **클라이언트**
  - 보안 위험 -> AT 메모리 저장 후 window.history.replaceState로 브라우저 히스토리에서 삭제
    
