/**
 * Copyright (c) 2006 The European Bioinformatics Institute, and others.
 * All rights reserved.
 */
package uk.ac.ebi.intact.services.validator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.trinidad.model.UploadedFile;

import javax.xml.transform.*;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;

import psidev.psi.mi.validator.extension.Mi25Validator;
import psidev.psi.mi.validator.ValidatorReport;
import psidev.psi.tools.validator.ValidatorMessage;
import psidev.psi.tools.validator.MessageLevel;
import psidev.psi.tools.validator.preferences.UserPreferences;

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

    /**
     * Creates a PsiReportBuilder instance using an URL
     *
     * @param name The name of the file, only needed for information purposes
     * @param url  The URL with the PSI xml
     */
    public PsiReportBuilder(String name, URL url) {
        this.name = name;
        this.url = url;

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
    public PsiReportBuilder(String name, File file) {
        this.name = name;
        this.file = file;

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
        validatePsiFile(report, file);

        if (report.isXmlSyntaxValid()) {
            // creating the view
            createHtmlView(report, getInputStream());
        } else {
            //if the xml validation is wrong, the second validation won't be run
            report.setSemanticsStatus("not checked, XML syntax needs to be valid first");
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
     * Creates the HTML view using Xstl Transformation, and sets it to the report
     *
     * @param report The report to set the view
     * @param is     The input stream with the PSI XML file
     */
    private static void createHtmlView(PsiReport report, InputStream is) {
        String transformedOutput = null;
        try {
            // we transform the xml to html using an utility class that returns
            // the output stream with the html content
            transformedOutput = TransformationUtil.transformToHtml(is).toString();
        }
        catch (TransformerException e) {
            e.printStackTrace();
        }
        report.setHtmlView(transformedOutput);
    }

    /**
     * Validates the PSI Semantics
     *
     * @param report the PsiReport to complete
     * @param file   the psi xml file
     */
    private static void validatePsiFile(PsiReport report, File file) {

        // Printwriter to get the stacktrace messages
        StringWriter sw = new StringWriter(1024);
        PrintWriter writer = new PrintWriter(sw);

        try {
            // TODO here we could use ValidatorProfile to encapsulate a set of configuration files so the user can choose what to validate !!

            // We read the configuration file, included inside the jar
            InputStream cvMappingCfg = PsiReportBuilder.class.getResourceAsStream("/psi-mi/validator-config/cv-mapping.xml");
            InputStream ruleCfg = PsiReportBuilder.class.getResourceAsStream("/psi-mi/validator-config/object-rules.xml");
            InputStream ontologyCfg = PsiReportBuilder.class.getResourceAsStream("/psi-mi/validator-config/ontologies.xml");

            // set work directory
            UserPreferences preferences = new UserPreferences();
            preferences.setKeepDownloadedOntologiesOnDisk(true);
            preferences.setWorkDirectory(new File(System.getProperty("java.io.tmpdir")));
            preferences.setSaxValidationEnabled(false);

            // we instantiate the MI25 validator
            Mi25Validator validator = new Mi25Validator(ontologyCfg, cvMappingCfg, ruleCfg);
            validator.setUserPreferences(preferences);
            final ValidatorReport validatorReport = validator.validate( file );

            // finally, we set the messages obtained (if any) to the report
            if (validatorReport.hasSyntaxMessages()) {
                report.setXmlSyntaxStatus("XML syntax validation failed.");
                report.setXmlSyntaxStatus(PsiReport.INVALID);
                report.setXmlSyntaxReport(new ArrayList(validatorReport.getSyntaxMessages()));
            } else {
                report.setXmlSyntaxStatus(PsiReport.VALID);
            }

            if (validatorReport.hasSemanticMessages()) {
                report.setSemanticsReport("XML semantic validation failed.");
                report.setSemanticsStatus(PsiReport.INVALID);
                report.setValidatorMessages(new ArrayList(validatorReport.getSemanticMessages()));
            }
        }
        catch (Exception e) {
            e.printStackTrace(writer);
        }

        String output = sw.getBuffer().toString();

        // if the output has content, an exception has been thrown, so the validation has failed
        if (!output.equals("")) {
            report.setSemanticsStatus(PsiReport.INVALID);
            report.setSemanticsReport(output);
            return;
        }

        // if the output does not contain anything, no exception has been thrown, validation ok
        String status = PsiReport.VALID;
        report.setSemanticsReport("Document is valid");

        // we need to determine the status of the semantics validation.
        // If there are no validatorMessages, the status is "valid" (already set).
        // If there are messages, but all of them are warnings, the status will be "warnings".
        // If there are error or fatal messages, the status, the status will be failed
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

        // set the status to the report
        report.setSemanticsStatus(status);
    }
}