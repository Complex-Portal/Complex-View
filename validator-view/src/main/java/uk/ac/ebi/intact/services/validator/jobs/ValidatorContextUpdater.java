package uk.ac.ebi.intact.services.validator.jobs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.exception.ExceptionUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import psidev.psi.tools.ontology_manager.OntologyManager;
import psidev.psi.tools.ontology_manager.impl.local.OntologyLoaderException;
import uk.ac.ebi.intact.services.validator.context.ValidatorWebContent;
import uk.ac.ebi.intact.services.validator.context.ValidatorWebContext;

/**
 * TODO comment this
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>28-Jun-2010</pre>
 */

public class ValidatorContextUpdater implements Job {
    /**
     * Logging, logging!
     */
    private static final Log log = LogFactory.getLog(ValidatorContextUpdater.class);

    public ValidatorContextUpdater(){

    }

    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        ValidatorWebContext validatorContext = ValidatorWebContext.getInstance();
        ValidatorWebContent validatorContent = validatorContext.getValidatorWebContent();

        try {
            boolean isPsiMiUpToDate = checkOntology(validatorContent, validatorContent.getPsiMiOntologyManager());
            boolean isPsiParUpToDate = checkOntology(validatorContent, validatorContent.getPsiParOntologyManager());

            ValidatorWebContent newWebContent = null;

            if (isPsiMiUpToDate && !isPsiParUpToDate){
                newWebContent = new ValidatorWebContent(validatorContent.getPsiMiOntologyManager(), validatorContent.getPsiMiRuleManager(), validatorContent.getPsiMiObjectRules());
            }
            else if (!isPsiMiUpToDate && isPsiParUpToDate){
                newWebContent = new ValidatorWebContent(validatorContent.getPsiParOntologyManager(), validatorContent.getPsiParRuleManager());
            }
            else if (!isPsiMiUpToDate && !isPsiParUpToDate){
                newWebContent = new ValidatorWebContent();
            }

            if (newWebContent != null){
                if (newWebContent.getPsiMiOntologyManager() == null){
                    String body = "A new update of the psi-mi ontology has been done recently and a problem occurred " +
                            "when re-uploading the PSI-MI ontology." +
                            " As the new ontology manager is null, we will keep the previous one and will not be able to use the new ontology. \n";
                    NullPointerException e = new NullPointerException(body);
                    validatorContent.sendEmail("Update of the psi-mi ontology failed", ExceptionUtils.getFullStackTrace(e));
                    throw e;
                }
                else if (newWebContent.getPsiParOntologyManager() == null){
                    String body = "A new update of the psi-par ontology has been done recently and a problem occurred " +
                            "when re-uploading the PSI-PAR ontology." +
                            " As the new ontology manager is null, we will keep the previous one and will not be able to use the new ontology. \n";
                    NullPointerException e = new NullPointerException(body);
                    validatorContent.sendEmail("Update of the psi-par ontology failed", ExceptionUtils.getFullStackTrace(e));
                    throw e;
                }
                else {
                    validatorContext.setValidatorWebContent(newWebContent);
                }
            }

        } catch (OntologyLoaderException e) {
            String body = "We couldn't get the last ontology update date. If a new update has been done " +
                    "recently, we will not be able to load the new ontology terms. \n \n" +
                    ExceptionUtils.getFullStackTrace(e);
            log.warn(body);
            validatorContent.sendEmail("Date of last ontology update not available", body);
        }
    }

    private boolean checkOntology(ValidatorWebContent validatorContent, OntologyManager ontologyManager) throws OntologyLoaderException {

        if (ontologyManager == null){
            String body = "A problem occurred when uploading the ontology and " +
                    "consequently the ontology manager is null. \n" +
                    "The validator will not be able to work properly.";
            NullPointerException e = new NullPointerException(body);
            validatorContent.sendEmail("Ontology Manager is null", ExceptionUtils.getFullStackTrace(e));
            throw e;
        }
        else {
            return ontologyManager.isUpToDate();
        }
    }
}
