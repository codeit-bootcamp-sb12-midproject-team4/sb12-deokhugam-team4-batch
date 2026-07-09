`.env`를 설정하지 않으면 의존성 추가가 되지 않습니다.

다음의 example을 참고하여 환경변수 설정이 필요합니다.

[.env.example](../.env.example)

```aiignore
GITHUB_REPOSITORY=OWNER/REPOSITORY 깃허브 레포가 필요합니다.
GITHUB_ACTOR=USERNAME 깃허브 유저네임이 필요합니다.
GITHUB_TOKEN=GITHUB_PERSONAL_ACCESS_TOKEN_CLASSIC 토큰이 필요합니다.
```

repo와 actor는 각각 레포의 경로와 본인의 유저네임이 필요합니다.

---

토큰은 다음 순서로 진행해서 발급이 필요합니다.

1. https://github.com/settings/apps
    - Developer settings에서 클래식 토큰 발급.

2. scope는 write, read:package

<img width="662" height="344" alt="Image" src="https://github.com/user-attachments/assets/94781398-0068-4bcc-b0d6-a59bb99340b4" />
