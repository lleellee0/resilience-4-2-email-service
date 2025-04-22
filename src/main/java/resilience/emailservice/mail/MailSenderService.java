package resilience.emailservice.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import resilience.emailservice.exception.InvalidEmailException;
import resilience.emailservice.exception.RetryableException;

@Service
public class MailSenderService {

    private static final Logger logger = LoggerFactory.getLogger(MailSenderService.class);

    @Autowired
    private SMTPClient smtpClient;

    @Retryable(
            retryFor = { RetryableException.class }, // 재시도할 예외 유형
            maxAttempts = 3,                         // 최대 재시도 횟수
            backoff = @Backoff(delay = 2000),           // 재시도 간 대기 시간 (밀리초 단위)
//            backoff = @Backoff(delay = 1000, multiplier = 2), // 첫 재시도: 1초, 이후 2배씩 증가 (1초 -> 2초 -> 4초)
            listeners = { "loggingRetryListener" }     // 재시도에 대한 Listener 지정
    )
    public void sendEmail(String email) {
        // 이메일 유효성 검증 (간단한 정규표현식 사용)
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new InvalidEmailException("유효하지 않은 이메일 주소입니다: " + email);
        }

        smtpClient.sendMail(email);
    }

    // 재시도를 모두 실패했을 때 실행되는 복구 메서드
    @Recover
    public void recover(RetryableException ex, String email) {
        logger.error("모든 재시도가 실패했습니다. 이메일: {}에 대해 예외 발생: {}", email, ex.getMessage());
        // 추가적인 복구 로직 수행 가능 (예: 이메일 큐에 재전송 요청 추가, 사용자에게 알림 등)

        // 예외를 다시 던져주지 않으면, 복구가 됐다고 가정하고 200 OK로 응답됩니다.
        throw ex;
    }

    @Recover
    public void recover(RuntimeException ex, String email) {
        throw ex;
    }

}
