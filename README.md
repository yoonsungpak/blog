# Blog

Spring Boot와 Next.js로 구성한 개인 블로그입니다. 회원과 역할 변경은 Spring Data JPA로 SQLite에 저장하고, Next.js는 Drizzle로 같은 DB를 읽기 전용으로 조회합니다. 로그인 세션은 Redis에 저장합니다.

## 구현 현황

- 홈, 게시판, 글 상세 화면은 샘플 데이터를 표시합니다.
- 회원가입, 로그인, 로그아웃이 동작합니다. 내비바는 서버에서 Redis 세션을 읽어 사용자 이름과 관리자 링크를 표시합니다.
- 관리자 화면(`/admin`)에서 사용자 목록을 보고 역할을 변경할 수 있습니다. 관리자 권한이 없으면 404 화면이 표시됩니다.
- 게시글·게시판 관리와 검색은 아직 구현 전입니다.

## 로컬 실행

JDK 17, Node.js 22 이상, npm, Redis가 필요합니다. 아래 명령은 각각 저장소 루트에서 시작하는 별도 터미널에서 실행합니다.

**Redis**

이미 로컬 Redis가 실행 중이면 이 단계는 생략합니다.

```sh
redis-server --bind 127.0.0.1 --port 6379
```

**백엔드**

`dev` 프로필은 개발 DB의 테이블을 갱신합니다. 아래 JDBC URL을 사용하면 `backend`에서 실행해도 저장소 루트의 `database.db`를 사용합니다.

```sh
cd backend
SPRING_PROFILES_ACTIVE=dev SPRING_DATASOURCE_URL=jdbc:sqlite:../database.db bash gradlew bootRun
```

**프런트엔드**

```sh
cd frontend
npm ci
npm run dev
```

브라우저에서 [localhost:3000](http://localhost:3000)을 엽니다. Next.js는 `/api/*` 요청을 `localhost:3001`로 전달합니다. [`/api/health`](http://localhost:3000/api/health)에서 백엔드 연결을 확인할 수 있습니다.

프런트의 기본 SQLite 경로는 `frontend` 기준 `../database.db`입니다. 다른 파일을 쓰려면 프런트에 `SQLITE_DB_PATH`를 지정하고 백엔드의 JDBC URL도 같은 파일로 맞춥니다. 프런트의 기본 Redis 주소는 `redis://127.0.0.1:6379`이며 `REDIS_URL`로 변경할 수 있습니다. 두 서버는 같은 Redis와 세션 네임스페이스 `blog:session`을 사용해야 합니다.

## 로컬 관리자 계정

회원가입한 계정의 기본 역할은 `VISITOR`입니다. 첫 관리자 계정은 로컬 개발 DB에서 역할을 바꿔 설정합니다. 저장소 루트에서 `your_username`을 실제 아이디로 바꿔 실행하세요.

```sh
sqlite3 database.db "UPDATE users SET role = 'ADMIN' WHERE username = 'your_username' AND deleted_at IS NULL;"
```

## 검증

저장소 루트에서 실행합니다.

```sh
(cd backend && bash gradlew test)
(cd frontend && npm run lint && npm run build)
```

## 라이선스

[GNU Affero General Public License v3.0](LICENSE)
