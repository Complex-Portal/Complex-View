package uk.ac.ebi.intact.services.validator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import psidev.psi.mi.validator.extension.Mi25Validator;
import psidev.psi.tools.ontology_manager.OntologyManager;
import psidev.psi.tools.validator.preferences.UserPreferences;
import psidev.psi.tools.validator.rules.codedrule.ObjectRule;
import psidev.psi.tools.validator.rules.cvmapping.CvRuleManager;
import uk.ac.ebi.intact.services.validator.context.ValidatorWebContent;
import uk.ac.ebi.intact.services.validator.context.ValidatorWebContext;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.io.File;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * TODO comment this
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>25-Jun-2010</pre>
 */

public class ValidatorFactory {

    /**
     * Logging, logging!
     */
    private static final Log log = LogFactory.getLog(ValidatorFactory.class);

    private static final String psiParOntology = "config/psi_par/ontologies.xml";
    private static final String psiMiOntology = "config/psi_mi/ontologies.xml";
    private static final String psiMiCvMapping = "config/psi_mi/cv-mapping.xml";
    private static final String psiParCvMapping = "config/psi_par/cv-mapping.xml";
    private static final String mimixRules = "config/psi_mi/mimix-rules.xml";
    private static final String imexRules = "config/psi_mi/imex-rules.xml";

    public Mi25Validator getReInitialisedValidator(ValidationScope scope, DataModel dataModel){
        if (dataModel == null){
            throw new IllegalArgumentException("The dataModel cannot be null.");
        }

        if ( log.isInfoEnabled() ) {
            log.info( "Model: " + dataModel.name() );
            log.info( "Selected validation scope: " + scope );
        }

        if( dataModel.equals( DataModel.PSI_PAR )){
            if( scope == null ) {
                // set default value
                log.warn( "The application didn't get a valid validation scope (null), setting PAR default to CV Mapping." );
                scope = ValidationScope.CV_ONLY;
            }
            return createPsiParValidator(scope);
        }
        else if (dataModel.equals( DataModel.PSI_MI)){
            if (scope == null){
                // set default value
                log.warn( "The application didn't get a valid validation scope (null), setting MI default to MIMIx." );
                scope = ValidationScope.MIMIX;
            }
            return createPsiMiValidator(scope);
        }
        else {
            throw new IllegalStateException( "Unknown data model: " + dataModel );

        }
    }

    public Mi25Validator getValidator(ValidationScope scope, DataModel dataModel){
        if (dataModel == null){
            throw new IllegalArgumentException("The dataModel cannot be null.");
        }

        if ( log.isInfoEnabled() ) {
            log.info( "Model: " + dataModel.name() );
            log.info( "Selected validation scope: " + scope );
        }

        if( dataModel.equals( DataModel.PSI_PAR )){
            if( scope == null ) {
                // set default value
                log.warn( "The application didn't get a valid validation scope (null), setting PAR default to CV Mapping." );
                scope = ValidationScope.CV_ONLY;
            }
            return getPsiParValidator(scope);
        }
        else if (dataModel.equals( DataModel.PSI_MI)){
            if (scope == null){
                // set default value
                log.warn( "The application didn't get a valid validation scope (null), setting MI default to MIMIx." );
                scope = ValidationScope.MIMIX;
            }
            return getPsiMiValidator(scope);
        }
        else {
            throw new IllegalStateException( "Unknown data model: " + dataModel );

        }
    }

    private void setUpUserPreferences(Mi25Validator validator){
        // set work directory
        UserPreferences preferences = new UserPreferences();
        preferences.setKeepDownloadedOntologiesOnDisk(true);
        preferences.setWorkDirectory(new File(System.getProperty("java.io.tmpdir")));
        preferences.setSaxValidationEnabled(true);

        validator.setUserPreferences(preferences);
    }

