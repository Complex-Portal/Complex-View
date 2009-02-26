/**
 * Copyright (c) 2006 The European Bioinformatics Institute, and others.
 * All rights reserved.
 */
package uk.ac.ebi.intact.services.validator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.trinidad.model.DefaultBoundedRangeModel;
import org.apache.myfaces.trinidad.model.BoundedRangeModel;
import psidev.psi.mi.validator.ValidatorReport;
import psidev.psi.mi.validator.extension.Mi25Validator;
import psidev.psi.tools.validator.MessageLevel;
import psidev.psi.tools.validator.ValidatorMessage;
import psidev.psi.tools.validator.preferences.UserPreferences;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;

/**
 * This class is the responsible of reading and validating the PSI File and creating a validation report
 * with the information found.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id: PsiReportBuilder.java 6474 2006-10-20 15:24:44Z baranda $
 * @since <pre>12-Jun-2006</pre>
 */
public class PsiReportBuilder {
    /**
     * Logging, logging!
     */
    private static final Log log = LogFactory.getLog(PsiReportBuilder.class);

    /**
     * Name of the PSI file.
     */
    private String name;

    /**
     * If using a URL, the URL.
     */
    private URL url;

    /**
     * If using a File, this value won't be null.
     */
    private File file;

    private ValidationScope validationScope;

    private DataModel model;

    /**
     * Handy enumeration to avoid null checks on the previous attributes, when trying to determine
     * whether the info comes from URL or a stream.
     */
    private enum SourceType {
        URL, FILE
    }

    /**
     * The current type used.
     */
    private SourceType currentSourceType;

    private DefaultBoundedRangeModel progressModel;

    /**
     * Creates a PsiReportBuilder instance using an URL
     *
     * @param name The name of the file, only needed for information purposes
     * @param url  The URL with the PSI xml
     */
    public PsiReportBuilder( String name, URL url, File tempFile, DataModel model, ValidationScope validationScope, DefaultBoundedRangeModel progressModel ) {
        this.name = name;
        this.url = url;
        this.file = tempFile;
        this.validationScope = validationScope;
        this.model = model;
        this.progressModel = progressModel;
        
        this.currentSourceType = SourceType.URL;
    }

    /**
     * Creates a PsiReportBuilder instance using an InputStream
     *
     * @param name The name of the file, only needed for information purposes
     * @param file The file containing the PSI xml. This InputStream has to be
     *             resettable in order to build the report properly. The stream will be reset a few times, so the
     *             information is parsed in the different validation phases
     */
    public PsiReportBuilder(String name, File file, DataModel model, ValidationScope validationScope, DefaultBoundedRangeModel progressModel) {
        this.name = name;
        this.file = file;
        this.validationScope = validationScope;
        this.model = model;
        this.progressModel = progressModel;

        this.currentSourceType = SourceType.FILE;
    }

    /**
     * Creates the PSI report
     *
     * @return the report created, after all the validations
     * @throws IOException thrown if there is something wrong with the I/O stuff
     */
    public PsiReport createPsiReport() throws IOException {
        // new instance of the report, that will be filled with the validation information
        PsiReport report = new PsiReport(name);

        // second validation: checks that the semantics is right
        try {

            if ( log.isDebugEnabled() ) {
                log.debug( "The model in use is: " + model );
            }

            if( model.equals( DataModel.PSI_MI ) ) {
                 validatePsiMiFile(report, file);
            } else if( model.equals( DataModel.PSI_PAR ) ) {
                 validatePsiParFile(report, file);
            } else {
                throw new IllegalStateException( "Unknown data model: " + model );
            }

        } catch (Throwable t) {
            log.error("Unexpected error thrown", t);

            FacesContext context = FacesContext.getCurrentInstance();
            FacesMessage facesMessage = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                                         "Sorry, an unexpected error occur, please contact the " +
                                                         "administrator of this web site if the issue persist.",
                                                         t.getMessage());
            context.addMessage(null, facesMessage);

            return report;
        }

        log.debug("Completed file validation ... about to build the report now ...");

        if (report.isXmlSyntaxValid()) {
            // creating the view if the model supports it
            if( model.hasHtmlViewBuilder() ) {
                model.createHtmlView( report, getInputStream() );
            }
        } else {
            //if the xml validation is wrong, the second validation won't be run
            report.setSemanticsStatus( PsiReport.NOT_RUN ); // not checked, XML syntax needs to be valid first
        }

