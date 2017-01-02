package launchpad.mail

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.ui.velocity.VelocityEngineUtils

import javax.mail.internet.MimeMessage;


@Service('mailService')
class MailService {

    @Autowired
    JavaMailSender javaMailSender;

    @Autowired
    VelocityEngine velocityEngine;

    @Async
    boolean send(MailMessage mailMessage) {

        String body = VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, mailMessage.template, "UTF-8", mailMessage.model);

        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
        mimeMessageHelper.setFrom(mailMessage.from);
        mimeMessageHelper.setTo(mailMessage.to);
        mimeMessageHelper.setSubject(mailMessage.subject);
        mimeMessageHelper.setText(body, true);
        javaMailSender.send(mimeMessage);

    }
}
