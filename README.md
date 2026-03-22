# JWT 구현 정리

## 1. 토큰 저장 전략

| 구분 | 저장 위치 | 이유 |
|------|----------|------|
| Access Token | 메모리 (변수) | localStorage 저장 시 XSS 공격으로 탈취 가능 → 메모리에 저장해 JS 접근 차단 |
| Refresh Token | httpOnly 쿠키 | JS로 접근 불가 → XSS 방어, 브라우저가 요청 시 자동 전송 |

---

## 2. 재발급 전략 - 고정 RT 방식

> RT는 재발급 시 교체하지 않고 고정으로 유지

| 상황 | 처리 방식 |
|------|----------|
| 새로고침 / 브라우저 재시작 | `useEffect`에서 `/reissue` 자동 호출 |
| AT 만료 | axios interceptor 401 감지 → `/reissue` 후 재시도 |
| RT 만료 | `/reissue` 실패 감지 → 로그인 페이지로 이동 |

---

## 3. 트러블슈팅

### 브라우저 재시작 / 새로고침 시 로그인 유지 안되는 문제

**문제**
> 브라우저 재시작 / 새로고침 시 로그인이 풀림

**원인**
- AT는 메모리 저장 → 새로고침 시 초기화
- RT 쿠키에 `maxAge` 미설정 → 세션 쿠키로 동작 → 브라우저 종료 시 쿠키 삭제

**해결**
- **서버** [영속 쿠키 적용]
  - 쿠키 `maxAge` 7일 설정 → 브라우저 재시작 후에도 RT 유지

- **클라이언트** [새로고침 시 AT 자동 재발급]
  - `App`에서 `AuthInit` 완료 전까지 `Routes` 렌더링 차단
  - `AuthInit`에서 `/reissue` 자동 호출 → RT로 새 AT 발급 → 로그인 상태 복구

- **클라이언트** [AT 만료 자동 처리 - axios interceptor]
  - AT 만료 요청 시 서버 401 응답
  - response interceptor 401 감지 → `/reissue` 호출 → 새 AT 발급
  - 새 AT로 실패한 요청 자동 재시도 → 사용자 경험 끊김 없이 유지


### token=null 시 403 반환 문제

**문제**
> token=null일 경우 JWT 필터에서 인증정보 없이 통과 → 인가 단계에서 403 반환

**원인**
```
token=null
→ 필터 통과 (SecurityContext 비어있음)
→ .anyRequest().authenticated() 에서 막힘
→ Spring Security 예외 판단
    ├── 인증 안됨 (익명사용자) → AuthenticationException → AuthenticationEntryPoint
    └── 인증은 됐는데 권한 없음 → AccessDeniedException → AccessDeniedHandler
```
- `AuthenticationEntryPoint` 미설정 시 Spring Security 기본 동작으로 403 반환

**해결**
- `authenticationEntryPoint` 추가 → 인증 실패 시 명시적으로 401 반환

