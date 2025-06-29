package plugins.services;

import jakarta.mail.*;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.List;
import java.util.Properties;

public class EmailService {
    private static final String USERNAME = "tavananh95@gmail.com";
    private static final String APP_PASSWORD = "jyja ouce rqlb ipqv";

    public static void sendEmail(List<String> recipients, String subject, String body) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        System.out.println("E-mail initiated.");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(USERNAME, APP_PASSWORD);
            }
        });

        session.setDebug(true);

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(USERNAME));

            InternetAddress[] recipientAddresses = recipients.stream()
                    .map(r -> {
                        try {
                            return new InternetAddress(r.trim());
                        } catch (AddressException e) {
                            throw new RuntimeException("Adresse invalide : " + r);
                        }
                    })
                    .toArray(InternetAddress[]::new);

            message.setRecipients(Message.RecipientType.TO, recipientAddresses);
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);

            System.out.println("E-mail envoyé avec succès.");

        } catch (MessagingException e) {
            System.err.println(" Échec de l'envoi de l'e-mail : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
