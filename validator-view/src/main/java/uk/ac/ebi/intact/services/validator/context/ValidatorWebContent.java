package uk.ac.ebi.intact.services.validator.context;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import psidev.psi.mi.validator.extension.Mi25Validator;
import psidev.psi.tools.ontology_manager.OntologyManager;
import psidev.psi.tools.validator.rules.codedrule.ObjectRule;
import psidev.psi.tools.validator.rules.cvmapping.CvRuleManager;
import uk.ac.ebi.intact.services.validator.DataModel;
import uk.ac.ebi.intact.services.validator.ValidationScope;
import uk.ac.ebi.intact.services.validator.ValidatorFactory;

import java.util.*;

/**
 * TODO comment this
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>25-Jun-2010</pre>
 */

public class ValidatorWebContent {

    private Map<ValidationScope, Set<ObjectRule>> psiMiObjectRules = new HashMap<ValidationScope, Set<ObjectRule>>();
    private CvRuleManager psiMiRuleManager;
    private CvRuleManager psiParRuleManager;
    private OntologyManager psiMiOntologyManager;
    private OntologyManager psiParOntologyManager;

    private MailSender mailSender;
    private final String emailSender = "imex.bot@gmail.com";
    private List<String> emailRecipients = new ArrayList<String>();
    private final String emailSubjectPrefix = "[Validator-view]";

    private List<ValidationScope> psiParScopes = new ArrayList<ValidationScope>();
    private List<ValidationScope> psiMiScopes = new ArrayList<ValidationScope>();

    public ValidatorWebContent(){

        // Initialize Spring for emails
        String[] configFiles = new String[]{"/beans.spring.xml"};
        BeanFactory beanFactory = new ClassPathXmlApplicationContext( configFiles );
        this.mailSender = ( MailSender ) beanFactory.getBean( "mailSender" );

        setUpEMailRecipients();

        ValidatorFactory factory = new ValidatorFactory();

        setUpPsiMiScopes();
        setUpPsiParScopes();
        setUpPsiParValidatorEnvironments(factory);
        setUpPsiMiValidatorEnvironments(factory);
    }

    public ValidatorWebContent(OntologyManager psiMiOntologyManager, CvRuleManager psiMiRuleManager, Map<ValidationScope, Set<ObjectRule>> psiMiObjectRules){

        // Initialize Spring for emails
        String[] configFiles = new String[]{"/beans.spring.xml"};
        BeanFactory beanFactory = new ClassPathXmlApplicationContext( configFiles );
        this.mailSender = ( MailSender ) beanFactory.getBean( "mailSender" );

        setUpEMailRecipients();

        ValidatorFactory factory = new ValidatorFactory();

        setUpPsiMiScopes();
        setUpPsiParScopes();
        setUpPsiParValidatorEnvironments(factory);

        setPsiMiOntologyManager(psiMiOntologyManager);
        setPsiMiRuleManager(psiMiRuleManager);
        setPsiMiObjectRules(psiMiObjectRules);
    }

    public ValidatorWebContent(OntologyManager psiParOntologyManager, CvRuleManager psiParRuleManager){

        // Initialize Spring for emails
        String[] configFiles = new String[]{"/beans.spring.xml"};
        BeanFactory beanFactory = new ClassPathXmlApplicationContext( configFiles );
        this.mailSender = ( MailSender ) beanFactory.getBean( "mailSender" );

        setUpEMailRecipients();

        ValidatorFactory factory = new ValidatorFactory();

        setUpPsiMiScopes();
        setUpPsiParScopes();
        setUpPsiMiValidatorEnvironments(factory);

        setPsiParOntologyManager(psiParOntologyManager);
        setPsiParRuleManager(psiParRuleManager);
    }

    private void setUpEMailRecipients(){
        emailRecipients.clear();

        emailRecipients.add("marine@ebi.ac.uk");
        //emailRecipients.add("baranda@ebi.ac.uk");
        //emailRecipients.add("skerrien@ebi.ac.uk");
    }

    public Map<ValidationScope, Set<ObjectRule>> getPsiMiObjectRules() {
        return psiMiObjectRules;
    }

    private void setPsiMiObjectRules(Map<ValidationScope, Set<ObjectRule>> psiMiObjectRules) {
        if (psiMiObjectRules == null){
            String body = "The map containing the psi-mi object rules in the validatorWebContent cannot be null. \n" +
                    "The validator will not be able to do a proper semantic validation.";
            NullPointerException e = new NullPointerException(body);
            sendEmail("Map containing pre-loaded psi-mi object rules is null in the validator web content", ExceptionUtils.getFullStackTrace(e));
            throw e;
        }
        else if (psiMiObjectRules.isEmpty()){
            String body = "The map containing the psi-mi object rules in the validatorWebContent cannot be empty. \n" +
                    "The validator will not be able to do a proper semantic validation.";
            IllegalStateException e = new IllegalStateException(body);
            sendEmail("Map containing pre-loaded psi-mi object rules is empty in the validator web content", ExceptionUtils.getFullStackTrace(e));
            throw e;
        }
        else if (!psiMiObjectRules.containsKey(ValidationScope.MIMIX)){
            String body = "The map containing the psi-mi object rules in the validatorWebContent doesn't have any pre-loaded MIMIX rules. \n" +
                    "The validator will not be able to do a proper MIMIx validation.";
            IllegalStateException e = new IllegalStateException(body);
            sendEmail("No MIMIx pre-loaded object rules in the validator web content", ExceptionUtils.getFullStackTrace(e));
            throw e;
        }
        else if (!psiMiObjectRules.containsKey(ValidationScope.IMEX)){
            String body = "The map containing the psi-mi object rules in the validatorWebContent doesn't have any pre-loaded IMEx rules. \n" +
                    "The validator will not be able to do a proper IMEx validation.";
            IllegalStateException e = new IllegalStateException(body);
            sendEmail("No IMEx pre-loaded object rules in the validator web content", ExceptionUtils.getFullStackTrace(e));
            throw e;
        }
        this.psiMiObjectRules = psiMiObjectRules;
    }

