## 인프런의 질문/스터디 모집 서비스
- 기능을 먼저 구현하고 실제로 많은 회원이 사용한다면 어떻게 대응할수 있을지 대응방법을 경험해보는것이 목표
- 모든 API에서 트래픽이 몰린다고 가정하는것이 아닌, 현실적으로 우선순위가 높은 부분부터 순차적으로 생각해보기
- [국내 커뮤니티 트래픽] (https://todaybeststory.com/ranking_monthly.html) 를참고하여 2등 커뮤니티의 트래픽 수치를 대략적으로 참고하였음

### Architecture

# 고민했던 내용들
## 회원가입시 회원 저장과 이메일전송의 강결합 + 응답속도 저하 문제를 어떻게 개선할지?

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
  - 회원저장이 메일전송의 응답을 기다리게되어 회원가입에 대한 응답속도가 늦어지는것이 비효율적이라고 판단하여 스프링의 @Async를 사용하여 메일을 비동기로 전송하였음
    
  - 트랜잭션 분리만 고려했을때는 @Transactional(propagation = Propagation.REQUIRES_NEW)도 고려하였으나, 회원은 메일에대해 알고있을 필요가 없다고 생각했고, 회원가입의 확장성(회원가입시 추가 이벤트가 생긴다면?) 등을 고려했을때 Mail이 아닌 다른 모듈에 대한 의존도 생길여지가 있다고 판단하여 스프링의 이벤트 핸들러인 ApplicationEventListenr를 사용하는게 좋겠다고 판단하였음

### TO-BE
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
### 성과
- MemberService에선 메일전송에 대한 방법을 알필요가 없어졌고, 메일 이벤트가 아닌 추가 이벤트가 발생한다 하더라도 해당 이벤트에 대해서도 MemberService가 알아야할 필요성이 사라지게 되었음


**ApplicationEventListener를 선택하고 고려해야 했던점**
- 서버간 이벤트를 공유하는가?   
  1. 이벤트 성질 고려
      - 메일전송이라는 이벤트의 성질을 고려해보면, 이벤트를 발행하고 서버간 이벤트를 공유하지않고 독립적으로 처리되는 성질로 인식하였음
      - 메시징 시스템을 학습하여 도입하지 않고 ApplicationEventListener를 사용해도 크게 문제가 되지않을거라고 판단하였음
  
  2. scale-out
      - 각자 서버에서 이벤트를 발행하고 처리하고 공유하지않기 때문에 스프링서버를 scale-out 하더라도 문제되지 않음

  3. 단지 비용만 아끼고, ``결국에 나중에 누군가 확장해야할 레거시코드가 되진않을지``?
      - 그럴수도 있다. 다만, 회원가입에 트래픽이 엄청나게 몰리는 선착순 이벤트같은게 당장 예정되어있지않다면 그럴 가능성은 낮아보인다.
        
- 트래픽이 몰리면 문제되지 않을까?
  1. 스프링 리소스 소요
      - 이벤트 처리 주체가 스프링이기때문에 스프링의 리소스가 소요된다.
      - 회원가입에 특정 이벤트 기능이 추가된다면 그때 성능테스트를 해보고 메시징 시스템을 도입해볼수 있을것같은데 그 전에 nginx의 부하분산이나 rate limiter를 우선적으로 고려해볼것 같다.
        
  2. 회원가입에 어느 경우에 트래픽이 몰릴수 있는건지?
      - 선착순 회원가입 이벤트같은 이벤트가 생기는게 아닌이상, 회원가입에 트래픽이 몰리는경우는 왠만해선 쉽지않을것이라고 판단했다.  
      - 만약 해당 이벤트가 생긴다면 메시징 시스템 선택을 고려해볼수있다고 생각한다. 그러나, 일반적으로 조회같은 트래픽이 엄청 몰리는구간은 아니라고 생각했기에, 단점을 감안하기로 했다.
      - 충분히 테스트 해볼만하지만 `우선순위가 낮다고 판단`하였다.

## Gmail 서비스에 문제가 생긴다면?
- 현재 Gmail SMTP 서버를 사용해 메일을 전송하고있는데 확률은 낮겠지만 Gmail에서 장애가 발생하는경우 우리서비스는 메일을 전송해주지못하는 SPOF가 발생할수있다.
  1. 네트워크 일시적 장애(ex. read timeout)인 경우
     - 외부서비스의 일시적 장애 때문에 유저가 직접 재요청을 보내는것 보다는, 실패한 메일전송 이벤트를 retry해주는게 좋다고 판단
  
  2. Gmail 서비스 자체에 큰 문제가 생긴다면
     - 현재는 gmail의 일시적인 네트워크 장애에 대비한 retry와 retry 회수소진시 직접 재전송하는 방식으로 구현했습니다.
     - 만약, 고려해야한다면 아래와 같이 여러 메일서버를 두고 사용할것 같습니다.
```java
try {
    sendEmailWithSMTP();
} catch (SMTPException smtpException) {
    log.error("primary 메일서버 전송실패", smtpException);
    
    try {
        sendEmailWithNaver();
    } catch (NaverMailException naverMailException) {
        log.error("secondary 메일서버 전송실패 ", naverMailException);
        // todo 예외 핸들링
    }
}
```
  **retry를 도입하고 고려했던점**
  - 만약 요청지연인 상황이라면, retry자체가 네트워크에 부담을 더할수있기 때문에 retry전략을 효율적으로 가져가야된다고 판단하였음 
       1. retry의 회수와 간격
          - Exponential Backoff라는 지수에 비례해 retry의 재전송 대기시간을 늘리는 방법(100ms, 200ms, 400ms ...)으로 개선할 수 있었음
          - 다만, 4시 30분이라는 똑같은 시간에 지연요청이 쌓이게 되면 모두 같은 특정시간에 retry가 몰린다는 문제가 있음
          - Jitter 라는 개념을 추가해 각 retry들은 backOff 대기시간 + 랜덤 대기시간을 부여받아 동시에 retry가 몰리는것을 분산,방지 할수있게 되었음
          
### To-BE
![image](https://github.com/youngreal/inflearn/assets/59333182/b701c7d2-f7fd-42aa-82bc-c9f0d390f7e8)
2-3-7-10 의 retry 대기시간
![image](https://github.com/youngreal/inflearn/assets/59333182/4a85833e-14ab-497b-af9b-ccfabedd6950)
1-3-5-14 의 retry 대기시간


  **retry가 모두 소진된다면**
  - retry 회수를 모두 소진한 이벤트들이 발생하면 로그를 남기고 후처리를 해줘야한다고 판단하였음 
    - 운영 정책에 따라서 반드시 재전송해줘야하는 이메일이라면, DB나 메세징 시스템을 이용하거나, 스케줄링을 설정해 이벤트를 재처리 예정
    - 현재 프로젝트에서는 단순히 error 로그만 남기고 재처리 로직은 구현하지않았음 
  
### 성과
- 정해진 retry 회수만큼 대기시간이 골고루 분산된 retry를 이용해 외부 gmail 서비스의 지연에 대응할 수 있게 되었음
- 외부 gmail 서비스의 큰 장애 발생시에 secondary 메일서버를 두는 등 어떻게 해볼지 생각해 볼 수 있었음 

## 인기글 조회에 트래픽이 엄청나게 몰린다면?

### TODO

