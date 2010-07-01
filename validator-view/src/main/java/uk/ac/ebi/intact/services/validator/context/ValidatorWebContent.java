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

import java.io.InputStream;
import java.util.*;

/**
 * TODO comment this
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>25-Jun-2010</pre>
 */

public class ValidatorWebContent {

    CvRuleReader cvRulesReader;

    private Map<ValidationScope, Set<ObjectRule>> psiMiObjectRules = new HashMap<ValidationScope, Set<ObjectRule>>();
    private CvMapping psiMiCvMapping;
    private CvMapping psiParCvMapping;
    private OntologyManager psiMiOntologyManager;
    private OntologyManager psiParOntologyManager;

    private List<ValidationScope> psiParScopes = new ArrayList<ValidationScope>();
    private List<ValidationScope> psiMiScopes = new ArrayList<ValidationScope>();

    public ValidatorWebContent(){
        cvRulesReader = new CvRuleReader();

        ValidatorFactory factory = new ValidatorFactory();

        setUpPsiMiScopes();
        setUpPsiParScopes();
        setUpPsiParValidatorEnvironments(factory);
        setUpPsiMiValidatorEnvironments(factory);
    }

    public ValidatorWebContent(OntologyManager psiMiOntologyManager, CvMapping psiMiRuleManager, Map<ValidationScope, Set<ObjectRule>> psiMiObjectRules){

        ValidatorFactory factory = new ValidatorFactory();

        setUpPsiMiScopes();
        setUpPsiParScopes();
        setUpPsiParValidatorEnvironments(factory);

        setPsiMiOntologyManager(psiMiOntologyManager);
        setPsiMiCvMapping(psiMiRuleManager);
        setPsiMiObjectRules(psiMiObjectRules);
    }

    public ValidatorWebContent(OntologyManager psiParOntologyManager, CvMapping psiParRuleManager){

        ValidatorFactory factory = new ValidatorFactory();

        setUpPsiMiScopes();
        setUpPsiParScopes();
        setUpPsiMiValidatorEnvironments(factory);

        setPsiParOntologyManager(psiParOntologyManager);
        setPsiParCvMapping(psiParRuleManager);
    }

    public Map<ValidationScope, Set<ObjectRule>> getPsiMiObjectRules() {
        return psiMiObjectRules;
    }

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

    public CvMapping getPsiMiCvMapping() {
        return psiMiCvMapping;
    }

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
        this.psiMiCvMapping = psiMiCvMapping;
    }

    public OntologyManager getPsiMiOntologyManager() {
        return psiMiOntologyManager;
    }

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

    public CvMapping getPsiParCvMapping() {
        return psiParCvMapping;
    }

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
        this.psiParCvMapping = psiParCvMapping;
    }

    public OntologyManager getPsiParOntologyManager() {
        return psiParOntologyManager;
    }

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

    private void setUpPsiParValidatorEnvironments(ValidatorFactory factory){
        Mi25Validator validator = factory.getReInitialisedValidator(ValidationScope.CV_ONLY, DataModel.PSI_PAR);
        setPsiParOntologyManager(validator.getOntologyMngr());
        ValidatorWebContext context = ValidatorWebContext.getInstance();

        InputStream cvConfig = Mi25Validator.class.getClassLoader().getResourceAsStream( ValidatorFactory.getPsiParCvMapping() );
        try {
            setPsiParCvMapping(cvRulesReader.read(cvConfig));
        } catch (CvRuleReaderException e) {
            String body = "A problem occurred when reading the psi-par cv-mapping rules. \n" +
                    "The validator will not be able to validate controlled-vocabulary usages.\n" + ExceptionUtils.getFullStackTrace(e);
            context.sendEmail("Cannot read the cv-mapping rules for psi-par", body);
        }
    }

    private void setUpPsiMiValidatorEnvironments(ValidatorFactory factory){
        Mi25Validator validator = factory.getReInitialisedValidator(ValidationScope.MIMIX, DataModel.PSI_MI);
        setPsiMiOntologyManager(validator.getOntologyMngr());
        ValidatorWebContext context = ValidatorWebContext.getInstance();

        this.psiMiObjectRules.put(ValidationScope.MIMIX, validator.getObjectRules());

        validator = factory.getReInitialisedValidator(ValidationScope.IMEX, DataModel.PSI_MI);

        this.psiMiObjectRules.put(ValidationScope.IMEX, validator.getObjectRules());

        InputStream cvConfig = Mi25Validator.class.getClassLoader().getResourceAsStream( ValidatorFactory.getPsiMiCvMapping() );
        try {
            setPsiMiCvMapping(cvRulesReader.read(cvConfig));
        } catch (CvRuleReaderException e) {
            String body = "A problem occurred when reading the psi-mi cv-mapping rules. \n" +
                    "The validator will not be able to validate controlled-vocabulary usages.\n" + ExceptionUtils.getFullStackTrace(e);
            context.sendEmail("Cannot read the cv-mapping rules for psi-mi", body);
        }
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

    public Collection<CvRule> getPsiMiCvRules(){
        CvRuleManager ruleManager = new CvRuleManager(this.psiMiOntologyManager, this.psiMiCvMapping);

        return ruleManager.getCvRules();
    }

    public Collection<CvRule> getPsiParCvRules(){
        CvRuleManager ruleManager = new CvRuleManager(this.psiParOntologyManager, this.psiParCvMapping);

        return ruleManager.getCvRules();
    }
}
