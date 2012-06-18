package uk.ac.ebi.intact.services.validator.context;

import org.apache.commons.lang.exception.ExceptionUtils;
import psidev.psi.mi.validator.extension.Mi25Validator;
import psidev.psi.tools.cvrReader.CvRuleReader;
import psidev.psi.tools.cvrReader.CvRuleReaderException;
import psidev.psi.tools.cvrReader.mapping.jaxb.CvMapping;
import psidev.psi.tools.ontology_manager.OntologyManager;
import psidev.psi.tools.validator.rules.codedrule.ObjectRule;
import psidev.psi.tools.validator.rules.cvmapping.CvRule;
import psidev.psi.tools.validator.rules.cvmapping.CvRuleManager;
import uk.ac.ebi.intact.services.validator.DataModel;
import uk.ac.ebi.intact.services.validator.ValidationScope;
import uk.ac.ebi.intact.services.validator.ValidatorFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * The ValidatorWebContent contains all the variable and/or environment we want to re-use when validating different files.
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>25-Jun-2010</pre>
 */

public class ValidatorWebContent {

    /**
     * The CVRule reader to read the cv-mapping rules
     */
    CvRuleReader cvRulesReader;

    /**
     * This map contains all the ObjectRules to be executed with a specific scope (IMEx or MIMIx)
     */
    private Map<ValidationScope, Set<ObjectRule>> psiMiObjectRules = new HashMap<ValidationScope, Set<ObjectRule>>();

    /**
     * The CVMapping object containing the cv-mapping rules for PSI-MI validation
     */
    private CvMapping psiMiCvMapping;

    /**
     * The CVMapping object containing the cv-mapping rules for PSI-PAR validation
     */
    private CvMapping psiParCvMapping;

    /**
     * The OntologyManager instance to re-use for PSI-MI validation
     */
    private OntologyManager psiMiOntologyManager;

    /**
     * The OntologyManager instance to re-use for PSI-PAR validation
     */
    private OntologyManager psiParOntologyManager;

    /**
     * The list of psi-par scopes
     */
    private List<ValidationScope> psiParScopes = new ArrayList<ValidationScope>();

    /**
     * The list of psi-mi scopes
     */
    private List<ValidationScope> psiMiScopes = new ArrayList<ValidationScope>();

    /**
     * Create a new instance of the web content with new instances of the validator ontology manager, cvmapping and object rules
     * @throws ValidatorWebContextException
     */
    public ValidatorWebContent() throws ValidatorWebContextException {
            // new cv rule reader
            cvRulesReader = new CvRuleReader();

            // new validator factory
            ValidatorFactory factory = new ValidatorFactory();

            // set up validator environment for PSI-MI
            setUpPsiMiScopes();
            setUpPsiMiValidatorEnvironments(factory);
            // set up validator environment for PSI-PAR
            setUpPsiParScopes();
            setUpPsiParValidatorEnvironments(factory);
    }

    /**
     * Create a new instance of the web content with new instances of the validator ontology manager and cvmapping for PSI-PAR validation
     * but re-use pre-instantiated instances of the validator ontology manager, cvmapping and object rules for PSI-MI validation
     * @throws ValidatorWebContextException
     */
    public ValidatorWebContent(OntologyManager psiMiOntologyManager, CvMapping psiMiRuleManager, Map<ValidationScope, Set<ObjectRule>> psiMiObjectRules) throws ValidatorWebContextException {
        // new cv rule reader
        cvRulesReader = new CvRuleReader();

        // new validator factory
        ValidatorFactory factory = new ValidatorFactory();

        // set up validator environment for PSI-PAR
        setUpPsiParScopes();
        setUpPsiParValidatorEnvironments(factory);

        // Re-use previous validator environment for PSI-MI
        setUpPsiMiScopes();
        setPsiMiOntologyManager(psiMiOntologyManager);
        setPsiMiCvMapping(psiMiRuleManager);
        setPsiMiObjectRules(psiMiObjectRules);
    }

