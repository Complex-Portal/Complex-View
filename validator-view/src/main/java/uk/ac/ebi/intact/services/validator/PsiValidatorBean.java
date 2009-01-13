/**
 * Copyright (c) 2006 The European Bioinformatics Institute, and others.
 * All rights reserved.
 */
package uk.ac.ebi.intact.services.validator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.trinidad.model.UploadedFile;
import org.apache.myfaces.trinidad.component.core.input.CoreInputText;
import org.apache.myfaces.orchestra.viewController.annotations.ViewController;
import org.apache.myfaces.orchestra.viewController.annotations.InitView;
import org.apache.myfaces.orchestra.viewController.annotations.PreRenderView;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.faces.controller.BaseController;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * This is the managed bean that contains the model of the information show to the user. From this bean,
 * all the information shown is handled. It creates the reports, etc.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 * @since 2.0
 */
@Controller("psiValidatorBean")
@Scope( "conversation.access" )
@ViewController(viewIds = "/start.xhtml")
public class PsiValidatorBean extends BaseController {

    public static final String URL_PARAM = "url";

    /**
     * Logging is an essential part of an application
     */
    private static final Log log = LogFactory.getLog(PsiValidatorBean.class);

    /**
     * If true, a local file is selected to be uploaded
     */
    private boolean uploadLocalFile;

    /**
     * The type of validation to be performed: syntax, cv, MIMIX, IMEx.
     */
    private ValidationScope validationScope;

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
    public PsiValidatorBean() {
        this.uploadLocalFile = true;
    }

    /**
     * This is a valueChangeEvent. When the selection of File/URL is changed, this event is fired.
     *
     * @param vce needed in valueChangeEvent methods. From it we get the new value
     */
    public void uploadTypeChanged(ValueChangeEvent vce) {
        String type = (String) vce.getNewValue();
        uploadLocalFile = type.equals("local");

        if (log.isDebugEnabled())
            log.debug("Upload type changed, is local file? " + uploadLocalFile);
    }

    /**
     * This is a valueChangeEvent. When the selection of File/URL is changed, this event is fired.
     *
     * @param vce needed in valueChangeEvent methods. From it we get the new value
     */
    public void validationScopeChanged(ValueChangeEvent vce) {
        String type = (String) vce.getNewValue();
        validationScope  = ValidationScope.forName( type );

        if (log.isDebugEnabled())
            log.debug("Validation scope changed to '" + validationScope + "'");
    }

    @PreRenderView
    public void initialParams() {
        FacesContext context = FacesContext.getCurrentInstance();

        String urlParam = context.getExternalContext().getRequestParameterMap().get(URL_PARAM);

        if (urlParam != null) {
            if (log.isInfoEnabled()) {
                log.info( "User submitted a request with data specified in the URL: " + urlParam );
            }

            uploadLocalFile=false;
            psiUrl = urlParam;
            try {
                uploadFromUrl();
            } catch (IOException e) {
                final String msg = "Failed to upload PSI data from given URL";
                FacesMessage message = new FacesMessage(msg);
                context.addMessage( "inputUrl", message );
            }
        }
    }

    // This is the method invoked when pressing the valdidate button, names after thte variable to be updated.
    public void onPsiFileUpload(ValueChangeEvent event) {

        System.out.println("PsiValidatorBean.psiFile: uploadLocalFile=" + uploadLocalFile);

        if(  uploadLocalFile ) {
            psiFile = (UploadedFile) event.getNewValue();
            if (psiFile != null) {
                FacesContext context = FacesContext.getCurrentInstance();
                FacesMessage message = new FacesMessage(
                        "Successfully uploaded file " + psiFile.getFilename() +
                                " (" + psiFile.getLength() + " bytes)");
                context.addMessage(event.getComponent().getClientId(context), message);

            }
        }
    }

    /**
     * Validates the data entered by the user upon pressing the validate button.
     * @param event
     */
    public void validate( ActionEvent event ) {
        try {
            if( uploadLocalFile ) {
                // we use a different upload method, depending on the user selection
                uploadFromLocalFile();
            } else {
                uploadFromUrl();
            }
        } catch (Throwable t) {
            final String msg = "Failed to upload from " + (uploadLocalFile ? "local file" : "URL");

            log.error(msg, t);

            FacesContext context = FacesContext.getCurrentInstance();
            FacesMessage message = new FacesMessage(msg);
            context.addMessage(event.getComponent().getClientId(context), message);
        }
    }

    /**
     * Reads the local file.
     *
     * @throws IOException if something has gone wrong with the file
     */
    private void uploadFromLocalFile() throws IOException {

        if( psiFile == null ) {
            throw new IllegalStateException("Failed to upload the file");
        }

        if (log.isInfoEnabled()) {
            log.info("Uploading local file: " + psiFile.getFilename());
        }

        // and now we can instantiate the builder to create the validation report,
        // using the name of the file and the stream.
        File f = storeAsTemporaryFile( psiFile.getInputStream());
        // we have the data on disk, clear memory
        psiFile.dispose();

        PsiReportBuilder builder = new PsiReportBuilder( psiFile.getFilename(), f, validationScope );

        // we execute the method of the builder that actually creates the report
        log.warn("About to start building the PSI report");
        
        this.currentPsiReport = builder.createPsiReport();
        if ( log.isWarnEnabled() ) {
            log.warn("After uploading a local file the report was " + (this.currentPsiReport == null ? "not present" : "present" ));
        }
    }