    private Mi25Validator createPsiParValidator(ValidationScope scope){
        FacesContext context = FacesContext.getCurrentInstance();

        try {
            // We read the configuration file, included inside the jar
            InputStream ontologyCfg = Mi25Validator.class.getClassLoader().getResourceAsStream( psiParOntology );
            InputStream cvMappingCfg = null;
            InputStream ruleCfg = null;

            switch( scope ) {
                case SYNTAX:
                    break;

                case CV_ONLY:
                    cvMappingCfg = Mi25Validator.class.getClassLoader().getResourceAsStream( psiParCvMapping );

                    if (cvMappingCfg == null){
                        throw new IllegalStateException("The file containing the Cv-mapping rules '" + psiParCvMapping + " has not been loaded and it is needed to" +
                                "validate the file with the scope " + scope.toString());
                    }
                    break;

                default:
                    throw new IllegalStateException( "Unsupported validation scope: '"+ scope +"', the application is not correctly configured." );
            }

            log.info( "Is the Cv mapping file null: " + ( cvMappingCfg == null ) );

            // we instantiate the MI25 validator
            Mi25Validator validator = new Mi25Validator(ontologyCfg, cvMappingCfg, ruleCfg);

            setUpUserPreferences(validator);

            return validator;

        } catch (Throwable t) {

            log.error( "An error occured while configuring the PAR validator", t );

            FacesMessage message = new FacesMessage( "An error occured while configuring the validator: " + t.getMessage() );
            context.addMessage( null, message );

            return null;
        }
    }

    private Mi25Validator getPsiParValidator(ValidationScope scope){
        FacesContext context = FacesContext.getCurrentInstance();

        try {
            ValidatorWebContent validatorContent = ValidatorWebContext.getInstance().getValidatorWebContent();

            // We get the pre-instantiated ontologyManager and object rules
            OntologyManager ontologymanager = validatorContent.getPsiParOntologyManager();
            CvRuleManager cvRuleManager = null;
            Set<ObjectRule> objectRules = new HashSet<ObjectRule>();

            switch( scope ) {
                case SYNTAX:
                    break;

                case CV_ONLY:
                    cvRuleManager = validatorContent.getPsiParRuleManager();
                    break;

                default:
                    throw new IllegalStateException( "Unsupported validation scope: '"+ scope +"', the application is not correctly configured." );
            }

            // we instantiate the MI25 validator
            Mi25Validator validator = new Mi25Validator(ontologymanager, cvRuleManager, objectRules);

            setUpUserPreferences(validator);

            return validator;

        } catch (Throwable t) {

            log.error( "An error occured while configuring the PAR validator", t );

            FacesMessage message = new FacesMessage( "An error occured while configuring the validator: " + t.getMessage() );
            context.addMessage( null, message );

            return null;
        }
    }

    private Mi25Validator getPsiMiValidator(ValidationScope scope){
        FacesContext context = FacesContext.getCurrentInstance();

        try {
            ValidatorWebContent validatorContent = ValidatorWebContext.getInstance().getValidatorWebContent();

            // We get the pre-instantiated ontologyManager and object rules
            OntologyManager ontologymanager = validatorContent.getPsiMiOntologyManager();
            CvRuleManager cvRuleManager = null;
            Set<ObjectRule> objectRules = new HashSet<ObjectRule>();

            switch( scope ) {
                case SYNTAX:
                    break;

                case CV_ONLY:
                    cvRuleManager = validatorContent.getPsiMiRuleManager();
                    break;

                case MIMIX:
                    cvRuleManager = validatorContent.getPsiMiRuleManager();
                    objectRules = validatorContent.getPsiMiObjectRules().get(scope);
                    break;

                case IMEX:
                    cvRuleManager = validatorContent.getPsiMiRuleManager();
                    objectRules = validatorContent.getPsiMiObjectRules().get(scope);
                    break;

                default:
                    throw new IllegalArgumentException( "Unsupported validation scope: '"+ scope +"', the application is not correctly configured." );
            }

            // we instantiate the MI25 validator
            Mi25Validator validator = new Mi25Validator(ontologymanager, cvRuleManager, objectRules);

            setUpUserPreferences(validator);

            return validator;

        } catch (Throwable t) {

            log.error( "An error occured while configuring the MI validator", t );

            FacesMessage message = new FacesMessage( "An error occured while configuring the validator: " + t.getMessage() );
            context.addMessage( null, message );

            return null;
        }
    }

