/**
 * Copyright (c) 2006 The European Bioinformatics Institute, and others.
 * All rights reserved.
 */
package uk.ac.ebi.intact.services.validator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.URL;
import java.util.Collection;
import java.util.ArrayList;

import psidev.psi.mi.validator.extension.Mi25Validator;
import psidev.psi.tools.validator.ValidatorException;
import psidev.psi.tools.validator.ValidatorMessage;
import psidev.psi.tools.validator.MessageLevel;
import psidev.psi.tools.validator.preferences.UserPreferences;
import psidev.psi.tools.ontology_manager.impl.local.OntologyLoaderException;


/**
 * This class is the responsible of reading and validating the PSI File and creating a validation report
 * with the information found
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id: PsiReportBuilder.java 6474 2006-10-20 15:24:44Z baranda $
 * @since <pre>12-Jun-2006</pre>
 */
public class PsiReportBuilder
{
    /**
     * Logging, logging!
     */
    private static final Log log = LogFactory.getLog(PsiReportBuilder.class);

    /**
     * Name of the PSI gile
     */
    private String name;

    /**
     * If using a URL, the URL
     */
    private URL url;

    /**
     * If using an InputStream, this value won't be null
     */
    private InputStream inputStream;

    /**
     * Handy enumeration to avoid null checks on the previous attributes, when trying to determine
     * whether the info comes from URL or a stream
     */
    private enum SourceType { URL, INPUT_STREAM }

    /**
     * The current type used
     */
    private SourceType currentSourceType;

    /**
     * Creates a PsiReportBuilder instance using an URL
     * @param name The name of the file, only needed for information purposes
     * @param url The URL with the PSI xml
     */
    public PsiReportBuilder(String name, URL url)
    {
        this.name = name;
        this.url = url;

        this.currentSourceType = SourceType.URL;
    }

    /**
     * Creates a PsiReportBuilder instance using an InputStream
     * @param name The name of the file, only needed for information purposes
     * @param resettableInputStream The stream with the PSI xml. This InputStream has to be
     * resettable in order to build the report properly. The stream will be reset a few times, so the information
     * is parsed in the different validation phases
     */
    public PsiReportBuilder(String name, InputStream resettableInputStream)
    {
        this.name = name;
        this.inputStream = resettableInputStream;

        this.currentSourceType = SourceType.INPUT_STREAM;
    }

    /**
     * Creates the PSI report
     * @return the report created, after all the validations
     * @throws IOException thrown if there is something wrong with the I/O stuff
     */
    public PsiReport createPsiReport() throws IOException
    {
        // new instance of the report, that will be filled with the validation information
        PsiReport report = new PsiReport(name);

        // first validation: checks that the XML is valid, and sets the status/report fields
        // in the report
//        boolean xmlValid = validateXmlSyntax(report, getInputStream());

        // if its valid, the HTML view of the document is created and the second validation can be done
//        if (xmlValid)
//        {
//            // creating the view
//            createHtmlView(report, getInputStream());

            // second validation: checks that the semantics is right
            validatePsiFileSemantics(report, getInputStream());


//        }
//        else
//        {
//             //if the xml validation is wrong, the second validation won't be run
//            report.setSemanticsStatus("not checked, XML syntax needs to be valid first");
//        }

        return report;

    }

    /**
     * Returns the InputStream, independently of the origin of the information
     * @return the stream with the info
     * @throws IOException throw when there are I/O problems
     */
    private InputStream getInputStream() throws IOException
    {
        // uses the currentSourceType to determine how to open and return the inputStream
        if (currentSourceType == SourceType.URL)
        {
            return url.openStream();
        }
        else
        {
            inputStream.reset();
            return inputStream;
        }
    }

//    /**
//     * This methods validates the xml syntax of the document
//     * @param report An instance of the report being created where the validation information will be set
//     * @param is The stream with the PSI xml
//     * @return returns true if the validation has been successfull
//     * @throws IOException
//     */
//    private static boolean validateXmlSyntax(PsiReport report, InputStream is) throws IOException, ValidatorException, OntologyLoaderException {
//
//        Mi25Validator validator = new Mi25Validator( null, null, null );
//        final Collection<ValidatorMessage> validatorMessages = validator.validate(is);
//
//        PsiValidatorReport validationReport = PsiValidator.validate(new InputSource(is));
//
//        boolean xmlValid = validationReport.isValid();
//
//        //InputStream xsd = PsiReportBuilder.class.getResourceAsStream("/uk/ac/ebi/imex/psivalidator/resource/MIF25.xsd");
//
//        // we create a printwriter to write the output of the exceptions, if any.
//        StringWriter sw = new StringWriter();
//
//        // the document is valid ?
//        if (xmlValid)
//        {
//            // we set the report status and report with the specified texts
//            report.setXmlSyntaxStatus("valid");
//            report.setXmlSyntaxReport("Document is valid");
//        }
//        else
//        {
//            // if the output contains information, the xml validation is invalid.
//            // We put that information as the xml syntax report.
//            report.setXmlSyntaxStatus("invalid");
//            report.setXmlSyntaxReport(getOutputFromReport(validationReport));
//        }
//
//        return false;
//    }

//    private static String getOutputFromReport(PsiValidatorReport report)
//    {
//        StringBuffer sb = new StringBuffer(128);
//
//        for (PsiValidatorMessage message : report.getMessages())
//        {
//            sb.append(message.toString()+"\n");
//        }
//
//        return sb.toString();
//    }