    /**
     * Create a new instance of the web content with new instances of the validator ontology manager, cvmapping and object rules for PSI-MI validation
     * but re-use pre-instantiated instances of the validator ontology manager and cvmapping for PSI-MI validation
     * @throws ValidatorWebContextException
     */
    public ValidatorWebContent(OntologyManager psiParOntologyManager, CvMapping psiParRuleManager) throws ValidatorWebContextException {
        // new cv rule reader
        cvRulesReader = new CvRuleReader();

        // new validator factory
        ValidatorFactory factory = new ValidatorFactory();

        // set up validator environment for PSI-MI
        setUpPsiMiScopes();
        setUpPsiMiValidatorEnvironments(factory);

        // Re-use previous validator environment for PSI-PAR
        setUpPsiParScopes();
        setPsiParOntologyManager(psiParOntologyManager);
        setPsiParCvMapping(psiParRuleManager);
    }

    /**
     *
     * @return the map containing the PSI-MI object rules
     */
    public Map<ValidationScope, Set<ObjectRule>> getPsiMiObjectRules() {
        return psiMiObjectRules;
    }

    /**
     * Set the map containing the PSI-MI object rules and checks it is not null or empty and there must be a scope MIMIx and IMEx
     * @param psiMiObjectRules
     */
    private void setPsiMiObjectRules(Map<ValidationScope, Set<ObjectRule>> psiMiObjectRules) {
        ValidatorWebContext context = ValidatorWebContext.getInstance();

        if (psiMiObjectRules == null){
            String body = "The map containing the psi-mi object rules in the validatorWebContent cannot be null. \n" +
                    "The validator will not be able to do a proper semantic validation.";
            NullPointerException e = new NullPointerException(body);
            context.sendEmail("Map containing pre-loaded psi-mi object rules is null in the validator web content", ExceptionUtils.getFullStackTrace(e));
            throw e;
        }
        else if (psiMiObjectRules.isEmpty()){
            String body = "The map containing the psi-mi object rules in the validatorWebContent cannot be empty. \n" +
                    "The validator will not be able to do a proper semantic validation.";
            IllegalArgumentException e = new IllegalArgumentException(body);
            context.sendEmail("Map containing pre-loaded psi-mi object rules is empty in the validator web content", ExceptionUtils.getFullStackTrace(e));
            throw e;
        }
        else if (!psiMiObjectRules.containsKey(ValidationScope.MIMIX)){
            String body = "The map containing the psi-mi object rules in the validatorWebContent doesn't have any pre-loaded MIMIX rules. \n" +
                    "The validator will not be able to do a proper MIMIx validation.";
            IllegalArgumentException e = new IllegalArgumentException(body);
            context.sendEmail("No MIMIx pre-loaded object rules in the validator web content", ExceptionUtils.getFullStackTrace(e));
            throw e;
        }
        else if (!psiMiObjectRules.containsKey(ValidationScope.IMEX)){
            String body = "The map containing the psi-mi object rules in the validatorWebContent doesn't have any pre-loaded IMEx rules. \n" +
                    "The validator will not be able to do a proper IMEx validation.";
            IllegalArgumentException e = new IllegalArgumentException(body);
            context.sendEmail("No IMEx pre-loaded object rules in the validator web content", ExceptionUtils.getFullStackTrace(e));
            throw e;
        }
        this.psiMiObjectRules = psiMiObjectRules;
    }

    /**
     *
     * @return the CVMapping for PSI-MI
     */
    public CvMapping getPsiMiCvMapping() {
        return psiMiCvMapping;
    }

