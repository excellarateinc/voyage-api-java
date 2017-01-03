package launchpad.mail

import freemarker.template.Configuration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import javax.mail.internet.MimeMessage;


@Service('mailService')
class MailService {

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    Configuration freeMarkerConfig;

    @Async
    boolean send(MailMessage mailMessage) {
        //TODO: support attachments and override to address, default from address
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
        mimeMessageHelper.setFrom(mailMessage.from);
        mimeMessageHelper.setTo(mailMessage.to);
        mimeMessageHelper.setSubject(mailMessage.subject);
        if (mailMessage.template) {
            String text = geContentFromTemplate(mailMessage.model, mailMessage.template)
            mimeMessageHelper.setText(text, true);
        } else if (mailMessage.text) {
            mimeMessageHelper.setText(mailMessage.text, true);
        }
        javaMailSender.send(mimeMessageHelper.getMimeMessage());

    }

    String geContentFromTemplate(Map<String, Object> model, String template) {
        StringBuffer content = new StringBuffer();
        try {
            content.append(FreeMarkerTemplateUtils.processTemplateIntoString(freeMarkerConfig.getTemplate(template), model));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return content.toString();
    }
}
