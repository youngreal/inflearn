## 소개
- 인프런의 질문/스터디 게시판을 기능을 먼저 구현하고, 실제로 많은 회원이 사용한다면 어떻게 대응할수 있을지 대응방법을 경험해보기

- 모든 API에서 트래픽이 몰린다고 가정하는것이 아닌, 현실적으로 ``우선순위가 높은 부분부터 순차적으로 생각해보기``
  
- [국내 커뮤니티 트래픽 정보](https://todaybeststory.com/ranking_monthly.html) 를 참고하여 2등 커뮤니티의 트래픽 수치를 대략적으로 참고하였음

## Architecture

## 고민했던 내용들
- [회원가입시 회원 저장과 이메일전송의 강결합 + 응답속도 저하 문제를 어떻게 개선할지?](##-1.-회원가입시-회원-저장과-이메일전송의-강결합-+-응답속도-저하-문제를-어떻게-개선할지?)
- [Gmail 서비스에 문제가 생긴다면?](#Gmail-서비스에-문제가-생긴다면?)
- [인기글 리스트 갱신은 어떻게 할것인가?](#인기글-리스트-갱신은-어떻게-할것인가?)
- [인기글 조회에 트래픽이 엄청나게 몰린다면?](#인기글-조회에-트래픽이-엄청나게-몰린다면?)
- [게시글 검색에 대한 고민(LIKE %word%)](#게시글-검색에-대한-고민(LIKE-%word%))

## 1. 회원가입시 회원 저장과 이메일전송의 강결합 + 응답속도 저하 문제를 어떻게 개선할지?

**문제 발견**
- 회원가입과 메일전송이 한 트랜잭션에 묶여 회원가입에 대한 응답을 메일전송이 끝나야만 받아볼수 있음(대략 5초 소요)
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
      - 회원가입에 특정 선착순 회원가입 이벤트 기능이 추가된다면 그때 성능테스트를 해보고 메시징 시스템등 다른방법을 도입해볼수 있을것같은데 그 전에 nginx의 부하분산이나 rate limiter를 우선적으로 고려해볼것 같다.
      - 해당 단점은 충분히 안고 갈수있는 요소라고 판단하였음

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
      - 스프링의 이벤트핸들러와 달리 메시지를 큐에 저장하고 , 처리하는과정이 비동기로 이뤄지기때문에 테스트 후 고려해볼수 있을것같다.  
  - 스레드 풀 튜닝 고려(ec2.medium (2 cpu, 4G ram))
     - 스프링 부트에선 별도의 AsyncConfig을 설정하지않아도 기본적으로 corePoolSize가 8, queueCapacity가 Integer.MAX_VALUE, maxPoolSize가 Integer.MAX_VALUE인  ThreadPoolTastExecutor가 적용된다.
       - corePoolSize 튜닝 : I/O 작업인 메일전송위주라서 cpu보다 더 많은 스레드를 이미 할당한 상태이다.
       - queueCapacity 튜닝 : 비동기 메일전송은 최대 8개까지만 동시처리가 가능하며, 나머지 모든 요청들은 모두 큐에 담긴다. 이는 큐에 엄청난 요청이 담겨 메모리부족 문제를 야기할것으로 보이기때문에  테스트를 통해 메모리 오류가 발생하지 않을 적절한 큐사이즈로 줄여야한다
       - maximumPoolSize 튜닝 : 큐 사이즈를 조절했다면 남은요청은 maximumPoolSize크기만큼 스레드를 계속해서 생성해 나갈것이고, 이렇게되면 CPU사용률이 급증하는 문제가 생긴다. 적절한 CPU사용률을 유지할정도의 크기로 줄이고, 나머지 요청은 Reject하거나, keepAlive시간을 늘려 대기시간을 상승시키도록 해봐야할것같다. 

## 2. Gmail 서비스에 문제가 생긴다면?
- 현재 Gmail SMTP 서버를 사용해 메일을 전송하고있는데 확률은 낮겠지만 Gmail에서 장애가 발생하는경우 우리서비스는 메일을 전송해주지못하는 SPOF가 발생할수있다.
- 메일전송 실패시 대응할 여러 정책이 있지만, 사용자에게 재전송 요청을 하는것보단 편의성을 고려하여 사용자는 한번만 요청하도록 하고싶었다. 
            
  1. **네트워크 일시적 장애(ex. read timeout)인 경우**
     - 외부서비스의 일시적 장애 때문에 유저가 직접 재요청을 보내는것 보다는, 실패한 메일전송 이벤트를 retry해주는게 좋다고 판단 
  
  2. **Gmail에 큰 장애가 발생한경우, 혹은 gmail 서버로 메일을 보낼수 없게되는 경우**
     - 이 경우엔 메일전송에 몰리는 트래픽이 어느정도인지에 따라 해결방법이 다를 수 있음
         1. 적당한 트래픽이 메일전송에 몰리는경우
            - 적절한 retry 전략을 준비시키고 retry 회수가 소진되면 보조 메일서버를 이용하는 방법 고려

         2. 선착순 회원가입 이벤트가 예정되어있는경우(구현 되어있지 않음)
            - 이 경우엔 gmail서버의 장애가 발생했는데도 불구하고 모든 요청이 retry회수를 강제로 채워야만 보조메일서버로 전송되기때문에 retry 트래픽 자체가 문제될수있음
            - 서버 스케일아웃, 로드밸런싱, 서킷브레이커 등의 방법으로 해당상황에 비용이 가장 적은 방법을 고려해 볼 것 같다.
           
  **retry를 도입하고 고려했던점**
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

### 추가로 도전 하면 좋을 부분
- 서킷 브레이커를 도입해 불필요한 retry 요청조차 차단해버리도록 할수있게끔 구축 해보기

## 3. 인기글 리스트 갱신은 어떻게 할것인가?
- 특정 기준(1주일 내에 좋아요가 가장 많은 5개의 게시물)에 충족되는 게시글을 DB에서 가져와야한다 

**redis와 분산락**
- 서버 스케일아웃을 고려한다면, redis 도입이 먼저 떠오르는데 redis없이 해결할수도 있지 않을까?
    - 조회수에 대한 정확도가 요구되지않기때문에 각 서버에서 받은 조회트래픽만큼의 조회수를 카운팅해서 DB에 반영하면 가능할지도 모른다
    - 하지만 각 서버에서 동일한 인기글 리스트를 갱신한다는걸 보장해야하기때문에 위 방법은 불가능하다
      - 인기글 리스트는 무조건 모든서버가 일치해야하므로 스케줄링 서비스에 redis + 분산락을 도입해 모든서버의 인기글리스트를 맞춰야 할것같다고 생각했다.

## 4. 인기글 조회에 트래픽이 엄청나게 몰린다면?
### AS-IS 
```java
    @Transactional
    public PostDto postDetail(long postId) {
        //해당 게시글이 존재하는지 검증한다
        Post post = postRepository.findById(postId).orElseThrow(DoesNotExistPostException::new);

        //게시글 조회수를 +1 상승시킨다
        post.plusViewCount();

        //게시글 정보를 가져온다 
        PostDto postDetail = postRepository.postDetail(postId);
        postDetail.inputHashtags(postRepository.postHashtagsBy(postDetail));
        postDetail.inputComments(postRepository.commentsBy(postDetail));
        return postDetail;
    }
```
- 위의 게시글 조회 API 코드에서 게시글검증, 게시글의 조회수 업데이트, 댓글과 해시태그를 가져오는 쿼리등 여러 쿼리가 발생하고, 특히나 매번 update쿼리가 발생하는것이 문제가 될수있다고 판단

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
- 조회수를 매번 update해서 DB에 바로 반영할게 아니라 캐싱후 카운팅하고 주기적으로 반영하면 되지않을까?
  
  - 서버가 1대라면 인메모리캐시를 이용해 조회수를 카운팅하고 주기적으로 반영해보자
     - 이 방식은 조회수 정합성이 바로바로 보장이 안되는 단점이 있지만 해당 효구사항엔 감안할수있는 단점이고 비용을 최소화 할수있다.
     - 그러나, update 쿼리 발생으로 인한 성능저하 우려가 있다.
     - 국내 커뮤니티 트래픽 조사결과 초당 500~1500개의 이상의 요청이 들어오는경우가 허다했기 때문에 단일서버로 처리하기엔 무리일 확률이 높기때문이다. 
   
  - 서버가 N대인 상황을 고려하자
     - 조회수 정합성 맞출 필요도 없기 때문에 정확도를 잃지만 빠른속도(레디스내 연산속도가 O(1)로 고정)와 적은메모리를 사용하는 hyperloglog의 존재를 알게되었고 사용해 볼수있겠다고 생각했다.
        - 싱글스레드의 redis에서의 연산속도가 O(1)로 고정된다는것은 충분히 메리트가 있었고, 캐시 메모리의 용량을 엄청적게 사용한다는점도 메리트로 다가왔다.


### TO-BE

 200vuser / 1sec / 1loop
 - **latency 2.9초 -> 0.9초**
 
![image](https://github.com/youngreal/inflearn/assets/59333182/c31231dd-6cd3-4fb7-bf70-b3b5d483c2a0)


300vuser / 1sec / 1loop
- **latency 5.5초 -> 2.3초**

![image](https://github.com/youngreal/inflearn/assets/59333182/eee1e0fe-e8f9-4077-9c71-74eb41bcbbbd)


1000user / 10sec / 1loop(실제 시나리오와 유사한 테스트)
- **latency 8초 -> 0.6초**

![image](https://github.com/youngreal/inflearn/assets/59333182/56dad0d9-75b5-4736-95d1-a719bca58dd0)

- 처리량은 모든경우 2배 향상 

### 추가로 고려 하면 좋을 부분
- redis 자체가 SPOF가 될 여지가있으며, redis에서 지원하는 클러스터링 방법에 대한 학습 필요

## 5. 게시글 검색에 대한 고민(LIKE %word%)
- LIKE %word% 쿼리는 인덱스를 적용할 수 없어서 테이블 풀 스캔으로 검색결과를 찾아야하는 문제 발견
- 게시글 테이블의 데이터수가 50만건부터 쿼리 응답속도가 2초, 약 200만건이 넘어가는순간부터 쿼리 응답속도만 3~5초가 소요되는문제 발견
![image](https://github.com/youngreal/inflearn/assets/59333182/2c384a83-d4a9-4591-a4cb-51fe76c868da)
    - 현재 페이징으로 20개의 결과만 가져오기때문에 운좋게 테이블 전체를 스캔하지않고 20개의 결과를 먼저 찾는경우 조금더 빠를수있지만, 검색어의 해당하는 결과가 없는 최악의경우 full-table scan으로 5초까지도 소요된다. 

**문제 발견후 사고과정** 
- mysql의 fulltext-search 와 검색엔진으로 해결해볼수 있을것같은데 큰 학습비용, 관리비용, 복잡도를 고려해 fulltext-search를 선택하였음

### TODO