    /**
     * Sets the CVMapping for PSI-MI. Checks that it is not null and the list of CVRules and CVReferences should neither be null nor be empty.
     * @param psiMiCvMapping
     */
    private void setPsiMiCvMapping(CvMapping psiMiCvMapping) {
        ValidatorWebContext context = ValidatorWebContext.getInstance();

        if (psiMiCvMapping == null){
            String body = "The psi-mi CvMapping in the validatorWebContent cannot be null. \n" +
                    "The validator will not be able to validate controlled vocabulary usages.";
            NullPointerException e = new NullPointerException(body);
            context.sendEmail("Psi-mi cvMapping is null in the validator web content", ExceptionUtils.getFullStackTrace(e));
            throw e;
        }
        else if (psiMiCvMapping.getCvMappingRuleList() == null){
            String body = "The list of psi-mi cv-mapping rules in the validatorWebContent cannot be null. \n" +
                    "The validator will not be able to validate controlled vocabulary usages.";
            NullPointerException e = new NullPointerException(body);
            context.sendEmail("No pre-loaded list of psi-mi cv-mapping rules in the validator web content", ExceptionUtils.getFullStackTrace(e));
            throw e;
        }
        else if (psiMiCvMapping.getCvMappingRuleList().getCvMappingRule() == null){
            String body = "The psi-mi cv-mapping rules in the validatorWebContent cannot be null. \n" +
                    "The validator will not be able to validate controlled vocabulary usages.";
            NullPointerException e = new NullPointerException(body);
            context.sendEmail("No pre-loaded psi-mi cv-mapping rules in the validator web content", ExceptionUtils.getFullStackTrace(e));
            throw e;
        }
        else if (psiMiCvMapping.getCvMappingRuleList().getCvMappingRule().isEmpty()){
            String body = "The list of psi-mi cv-mapping rules in the validatorWebContent cannot be empty. \n" +
                    "The validator will not be able to validate controlled vocabulary usages.";
            IllegalArgumentException e = new IllegalArgumentException(body);
            context.sendEmail("Empty list of psi-mi cv-mapping rules in the validator web content", ExceptionUtils.getFullStackTrace(e));
            throw e;
        }
        else if (psiMiCvMapping.getCvReferenceList() == null){
            String body = "The list of psi-mi cv-mapping references in the validatorWebContent cannot be null. \n" +
                    "The validator will not be able to validate controlled vocabulary usages.";
            NullPointerException e = new NullPointerException(body);
            context.sendEmail("No pre-loaded list of psi-mi cv-mapping references in the validator web content", ExceptionUtils.getFullStackTrace(e));
            throw e;
        }
        else if (psiMiCvMapping.getCvReferenceList().getCvReference() == null){
            String body = "The psi-mi cv-mapping references in the validatorWebContent cannot be null. \n" +
                    "The validator will not be able to validate controlled vocabulary usages.";
            NullPointerException e = new NullPointerException(body);
            context.sendEmail("No pre-loaded psi-mi cv-mapping references in the validator web content", ExceptionUtils.getFullStackTrace(e));
            throw e;
        }
        else if (psiMiCvMapping.getCvReferenceList().getCvReference().isEmpty()){
            String body = "The list of psi-mi cv-mapping references in the validatorWebContent cannot be empty. \n" +
                    "The validator will not be able to validate controlled vocabulary usages.";
            IllegalArgumentException e = new IllegalArgumentException(body);
            context.sendEmail("Empty list of psi-mi cv-mapping references in the validator web content", ExceptionUtils.getFullStackTrace(e));
            throw e;
        }
        this.psiMiCvMapping = psiMiCvMapping;
    }

    /**
     *
     * @return the Ontology manager for PSI-MI
     */
    public OntologyManager getPsiMiOntologyManager() {
        return psiMiOntologyManager;
    }

    /**
     * Sets the Ontology manager for PSI-MI. Checks it is not null
     * @param psiMiOntologyManager
     */
    private void setPsiMiOntologyManager(OntologyManager psiMiOntologyManager) {
        ValidatorWebContext context = ValidatorWebContext.getInstance();

        if (psiMiOntologyManager == null){
            String body = "A problem occurred when uploading the psi-mi ontology and the ontology manager is null. \n" +
                    "The validator will not be able to validate psi-mi files.";
            NullPointerException e = new NullPointerException(body);
            context.sendEmail("Ontology Manager is null", ExceptionUtils.getFullStackTrace(e));
            throw e;
        }
        this.psiMiOntologyManager = psiMiOntologyManager;
    }

    /**
     *
     * @return the CVMapping for PSI-PAR
     */
    public CvMapping getPsiParCvMapping() {
        return psiParCvMapping;
    }

