/**
 * Copyright (c) 2006 The European Bioinformatics Institute, and others.
 * All rights reserved.
 */
package uk.ac.ebi.intact.services.validator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import psidev.psi.tools.validator.ValidatorMessage;

import java.util.List;

/**
 * This class is the model of the information reported after the validations
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 * @since 2.0
 */
public class PsiReport
{

    private static final Log log = LogFactory.getLog(PsiReport.class);

    /**
     * Name of the file/report
     */
    private String name;

    /**
     * The status of the xml syntax validation (valid|invalid)
     */
    private String xmlSyntaxStatus;

    /**
     * Report of the validation. If failed, contains the stacktrace
     */
    private String xmlSyntaxReport;

    /**
     * The status of the semantics validation (valid|warnings|invalid)
     */
    private String semanticsStatus;

    /**
     * Report of the validation.
     */
    private String semanticsReport;

    /**
     * Collection of ValidatorMessages after the semantic validation
     */
    private List<ValidatorMessage> validatorMessages;

    /**
     * String containing the HTML View of the PSI file shown to the user, after transforming it
     * using XSLT
     */
    private String htmlView;

    /**
     * Constructor
     * @param name of the file/report
     */
    public PsiReport(String name) {
         this.name = name;
    }

    // ACCESSOR METHODS

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getXmlSyntaxStatus()
    {
        return xmlSyntaxStatus;
    }

    public void setXmlSyntaxStatus(String xmlSyntaxStatus)
    {
        this.xmlSyntaxStatus = xmlSyntaxStatus;
    }

    public String getXmlSyntaxReport()
    {
        return xmlSyntaxReport;
    }

    public void setXmlSyntaxReport(String xmlSyntaxReport)
    {
        this.xmlSyntaxReport = xmlSyntaxReport;
    }

    public String getSemanticsStatus()
    {
        return semanticsStatus;
    }

    public void setSemanticsStatus(String semanticsStatus)
    {
        this.semanticsStatus = semanticsStatus;
    }

    public String getSemanticsReport()
    {
        return semanticsReport;
    }

    public void setSemanticsReport(String semanticsReport)
    {
        this.semanticsReport = semanticsReport;
    }

    public String getHtmlView()
    {
        return htmlView;
    }

    public void setHtmlView(String htmlView)
    {
        this.htmlView = htmlView;
    }

    public List<ValidatorMessage> getValidatorMessages()
    {
        return validatorMessages;
    }

    public void setValidatorMessages(List<ValidatorMessage> validatorMessages)
    {
        this.validatorMessages = validatorMessages;
    }
}
