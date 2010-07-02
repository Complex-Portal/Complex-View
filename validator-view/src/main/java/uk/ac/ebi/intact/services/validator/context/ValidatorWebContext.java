package uk.ac.ebi.intact.services.validator.context;

import org.hibernate.exception.ExceptionUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Singleton which contains the ValidatorWebContent with all the environment variables to re-use when validating a file.
 * It needs to be unique for the all application so we can re-use the same environment variables when validating a file.
 * Using pre-loaded instances of Ontology manager, cv-mapping and object rules is important to not spend too much time reloading
 * dependency rules and the ontology at each validation..
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>22-Jun-2010</pre>
 */

public class ValidatorWebContext {

    /**
     * The current instance
     */
    private static ValidatorWebContext ourInstance = new ValidatorWebContext();

    /**
     * The validator web content
     */
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

        try {
            this.validatorWebContent = new ValidatorWebContent();
        } catch (ValidatorWebContextException e) {
            String body = "The validator web content has not been properly initialized." + ExceptionUtils.getFullStackTrace(e);

            sendEmail("Problem initializing the validator web content", body);
        }
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
