package launchpad.mail

import freemarker.template.Configuration
import freemarker.template.TemplateException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils

import javax.mail.internet.MimeMessage

@Service('mailService')
class MailService {
    private static final Logger LOGGER = LoggerFactory.getLogger(this.getClass())

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

    @Async
    boolean send(MailMessage mailMessage) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage()
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true)
        if (!mailMessage.from) {
            mailMessage.from = from
        }
        mimeMessageHelper.setFrom(mailMessage.from)
        if (overrideAddress) {
            mailMessage.to = overrideAddress
        }
        mimeMessageHelper.setTo(mailMessage.to)
        mimeMessageHelper.setSubject(mailMessage.subject)
        if (mailMessage.template) {
            String text = geContentFromTemplate(mailMessage.model, mailMessage.template)
            mimeMessageHelper.setText(text, true)
        } else if (mailMessage.text) {
            mimeMessageHelper.setText(mailMessage.text, true)
        }
        javaMailSender.send(mimeMessageHelper.mimeMessage)
    }

    String geContentFromTemplate(Map<String, Object> model, String template) {
        StringBuffer content = new StringBuffer()
        try {
            content.append(FreeMarkerTemplateUtils.processTemplateIntoString(freeMarkerConfig.getTemplate(template), model))
        } catch (IOException e) {
            LOGGER.error('Template {} was not found or could not be read, exception : {}', template, e.message)
        } catch (TemplateException e) {
            LOGGER.error('Template {} rendering failed, exception : {}', template, e.message)
        }
        return content.toString()
    }
}