    /**
     * Sets the CVMapping for PSI-PAR. Checks that it is not null and the list of CVRules and CVReferences should neither be null nor be empty.
     * @param psiParCvMapping
     */
    private void setPsiParCvMapping(CvMapping psiParCvMapping) {
        ValidatorWebContext context = ValidatorWebContext.getInstance();

        if (psiParCvMapping == null){
            String body = "The psi-par CvMapping in the validatorWebContent cannot be null. \n" +
                    "The validator will not be able to validate controlled vocabulary usages.";
            NullPointerException e = new NullPointerException(body);
            context.sendEmail("Psi-par cvMapping is null in the validator web content", ExceptionUtils.getFullStackTrace(e));
            throw e;
        }
        else if (psiParCvMapping.getCvMappingRuleList() == null){
            String body = "The list of psi-par cv-mapping rules in the validatorWebContent cannot be null. \n" +
                    "The validator will not be able to validate controlled vocabulary usages.";
            NullPointerException e = new NullPointerException(body);
            context.sendEmail("No pre-loaded list of psi-par cv-mapping rules in the validator web content", ExceptionUtils.getFullStackTrace(e));
            throw e;
        }
        else if (psiParCvMapping.getCvMappingRuleList().getCvMappingRule() == null){
            String body = "The psi-par cv-mapping rules in the validatorWebContent cannot be null. \n" +
                    "The validator will not be able to validate controlled vocabulary usages.";
            NullPointerException e = new NullPointerException(body);
            context.sendEmail("No pre-loaded psi-par cv-mapping rules in the validator web content", ExceptionUtils.getFullStackTrace(e));
            throw e;
        }
        else if (psiParCvMapping.getCvMappingRuleList().getCvMappingRule().isEmpty()){
            String body = "The list of psi-par cv-mapping rules in the validatorWebContent cannot be empty. \n" +
                    "The validator will not be able to validate controlled vocabulary usages.";
            IllegalArgumentException e = new IllegalArgumentException(body);
            context.sendEmail("Empty list of psi-par cv-mapping rules in the validator web content", ExceptionUtils.getFullStackTrace(e));
            throw e;
        }
        else if (psiParCvMapping.getCvReferenceList() == null){
            String body = "The list of psi-par cv-mapping references in the validatorWebContent cannot be null. \n" +
                    "The validator will not be able to validate controlled vocabulary usages.";
            NullPointerException e = new NullPointerException(body);
            context.sendEmail("No pre-loaded list of psi-par cv-mapping references in the validator web content", ExceptionUtils.getFullStackTrace(e));
            throw e;
        }
        else if (psiParCvMapping.getCvReferenceList().getCvReference() == null){
            String body = "The psi-par cv-mapping references in the validatorWebContent cannot be null. \n" +
                    "The validator will not be able to validate controlled vocabulary usages.";
            NullPointerException e = new NullPointerException(body);
            context.sendEmail("No pre-loaded psi-par cv-mapping references in the validator web content", ExceptionUtils.getFullStackTrace(e));
            throw e;
        }
        else if (psiParCvMapping.getCvReferenceList().getCvReference().isEmpty()){
            String body = "The list of psi-par cv-mapping references in the validatorWebContent cannot be empty. \n" +
                    "The validator will not be able to validate controlled vocabulary usages.";
            IllegalArgumentException e = new IllegalArgumentException(body);
            context.sendEmail("Empty list of psi-par cv-mapping references in the validator web content", ExceptionUtils.getFullStackTrace(e));
            throw e;
        }
        this.psiParCvMapping = psiParCvMapping;
    }

    /**
     *
     * @return the Ontology manager for PSI-PAR
     */
    public OntologyManager getPsiParOntologyManager() {
        return psiParOntologyManager;
    }

    /**
     * Sets the Ontology manager for PSI-PAR. Checks it is not null
     * @param psiParOntologyManager
     */
    private void setPsiParOntologyManager(OntologyManager psiParOntologyManager) {
        ValidatorWebContext context = ValidatorWebContext.getInstance();

        if (psiParOntologyManager == null){
            String body = "A problem occurred when uploading the psi-par ontology and the ontology manager is null. \n" +
                    "The validator will not be able to validate psi-par files.";
            NullPointerException e = new NullPointerException(body);
            context.sendEmail("Ontology Manager is null", ExceptionUtils.getFullStackTrace(e));
            throw e;
        }
        this.psiParOntologyManager = psiParOntologyManager;
    }

    /**
     * Sets up the environment variables to re-use for PSI-PAR validation
     * @param factory : the validator factory
     * @throws ValidatorWebContextException
     */
    private void setUpPsiParValidatorEnvironments(ValidatorFactory factory) throws ValidatorWebContextException {
        Mi25Validator validator = factory.getReInitialisedValidator(ValidationScope.CV_ONLY, DataModel.PSI_PAR);
        setPsiParOntologyManager(validator.getOntologyMngr());
        ValidatorWebContext context = ValidatorWebContext.getInstance();

        InputStream cvConfig = Mi25Validator.class.getClassLoader().getResourceAsStream( ValidatorFactory.getPsiParCvMapping() );
        try {
            setPsiParCvMapping(cvRulesReader.read(cvConfig));
            //preLoadOntologySynonyms(this.psiParCvMapping, this.psiParOntologyManager);
        } catch (CvRuleReaderException e) {
            String body = "A problem occurred when reading the psi-par cv-mapping rules. \n" +
                    "The validator will not be able to validate controlled-vocabulary usages.\n" + ExceptionUtils.getFullStackTrace(e);
            context.sendEmail("Cannot read the cv-mapping rules for psi-par", body);
        }
        finally {
            try {
                cvConfig.close();
            } catch (IOException e) {
                System.out.println(ExceptionUtils.getFullStackTrace(e));
            }
        }
    }

