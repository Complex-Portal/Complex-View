/**
 * Copyright (c) 2006 The European Bioinformatics Institute, and others.
 * All rights reserved.
 */
package uk.ac.ebi.intact.services.validator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.trinidad.model.UploadedFile;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import uk.ac.ebi.faces.controller.BaseController;

/**
 * This is the managed bean that contains the model of the information show to the user. From this bean,
 * all the information shown is handled. It creates the reports, etc.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 * @since 2.0
 */
public class PsiValidatorBean extends BaseController {

    /**
     * Logging is an essential part of an application
     */
    private static final Log log = LogFactory.getLog(PsiValidatorBean.class);

    /**
     * If true, a local file is selected to be uploaded
     */
    private boolean uploadLocalFile;

    /**
     * The file to upload
     */
    private UploadedFile psiFile;

    /**
     * The URL to upload
     */
    private String psiUrl;

    /**
     * If we are viewing a report, this is the report viewed
     */
    private PsiReport currentPsiReport;

    /**
     * Constructor
     */
    public PsiValidatorBean()
    {
        this.uploadLocalFile = true;
    }

    /**
     * This is a valueChangeEvent. When the selection of File/URL is changed, this event is fired.
     * @param vce needed in valueChangeEvent methods. From it we get the new value
     */
    public void uploadTypeChanged(ValueChangeEvent vce)
    {
        String type = (String) vce.getNewValue();
        uploadLocalFile = type.equals("local");

        if (log.isDebugEnabled())
            log.debug("Upload type changed, is local file? "+uploadLocalFile);
    }

    /**
     * This is an event thrown when the Upload button has been clicked
     * @param evt
     */
    public void uploadFile(ActionEvent evt)
    {

        try
        {
            // we use a different upload method, depending on the user selection
            if (uploadLocalFile)
            {
                uploadFromLocalFile();
            }
            else
            {
                uploadFromUrl();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Reads the local file
     * @throws IOException if something has gone wrong with the file
     */
    private void uploadFromLocalFile() throws IOException {
        
        if (log.isInfoEnabled()) {
            log.info("Uploading local file: "+psiFile.getFilename());
        }

        // we create the input stream
        InputStream is = psiFile.getInputStream();

        // and now we can instantiate the builder to create the validation report,
        // using the name of the file and the stream.
        PsiReportBuilder builder = new PsiReportBuilder("a cute name for by report ;)", is);

        // we execute the method of the builder that actually creates the report
        this.currentPsiReport = builder.createPsiReport();
    }

    /**
     * Reads the file from a URL, so it can read locally and remotely
     * @throws IOException if something goes wrong with the file or the connection
     */
    private void uploadFromUrl() throws IOException
    {
        if (log.isInfoEnabled())
        {
           log.info("Uploading Url: "+psiUrl);
        }

        try
        {
            // we create the URL object with the string provided by the user in the form
            URL url = new URL(psiUrl);

            // we only want the name of the file, and not the whole URL.
            // Gets the last part of the URL
            String name = psiUrl.substring(psiUrl.lastIndexOf("/")+1, psiUrl.length());

            // and now we can instantiate the builder to create the validation report,
            // using the name of the file and the URL.
            PsiReportBuilder builder = new PsiReportBuilder(name, url);

            // we execute the method of the builder that actually creates the report
            this.currentPsiReport = builder.createPsiReport();
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }

    }

    /**
     * This method is a "validator" method. It has the arguments that JSF specifies for this kind of methods.
     * The objective is to validate the URL provided by the user, whether it is in the correct form
     * or the place where it points it does exist
     * @param context The JSF FacesContext
     * @param toValidate The UIComponent to validate (this is a UIInput component), the controller of the text box
     * @param value The value provided in the text box by the user
     */
    public void validateUrlFormat(FacesContext context,
                          UIComponent toValidate,
                          Object value)
    {
        if (log.isDebugEnabled())
        {
            log.debug("Validating URL: "+value);
        }

        // we put the current report to null.
        // This is done because we want to clear the existing report from the form.
        // The form renders the report part if this variable is not null
        currentPsiReport = null;

        URL url = null;

        // Our UIComponent is an instance of UIInput, which is the component behind the text box
        UIInput inputCompToValidate = (UIInput)toValidate;

        // We get the id of that component. Take into account that the id rendered in the HTML cannot
        // be the same that the real id of the component
        String toValidateClientId = inputCompToValidate.getClientId(context);

        try
        {
            // we create the url with the value provided. If a MalformedUrlException is thrown,
            // that means that the url does not have the appropiate form
            url = new URL((String)value);
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();

            // if it fails, we need to invalidate the component (this is the way to tell in JSF
            // that there has been an invalid value)
            inputCompToValidate.setValid(false);

            // we add the message error to the facesContext, using the clientId of the component.
            // This way, the message will be rendered in the expected place
            context.addMessage(toValidateClientId, new FacesMessage("Not a valid URL"));
            return;
        }

        try
        {
            // if the url is ok, we try to connect to it and open the stream
            url.openStream();
        }
        catch (IOException e)
        {
            e.printStackTrace();

            // if it fails, invalidate the component and add the error message shown to the user
            inputCompToValidate.setValid(false);
            context.addMessage(toValidateClientId, new FacesMessage("Unknown URL"));
        }

    }

    // ACCESSOR METHODS

    public boolean isUploadLocalFile()
    {
        return uploadLocalFile;
    }

    public void setUploadLocalFile(boolean uploadLocalFile)
    {
        this.uploadLocalFile = uploadLocalFile;
    }

    public UploadedFile getPsiFile()
    {
        return psiFile;
    }

    public void setPsiFile(UploadedFile psiFile)
    {
        this.psiFile = psiFile;
    }

    public String getPsiUrl()
    {
        return psiUrl;
    }

    public void setPsiUrl(String psiUrl)
    {
        this.psiUrl = psiUrl;
    }

    public PsiReport getCurrentPsiReport()
    {
        return currentPsiReport;
    }

    public void setCurrentPsiReport(PsiReport currentPsiReport)
    {
        this.currentPsiReport = currentPsiReport;
    }
}