    public CvRuleManager getPsiMiRuleManager() {
        return psiMiRuleManager;
    }

    private void setPsiMiRuleManager(CvRuleManager psiMiRuleManager) {
        if (psiMiRuleManager == null){
            String body = "The psi-mi CvRule manager in the validatorWebContent cannot be null. \n" +
                    "The validator will not be able to validate controlled vocabulary usages.";
            NullPointerException e = new NullPointerException(body);
            sendEmail("Psi-mi cvRule manager is null in the validator web content", ExceptionUtils.getFullStackTrace(e));
            throw e;
        }
        else if (psiMiRuleManager.getCvRules().isEmpty()){
            String body = "The psi-mi cv-mapping rules in the validatorWebContent cannot be empty. \n" +
                    "The validator will not be able to validate controlled vocabulary usages.";
            IllegalStateException e = new IllegalStateException(body);
            sendEmail("No pre-loaded psi-mi cv-mapping rules in the validator web content", ExceptionUtils.getFullStackTrace(e));
            throw e;
        }
        this.psiMiRuleManager = psiMiRuleManager;
    }

    public OntologyManager getPsiMiOntologyManager() {
        return psiMiOntologyManager;
    }

    private void setPsiMiOntologyManager(OntologyManager psiMiOntologyManager) {
        if (psiMiOntologyManager == null){
            String body = "A problem occurred when uploading the psi-mi ontology and the ontology manager is null. \n" +
                    "The validator will not be able to validate psi-mi files.";
            NullPointerException e = new NullPointerException(body);
            sendEmail("Ontology Manager is null", ExceptionUtils.getFullStackTrace(e));
            throw e;
        }
        this.psiMiOntologyManager = psiMiOntologyManager;
    }

    public CvRuleManager getPsiParRuleManager() {
        return psiParRuleManager;
    }

    private void setPsiParRuleManager(CvRuleManager psiParRuleManager) {
        if (psiParRuleManager == null){
            String body = "The psi-par CvRule manager in the validatorWebContent cannot be null. \n" +
                    "The validator will not be able to validate controlled vocabulary usages.";
            NullPointerException e = new NullPointerException(body);
            sendEmail("Psi-par cvRule manager is null in the validator web content", ExceptionUtils.getFullStackTrace(e));
            throw e;
        }
        else if (psiParRuleManager.getCvRules().isEmpty()){
            String body = "The psi-par cv-mapping rules in the validatorWebContent cannot be empty. \n" +
                    "The validator will not be able to validate controlled vocabulary usages.";
            IllegalStateException e = new IllegalStateException(body);
            sendEmail("No pre-loaded psi-par cv-mapping rules in the validator web content", ExceptionUtils.getFullStackTrace(e));
            throw e;
        }
        this.psiParRuleManager = psiParRuleManager;
    }

    public OntologyManager getPsiParOntologyManager() {
        return psiParOntologyManager;
    }

    private void setPsiParOntologyManager(OntologyManager psiParOntologyManager) {
        if (psiParOntologyManager == null){
            String body = "A problem occurred when uploading the psi-par ontology and the ontology manager is null. \n" +
                    "The validator will not be able to validate psi-par files.";
            NullPointerException e = new NullPointerException(body);
            sendEmail("Ontology Manager is null", ExceptionUtils.getFullStackTrace(e));
            throw e;
        }
        this.psiParOntologyManager = psiParOntologyManager;
    }

    private void setUpPsiParValidatorEnvironments(ValidatorFactory factory){
        Mi25Validator validator = factory.getReInitialisedValidator(ValidationScope.CV_ONLY, DataModel.PSI_PAR);

        setPsiParOntologyManager(validator.getOntologyMngr());
        setPsiParRuleManager(validator.getCvRuleManager());
    }

    private void setUpPsiMiValidatorEnvironments(ValidatorFactory factory){
        Mi25Validator validator = factory.getReInitialisedValidator(ValidationScope.MIMIX, DataModel.PSI_MI);
        setPsiParOntologyManager(validator.getOntologyMngr());
        setPsiParRuleManager(validator.getCvRuleManager());

        this.psiMiObjectRules.put(ValidationScope.MIMIX, validator.getObjectRules());

        validator = factory.getReInitialisedValidator(ValidationScope.IMEX, DataModel.PSI_MI);
        setPsiMiOntologyManager(validator.getOntologyMngr());
        setPsiMiRuleManager(validator.getCvRuleManager());

        this.psiMiObjectRules.put(ValidationScope.IMEX, validator.getObjectRules());
    }

    private void setUpPsiMiScopes(){
        this.psiMiScopes.add(ValidationScope.SYNTAX);
        this.psiMiScopes.add(ValidationScope.CV_ONLY);
        this.psiMiScopes.add(ValidationScope.MIMIX);
        this.psiMiScopes.add(ValidationScope.IMEX);
    }

    private void setUpPsiParScopes(){
        this.psiMiScopes.add(ValidationScope.SYNTAX);
        this.psiMiScopes.add(ValidationScope.CV_ONLY);
    }

    public void sendEmail( String title, String body ) {
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
