package Utils;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;

public class EmailSender {
    private static final String MY_EMAIL = "safabennasr14@gmail.com";
    private static final String MY_PASSWORD = "jruxvtzhlkdgkqaw";

    public static void sendResetCode(String recipientEmail, String code) throws MessagingException {
        //Configuration de la connexion au serveur Gmail
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true"); // Exiger une connexion avec identifiants
        props.put("mail.smtp.starttls.enable", "true"); // Activer le chiffrement sécurisé
        props.put("mail.smtp.host", "smtp.gmail.com"); // Serveur d'envoi = Gmail
        props.put("mail.smtp.port", "587"); // Port standard pour l'envoi sécurisé

        //On crée une session de connexion à Gmail avec nos identifiants
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(MY_EMAIL, MY_PASSWORD);
            }
        });

        //On compose l'email (de qui, à qui, sujet, contenu)
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(MY_EMAIL));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
        message.setSubject("TripEase - Code de récupération");
        message.setText("Bonjour,\n\nVotre code de réinitialisation pour TripEase est : " + code);

        Transport.send(message);
    }
}