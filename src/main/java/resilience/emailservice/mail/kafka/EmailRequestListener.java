package resilience.emailservice.mail.kafka;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import resilience.emailservice.mail.EmailRequest; // EmailRequest 클래스 임포트
import resilience.emailservice.mail.MailSenderService; // MailSenderService 클래스 임포트

@Service
public class EmailRequestListener {

    private static final Logger log = LoggerFactory.getLogger(EmailRequestListener.class);

    private final MailSenderService mailSenderService;

    public EmailRequestListener(MailSenderService mailSenderService) {
        this.mailSenderService = mailSenderService;
    }

    @KafkaListener(
            topics = "email-send-requests", // 토픽 이름을 직접 사용하거나 상수로 관리
            groupId = "${spring.kafka.consumer.group-id}", // application.yml의 group-id 사용
            containerFactory = "kafkaListenerContainerFactory" // 기본값 사용 시 생략 가능
    )
    public void consumeEmailRequest(EmailRequest emailRequest) { // Kafka 메시지로부터 EmailRequest 객체를 받음
        log.info("Received EmailRequest via Kafka: To={}, Body={}", emailRequest.getEmail(), emailRequest.getEmailBody());

        if (emailRequest == null || emailRequest.getEmail() == null || emailRequest.getEmail().isEmpty()) {
            log.warn("Received invalid EmailRequest (null or missing email): {}", emailRequest);
            // TODO: 잘못된 메시지 처리 로직 (예: DLQ 전송)
            return;
        }

        try {
            // MailSenderService를 사용하여 이메일 발송 로직 호출
            // MailController와 마찬가지로 email 주소만 전달하는 것으로 가정
            log.info("Requesting email sending via MailSenderService for: {}", emailRequest.getEmail());

            // MailSenderService의 sendEmail 메소드 호출 (이메일 주소 전달)
            // 참고: Kafka 메시지에는 emailBody도 포함되어 있지만, 현재 MailSenderService는 email만 사용하는 것으로 보임
            mailSenderService.sendEmail(emailRequest.getEmail());

            log.info("Successfully requested email sending for: {}", emailRequest.getEmail());

        } catch (Exception e) {
            // MailSenderService 호출 중 발생할 수 있는 예외 처리
            log.error("Error requesting email sending for {}: {}", emailRequest.getEmail(), e.getMessage(), e);
            // TODO: 예외 처리 및 재시도 또는 DLQ 전송 로직
            // throw new RuntimeException("Email processing failed", e); // 필요시 예외를 다시 던져 Kafka 재시도 유발
        }
    }
}