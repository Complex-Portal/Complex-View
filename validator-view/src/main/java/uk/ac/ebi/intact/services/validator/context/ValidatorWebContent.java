package uk.ac.ebi.intact.services.validator.context;

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

    private Date lastOntologyLoadDate;
    private Map<ValidationScope, Set<ObjectRule>> psiMiObjectRules = new HashMap<ValidationScope, Set<ObjectRule>>();
    private CvRuleManager psiMiRuleManager;
    private CvRuleManager psiParRuleManager;
    private OntologyManager psiMiOntologymanager;
    private OntologyManager psiParOntologymanager;
    private ValidatorFactory factory;

    private List<ValidationScope> psiParScopes = new ArrayList<ValidationScope>();
    private List<ValidationScope> psiMiScopes = new ArrayList<ValidationScope>();

    public ValidatorWebContent(){
        factory = new ValidatorFactory();
        
        this.lastOntologyLoadDate = new Date(System.currentTimeMillis());
        setUpPsiMiScopes();
        setUpPsiParScopes();
        setUpPsiParValidatorEnvironments();
        setUpPsiMiValidatorEnvironments();
    }

    public Date getLastOntologyLoadDate() {
        return lastOntologyLoadDate;
    }

    public void setLastOntologyLoadDate(Date lastOntologyLoadDate) {
        this.lastOntologyLoadDate = lastOntologyLoadDate;
    }

    public Map<ValidationScope, Set<ObjectRule>> getPsiMiObjectRules() {
        return psiMiObjectRules;
    }

    public void setPsiMiObjectRules(Map<ValidationScope, Set<ObjectRule>> psiMiObjectRules) {
        this.psiMiObjectRules = psiMiObjectRules;
    }

    public CvRuleManager getPsiMiRuleManager() {
        return psiMiRuleManager;
    }

    public void setPsiMiRuleManager(CvRuleManager psiMiRuleManager) {
        this.psiMiRuleManager = psiMiRuleManager;
    }

    public OntologyManager getPsiMiOntologymanager() {
        return psiMiOntologymanager;
    }

    public void setPsiMiOntologymanager(OntologyManager psiMiOntologymanager) {
        this.psiMiOntologymanager = psiMiOntologymanager;
    }

    public CvRuleManager getPsiParRuleManager() {
        return psiParRuleManager;
    }

    public void setPsiParRuleManager(CvRuleManager psiParRuleManager) {
        this.psiParRuleManager = psiParRuleManager;
    }

    public OntologyManager getPsiParOntologymanager() {
        return psiParOntologymanager;
    }

    public void setPsiParOntologymanager(OntologyManager psiParOntologymanager) {
        this.psiParOntologymanager = psiParOntologymanager;
    }

    public void setUpPsiParValidatorEnvironments(){
        Mi25Validator validator = this.factory.getReInitialisedValidator(ValidationScope.CV_ONLY, DataModel.PSI_PAR);

        setPsiParOntologymanager(validator.getOntologyMngr());
        setPsiParRuleManager(validator.getCvRuleManager());
    }

    public void setUpPsiMiValidatorEnvironments(){
        Mi25Validator validator = this.factory.getReInitialisedValidator(ValidationScope.MIMIX, DataModel.PSI_MI);
        setPsiParOntologymanager(validator.getOntologyMngr());
        setPsiParRuleManager(validator.getCvRuleManager());

        this.psiMiObjectRules.put(ValidationScope.MIMIX, validator.getObjectRules());

        validator = this.factory.getReInitialisedValidator(ValidationScope.IMEX, DataModel.PSI_MI);
        setPsiMiOntologymanager(validator.getOntologyMngr());
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
}
