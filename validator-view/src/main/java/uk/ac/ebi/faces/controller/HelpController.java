package uk.ac.ebi.faces.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.orchestra.viewController.annotations.ViewController;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import psidev.psi.tools.validator.rules.codedrule.ObjectRule;
import psidev.psi.tools.validator.rules.cvmapping.CvRule;
import uk.ac.ebi.intact.services.validator.ValidationScope;
import uk.ac.ebi.intact.services.validator.context.ValidatorWebContent;
import uk.ac.ebi.intact.services.validator.context.ValidatorWebContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * TODO comment this
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>01-Jul-2010</pre>
 */
@Controller( "helpController" )
@Scope( "conversation.access" )
@ViewController( viewIds = {"/help.xhtml", "/start.xhtml"})
public class HelpController extends BaseController {

    private static final Log log = LogFactory.getLog(HelpController.class);

    public List<ObjectRule> getImexRules(){
        ValidatorWebContext validatorContext = ValidatorWebContext.getInstance();

        ValidatorWebContent validatorContent = validatorContext.getValidatorWebContent();

        Set<ObjectRule> rules = validatorContent.getPsiMiObjectRules().get(ValidationScope.IMEX);

        List<ObjectRule> imexRules = extractImexRulesFrom(rules);
        imexRules.addAll(extractMimixRulesFrom(rules));
        imexRules.addAll(extractObjectRulesFrom(rules));

        return imexRules;
    }

    public List<ObjectRule> getMimixRules(){
        ValidatorWebContext validatorContext = ValidatorWebContext.getInstance();

        ValidatorWebContent validatorContent = validatorContext.getValidatorWebContent();

        Set<ObjectRule> rules = validatorContent.getPsiMiObjectRules().get(ValidationScope.MIMIX);

        List<ObjectRule> mimixRules = extractMimixRulesFrom(rules);
        mimixRules.addAll(extractObjectRulesFrom(rules));

        return mimixRules;
    }

    public List<CvRule> getPsiMiCvRules(){
        ValidatorWebContext validatorContext = ValidatorWebContext.getInstance();

        ValidatorWebContent validatorContent = validatorContext.getValidatorWebContent();

        return getCvRules(validatorContent.getPsiMiCvRules());
    }

    public List<CvRule> getPsiParCvRules(){
        ValidatorWebContext validatorContext = ValidatorWebContext.getInstance();

        ValidatorWebContent validatorContent = validatorContext.getValidatorWebContent();

        return getCvRules(validatorContent.getPsiParCvRules());
    }

    public List<CvRule> getCvRules(Collection<CvRule> rules){
        ValidatorWebContext validatorContext = ValidatorWebContext.getInstance();

        ValidatorWebContent validatorContent = validatorContext.getValidatorWebContent();

        List<CvRule> cvRules = new ArrayList<CvRule>();

        for (CvRule rule : rules){
            cvRules.add(rule);
        }
        return cvRules;
    }

    private List<ObjectRule> extractImexRulesFrom(Set<ObjectRule> rules){
        List<ObjectRule> imexRules = new ArrayList<ObjectRule>();

        for (ObjectRule rule : rules){
            if (ValidationScope.IMEX.toString().equalsIgnoreCase(rule.getScope())){
                imexRules.add(rule);
            }
        }

        return imexRules;
    }

    private List<ObjectRule> extractMimixRulesFrom(Set<ObjectRule> rules){
        List<ObjectRule> mimixRules = new ArrayList<ObjectRule>();

        for (ObjectRule rule : rules){
            if (ValidationScope.MIMIX.toString().equalsIgnoreCase(rule.getScope())){
                mimixRules.add(rule);
            }
        }

        return mimixRules;
    }

    private List<ObjectRule> extractObjectRulesFrom(Set<ObjectRule> rules){
        List<ObjectRule> simpleRules = new ArrayList<ObjectRule>();

        for (ObjectRule rule : rules){
            if (!ValidationScope.MIMIX.toString().equalsIgnoreCase(rule.getScope()) && !ValidationScope.IMEX.toString().equalsIgnoreCase(rule.getScope())){
                simpleRules.add(rule);
            }
        }

        return simpleRules;
    }
}
