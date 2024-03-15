## 소개
- 인프런 교육 플랫폼의 질문/스터디 모집 서비스를 모토로 한 개인프로젝트 입니다.
- 부족하지만 **생각해 볼 수 있는 범위 내에서** **잠재적인 문제가 생길수 있는 상황을 만들어보고 해결해보는 능력을 기르기위한 챌린지**로, 엔지니어링 능력을 향상시키는 경험을 해보는게 목적이었습니다.
- 문제 해결에 추가적인 비용(아키텍처 추가, 학습비용 등)이 드는 방법들은 최후의 방법으로 두는 방식으로 진행했습니다.

## 개발 기간
- 2023.08 ~ 2024.01

## 기술 스택
- Application : Java 17, Springboot 3, JPA, Querydsl
- DB : MySQL
- Test : JUnit5, Mockito
- Infra : AWS LoadBalancer, AWS AutoScaling, Docker, Grafana, Prometheus, Github Actions

## Architecture
![image](https://github.com/youngreal/inflearn/assets/59333182/7959ee11-890d-4133-8ecf-850aa081a371)


## 문제 해결
- [ApplicationEventListener와 @Async로 회원가입 시 회원 저장과 이메일 전송의 강결합 + 레이턴시 증가 문제 개선하기](#1-applicationeventlistener와-async로-회원가입-시-회원-저장과-이메일-전송의-강결합--레이턴시-증가-문제-개선하기)
- [외부 서비스(Gmail)의 지연과 장애를 대비한 retry전략과 recover작성](#2-외부-서비스gmail의-지연과-장애를-대비한-retry전략과-recover작성)
- [shedLock으로 서버 간 동일한 인기글 리스트 갱신을 보장하기](#3-shedlock으로-서버-간-동일한-인기글-리스트-갱신을-보장하기)
- [인기글 조회에 트래픽이 몰려 대량의 update 쿼리가 발생하는 상황 해결하기](#4-인기글-조회에-트래픽이-몰려-대량의-update-쿼리가-발생하는-상황-해결하기)
- [LIKE %word%로 게시글 검색 시 full table scan이 발생해 레이턴시가 증가하는 문제를 fulltext-search로 개선하기](#5-like-word로-게시글-검색-시-full-table-scan이-발생해-레이턴시가-증가하는-문제를-fulltext-search로-개선하기)

## 1. ApplicationEventListener와 @Async로 회원가입 시 회원 저장과 이메일 전송의 강결합 + 레이턴시 증가 문제 개선하기

### 문제 발견
- 회원가입에 대한 응답을 메일 전송이 끝나야만 받을 수 있는 문제가 있습니다. 또한 메일전송에 실패하는경우 회원가입을 재요청해야합니다.
- 회원가입 시 메일 전송 이외의 추가적인 이벤트가 발생한다면 AService, BService 등의 추가적인 의존이 생길 여지가 있는데 이를 MemberService가 의존할 대상은 아니라고 판단했습니다.

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

### 당시 요구사항
- 회원 저장에 성공하는 경우에만 메일 전송해야한다
- 메일은 최대한 가입 후 빨리 받아봐야한다

### 해결 과정
**우선 순위**가 더 높은문제를 먼저 해결했습니다. 결합을 줄이는 문제도 중요하지만, API응답이 늦어지는 문제가 더 중요하다고 생각했습니다.

**회원가입 API응답 자체가 늦어지는 문제**    
- 메일전송 자체가 늦어서 응답이 늦는거라면 즉시 메일을 보내지않고 요청이 들어왔다는 의미를 DB에 저장해두고 스케줄링 서비스로 차례로 메일을 보내주는 방법도 있으나, 인프런 서비스 상 회원가입후 메일인증을 하지않으면 서비스 이용에 제한이 있기때문에 이 방법은 회원가입후 최대한 빨리 메일을 받아야하는 요구사항을 충족시키지 못했습니다.
- 메일 전송을 비동기로 보내는 방법을 고려했습니다. 이중 스레드 풀, 작업을 등록하는 코드를 서비스코드에 침투시키지 않고 편하게 관리해 준다는점에서 @Async를 선택했습니다.

**회원과 메일의 결합을 줄이기위한 사고과정**
  - 메일전송 이벤트를 생각해 봤을때, 서버간 이벤트를 공유할일이 없으므로 메시징 시스템같은 아키텍처를 추가하는것 보단 비용이 적은 스프링 이벤트 핸들러, AOP를 고려하게 되었습니다.
    - ex) 주문 이벤트 발생시 결제를 해야하는 상황에 서버1이 주문 이벤트를 발행하면 서버2가 이벤트를 확인하고 결제를 진행하는 등의 시나리오가 아닌, 각 서버가 메일전송 이벤트를 발행하고 해당서버에서 메일을 전송합니다. 
```java
// AOP 방식 + ThreadLocal
@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    ...
    ...

	@Transactional
	@AfterCommit(task = "sendMailWithAsync") // 해당 메서드 종료후 Aspect 실행
	public void signUp(Member member) {
        ...
		...
		memberRepository.save(member); //회원 저장
		MemberThreadLocal.set(member);
	}

@Component
@Aspect
public class MailSendingAspect {

	private final MailService gmailService;

	public MailSendingAspect(@Qualifier("gmailService")MailService gmailService) {
		this.gmailService = gmailServiceAOP;
	}

	@AfterReturning(pointcut = "@annotation(org.springframework.transaction.annotation.Transactional)")
	public void sendMail(JoinPoint joinPoint) {
		if (joinPoint.getSignature().getName().equals("signUp")) {
			try {
				Member member = MemberThreadLocal.get();
				gmailService.send(new MailSentEvent(member).getMessage());
			} finally {
				MemberThreadLocal.clear();
			}
		}
	}
}
```
  - AOP방식은 회원을 Aspect로 전달하기위해 ThreadLocal을 직접 사용해야했습니다. 개발자가 직접 사용이 끝난 정보를 제거해야하는 리스크를 감수해야한다는점이 찝찝했고 휴먼오류 발생지점을 만들 필요는 없다고 판단했고, 또한 트랜잭션이 커밋시에는 정상동작하지만 롤백되는경우에도 ThreadLocal에 데이터가 저장되는 문제가 있었습니다. 최종적으로 Spring이벤트를 선택했습니다.
    

**ApplicationEventListener를 선택하고 고려해야 했던점**
  
  1. scale-out
      - 각자 서버에서 이벤트를 발행하고 처리하고 서버간 공유하지 않기 때문에 스프링 서버를 scale-out 하더라도 문제 되지 않는다고 판단했습니다. 

  2. 회원 저장에 실패했는데 메일은 보내지는 경우
      - 두 작업을 @Async로 처리하면서 위와 같은 경우가 발생할 수 있어서 **@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)** 을 사용하여 회원 저장에 성공하는 경우에만 메일을 보내게끔하는 요구사항을 충족했습니다.

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

**@Async를 선택하고 고려해야 했던점**
  1. Async 스레드풀 설정(ec2.medium (2 cpu, 4G ram))
     - 별도 설정하지 않았습니다.  스프링 부트에선 별도의 AsyncConfig을 설정하지 않아도 기본적으로 corePoolSize가 8, queueCapacity가 Integer.MAX_VALUE, maxPoolSize가 Integer.MAX_VALUE인  ThreadPoolTastExecutor가 적용되기 때문입니다.
       - corePoolSize 튜닝 : I/O 작업인 메일전송위주라서 cpu보다 더 많은 스레드를 이미 할당한 상태입니다.
       - queueCapacity 튜닝 : 비동기 메일전송은 최대 8개까지만 동시처리가 가능하며, 나머지 모든 요청들은 모두 큐에 담깁니다. 만약 회원가입 이벤트같은 엄청난 요청이 담겨 메모리부족 문제를 야기할 정도가 된다면,  테스트를 통해 메모리 오류가 발생하지 않을 적절한 큐사이즈로 줄여야합니다.
       - maximumPoolSize 튜닝 : 큐 사이즈를 조절했다면 남은요청은 maximumPoolSize크기만큼 스레드를 계속해서 생성해 나갈것이고, 이렇게되면 메모리 사용률이 급증하는 문제가 생깁니다. 적절한크기로 줄이고, 나머지 요청은 Reject하거나, keepAlive시간을 늘려 대기시간을 상승시키도록 해봐야할것같습니다.

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

### 잠재적 문제 & 한계
- 회원가입 이벤트라도 생긴다면 Async스레드풀의 큐사이즈와 메모리 사용량을 조절해가며 반드시 조율해야합니다. 
 

## 2. 외부 서비스(Gmail)의 지연과 장애를 대비한 retry전략과 recover작성
### 문제 발견
- 현재 Gmail SMTP 서버를 사용해 메일을 전송하고 있는데 확률은 낮겠지만 Gmail에서 지연이나 장애가 발생하는 경우 우리 서비스는 메일을 전송해 주지 못하는 문제가 발생할 수 있습니다.
- 메일 전송 실패 시 대응할 여러 정책이 있지만, 사용자에게 재전송 요청을 하는 것보단 편의성을 고려하여 사용자는 한 번만 요청하도록 하고 싶었습니다.

### 요구사항
- 사용자의 회원가입 요청 단한번만으로 메일서버에 문제가 생기더라도 정상응답을 받아야한다. 정말 특별한 경우가 아니라면 회원가입 재전송요청을 하는일이 없도록하자. 

### 해결과정
- 메일 전송은 SMTP 프로토콜로 구현되어있었고, retry나 보조 메일서버를 추가로 두는 방법을 고려했습니다. SMTP 프로토콜의 일일 메일 전송량이 500개로 한정되어있기때문에(개인 계정 기준) 메일서버에 문제 생길시 바로 보조메일 서버로 요청을 보내는 방법보다는, 일시적인 지연같이 retry로 해결이 가능한경우에는 실패한 메일 전송 이벤트를 retry 해주는 게 좋다고 판단하였습니다.
    
**retry를 도입하고 고려했던점**
  - 우리 서버에서 try-catch로 직접 Retry 해줄 수도있지만, 이는 Retry의 재전송 텀과 횟수를 지정할 수 없는 문제가 있었습니다. 
  - 만약 요청 지연인 상황이라면, retry 자체가 네트워크에 부담을 더할 수 있기 때문에 retry 전략을 효율적으로 가져가야 된다고 판단했습니다.
       1. retry의 회수와 간격
          - Exponential Backoff라는 지수에 비례해 retry의 재전송 대기시간을 늘리는 방법(100ms, 200ms, 400ms ...)으로 개선할 수 있었습니다.
          - 다만, 4시 30분이라는 똑같은 시간에 지연 요청이 쌓이게 되면 모두 같은 특정 시간에 retry가 몰린다는 문제가 있습니다.
          - Jitter라는 개념을 추가해 각 retry들은 backOff 대기시간 + 랜덤 대기시간을 부여받아 동시에 retry가 몰리는 것을 분산, 방지 할수있게 되었습니다.
          
### To-BE
- retry의 재 전송 대기시간(1-1-1-1 -> 1-3-5-14로 개선)의 간격을 두고 각 간격들도 골고루 분산시켜서 일시적인 네트워크 부담을 줄여줄 수 있게 되었습니다.
![image](https://github.com/youngreal/inflearn/assets/59333182/b701c7d2-f7fd-42aa-82bc-c9f0d390f7e8)
1-3-5-14 의 retry 대기시간   
![image](https://github.com/youngreal/inflearn/assets/59333182/4a85833e-14ab-497b-af9b-ccfabedd6950)
2-2-8-14 의 retry 대기시간

- retry를 모두 소진하고도 실패한 메일전송은 보조 메일전송서버로 전송되어 사용자는 한번의 요청으로 최대한 손실되지않고 메일을 받을 수 있게 되었습니다.
```java
@RequiredArgsConstructor
@Service
public class MailSentEventHandler {

    private final GMailService gmailService;
    private final NaverMailService naverMailService;

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
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(MailSentEvent event) {
        gmailService.send(event.getMessage());
    }

    @Recover
    public void recoverMailSend(MailSentEvent event) {
        log.warn("recover start : retry 소진후 메일 전송실패");
        naverMailService.send(event.getMessage());
    }

    ...
}
```

### 잠재적 문제 & 한계
- 큰 장애가 나서 당분간 복구가 안되는 상황이라면, retry자체가 낭비가 될수있으며 쓸데없는 응답시간이 길어지는 상황이 발생할수있습니다. 이 경우를 고려해야한다면 서킷브레이커나 fallbacak이라는 키워드를 학습해서 해결해 볼수 있습니다.
- Jitter라는 retry의 랜덤 재전송값을 부여하여 순서가 보장되질 않습니다. 예를들어 요청1,2가 각각 4시 30분 20, 25초에 메일전송 요청이 들어오고, 요청에 실패해 4시 30분 40초, 4시 30분 37초에 retry될수 있습니다. 

## 3. shedLock으로 서버 간 동일한 인기글 리스트 갱신을 보장하기

**당시 상황**

![image](https://github.com/youngreal/inflearn/assets/59333182/513b5263-5b07-4373-941e-a074cf28edea)   
- post 테이블과 likes 테이블은 1:M관계로 분리된 상태입니다.

```java
    @Scheduled(fixedDelay = 5 * MINUTE)
    public void updatePopularPosts() {
            postQueryService.updatePopularPosts();
    }
```
```sql
select
...
(select count(likes.id) from likes l where p.id=l.post_id),
...
from post p
where .. 
```
- 5분에 한번씩 게시글의 좋아요 개수순으로 정렬해 likes 스케줄링 서비스로 5분에 한 번씩 DB에서 여러 서버가 가져오는 상황에서 likes 테이블과 post테이블을 조인한 로우의 개수를 계산하는 상황입니다.

### 문제 발견
![image](https://github.com/youngreal/inflearn/assets/59333182/7829334f-856c-415e-a436-e0472b603670)

- 서버 간 select이 발생하는 사이 중간에 likes 테이블에 insert가 발생하면 **서버 간 서로 다른 인기글 리스트를 select**하는 문제를 발견했습니다.
- 이는 여러 서버에서 @Scheduled로 설정된 스케줄러가 여러번 실행되서 생기는 문제입니다. 

### 해결과정

1. **꼭 서버 간 인기글 리스트가 같게 맞춰줘야 하는가?**
![image](https://github.com/youngreal/inflearn/assets/59333182/c92e240a-304d-493b-b5ec-9634411d07b3)


- 현재 게시글 조회 API는 GET /posts/{postId} 로, postId만 받아서 게시글을 조회합니다.
- 캐시 히트에 성공하는 postId인 경우 db에 update 하지않고 캐시나 메모리에서 업데이트하며, 캐시 미스가 나면 db에 update쿼리가 발생합니다.
```java
    @Transactional
    public PostDto postDetail(long postId) {
        // 게시글 존재여부 검증
        Post post = postRepository.findById(postId).orElseThrow(DoesNotExistPostException::new);

        // 조회수 업데이트
        addViewCount(post);

        // 게시글 상세 내용 조회(해시태그, 댓글)
	...
    }

    private void addViewCount(Post post) {
        // 인기글이아니라면 조회수 +1 업데이트, 캐시메모리에 있다면 조회수 카운팅
    }
```
   
만약 서버 간 인기글 리스트가 아래와 같이 다르다고 가정해 보겠습니다.
서버 1의 인기글 리스트: 1,2,3,4,5 (postId)
서버 2의 인기글 리스트: 2,3,4,5,6 (postId) 

이때, postId가 1인 게시글에 조회가 엄청 발생해 서버 2로 몰리게 된다면, 의도와 다르게 캐시 미스가 발생해 성능이 저하됩니다.
이 문제 때문에, 서버 간 인기글 리스트를 맞춰주는 게 적절하다고 결론지었습니다.

2. **락으로 해결할수 있는가?**
- select for update나 mysql의 네임드락으로 시도해 봤을 때, 결국 서버 1 select -> 좋아요 insert 발생 -> 서버 2 select 순서로 요청이 들어오면 이 문제를 해결해 주지 못한다고 판단하였습니다.

3. **분산 잠금 도입**
- 각 서버에서 여러 번 select 해서 데이터를 일치시키려는 것보다는, 락을걸고 1번만 select 하는 게 쉽게 해결하는 방법이라고 판단하였습니다. 이를해결하기위해 Redis의 분산락이나 분산환경에서 스케줄러에 락을걸수있는 Quartz나 ScheLock을 고려했습니다. 모든방법 전부 추가적인 관리포인트가 생기지만, Redis에 비해 비용이저렴한 ScheLock을 선택했습니다.

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
- 커넥션풀 대기가 발생한다면 커넥션 반납이 늦어지는걸 줄여볼수있다고 생각했고 그 방법들로는 트랜잭션이 너무 길지않은지, DB쿼리 횟수나 쿼리응답시간을 최적화해보기 였습니다.
  
- 우선 X락으로 인해 여러 조회요청이 몰리는경우 앞의 트랜잭션이 커밋이나 롤백되기전까지 일시적으로 대기하기때문에 인메모리나 캐시에서 카운팅하고 주기적으로 반영해보고, 조회마다 발생하는 좋아요와 댓글수에 대한 count쿼리를 매번 발생시키기보다는, 
데이터의 실시간성은 포기하고 메모리에 update해두고 count쿼리 자체를 줄여보는 방법을 선택했습니다.

- 조회수 요청마다 이벤트발생 + 비동기 방식으로 처리해주는 방법도 결국 update쿼리가 발생하는것 자체가 문제이기때문에 고려하지않았습니다.
   

### TO-BE
- API응답속도가 3초미만인 구간기준으로 최대 처리량이 3배가량 상승했습니다
  - tps200 -> 600, 실제 서비스 트래픽 시나리오에서 2배 상승(10초동안 2000명의 요청)
  - 600vuser / 1sec / 1loop (3초미만의 응답속도를 보이는 최대구간)
![image](https://github.com/youngreal/inflearn/assets/59333182/eb427dd4-a72a-4180-a5bf-b8c8641908de)

1000user / 10sec / 1loop
- **latency 8초 -> 0.5초**
![image](https://github.com/youngreal/inflearn/assets/59333182/1486292a-6e30-4bba-a6d0-03812ea620b6)

2000vuser / 10sec / 1loop (3초미만의 응답속도를 보이는 최대구간)
![image](https://github.com/youngreal/inflearn/assets/59333182/99dd1c54-9cc1-433d-ac74-bbd030fa95a5)

![image](https://github.com/youngreal/inflearn/assets/59333182/471695a2-5aaf-47ed-9123-dc2ee347cfad)
![image](https://github.com/youngreal/inflearn/assets/59333182/0d70cc47-4e90-4324-96d6-24109904e583)

### 잠재적 문제 & 한계
- 특정 서버에서 장애발생시 해당 서버에서 카운팅된 조회수가 손실될수 있습니다. 조회수는 어느정도 손실되도 괜찮은 성질이므로 감당 가능합니다.
- redis를 도입해서 인기글테이블의 존재대신, redis에 캐싱하게되면 현재 커넥션 풀 대기상황을 좀더 완화하여 **더 많은 트래픽을 처리할 수** 있습니다. 하지만 서버를 scale-out해서 더 많은 처리량을 얻어낼수도있습니다. 스프링서버 scale-out을 먼저할지 redis를 먼저 도입해볼지 결정하기가 선뜻 어려웠습니다. 

## 5. LIKE %word%로 게시글 검색 시 full table scan이 발생해 레이턴시가 증가하는 문제를 fulltext-search로 개선하기

### 문제 발견
- like %word% 검색 사용중 대부분은 limit으로인해 전체 테이블을 스캔하는 일이 거의  없었지만, 검색결과에 해당하는 로우가 하나도 없는 검색어 입력시 전체 테이블을 스캔하는 문제가 있었습니다. 
- 검색 결과에 해당하지 않는 게시글 검색 시 테이블의 데이터 수가 50만건일 때 쿼리 응답속도가 3초, 약 200만 건이 넘어가는 순간부터 쿼리 응답속도만 5초 이상이 소요되는 문제 발견했습니다.
    - 현재 페이징으로 20개의 결과만 가져오기 때문에 운 좋게 테이블 전체를 스캔하지 않고 20개의 결과를 먼저 찾는 경우 조금 더 빠를 수 있지만, ``검색어의 해당하는 결과가 없는 최악의 경우`` full-table scan으로 5초까지도 소요됩니다.


**문제 발견후 사고과정** 
- 서비스 특성상 “자바”, “스프링” 같은 키워드에대한 검색결과가 많을것으로 예상되는데, 이들을 인기검색결과 테이블을 따로 분리하고, like 쿼리를쓰는 방법을 고려하였으나 실제 서비스 하지 않았기 때문에 수치가 어느정도 되는지 알 수 없는 한계가 있었습니다.
- 위의 방법이 불가능하다고 가정하면 해결방법은 검색엔진과 fulltext-search가 있었는데 학습비용, 복잡도를 고려해 mysql의 fulltext-search를 선택하였습니다.
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
   
### 잠재적 문제 & 한계
- 실제 서비스를 하게 된다면 검색 결과들의 분포가 어떤 특성을 가지게 될지 어려웠습니다.  테이블 크기의 30% 이상의 해당하는 결과를 검색하는 일이 더 잦다면, 오히려 like%word% 방식이 좋을 수도 있습니다.
  - 다만, **검색 결과가 0건인 최악의 경우(6초이상)** 보다는 평균적으로 1~2초내에 검색이 가능한 방식이라는 점에서 좀 더 자연스러운 최선의 방법이라 생각하였습니다.
- 테이블 데이터 200만건 기준 검색결과에 해당하는 로우의 비율이 50% 넘어가는 시점부터 like %word% 의 table full scan보다 느린 분기점이 발생합니다.  