    /**
     * Creates the HTML view using Xstl Transformation, and sets it to the report
     * @param report The report to set the view
     * @param is The input stream with the PSI XML file
     */
    private static void createHtmlView(PsiReport report, InputStream is)
    {
        String transformedOutput = null;
        try
        {
            // we transform the xml to html using an utility class that returns
            // the output stream with the html content
            transformedOutput = TransformationUtil.transformToHtml(is).toString();
        }
        catch (TransformerException e)
        {
            e.printStackTrace();
        }
        report.setHtmlView(transformedOutput);
    }

    /**
     * Validates the PSI Semantics
     * @param report the PsiReport to complete
     * @param is the stream with the psi xml file
     */
    private static void validatePsiFileSemantics(PsiReport report, InputStream is)
    {

        // Printwriter to get the stacktrace messages
        StringWriter sw = new StringWriter();
        PrintWriter writer = new PrintWriter(sw);

        try
        {
            // we "expand" the psi xml file, so we always have an expanded psi. If the psi is already
            // expanded the transformation does not change anything
            String expandedFile = TransformationUtil.transformToExpanded(is).toString();

            // InputStream with the transformed (expanded) xml
            InputStream expandedStream = new ByteArrayInputStream(expandedFile.getBytes());

            // We read the configuration file, included inside the jar
            InputStream configFile = PsiReportBuilder.class.getResourceAsStream("resource/config-mi-validator.xml");

            // set work directory
            UserPreferences preferences = new UserPreferences();
            preferences.setKeepDownloadedOntologiesOnDisk( true );
            preferences.setWorkDirectory(new File(System.getProperty("java.io.tmpdir")));
            preferences.setSaxValidationEnabled( false );

            // we instantiate the MI25 validator (for PSI 2.5 XML)
            Mi25Validator validator = new Mi25Validator( null, null, null );
            validator.setUserPreferences( preferences );
            final Collection<ValidatorMessage> messages = validator.validate(expandedStream);

            // finally, we set the messages obtained (if any) to the report
            report.setValidatorMessages(new ArrayList<ValidatorMessage>(messages));
        }
        catch (Exception e)
        {
            e.printStackTrace(writer);
        }

        String output = sw.getBuffer().toString();

        // if the output has content, an exception has been thrown, so the validation has failed
        if (!output.equals(""))
        {
            report.setSemanticsStatus("invalid");
            report.setSemanticsReport(output);
            return;
        }

        // if the output does not contain anything, no exception has been thrown, validation ok
        String status = "valid";
        report.setSemanticsReport("Document is valid");

        // we need to determine the status of the semantics validation.
        // If there are no validatorMessages, the status is "valid" (already set).
        // If there are messages, but all of them are warnings, the status will be "warnings".
        // If there are error or fatal messages, the status, the status will be failed
        for (ValidatorMessage message : report.getValidatorMessages())
        {
            // if we find a warning, set the status to warning and continue looping
            if (message.getLevel() == MessageLevel.WARN)
            {
                status = "warnings";
                report.setSemanticsReport("Validated with warnings");
            }

            // if a message with a level higher than warning is found, set the status to
            // error and stop the loop
            if (message.getLevel().isHigher(MessageLevel.WARN))
            {
                status = "invalid";
                report.setSemanticsReport("Validation failed");
                break;
            }
        }

        // set the status to the report
        report.setSemanticsStatus(status);
    }
}