        return report;
    }

    /**
     * Returns the InputStream, independently of the origin of the information
     *
     * @return the stream with the info
     * @throws IOException throw when there are I/O problems
     */
    private InputStream getInputStream() throws IOException {
        // uses the currentSourceType to determine how to open and return the inputStream
        if (currentSourceType == SourceType.URL) {
            return url.openStream();
        } else {
            return new FileInputStream( file );
        }
    }

    /**
     * Validates PSI-MI data.
     *
     * @param report the PsiReport to complete
     * @param file   the psi xml file
     */
    private void validatePsiMiFile(PsiReport report, File file) {

        // Printwriter to get the stacktrace messages
        StringWriter sw = new StringWriter(1024);

        try {
            // We read the configuration file, included inside the jar
            InputStream ontologyCfg = PsiReportBuilder.class.getResourceAsStream( "/validator/config/psi_mi/ontologies.xml" );
            InputStream cvMappingCfg = null;
            InputStream ruleCfg = null;

            if ( log.isInfoEnabled() ) {
                log.info( "Model: " + model );
                log.info( "Selected validation scope: " + validationScope );
            }

            if( validationScope == null ) {
                // set default value
                log.warn( "The application didn't get a valid validation scope (null), setting MI default to MIMIx." );
                validationScope = ValidationScope.MIMIX;
            }

            switch( validationScope ) {
                case SYNTAX:
                    break;

                case CV_ONLY:
                    cvMappingCfg = PsiReportBuilder.class.getResourceAsStream( "/validator/config/psi_mi/cv-mapping.xml" );
                    break;

                case MIMIX:
                    cvMappingCfg = PsiReportBuilder.class.getResourceAsStream( "/validator/config/psi_mi/cv-mapping.xml" );
                    ruleCfg = PsiReportBuilder.class.getResourceAsStream( "/validator/config/psi_mi/mimix-object-rules.xml" );
                    break;

                case IMEX:
                    cvMappingCfg = PsiReportBuilder.class.getResourceAsStream( "/validator/config/psi_mi/cv-mapping.xml" );
                    ruleCfg = PsiReportBuilder.class.getResourceAsStream( "/validator/config/psi_mi/imex-object-rules.xml" );
                    break;

                default:
                    throw new IllegalStateException( "Unsupported validation scope: '"+ validationScope +"', the application is not correctly configured." );
            }

            log.info( "Is the Cv mapping file null: " + ( cvMappingCfg == null ) );

            // run validation
            validatePsiFile( report, file, ontologyCfg, cvMappingCfg, ruleCfg );

        } catch (Throwable t) {

            log.error( "An error occured while configuring the MI validator", t );

            FacesContext context = FacesContext.getCurrentInstance();
            FacesMessage message = new FacesMessage( "An error occured while validating your data: " + t.getMessage() );
            context.addMessage( null, message );
        }
    }

    /**
     * Validates PSI-PAR data.
     *
     * @param report the PsiReport to complete
     * @param file   the psi xml file
     */
    private void validatePsiParFile(PsiReport report, File file) {

        try {
            // We read the configuration file, included inside the jar
            InputStream ontologyCfg = PsiReportBuilder.class.getResourceAsStream( "/validator/config/psi_par/ontologies.xml" );
            InputStream cvMappingCfg = null;
            InputStream ruleCfg = null;

            if ( log.isInfoEnabled() ) {
                log.info( "Model: " + model );
                log.info( "Selected validation scope: " + validationScope );
            }

            if( validationScope == null ) {
                // set default value
                log.warn( "The application didn't get a valid validation scope (null), setting PAR default to CV Mapping." );
                validationScope = ValidationScope.CV_ONLY;
            }

            switch( validationScope ) {
                case SYNTAX:
                    break;

                case CV_ONLY:
                    cvMappingCfg = PsiReportBuilder.class.getResourceAsStream( "/validator/config/psi_par/cv-mapping.xml" );
                    break;

                default:
                    throw new IllegalStateException( "Unsupported validation scope: '"+ validationScope +"', the application is not correctly configured." );
            }

            log.info( "Is the Cv mapping file null: " + ( cvMappingCfg == null ) );

            // run validation
            validatePsiFile( report, file, ontologyCfg, cvMappingCfg, ruleCfg );

        } catch (Throwable t) {

            log.error( "An error occured while configuring the PAR validator", t );

            FacesContext context = FacesContext.getCurrentInstance();
            FacesMessage message = new FacesMessage( "An error occured while validating your data: " + t.getMessage() );
            context.addMessage( null, message );
        }
    }

    /**
     * Runs the validation with a given set of configuration files.
     *
     * @param report report to be filled as an outcome of the validation process.
     * @param file the data to validate.
     * @param ontologyCfg ontology configuration to be passed to the validator.
     * @param cvMappingCfg CV mapping to be passed to the validator.
     * @param ruleCfg Object rules to be passed to the validator.
     */
    private void validatePsiFile(PsiReport report, File file,
                                 InputStream ontologyCfg,
                                 InputStream cvMappingCfg,
                                 InputStream ruleCfg) {

        // Printwriter to get the stacktrace messages
        StringWriter sw = new StringWriter(1024);

        progressModel.setValue( 1L );

        try{
            // set work directory
            UserPreferences preferences = new UserPreferences();
            preferences.setKeepDownloadedOntologiesOnDisk(true);
            preferences.setWorkDirectory(new File(System.getProperty("java.io.tmpdir")));
            preferences.setSaxValidationEnabled(true);

            // we instantiate the MI25 validator
            Mi25Validator validator = new Mi25Validator(ontologyCfg, cvMappingCfg, ruleCfg);
            validator.setUserPreferences(preferences);

            log.debug("Validation starting");
            progressModel.setValue( 4L );

            final ValidatorReport validatorReport = validator.validate( file );

            progressModel.setValue( 5L );

            if ( log.isInfoEnabled() ) {
                log.info( "Validator reported " + validatorReport.getSyntaxMessages().size() + " syntax messages" );
                log.info( "Validator reported " + validatorReport.getSemanticMessages().size() + " semantic messages" );
            }

            report.setInteractionCount( validatorReport.getInteractionCount() );

            // finally, we set the messages obtained (if any) to the report
            if (validatorReport.hasSyntaxMessages()) {
                report.setXmlSyntaxStatus("XML syntax validation failed.");
                report.setXmlSyntaxStatus(PsiReport.INVALID);
                report.setXmlSyntaxReport(new ArrayList<ValidatorMessage>(validatorReport.getSyntaxMessages()));
            } else {
                report.setXmlSyntaxStatus(PsiReport.VALID);
            }

            if( ! validationScope.involveSemanticValidation() ) {
                // there is no semantic involved so set message accoringly
                report.setSemanticsStatus(PsiReport.NOT_RUN);
                report.setSemanticsReport("Semantic validation was not requested");

            } else if ( validatorReport.hasSemanticMessages()) {

                report.setValidatorMessages(new ArrayList<ValidatorMessage>(validatorReport.getSemanticMessages()));

                // we need to determine the status of the semantics validation.
                // If there are no validatorMessages, the status is "valid" (already set).
                // If there are messages, but all of them are warnings, the status will be "warnings".
                // If there are error or fatal messages, the status, the status will be failed
                String status = null;

                if( report.getValidatorMessages() != null ) {
                    for (ValidatorMessage message : report.getValidatorMessages()) {
                        // if we find a warning, set the status to warning and continue looping
                        if (message.getLevel() == MessageLevel.WARN) {
                            status = PsiReport.WARNINGS;
                            report.setSemanticsReport("Validated with warnings");
                        }

                        // if a message with a level higher than warning is found, set the status to
                        // error and stop the loop
                        if (message.getLevel().isHigher(MessageLevel.WARN)) {
                            status = PsiReport.INVALID;
                            report.setSemanticsReport("Validation failed");
                            break;
                        }
                    }

                    report.setSemanticsStatus( status );
                }

                // set the status to the report
                report.setSemanticsStatus(status);

            } else {

                report.setSemanticsStatus(PsiReport.VALID);
                report.setSemanticsReport("Document is valid");
            }

        } catch (Throwable t) {

            StringBuilder sb = new StringBuilder( 512 );
            sb.append( "An error occured while validating your data model" );

            Throwable cause = t.getCause();
            while( cause != null ) {
                sb.append( " > " ).append( cause.getMessage() );
                cause = cause.getCause();
            }

            String msg = sb.toString();
            log.error( msg, t );

            FacesContext context = FacesContext.getCurrentInstance();
            FacesMessage message = new FacesMessage( msg );
            context.addMessage( null, message );

            return;
        }

        String output = sw.getBuffer().toString();

        // if the output has content, an exception has been thrown, so the validation has failed
        if (output.length() > 0) {
            report.setSemanticsStatus(PsiReport.INVALID);
            report.setSemanticsReport(output);
            return;
        }
    }
}