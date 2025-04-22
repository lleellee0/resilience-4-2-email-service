package resilience.emailservice.mail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mail")
public class MailController {

    @Autowired
    private MailSenderService mailSenderService;

    // http://localhost:8081/mail/send 로 요청
    // POST 요청으로 email을 JSON 형식으로 받습니다.
    @PostMapping("/send")
    public String sendMail(@RequestBody EmailRequest emailRequest) {
        mailSenderService.sendEmail(emailRequest.getEmail());
        return "메일 전송 요청이 접수되었습니다.";
    }

}