    private Mi25Validator createPsiMiValidator(ValidationScope scope){
        FacesContext context = FacesContext.getCurrentInstance();

        try {

            // We read the configuration file, included inside the jar
            InputStream ontologyCfg = Mi25Validator.class.getClassLoader().getResourceAsStream( psiMiOntology );
            InputStream cvMappingCfg = null;
            InputStream ruleCfg = null;

            switch( scope ) {
                case SYNTAX:
                    break;

                case CV_ONLY:
                    cvMappingCfg = Mi25Validator.class.getClassLoader().getResourceAsStream( psiMiCvMapping );

                    if (cvMappingCfg == null){
                        throw new IllegalStateException("The file containing the Cv-mapping rules '" + psiMiCvMapping + " has not been loaded and it is needed to" +
                                "validate the file with the scope " + scope.toString());
                    }
                    break;

                case MIMIX:
                    cvMappingCfg = Mi25Validator.class.getClassLoader().getResourceAsStream( psiMiCvMapping );

                    if (cvMappingCfg == null){
                        throw new IllegalStateException("The file containing the Cv-mapping rules '" + psiMiCvMapping + " has not been loaded and it is needed to" +
                                "validate the file with the scope " + scope.toString());
                    }

                    ruleCfg = Mi25Validator.class.getClassLoader().getResourceAsStream( mimixRules );

                    if (ruleCfg == null){
                        throw new IllegalStateException("The file containing the mimix rules '" + mimixRules + " has not been loaded and it is needed to" +
                                "validate the file with the scope " + scope.toString());
                    }
                    break;

                case IMEX:
                    cvMappingCfg = Mi25Validator.class.getClassLoader().getResourceAsStream( psiMiCvMapping );

                    if (cvMappingCfg == null){
                        throw new IllegalStateException("The file containing the Cv-mapping rules '" + psiParCvMapping + " has not been loaded and it is needed to" +
                                "validate the file with the scope " + scope.toString());
                    }

                    ruleCfg = Mi25Validator.class.getClassLoader().getResourceAsStream( imexRules );

                    if (ruleCfg == null){
                        throw new IllegalStateException("The file containing the imex rules '" + imexRules + " has not been loaded and it is needed to" +
                                "validate the file with the scope " + scope.toString());
                    }

                    break;

                default:
                    throw new IllegalArgumentException( "Unsupported validation scope: '"+ scope +"', the application is not correctly configured." );
            }

            // we instantiate the MI25 validator
            Mi25Validator validator = new Mi25Validator(ontologyCfg, cvMappingCfg, ruleCfg);

            setUpUserPreferences(validator);

            return validator;

        }catch (Throwable t) {

            log.error( "An error occured while configuring the MI validator", t );

            FacesMessage message = new FacesMessage( "An error occured while configuring the validator: " + t.getMessage() );
            context.addMessage( null, message );

            return null;
        }
    }

    public ValidationScope getDefaultValidationScope(DataModel dataModel){
        if (dataModel == null){
            throw new IllegalArgumentException("The dataModel cannot be null.");
        }

        if( dataModel.equals( DataModel.PSI_PAR )){
            return ValidationScope.CV_ONLY;
        }
        else if (dataModel.equals( DataModel.PSI_MI)){
            return ValidationScope.MIMIX;
        }
        else {
            throw new IllegalStateException( "Unknown data model: " + dataModel );

        }
    }
}