    /**
     * Store the content of the given input stream into a temporary file and return its descriptor.
     *
     * @param is the input stream to store.
     * @return a File descriptor describing a temporary file storing the content of the given input stream.
     * @throws IOException if an IO error occur.
     */
    private File storeAsTemporaryFile( InputStream is ) throws IOException {

        if ( is == null ) {
            throw new IllegalArgumentException( "You must give a non null InputStream" );
        }

        BufferedReader in = new BufferedReader( new InputStreamReader( is ) );

        // Create a temp file and write URL content in it.
        File tempDirectory = new File( System.getProperty( "java.io.tmpdir", "tmp" ) );
        if ( !tempDirectory.exists() ) {
            if ( !tempDirectory.mkdirs() ) {
                throw new IOException( "Cannot create temp directory: " + tempDirectory.getAbsolutePath() );
            }
        }

        long id = System.currentTimeMillis();

        File tempFile = File.createTempFile( "validator." + id, ".xml", tempDirectory );
        tempFile.deleteOnExit();

        log.info( "The file is temporary store as: " + tempFile.getAbsolutePath() );

        BufferedWriter out = new BufferedWriter( new FileWriter( tempFile ) );

        String line;
        while ( ( line = in.readLine() ) != null ) {
            out.write( line );
        }

        in.close();

        out.flush();
        out.close();

        return tempFile;
    }

    /**
     * Reads the file from a URL, so it can read locally and remotely
     *
     * @throws IOException if something goes wrong with the file or the connection
     */
    private void uploadFromUrl() throws IOException {

        if (log.isInfoEnabled()) {
            log.info("Uploading Url: " + psiUrl);
        }

        try {
            // we create the URL object with the string provided by the user in the form
            URL url = new URL(psiUrl);

            File f = storeAsTemporaryFile( url.openStream() );

            // we only want the name of the file, and not the whole URL.
            // Gets the last part of the URL
            String name = psiUrl.substring(psiUrl.lastIndexOf("/") + 1, psiUrl.length());

            // and now we can instantiate the builder to create the validation report,
            // using the name of the file and the URL.
            PsiReportBuilder builder = new PsiReportBuilder(name, url, f, validationScope);

            // we execute the method of the builder that actually creates the report
            log.warn("ABout to start building the PSI report");
            this.currentPsiReport = builder.createPsiReport();
            log.warn("After uploading a URL the report was " + (this.currentPsiReport == null ? "not present" : "present" ));
        }
        catch (MalformedURLException e) {
            log.error( "The given URL wasn't valid", e );
        }
    }

    /**
     * This method is a "validator" method. It has the arguments that JSF specifies for this kind of methods.
     * The objective is to validate the URL provided by the user, whether it is in the correct form
     * or the place where it points it does exist
     *
     * @param context    The JSF FacesContext
     * @param toValidate The UIComponent to validate (this is a UIInput component), the controller of the text box
     * @param value      The value provided in the text box by the user
     */
    public void validateUrlFormat(FacesContext context,
                                  UIComponent toValidate,
                                  Object value) {
        if (log.isDebugEnabled()) {
            log.debug("Validating URL: " + value);
        }

        // we put the current report to null.
        // This is done because we want to clear the existing report from the form.
        // The form renders the report part if this variable is not null
        currentPsiReport = null;

        URL url = null;

        // Our UIComponent is an instance of UIInput, which is the component behind the text box
        CoreInputText inputCompToValidate = (CoreInputText) toValidate;

        // We get the id of that component. Take into account that the id rendered in the HTML cannot
        // be the same that the real id of the component
        String toValidateClientId = inputCompToValidate.getClientId(context);

        try {
            // we create the url with the value provided. If a MalformedUrlException is thrown,
            // that means that the url does not have the appropiate form
            url = new URL((String) value);
        }
        catch (MalformedURLException e) {
            log.warn("Invalid URL given by the user: " + value, e);

            // if it fails, we need to invalidate the component (this is the way to tell in JSF
            // that there has been an invalid value)
            inputCompToValidate.setValid(false);

            // we add the message error to the facesContext, using the clientId of the component.
            // This way, the message will be rendered in the expected place
            context.addMessage(toValidateClientId, new FacesMessage("The given URL was not valid."));
            return;
        }

        try {
            // if the url is ok, we try to connect to it and open the stream
            url.openStream();
        }
        catch (Throwable e) {
            log.error("Error while validating the URL.", e);

            // if it fails, invalidate the component and add the error message shown to the user
            inputCompToValidate.setValid(false);
            context.addMessage(toValidateClientId, new FacesMessage("Could not read URL content."));
        }
    }

    // ACCESSOR METHODS

    public boolean isUploadLocalFile() {
        return uploadLocalFile;
    }

    public void setUploadLocalFile(boolean uploadLocalFile) {
        this.uploadLocalFile = uploadLocalFile;
    }

    public UploadedFile getPsiFile() {
        return psiFile;
    }

    public void setPsiFile(UploadedFile psiFile) {
        this.psiFile = psiFile;
    }

    public String getPsiUrl() {
        return psiUrl;
    }

    public void setPsiUrl(String psiUrl) {
        this.psiUrl = psiUrl;
    }

    public PsiReport getCurrentPsiReport() {
        return currentPsiReport;
    }

    public void setCurrentPsiReport(PsiReport currentPsiReport) {
        this.currentPsiReport = currentPsiReport;
    }
}
