## 소개
- 인프런의 질문/스터디 게시판을 기능을 구현하고 서버 scale-out 을 고려했을 때도 최대한 문제가 없을 수 있는 방법으로 구현해 보는 **개인 프로젝트** 입니다.

- 트래픽 관련 정보는 [국내 커뮤니티 트래픽 정보](https://todaybeststory.com/ranking_monthly.html) 를 참고해 대략적으로 계산하였습니다.

## 개발 기간
- 2023.08 ~ 2024.01

## 기술 스택
- Application : Java 17, Spring boot 3, JPA, Querydsl
- DB : MySQL, Redis
- Test : JUnit5, Mockito
- Infra : AWS, Docker, Grafana, prometheus, github actions

## Architecture
![image](https://github.com/youngreal/inflearn/assets/59333182/ffe17a9e-c1f3-49b4-868d-253e5955ee2a)

## 문제 해결
- [ApplicationEventListener와 @Async로 회원가입 시 회원 저장과 이메일 전송의 강결합 + 레이턴시 증가 문제 개선하기](#1-applicationeventlistener와-async로-회원가입-시-회원-저장과-이메일-전송의-강결합--레이턴시-증가-문제-개선하기)
- [외부 서비스(Gmail)의 네트워크 지연에 대비해 적절한 retry 전략 도입하기](#2-외부-서비스gmail의-네트워크-지연에-대비해-적절한-retry-전략-도입하기)
- [redis 분산락으로 서버 간 동일한 인기글 리스트 갱신을 보장하기](#3-redis-분산락으로-서버-간-동일한-인기글-리스트-갱신을-보장하기)
- [인기글 조회에 트래픽이 몰려 대량의 update 쿼리가 발생하는 상황 해결하기](#4-인기글-조회에-트래픽이-몰려-대량의-update-쿼리가-발생하는-상황-해결하기)
- [LIKE %word%로 게시글 검색 시 full table scan이 발생해 레이턴시가 증가하는 문제를 fulltext-search로 개선하기](#5-like-word로-게시글-검색-시-full-table-scan이-발생해-레이턴시가-증가하는-문제를-fulltext-search로-개선하기)

## 1. ApplicationEventListener와 @Async로 회원가입 시 회원 저장과 이메일 전송의 강결합 + 레이턴시 증가 문제 개선하기

### 문제 발견

- 메일 전송에 실패하는 경우 회원가입을 재요청 해야 한다는 문제를 최초로 인식하였고 이는 한 트랜잭션에 묶인 것이 원인이라고 판단하였습니다.
- 이로 인해 회원가입에 대한 응답을 메일 전송이 끝나야만 받아볼 수 있는 단점도 인식했습니다.(부가적으로 DB 커넥션을 오래 소유하게 되어 커넥션 반납이 늦어질 수 있다는 점도 인식)
- 회원은 메일에 대해 알 필요가 없으며 회원가입 시 메일 전송 이외의 추가적인 이벤트가 발생한다면 AService, BService 등의 추가적인 의존이 생길 여지가 있는데 이를 MemberService가 의존할 대상은 아니라고 판단했습니다.

### AS-IS
```java
...
public class MemberService {

    private final MemberRepository memberRepository;
    private final MailService mailService;

    @Transactional
    public Member signUp(Member member) {
        ...
        //메일 전송
        mailService.send(emailMessage(member));

        //회원 저장
        return memberRepository.save(member);
    }
}
```

### 해결 과정
**결합을 줄이고 응답속도 개선하기**
  - 메일 전송과 회원 저장, 두 작업을 비동기적으로 실행하는 것이 효율적이라고 판단하였고,  메일 전송을 스프링의 @Async를 사용하여 비동기로 처리해야겠다고 판단했습니다.
    
  - 트랜잭션 분리만 고려했을 때는 @Transactional(propagation = Propagation.REQUIRES_NEW)도 고려하였으나, 강한 결합, 회원가입의 확장성(회원가입 시 추가 이벤트가 생긴다면?) 등을 고려했을 때 Mail이 아닌 다른 모듈에 대한 의존도 생길 여지가 있다고 판단하여 스프링의 이벤트 핸들러인 ApplicationEventListenr를 선택하게 되었습니다.
    
  - 당장 응답속도와 메일과 회원의 강결합문제는 ApplicationEventListener와 @Async를 사용하여 해결할 수 있다고 생각하였으나 추후 문제가 될 수있는 여지들을 아래와 같이 살펴보기로 했습니다.

**ApplicationEventListener를 선택하고 고려해야 했던점**
- 서버 간 이벤트를 공유하는가?   
  1. 이벤트 성질 고려
      - 메일 전송이라는 이벤트의 성질을 고려해 보면, 이벤트를 발행하고 서버 간 이벤트를 공유하지 않고 독립적으로 처리되는 성질로 인식하였습니다.
      - 메시징 시스템을 학습하여 도입하지 않고 ApplicationEventListener를 사용해도 크게 문제가 되지 않을 거라고 판단하였습니다.
  
  2. scale-out
      - 각자 서버에서 이벤트를 발행하고 처리하고 공유하지 않기 때문에 스프링 서버를 scale-out 하더라도 문제 되지 않는다고 판단했습니다.
  
  3. 스프링 리소스 소요
      - 이벤트 처리 주체가 스프링이기 때문에 스프링의 리소스가 소요됩니다.
      - 회원가입에 특정 선착순 회원가입 이벤트 기능이 추가된다면 그때 성능 테스트를 해보고 메시징 시스템 등 다른 방법을 도입해 볼 수 있을 것 같은데 그전에 nginx 같은 웹서버의 부하 분산을 우선적으로 고려해 볼 것 같습니다.
      - 해당 단점은 충분히 안고 갈 수 있는 요소라고 판단했습니다.

**@Async를 선택하고 고려해야 했던점**
  1. 회원 저장에 실패했는데 메일은 보내지는 경우

      - 두 작업을 비동기로 처리하면서 위와 같은 경우가 발생할 수 있어서 **@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)** 을 사용하여 회원 저장에 성공하는 경우에만 메일을 보내게끔 해줬습니다.
```java
@RequiredArgsConstructor
@Service
public class MailSentEventHandler {

    private final MailService mailService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT) // 트랜잭션이 커밋되어야만 메일 전송
    public void handle(MailSentEvent event) {
        mailService.send(event.getMessage());
    }

    ...
}
```

  2. 스프링 MVC 예외 핸들러로 메일 전송 시 생길 수 있는 비동기 동작시 예외를 잡을 수 없는 문제
      - AsyncUncaughtExceptionHandler를 정의한 후 예외 핸들링 했습니다.

### TO-BE
- MemberService에선 메일 전송에 대한 방법을 알 필요가 없어졌고, 메일 이벤트가 아닌 추가 이벤트가 발생한다 하더라도 해당 이벤트에 대해서도 MemberService가 알아야 할 필요성이 사라지게 되었습니다.
- @Async 메일 전송으로 인해 회원가입에 대한 응답속도가 5초 -> 0.7초로 감소하였습니다.

```java
...
public class MemberService {

    private final MemberRepository memberRepository;

    @Transactional
    public Member signUp(Member member) {
        ...
        //메일 이벤트 발행
        Events.raise(new MailSentEvent(member));

        //회원 저장
        return memberRepository.save(member);
    }
}
```

## 2. 외부 서비스(Gmail)의 네트워크 지연에 대비해 적절한 retry 전략 도입하기
### 문제 발견
- 현재 Gmail SMTP 서버를 사용해 메일을 전송하고 있는데 확률은 낮겠지만 Gmail에서 지연이나 장애가 발생하는 경우 우리 서비스는 메일을 전송해 주지 못하는 SPOF가 발생할 수 있습니다.
- 메일 전송 실패 시 대응할 여러 정책이 있지만, 사용자에게 재전송 요청을 하는 것보단 편의성을 고려하여 사용자는 한 번만 요청하도록 하고 싶었습니다.

### 해결과정
            
  1. **네트워크 일시적 장애(ex. read timeout)인 경우**
     - 외부 서비스의 일시적 장애 때문에 유저가 직접 재요청을 보내는 것보다는, 실패한 메일 전송 이벤트를 retry 해주는 게 좋다고 판단하였습니다.
    
  2. **retry를 도입하고 고려했던점**
  - 만약 요청 지연인 상황이라면, retry 자체가 네트워크에 부담을 더할 수 있기 때문에 retry 전략을 효율적으로 가져가야 된다고 판단했습니다.
       1. retry의 회수와 간격
          - Exponential Backoff라는 지수에 비례해 retry의 재전송 대기시간을 늘리는 방법(100ms, 200ms, 400ms ...)으로 개선할 수 있었습니다.
          - 다만, 4시 30분이라는 똑같은 시간에 지연 요청이 쌓이게 되면 모두 같은 특정 시간에 retry가 몰린다는 문제가 있습니다.
          - Jitter라는 개념을 추가해 각 retry들은 backOff 대기시간 + 랜덤 대기시간을 부여받아 동시에 retry가 몰리는 것을 분산, 방지 할수있게 되었습니다.
          
### To-BE
```java
@RequiredArgsConstructor
@Service
public class MailSentEventHandler {

    private final MailService mailService;

    @Async
    @Retryable(
            retryFor = CustomMessagingException.class,
            maxAttempts = 4,
            backoff = @Backoff(
                    delay = 1000,
                    maxDelay = 20000,
                    multiplier = 2.0,
                    random = true // jitter
            )
    )
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT) // 트랜잭션이 커밋되어야만 메일 전송
    public void handle(MailSentEvent event) {
        mailService.send(event.getMessage());
    }

    ...
}
```

- retry의 재 전송 대기시간(1-1-1-1 -> 2-3-7-10로 개선)의 간격을 두고 각 간격들도 골고루 분산시켜서 일시적인 네트워크 부담을 줄여줄 수 있게 되었습니다.
- 외부 gmail 서비스의 큰 장애 발생 시에 secondary 메일 서버를 두고,  선착순 이벤트 등과 같은 이벤트가 예정되어 있더라도 추가로 어떻게 대응할지 고려해 볼 수 있습니다.

![image](https://github.com/youngreal/inflearn/assets/59333182/b701c7d2-f7fd-42aa-82bc-c9f0d390f7e8)
2-3-7-10 의 retry 대기시간

![image](https://github.com/youngreal/inflearn/assets/59333182/4a85833e-14ab-497b-af9b-ccfabedd6950)
1-3-5-14 의 retry 대기시간


## 3. redis 분산락으로 서버 간 동일한 인기글 리스트 갱신을 보장하기
### 문제 발견
- likes 테이블과 post 테이블은 분리되어 있고, likes 테이블의 개수 (게시글의 좋아요 개수)순으로 정렬해 스케줄링 서비스로 5분에 한 번씩 DB에서 여러 서버가 가져오는 상황입니다.
![image](https://github.com/youngreal/inflearn/assets/59333182/7829334f-856c-415e-a436-e0472b603670)

- 서버 간 select이 발생하는 사이 중간에 likes 테이블에 insert가 발생하면 서버 간 서로 다른 인기글 리스트를 갱신하는 문제를 발견했습니다.

### 해결과정

1. **꼭 서버 간 인기글 리스트가 같게 맞춰줘야 하는가?**

- 현재 게시글 조회 API는 GET /posts/{postId} 로, postId만 받아서 게시글을 조회합니다.
- 캐시 히트에 성공하는 postId인 경우 db에 update 하지않고, 캐시 미스가 나면 db에 update가 발생합니다.
   
만약 서버 간 인기글 리스트가 아래와 같이 다르다고 가정해 보겠습니다.
서버 1: 1,2,3,4,5 (postId)
서버 2: 2,3,4,5,6 (postId) 

이때, postId가 1인 게시글에 조회가 엄청 발생해 서버 2로 몰리게 된다면, 의도와 다르게 캐시 미스가 발생해 성능이 저하됩니다.
이 문제 때문에, 서버 간 인기글 리스트를 맞춰주는 게 적절하다고 결론지었습니다.

2. **락으로 해결할수 있는가?**
- select for update나 mysql의 네임드락으로 시도해 봤을 때, 결국 서버 1 select -> 좋아요 insert 발생 -> 서버 2 select 순서로 요청이 들어오면 이 문제를 해결해 주지 못한다고 판단하였습니다.

3. **redis 도입**
- redis를 도입해 각 서버에서 여러 번 select 해서 데이터를 일치시키려는 것보다는, 분산락으로 락이 걸려있다면 재시도 하지 않고 1번만 select 하는 게 쉽게 해결하는 방법이라고 판단하였습니다.

- 락을 걸고 해제할 때마다 네트워크 통신 비용이 들지만 5분에 한 번씩만 실행되는 성질이므로 크게 문제 되지 않을 것 같다고 판단했습니다.

- DB가 락을 보장하는 등의 성질이 아닌 개발자가 직접 구현해야 하므로 휴먼에러 발생 지점을 신경 써봐야 합니다.

- 락을 redis로 관리하게 되면서 레디스에 문제가 생긴다면 데이터 손실 등의 위험이 있지만, 인기글의 조회 수의 손실은 크리티컬 하지않기 때문에 천천히 개선해 보고자 했습니다.

## 4. 인기글 조회에 트래픽이 몰려 대량의 update 쿼리가 발생하는 상황 해결하기
### AS-IS 
```java
    @Transactional
    public PostDto postDetail(long postId) {
        //해당 게시글이 존재하는지 검증한다
        Post post = postRepository.findById(postId).orElseThrow(DoesNotExistPostException::new);

        //게시글 조회수를 +1 상승시킨다(우려했던부분)
        post.plusViewCount();

        //게시글 정보를 가져온다 
        ... (추가 쿼리)
    }
```

### 문제 발견
- 위의 게시글 조회 API 코드에서 게시글 검증, 게시글의 조회 수 업데이트, 댓글과 해시태그를 가져오는 쿼리 등 여러 쿼리가 발생하고, 특히나 **매번 update 쿼리가 발생**하여 레코드락이 걸리는 것을 [확인하였음](https://velog.io/@rodlsdyd/%EA%B2%8C%EC%8B%9C%EA%B8%80-%EC%A1%B0%ED%9A%8C%EC%88%98-update-%EC%84%B1%EB%8A%A5-%EA%B0%9C%EC%84%A0%ED%95%B4%EB%B3%B4%EA%B8%B0)

- 가장 트래픽이 많을 API였기 때문에 철저한 테스트가 필요하다고 판단하여 부하 테스트 진행

- P99 레이턴시가 2초 이상인 경우 개선이 필요하고, 3초 이상인 경우 무조건 개선이 필요한 기준으로 잡았습니다.


### 성능 테스트 결과
200vuser / 1sec / 1loop : 3초정도의 레이턴시, 낮은처리량을 보여서 개선이 필요하다고 느꼈던 수치
![image](https://github.com/youngreal/inflearn/assets/59333182/f50a6dc3-81cb-459b-95be-7a5d0259a609)

300vuser / 1sec / 1loop : 5.5초의 레이턴시 + 커넥션풀에 26개의 요청이 대기(무조건 개선해야하는 문제)
![image](https://github.com/youngreal/inflearn/assets/59333182/8f1142aa-7da4-428f-ae74-fb1bdbe9e70b)
![image](https://github.com/youngreal/inflearn/assets/59333182/dd4ba7ed-77de-45cf-bf8e-cc9854d67303)

1000user / 10sec / 1loop : 실제 시나리오와 비슷한 테스트로, 10초동안 1000회의 요청이 발생하는경우 8초의 레이턴시 발생 

![image](https://github.com/youngreal/inflearn/assets/59333182/89fbd4d7-5d65-4c73-9350-8da571729c48)
![image](https://github.com/youngreal/inflearn/assets/59333182/ac8f8c22-dfe2-45f3-bc88-d80617a2be5d)   

대부분의 요청도 커넥션 풀 대기가 발생하는 상황 발견

**문제 발견 후 사고과정**
1. update가 발생하는 트랜잭션의 쿼리를 확인해 보던가 최적화해볼까?
- 해당 트랜잭션에서 발생하는 쿼리의 실행계획에서는 크게 문제가 없었고, 결국 언젠가 기능이 추가된다면 또다시 직면할 문제라고 판단하였습니다.
 
2. update 쿼리 발생 자체를 줄여보는 게 좋을 것 같다.
- redis를 이미 사용 중이기 때문에 어떤 자료구조를 사용할지 결정해야 합니다.

- 조회 수 정합성 맞출 필요도 없기 때문에 정확도를 잃지만 빠른 속도(레디스내 연산속도가 O(1)로 고정)와 적은 메모리를 사용하는 hyperloglog의 존재를 알게 되었고 사용해 볼 수 있겠다고 생각했습니다.
    - 캐시 메모리의 용량을 엄청 적게 사용한다는 점도 메리트로 다가왔습니다.

- redis의 hash를 사용했을때와 성능 비교/측정 후  조회수 정확도가 조금 떨어지는대신 속도가 빠른것을 확인하였습니다.

### TO-BE

 200vuser / 1sec / 1loop
 - **latency 2.9초 -> 1.7초**
 
![image](https://github.com/youngreal/inflearn/assets/59333182/a1287f52-577b-4b24-9581-3fd67b6d7dc8)

1000user / 10sec / 1loop(실제 시나리오와 유사한 테스트)
- **latency 8초 -> 1.8초**

![image](https://github.com/youngreal/inflearn/assets/59333182/adca967a-84b6-4c2a-bccb-b1df44c737b2)

- 처리량은 모든경우 1.5배 향상 

## 5. LIKE %word%로 게시글 검색 시 full table scan이 발생해 레이턴시가 증가하는 문제를 fulltext-search로 개선하기

### 문제 발견
- LIKE %word% 쿼리는 인덱스를 적용할 수 없어서 테이블 풀 스캔으로 검색 결과를 찾아야 하는 문제를 인식하였고, 수치 확인을 위한 테스트를 진행했습니다.
- 검색 결과에 해당하지 않는 게시글 검색 시 테이블의 데이터 수가 50만건일 때 쿼리 응답속도가 3초, 약 200만 건이 넘어가는 순간부터 쿼리 응답속도만 5초 이상이 소요되는 문제 발견했습니다.
![image](https://github.com/youngreal/inflearn/assets/59333182/c0be383a-0bb5-4df9-b196-9c9e008706d7)
    - 현재 페이징으로 20개의 결과만 가져오기 때문에 운 좋게 테이블 전체를 스캔하지 않고 20개의 결과를 먼저 찾는 경우 조금 더 빠를 수 있지만, ``검색어의 해당하는 결과가 없는 최악의 경우`` full-table scan으로 5초까지도 소요됩니다.


**문제 발견후 사고과정** 
- 학습비용, 복잡도를 고려해 mysql의 fulltext-search를 선택하였습니다.
- like %word% 쿼리와 Full-text search 쿼리의 성능 비교 후 , 검색 결과에 따라 성능이 달라지는 것을 발견했습니다.
  - 검색결과가 0건인경우 쿼리응답 속도
    - Like%word% : 6.234 sec
    - full-text search : 0.515 sec
  - 검색결과가 77만건인경우 쿼리응답 속도
    - Like%word% : 1 sec
    - full-text search : 2 sec
  - 검색결과가 100만건이 넘어가는경우 쿼리응답 속도
    - Like%word% : 0.15 sec
    - full-text search : 2.140 sec
   
### 한계, 고려해야할점
- 실제 서비스를 하게 된다면 검색 결과들의 분포가 어떤 특성을 가지게 될지 어려웠습니다.  테이블 크기의 30% 이상의 해당하는 결과를 검색하는 일이 더 잦다면, 오히려 like%word% 방식이 좋을 수도 있습니다.
- 다만, 검색 결과가 0건인 최악의 경우(6초이상)보다는 평균적으로 1~2초내에 검색이 가능한 방식이라는 점에서 좀 더 자연스러운 최선의 방법이라 생각하였습니다.

### TODO
- 선착순 회원가입 이벤트가 생겨 트래픽이 몰린다면 아래와 같은 점들을 고려해야 합니다.
  - 스프링 이벤트 핸들러의 리소스 소모량 확인
      - 이벤트 처리 주체가 스프링이기 때문에 스프링의 리소스가 소요됩니다.
      - 사용 가능한 비용 내에서 서버 scale-out과 nginx의 부하 분산도 고려해 본 후,  성능 테스트를 진행해 보고 예상 트래픽을 받고도 스프링 서버가 멀쩡할 수 있는지 확인해 봐야 합니다.
  - 메시징 시스템 고려
      - 스프링의 이벤트 핸들러는 이벤트를 큐에 저장하고 , 처리하는 과정이 동기로 이뤄지기 때문에 이벤트 등록과 처리를 비동기로 처리할 수 있는 메시징 시스템에 대해 학습해 보고 테스트 후 고려해 볼 수 있을 것 같습니다.
  - 스레드 풀 튜닝 고려(ec2.medium (2 cpu, 4G ram))
     - 스프링 부트에선 별도의 AsyncConfig을 설정하지 않아도 기본적으로 corePoolSize가 8, queueCapacity가 Integer.MAX_VALUE, maxPoolSize가 Integer.MAX_VALUE인  ThreadPoolTastExecutor가 적용됩니다.
       - corePoolSize 튜닝 : I/O 작업인 메일전송위주라서 cpu보다 더 많은 스레드를 이미 할당한 상태입니다.
       - queueCapacity 튜닝 : 비동기 메일전송은 최대 8개까지만 동시처리가 가능하며, 나머지 모든 요청들은 모두 큐에 담깁니다. 이는 큐에 엄청난 요청이 담겨 메모리부족 문제를 야기할것으로 보이기때문에  테스트를 통해 메모리 오류가 발생하지 않을 적절한 큐사이즈로 줄여야합니다.
       - maximumPoolSize 튜닝 : 큐 사이즈를 조절했다면 남은요청은 maximumPoolSize크기만큼 스레드를 계속해서 생성해 나갈것이고, 이렇게되면 메모리 사용률이 급증하는 문제가 생깁니다. 적절한크기로 줄이고, 나머지 요청은 Reject하거나, keepAlive시간을 늘려 대기시간을 상승시키도록 해봐야할것같습니다.

- Gmail에 큰 장애가 발생한 경우, 혹은 gmail 서버로 메일을 보낼 수 없게 되는 경우
  - 이 경우엔 메일 전송에 몰리는 트래픽이 어느 정도인지에 따라 해결 방법이 다를 수 있습니다.
  
    1. 선착순 회원가입 이벤트가 예정되어있는경우
        - 이 경우엔 gmail서버의 장애가 발생했는데도 불구하고 모든 요청이 retry회수를 강제로 채워야만 보조메일서버로 전송되기때문에 retry 트래픽 자체가 문제될수있습니다.     
    2. 외부 메일 서버가 장애 상황이라면, retry 횟수를 채우는 것 자체가 의미 없을 수 있습니다. 불필요한 retry 요청조차 차단해버리도록 할 수 있게끔 구축 해 볼 수도 있습니다. 
    3. 적당한 트래픽이 메일 전송에 몰리는 경우
        - 적절한 retry 전략을 준비시키고 retry 회수가 소진되면 보조 메일 서버를 이용하는 방법을 고려할 수 있습니다.
```java
try {
    sendEmailWithSMTP();
} catch (SMTPException smtpException) {
    log.error("primary 메일서버 전송실패", smtpException);
    
    try {
        sendEmailWithNaver();
    } catch (NaverMailException naverMailException) {
        log.error("secondary 메일서버 전송실패 ", naverMailException);
        // todo 예외 처리
    }
}
```

- 게시글 조회 로직에서  update는 반드시 같은 트랜잭션에 묶일 필요가 없는 성질이므로, 비동기로 처리해 보고 성능 측정해 볼 수 있을 것 같습니다. 
- 분산락 휴먼에러를 최대한 줄이기 위해 AOP 적용하면 좋을 것 같습니다.
- 실제 scale-out을 고려한다면 DB나 redis, 모니터링 도구 들은 분리해야 합니다.
