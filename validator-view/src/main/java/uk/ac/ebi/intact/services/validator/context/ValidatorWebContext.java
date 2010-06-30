package uk.ac.ebi.intact.services.validator.context;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Singleton pattern.
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>22-Jun-2010</pre>
 */

public class ValidatorWebContext {

    private static ValidatorWebContext ourInstance = new ValidatorWebContext();

    private ValidatorWebContent validatorWebContent;

    private MailSender mailSender;
    private final String emailSender = "validator-admin-noreply@gmail.com";
    private List<String> emailRecipients = new ArrayList<String>();
    private final String emailSubjectPrefix = "[Validator-view]";

    public static ValidatorWebContext getInstance() {

        return ourInstance;
    }

    private ValidatorWebContext(){
        // Initialize Spring for emails
        String[] configFiles = new String[]{"/beans.spring.xml"};
        BeanFactory beanFactory = new ClassPathXmlApplicationContext( configFiles );
        this.mailSender = ( MailSender ) beanFactory.getBean( "mailSender" );

        setUpEMailRecipients();

        this.validatorWebContent = new ValidatorWebContent();
    }

    public synchronized ValidatorWebContent getValidatorWebContent() {
        return validatorWebContent;
    }

    public synchronized void setValidatorWebContent(ValidatorWebContent validatorWebContent) {
        this.validatorWebContent = validatorWebContent;
    }

    private void setUpEMailRecipients(){
        emailRecipients.clear();

        emailRecipients.add("marine@ebi.ac.uk");
        //emailRecipients.add("baranda@ebi.ac.uk");
        //emailRecipients.add("skerrien@ebi.ac.uk");
    }

    public synchronized void sendEmail( String title, String body ) {
        if ( mailSender != null ) {
            final SimpleMailMessage message = new SimpleMailMessage();
            message.setTo( emailRecipients.toArray( new String[]{} ) );
            message.setFrom( emailSender );
            message.setSubject( emailSubjectPrefix + " " + title );
            message.setText( body );
            mailSender.send( message );
        }
    }
}