    /**
     * Sets up the environment variables to re-use for PSI-MI validation
     * @param factory : the validator factory
     * @throws ValidatorWebContextException
     */
    private void setUpPsiMiValidatorEnvironments(ValidatorFactory factory) throws ValidatorWebContextException {
        Mi25Validator validator = factory.getReInitialisedValidator(ValidationScope.MIMIX, DataModel.PSI_MI);
        setPsiMiOntologyManager(validator.getOntologyMngr());
        ValidatorWebContext context = ValidatorWebContext.getInstance();

        this.psiMiObjectRules.put(ValidationScope.MIMIX, validator.getObjectRules());

        validator = factory.getReInitialisedValidator(ValidationScope.IMEX, DataModel.PSI_MI);

        this.psiMiObjectRules.put(ValidationScope.IMEX, validator.getObjectRules());

        validator = factory.getReInitialisedValidator(ValidationScope.PSI_MI, DataModel.PSI_MI);

        this.psiMiObjectRules.put(ValidationScope.PSI_MI, validator.getObjectRules());

        InputStream cvConfig = Mi25Validator.class.getClassLoader().getResourceAsStream( ValidatorFactory.getPsiMiCvMapping() );
        try {
            setPsiMiCvMapping(cvRulesReader.read(cvConfig));
            //preLoadOntologySynonyms(this.psiMiCvMapping, this.psiMiOntologyManager);
        } catch (CvRuleReaderException e) {
            e.printStackTrace();
            String body = "A problem occurred when reading the psi-mi cv-mapping rules. \n" +
                    "The validator will not be able to validate controlled-vocabulary usages.\n" + ExceptionUtils.getFullStackTrace(e);
            context.sendEmail("Cannot read the cv-mapping rules for psi-mi", body);
        } finally {
            try {
                cvConfig.close();
            } catch (IOException e) {
                System.out.println(ExceptionUtils.getFullStackTrace(e));
            }
        }
    }

    /*private void preLoadOntologySynonyms(CvMapping cvMapping, OntologyManager ontologyManager){
        List<CvReference> cvReferenceList = cvMapping.getCvReferenceList().getCvReference();

        for (CvReference ref : cvReferenceList){
            String ontologyName = ref.getCvIdentifier();

            if (ontologyName != null){
                OntologyAccess access = ontologyManager.getOntologyAccess(ontologyName);

                if (access instanceof OlsOntology){
                    OlsOntology olsAccess = (OlsOntology) access;

                    olsAccess.preLoadAllOntologyTerms();
                }
            }
        }
    }*/

    /**
     * Sets up the PSI-MI scopes
     */
    private void setUpPsiMiScopes(){
        this.psiMiScopes.add(ValidationScope.SYNTAX);
        this.psiMiScopes.add(ValidationScope.CV_ONLY);
        this.psiMiScopes.add(ValidationScope.MIMIX);
        this.psiMiScopes.add(ValidationScope.IMEX);
    }

    /**
     * Sets up the PSI-PAR scopes
     */
    private void setUpPsiParScopes(){
        this.psiMiScopes.add(ValidationScope.SYNTAX);
        this.psiMiScopes.add(ValidationScope.CV_ONLY);
    }

    /**
     *
     * @return the collection of psi-mi cv-mapping rules extracted from the CvMapping object
     */
    public Collection<CvRule> getPsiMiCvRules(){
        CvRuleManager ruleManager = new CvRuleManager(this.psiMiOntologyManager, this.psiMiCvMapping);

        return ruleManager.getCvRules();
    }

    /**
     *
     * @return the collection of psi-par cv-mapping rules extracted from the CvMapping object
     */
    public Collection<CvRule> getPsiParCvRules(){
        CvRuleManager ruleManager = new CvRuleManager(this.psiParOntologyManager, this.psiParCvMapping);

        return ruleManager.getCvRules();
    }
}
