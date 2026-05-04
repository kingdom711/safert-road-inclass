# Historical Migrations (Reference Only)

이 폴더의 SQL 파일들은 **Flyway 도입 이전** 시점에 작성된 마이그레이션 자료입니다.

## 상태
- 원래 위치: `sql/`, `src/main/resources/sql/`
- **실제로 실행된 적 없음** (Flyway는 비활성 상태였고 스키마는 `ddl-auto=update` 가 만들었음)
- V4, V5에 같은 버전번호가 두 개씩 존재 → Flyway 호환 X

## 처리 방식
- prod 도입 시점에 Flyway는 `baseline-version=12` 로 baseline 됨
- 즉, 이 파일들은 Flyway가 **이미 적용된 것으로 간주**하고 건너뜀
- 신규 마이그레이션은 `src/main/resources/db/migration/` 에 `V13__...` 부터 작성

## 새 마이그레이션 작성 규칙
1. `src/main/resources/db/migration/V{n}__설명.sql` 생성 (n ≥ 13)
2. PostgreSQL 문법 사용 (Supabase = Postgres)
3. 엔티티 변경 시 함께 커밋
4. dev(SQLite)에서는 Flyway 비활성이므로 ddl-auto가 처리
