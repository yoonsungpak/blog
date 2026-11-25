# Blog

Spring Boot 백엔드와 Next.js 프론트엔드로 구성한 블로그 프로젝트입니다.

## 백엔드

JDK 17이 필요합니다. 저장소 루트에서 아래 명령을 실행하면 백엔드 테스트를 실행할 수 있습니다.

```sh
cd backend
bash gradlew test
```

현재 백엔드는 Spring Boot 초기 구성 상태이며 HTTP API는 아직 없습니다.

## 프런트엔드

Node.js 20 이상과 npm이 필요합니다. 저장소 루트에서 아래 명령을 실행합니다.

```sh
cd frontend
npm ci
npm run dev
```

브라우저에서 [localhost:3000](http://localhost:3000)을 열면 개발 화면을 볼 수 있습니다. `npm run lint`로 코드를 검사하고 `npm run build`로 프로덕션 빌드를 만들 수 있습니다.

## 라이선스

[GNU Affero General Public License v3.0](LICENSE)
