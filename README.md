# invite Project

## 사용 기술
- Spring Boot, Java, JPA, H2, Redis(EmbeddedRedis, port = 6378)

## 기능 소개
- 회원은 그룹을 생성할 수 있다.
- 그룹을 생성하면 그룹 매니저가 된다.
- 매니저는 그룹에 참여자를 초대할 수 있다.
  - 기존 회원 : 회원 ID를 사용하여 초대
  - 신규 회원 : 회원 정보를 받아 임시 회원을 생성하여 초대
  - 초대 정보는 레디스에 저장하고 한시간 뒤 자동 만료된다.
- 초대링크를 통해 그룹에 참여할 수 있다.
  - 신규 회원은 임시 회원에서 일반 회원이 된다. (초기 비밀번호 = 00000000)

## 구현 API
### USER
- POST - /users/sign-up
  - 회원 가입

- POST - /users/sign-in
  - 로그인
  - 로그인 성공시 토큰 발행

### GROUP
- POST -  /groups
  - 그룹 생성
  - 회원만 그룹 생성 가능, 그룹을 생성하면 매니저가 됨
    
- POST - /groups/invite/new/{groupId}
  - 신규회원 초대링크 생성
    - 이메일, 이름, 핸드폰번호를 입력받아 초대링크를 생성하여 레디스에 저장
      
- POST - /groups/invite/exist/{groupId}
  - 기존회원 초대링크 생성
  - 이메일만 입력받아 초대링크를 생성하여 레디스에 저장
    
- POST - /groups/join
  - 그룹 가입
  - 요청파라미터로 초대링크URL 받아 그룹에 가입하고 해당 URL은 만료시킴
    
