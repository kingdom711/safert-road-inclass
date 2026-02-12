변경 요약
1. Role.java - ROLE_ADMIN 추가

기존 3개 역할(WORKER, SUPERVISOR, SAFETY_MANAGER)은 그대로 유지
ROLE_ADMIN: 모든 역할의 기능을 사용할 수 있는 테스트용 슈퍼 역할
2. AdminAccountSeeder.java - ROLE_ADMIN 사용

ROLE_SAFETY_MANAGER → ROLE_ADMIN으로 변경
3. SecurityConfig.java:75-77 - URL 접근 제어에 ADMIN 추가

/api/v1/reviews/** → hasAnyRole("SUPERVISOR", "SAFETY_MANAGER", "ADMIN")
/api/v1/admin/** → hasAnyRole("SAFETY_MANAGER", "ADMIN")
4. ReviewService.java:53-57 - 검토 권한에 ADMIN 추가

5. ChecklistService.java:117-122 - 기존 SAFETY_MANAGER 권한 원복 + ROLE_ADMIN 바이패스 추가

admin 계정으로 사용 가능한 기능
역할 기능	예시	admin 접근
WORKER 기능	체크리스트 작성/제출	OK
SUPERVISOR 기능	체크리스트 검토/승인	OK
SAFETY_MANAGER 기능	관리자 페이지, 전체 현황	OK
보상 교환	골드 → 상품 교환	OK (isVerified=true)
로그인: POST /api/v1/auth/login → { "username": "admin", "password": "123123" }