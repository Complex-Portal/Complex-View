package uk.ac.ebi.intact.services.validator.context;

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

    public static ValidatorWebContext getInstance() {

        return ourInstance;
    }

    private ValidatorWebContext(){
        this.validatorWebContent = new ValidatorWebContent();
    }

    public synchronized ValidatorWebContent getValidatorWebContent() {
        return validatorWebContent;
    }

    public synchronized void setValidatorWebContent(ValidatorWebContent validatorWebContent) {
        this.validatorWebContent = validatorWebContent;
    }
}
