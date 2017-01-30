package launchpad.mail

import freemarker.template.Configuration
import launchpad.error.MailSendException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils

import javax.mail.internet.MimeMessage

@Service
class MailService {
    private static final Logger LOG = LoggerFactory.getLogger(this.getClass())

    @Value('${app.contact-support.email}')
    private String from

    @Value('${spring.mail.overrideAddress}')
    private String overrideAddress

    private final JavaMailSender javaMailSender
    private final Configuration freeMarkerConfig

    @Autowired
    MailService(JavaMailSender javaMailSender, Configuration freeMarkerConfig) {
        this.javaMailSender = javaMailSender
        this.freeMarkerConfig = freeMarkerConfig
    }

    void send(MailMessage mailMessage) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage()
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true)
        mailMessage.from = mailMessage.from ?: from
        mimeMessageHelper.setFrom(mailMessage.from)
        if (overrideAddress) {
            mailMessage.to = overrideAddress
        }
        mimeMessageHelper.setTo(mailMessage.to)
        mimeMessageHelper.setSubject(mailMessage.subject)
        if (mailMessage.template) {
            String text = FreeMarkerTemplateUtils.processTemplateIntoString(freeMarkerConfig.getTemplate(mailMessage.template), mailMessage.model)
            mimeMessageHelper.setText(text, true)
        } else if (mailMessage.text) {
            mimeMessageHelper.setText(mailMessage.text, true)
        }
        try {
            LOG.info('sending mail to ' + mailMessage.to)
            javaMailSender.send(mimeMessageHelper.mimeMessage)
        } catch (Exception e) {
            throw new MailSendException()
        }
    }
}
