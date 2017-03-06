package voyage.common.mail

class MailMessage {
    String to
    String from
    String cc
    String bcc
    String subject
    String template
    Map<String, Object> model
    String text
}
