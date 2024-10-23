package org.foo.modules.jahia.actions;

import org.jahia.bin.Action;
import org.jahia.bin.ActionResult;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Component(service = Action.class)
public class MailAction extends Action {
    private static final Logger logger = LoggerFactory.getLogger(MailAction.class);

    private String auth;
    private String startTls;
    private String host;
    private String port;
    private String username;
    private String password;
    private String from;
    private String to;
    private String subject;
    private String message;

    public MailAction() {
        setName("sendMail");
        setRequiredMethods("GET,POST");
        setRequireAuthenticatedUser(false);
    }

    @Activate
    private void onActivate(Map<String, String> params) {
        auth = params.get("mail.smtp.auth");
        startTls = params.get("mail.smtp.startTls");
        host = params.get("mail.smtp.host");
        port = params.get("mail.smtp.port");
        username = params.get("mail.smtp.username");
        password = params.get("mail.smtp.password");
        from = params.get("mail.smtp.from");
        to = params.get("mail.smtp.to");
        subject = params.get("mail.smtp.subject");
        message = params.get("mail.smtp.message");
    }

    @Override
    public ActionResult doExecute(HttpServletRequest httpServletRequest, RenderContext renderContext, Resource resource, JCRSessionWrapper jcrSessionWrapper, Map<String, List<String>> map, URLResolver urlResolver) {
        final Properties config = new Properties();
        config.put("mail.smtp.auth", auth);
        config.put("mail.smtp.starttls.enable", startTls);
        config.put("mail.smtp.host", host);
        config.put("mail.smtp.port", port);
        final Session session = Session.getInstance(config, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            final Message mailMessage = new MimeMessage(session);
            mailMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
            mailMessage.setFrom(new InternetAddress(from));
            mailMessage.setSubject(subject);
            mailMessage.setText(message);
            mailMessage.setSentDate(new Date());
            Transport.send(mailMessage);
        } catch (final MessagingException e) {
            logger.error("", e);
        }

        return ActionResult.OK;
    }
}
