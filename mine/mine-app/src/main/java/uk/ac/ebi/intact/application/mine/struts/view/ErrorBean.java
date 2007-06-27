/*
 * Created on 23.07.2004
 */

package uk.ac.ebi.intact.application.mine.struts.view;

import org.apache.struts.action.ActionForm;

/**
 * @author Andreas Groscurth
 */
public class ErrorBean extends ActionForm {
    private String error;

    public ErrorBean()
    {
        
    }

    public ErrorBean(String error) {
        this.error = error;
    }

    /**
     * @return Returns the error.
     */
    public String getError() {
        return error;
    }
}