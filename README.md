## 소개
- 인프런의 질문/스터디 게시판을 기능을 구현하고 **단일 인스턴스에서 최대한** 트래픽을 받아보도록 고민하되, 서버 scale-out 을 고려 했을때도 최대한 문제가 없을수 있는 방법으로 구현해보기

- ``우선순위가 높은 부분``부터 순차적으로 생각해보기
  
- 트래픽 관련 정보는 [국내 커뮤니티 트래픽 정보](https://todaybeststory.com/ranking_monthly.html) 를 참고해 대략적으로 계산하였음

## Architecture
![image](https://github.com/youngreal/inflearn/assets/59333182/ffe17a9e-c1f3-49b4-868d-253e5955ee2a)



## 고민했던 내용들
- [회원가입시 회원 저장과 이메일전송의 강결합 + 응답속도 저하 문제를 어떻게 개선할지?](#1-회원가입시-회원-저장과-이메일전송의-강결합--응답속도-저하-문제를-어떻게-개선할지)
- [Gmail 서비스에 문제가 생긴다면?](#2-Gmail-서비스에-문제가-생긴다면)
- [인기글 리스트 갱신은 어떻게 할것인가?](#3-인기글-리스트-갱신은-어떻게-할것인가)
- [인기글 조회에 트래픽이 엄청나게 몰린다면?](#4-인기글-조회에-트래픽이-엄청나게-몰린다면)
- [게시글 검색에 대한 고민(LIKE %word%)](#5-게시글-검색에-대한-고민LIKE-word)

## 1. 회원가입시 회원 저장과 이메일전송의 강결합 + 응답속도 저하 문제를 어떻게 개선할지?

### 문제 발견

- 메일전송에 실패하는경우 회원가입을 재요청 해야한다는 문제를 최초로 인식하였고 이는 한 트랜잭션에 묶인것이 원인이라고 판단하였음
- 이로인해 회원가입에 대한 응답을 메일전송이 끝나야만 받아볼수 있는 단점도 인식(부가적으로 DB커넥션을 오래 소유하게 되어 커넥션 반납이 늦어질수 있다는 점도 인식)
- 회원은 메일에 대해 알 필요가 없으며 회원가입시 메일전송 이외의 추가적인 이벤트가 발생한다면 AService,BService 등의 추가적인 의존이 생길 여지가 있는데 이를 MemberService가 의존할 대상은 아니라고 판단 

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
  - 메일전송과 회원저장, 두 작업을 비동기적으로 실행하는것이 효율적이라고 판단하였고,  메일전송을 스프링의 @Async를 사용하여 비동기로 처리해야겠다고 판단
    
  - 트랜잭션 분리만 고려했을때는 @Transactional(propagation = Propagation.REQUIRES_NEW)도 고려하였으나, 강한 결합, 회원가입의 확장성(회원가입시 추가 이벤트가 생긴다면?) 등을 고려했을때 Mail이 아닌 다른 모듈에 대한 의존도 생길여지가 있다고 판단하여 스프링의 이벤트 핸들러인 ApplicationEventListenr를 선택
    
  - 당장 응답속도와 메일과 회원의 강결합문제는 ApplicationEventListener와 @Async를 사용하여 해결할수있다고 생각하였으나 추후 문제가 될수있는 여지들을 아래와 같이 살펴보기로 했음 

**ApplicationEventListener를 선택하고 고려해야 했던점**
- 서버간 이벤트를 공유하는가?   
  1. 이벤트 성질 고려
      - 메일전송이라는 이벤트의 성질을 고려해보면, 이벤트를 발행하고 서버간 이벤트를 공유하지않고 독립적으로 처리되는 성질로 인식하였음
      - 메시징 시스템을 학습하여 도입하지 않고 ApplicationEventListener를 사용해도 크게 문제가 되지않을거라고 판단하였음
  
  2. scale-out
      - 각자 서버에서 이벤트를 발행하고 처리하고 공유하지않기 때문에 스프링서버를 scale-out 하더라도 문제되지 않음
  
  3. 스프링 리소스 소요
      - 이벤트 처리 주체가 스프링이기때문에 스프링의 리소스가 소요된다.
      - 회원가입에 특정 선착순 회원가입 이벤트 기능이 추가된다면 그때 성능테스트를 해보고 메시징 시스템등 다른방법을 도입해볼수 있을것같은데 그 전에 nginx같은 웹서버의 부하 분산을 우선적으로 고려해볼것 같다.
      - 해당 단점은 충분히 안고 갈수있는 요소라고 판단하였음

**@Async를 선택하고 고려해야 했던점**
  1. 회원 저장에 실패했는데 메일은 보내지는 경우
      - 두 작업을 비동기로 처리하면서 위와같은 경우가 발생할수있어서 @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT) 사용하여 회원저장에 성공하는경우에만 메일을 보내게끔 해주었다.

  2. 스프링 MVC 예외핸들러로 메일전송시 생길수있는 비동기 동작시 예외를 잡을수 없는 문제
      - AsyncUncaughtExceptionHandler를 정의한후 예외 핸들링
  
  3. Async 스레드풀
      - 아래 한계, 고려해야할점에서 다룬다.

### TO-BE
- MemberService에선 메일전송에 대한 방법을 알필요가 없어졌고, 메일 이벤트가 아닌 추가 이벤트가 발생한다 하더라도 해당 이벤트에 대해서도 MemberService가 알아야할 필요성이 사라지게 되었음
- @Async 메일전송으로 인해 회원가입에 대한 응답속도가 5초 -> 0.7초로 감소하였음 

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

### 한계, 고려해야할점
- 선착순 회원가입 이벤트가 생겨 트래픽이 몰린다면?
  - 스프링 이벤트 핸들러의 리소스 소모량 확인
      - 이벤트 처리 주체가 스프링이기 때문에 스프링의 리소스가 소요된다. 
      - 사용가능한 비용 내에서 서버 scale-out과 nginx의 부하분산도 고려해본 후,  성능테스트를 진행해보고 예상 트래픽을 받고도 스프링 서버가 멀쩡할수있는지 확인해봐야한다 
  - 메시징 시스템 고려
      - 스프링의 이벤트핸들러는 이벤트를를 큐에 저장하고 , 처리하는과정이 동기로 이뤄지기때문에 이벤트 등록과 처리를 비동기로 처리할있는 메시징 시스템에 대해 학습해보고 테스트 후 고려해볼수 있을것같다.  
  - 스레드 풀 튜닝 고려(ec2.medium (2 cpu, 4G ram))
     - 스프링 부트에선 별도의 AsyncConfig을 설정하지않아도 기본적으로 corePoolSize가 8, queueCapacity가 Integer.MAX_VALUE, maxPoolSize가 Integer.MAX_VALUE인  ThreadPoolTastExecutor가 적용된다.
       - corePoolSize 튜닝 : I/O 작업인 메일전송위주라서 cpu보다 더 많은 스레드를 이미 할당한 상태이다.
       - queueCapacity 튜닝 : 비동기 메일전송은 최대 8개까지만 동시처리가 가능하며, 나머지 모든 요청들은 모두 큐에 담긴다. 이는 큐에 엄청난 요청이 담겨 메모리부족 문제를 야기할것으로 보이기때문에  테스트를 통해 메모리 오류가 발생하지 않을 적절한 큐사이즈로 줄여야한다
       - maximumPoolSize 튜닝 : 큐 사이즈를 조절했다면 남은요청은 maximumPoolSize크기만큼 스레드를 계속해서 생성해 나갈것이고, 이렇게되면 메모리 사용률이 급증하는 문제가 생긴다. 적절한크기로 줄이고, 나머지 요청은 Reject하거나, keepAlive시간을 늘려 대기시간을 상승시키도록 해봐야할것같다. 

## 2. Gmail 서비스에 문제가 생긴다면?
### 문제 발견
- 현재 Gmail SMTP 서버를 사용해 메일을 전송하고있는데 확률은 낮겠지만 Gmail에서 장애가 발생하는경우 우리서비스는 메일을 전송해주지못하는 SPOF가 발생할수있다.
- 메일전송 실패시 대응할 여러 정책이 있지만, 사용자에게 재전송 요청을 하는것보단 편의성을 고려하여 사용자는 한번만 요청하도록 하고싶었다. 

### 해결과정
            
  1. **네트워크 일시적 장애(ex. read timeout)인 경우**
     - 외부서비스의 일시적 장애 때문에 유저가 직접 재요청을 보내는것 보다는, 실패한 메일전송 이벤트를 retry해주는게 좋다고 판단 
  
  2. **Gmail에 큰 장애가 발생한경우, 혹은 gmail 서버로 메일을 보낼수 없게되는 경우**
     - 이 경우엔 메일전송에 몰리는 트래픽이 어느정도인지에 따라 해결방법이 다를 수 있음
         1. 적당한 트래픽이 메일전송에 몰리는경우
            - 적절한 retry 전략을 준비시키고 retry 회수가 소진되면 보조 메일서버를 이용하는 방법 고려

         2. 선착순 회원가입 이벤트가 예정되어있는경우(구현 되어있지 않음)
            - 이 경우엔 gmail서버의 장애가 발생했는데도 불구하고 모든 요청이 retry회수를 강제로 채워야만 보조메일서버로 전송되기때문에 retry 트래픽 자체가 문제될수있음
            - 서버 스케일아웃, 로드밸런싱, 서킷브레이커 등의 방법으로 해당상황에 비용이 가장 적은 방법을 고려해 볼 것 같다.
           
  3. **retry를 도입하고 고려했던점**
  - 만약 요청지연인 상황이라면, retry자체가 네트워크에 부담을 더할수있기 때문에 retry전략을 효율적으로 가져가야된다고 판단하였음 
       1. retry의 회수와 간격
          - Exponential Backoff라는 지수에 비례해 retry의 재전송 대기시간을 늘리는 방법(100ms, 200ms, 400ms ...)으로 개선할 수 있었음
          - 다만, 4시 30분이라는 똑같은 시간에 지연요청이 쌓이게 되면 모두 같은 특정시간에 retry가 몰린다는 문제가 있음
          - Jitter 라는 개념을 추가해 각 retry들은 backOff 대기시간 + 랜덤 대기시간을 부여받아 동시에 retry가 몰리는것을 분산,방지 할수있게 되었음
          
### To-BE
- retry의 재 전송 대기시간(1-1-1-1 -> 2-3-7-10로 개선)의 간격을두고 각 간격들도 골고루 분산시켜서 일시적인 네트워크 부담을 줄여줄수 있게 되었음
- 외부 gmail 서비스의 큰 장애 발생시에 secondary 메일서버를 두고,  선착순 이벤트 등과같은 이벤트가 예정되어있더라도 추가로 어떻게 대응할지 고려해볼 수 있음

![image](https://github.com/youngreal/inflearn/assets/59333182/b701c7d2-f7fd-42aa-82bc-c9f0d390f7e8)
2-3-7-10 의 retry 대기시간

![image](https://github.com/youngreal/inflearn/assets/59333182/4a85833e-14ab-497b-af9b-ccfabedd6950)
1-3-5-14 의 retry 대기시간


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

### 한계, 고려해야할점
- 외부 메일서버가 장애 상황이라면, retry횟수를 채우는것 자체가 의미없을수있다. 서킷 브레이커를 도입해 불필요한 retry 요청조차 차단해버리도록 할수있게끔 구축 해볼수도 있다. 

## 3. 인기글 리스트 갱신은 어떻게 할것인가?
### 문제 발견
- likes 테이블과 post테이블은 분리되어있고, likes테이블의 개수 (게시글의 좋아요 개수)순으로 정렬해 스케줄링 서비스로 5분에 한번씩 DB에서 여러 서버가 가져오는 상황
![image](https://github.com/youngreal/inflearn/assets/59333182/7829334f-856c-415e-a436-e0472b603670)

- 서버간 select이 발생하는사이 중간에 likes 테이블에 insert가 발생하면 서버간 서로 다른 인기글 리스트를 갱신 하는 문제 발견

### 해결과정

1. **꼭 서버간 인기글 리스트가 같게 맞춰줘야 하는가?**

- 현재 게시글 조회 API는 GET /posts/{postId} 로, postId만 받아서 게시글을 조회한다.
- 캐시히트에 성공하는 postId인경우 db에 update하지않고, 캐시미스가 나면 db에 update가 발생한다.
   
만약 서버간 인기글 리스트가 아래와 같이 다르다고 가정해보자
서버 1: 1,2,3,4,5 (postId)
서버 2: 2,3,4,5,6 (postId) 

이때, postId가 1인 게시글에 조회가 엄청발생해 서버2로 몰리게된다면, 의도와 다르게 캐시미스가 발생해 성능이 저하된다. 
이 문제 때문에, 서버간 인기글 리스트를 맞춰주는게 적절하다고 결론지었다.

2. **락으로 해결할수 있는가?**
- select for update나 mysql의 네임드락으로 시도해봤을때, 결국 서버1 select -> 좋아요 insert 발생 -> 서버2 select 순서로 요청이 들어오면 이 문제를 해결해주지 못한다고 판단하였다.

3. **redis 도입**
- redis를 도입해 각 서버에서 여러번 select 해서 데이터를 일치시키려는것 보다는, 분산락으로 락이 걸려있다면 재시도 하지않고 1번만 select하는게 쉽게 해결하는 방법이라고 판단하였음

- 락을 걸고 해제할때마다 네트워크 통신비용이 들지만 5분에 한번씩만 실행되는 성질이므로 크게 문제 되지않을것 같다.

- DB가 락을 보장하는등의 성질이 아닌 개발자가 직접 구현해야하므로 휴먼에러 발생지점을 신경써보자

- 락을 redis로 관리하게 되면서 레디스에 문제가 생긴다면 데이터 손실등의 위험이있지만, 인기글의 조회수의 손실은 크리티컬 하지않기 때문에 천천히 개선해보자. 

## 4. 인기글 조회에 트래픽이 엄청나게 몰린다면?
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
- 위의 게시글 조회 API 코드에서 게시글검증, 게시글의 조회수 업데이트, 댓글과 해시태그를 가져오는 쿼리등 여러 쿼리가 발생하고, 특히나 **매번 update쿼리가 발생**하여 레코드락이 걸리는것을 [확인하였음](https://velog.io/@rodlsdyd/%EA%B2%8C%EC%8B%9C%EA%B8%80-%EC%A1%B0%ED%9A%8C%EC%88%98-update-%EC%84%B1%EB%8A%A5-%EA%B0%9C%EC%84%A0%ED%95%B4%EB%B3%B4%EA%B8%B0)

- 가장 트래픽이 많을 API 였기때문에 철저한 테스트가 필요하다고 판단하여 부하테스트 진행

- P99레이턴시가 2초이상인경우 개선이필요하고, 3초이상인경우 무조건 개선이 필요한 기준으로 잡았음 


### 성능 테스트 결과
200vuser / 1sec / 1loop : 3초정도의 레이턴시, 낮은처리량을 보여서 개선이 필요하다고 느꼈던 수치
![image](https://github.com/youngreal/inflearn/assets/59333182/f50a6dc3-81cb-459b-95be-7a5d0259a609)

300vuser / 1sec / 1loop : 5.5초의 레이턴시 + 커넥션풀에 26개의 요청이 대기(무조건 개선해야하는 문제)
![image](https://github.com/youngreal/inflearn/assets/59333182/8f1142aa-7da4-428f-ae74-fb1bdbe9e70b)
![image](https://github.com/youngreal/inflearn/assets/59333182/dd4ba7ed-77de-45cf-bf8e-cc9854d67303)

1000user / 10sec / 1loop : 실제 시나리오와 비슷한 테스트로, 10초동안 1000회의 요청이 발생하는경우 8초의 레이턴시 발생 

![image](https://github.com/youngreal/inflearn/assets/59333182/89fbd4d7-5d65-4c73-9350-8da571729c48)
![image](https://github.com/youngreal/inflearn/assets/59333182/ac8f8c22-dfe2-45f3-bc88-d80617a2be5d)   

대부분의 요청도 커넥션풀 대기가 발생하는상황 

**문제 발견후 사고과정**
1. update가 발생하는 트랜잭션의 쿼리를 확인해보던가 최적화해볼까?
=> 해당 트랜잭션에서 발생하는 쿼리의 실행계획에서는 크게 문제가 없었고, 결국 언젠가 기능이 추가된다면 또다시 직면할 문제라고 판단하였음
2. update 쿼리 자체를 줄여보는게 좋을것 같다.
=> 
 
- redis를 이미 사용중이기때문에 어떤 자료구조를 사용할지 결정해야한다.
- 조회수 정합성 맞출 필요도 없기 때문에 정확도를 잃지만 빠른속도(레디스내 연산속도가 O(1)로 고정)와 적은메모리를 사용하는 hyperloglog의 존재를 알게되었고 사용해 볼수있겠다고 생각했다.
    - 싱글스레드의 redis에서의 연산속도가 O(1)로 고정된다는것은 충분히 메리트가 있었고, 캐시 메모리의 용량을 엄청적게 사용한다는점도 메리트로 다가왔다.
- redis의 hash를 사용했을때와 성능 비교/측정 후  조회수 정확도가 조금 떨어지는대신 속도가 빠른것을 확인하였음

### TO-BE

 200vuser / 1sec / 1loop
 - **latency 2.9초 -> 1.7초**
 
![image](https://github.com/youngreal/inflearn/assets/59333182/a1287f52-577b-4b24-9581-3fd67b6d7dc8)

1000user / 10sec / 1loop(실제 시나리오와 유사한 테스트)
- **latency 8초 -> 1.8초**

![image](https://github.com/youngreal/inflearn/assets/59333182/adca967a-84b6-4c2a-bccb-b1df44c737b2)

- 처리량은 모든경우 1.5배 향상 

## 5. 게시글 검색에 대한 고민(LIKE %word%)

### 문제 발견
- LIKE %word% 쿼리는 인덱스를 적용할 수 없어서 테이블 풀 스캔으로 검색결과를 찾아야하는 문제를 인식하였고, 수치확인을 위한 테스트 진행
- 검색결과에 해당하지않는 게시글 검색시 테이블의 데이터수가 50만건일때 쿼리 응답속도가 3초, 약 200만건이 넘어가는순간부터 쿼리 응답속도만 5초이상이 소요되는문제 발견
![image](https://github.com/youngreal/inflearn/assets/59333182/c0be383a-0bb5-4df9-b196-9c9e008706d7)
    - 현재 페이징으로 20개의 결과만 가져오기때문에 운좋게 테이블 전체를 스캔하지않고 20개의 결과를 먼저 찾는경우 조금더 빠를수있지만, ``검색어의 해당하는 결과가 없는 최악의경우`` full-table scan으로 5초까지도 소요된다. 


**문제 발견후 사고과정** 
- mysql의 fulltext-search 와 검색엔진으로 해결해볼수 있을것같은데 큰 학습비용, 복잡도를 고려해 fulltext-search를 선택하였음
- like %word% 쿼리와 Full-text search 쿼리의 성능비교 후 , 검색 결과에 따라 성능이 달라지는것을 발견
  - 검색결과가 0건인경우 쿼리응답 속도
  ![](https://velog.velcdn.com/images/rodlsdyd/post/ec9ca5c1-bff5-46bc-881c-f421bc5d3351/image.png)
    - Like%word% : 6.234 sec
 ![](https://velog.velcdn.com/images/rodlsdyd/post/2372fd9f-7812-469c-a817-68d6593513d9/image.png)
    - full-text search : 0.515 sec
  - 검색결과가 77만건인경우 쿼리응답 속도
![](https://velog.velcdn.com/images/rodlsdyd/post/87e4aa1d-3e96-4665-aaea-af1deb2c7887/image.png)
    - Like%word% : 1 sec
![](https://velog.velcdn.com/images/rodlsdyd/post/6057f32d-4df1-4924-8f15-47747bfe9601/image.png)
    - full-text search : 2 sec
  - 검색결과가 100만건이 넘어가는경우 쿼리응답 속도
![](https://velog.velcdn.com/images/rodlsdyd/post/7da5b213-a56b-484d-a3c5-8fa1eee59f12/image.png)
    - Like%word% : 0.15 sec
![](https://velog.velcdn.com/images/rodlsdyd/post/4d2c2c2c-70fe-4167-af1f-c4977aa6b4c4/image.png)
    - full-text search : 2.140 sec
   
### 한계, 고려해야할점
- 실제 서비스를 하게된다면 검색결과들의 분포가 어떤 특성을 가지게 될지 어려웠다.  테이블크기의 30% 이상의 해당하는 결과를 검색하는일이 더 잦다면, 오히려 like%word% 방식이 좋을수도 있다.
- 다만, 검색결과가 0건인 최악의 경우(6초이상)보다는 평균적으로 1~2초내에 검색이 가능한 방식이라는점에서 좀더 자연스러운 최선의 방법이라 생각하였음.

### TODO
- 분산락 휴먼에러를 최대한 줄이기 위해 AOP 적용하기
- 실제 scale-out을 고려한다면 DB나 redis, 모니터링도구 들은 분리해야함
