/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.binarysearch.webapp.application;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.custom.tree2.TreeModel;
import uk.ac.ebi.intact.binarysearch.webapp.StartupListener;
import uk.ac.ebi.intact.binarysearch.webapp.model.tree.TreeBuilder;
import uk.ac.ebi.intact.util.ols.Term;

import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Application-scope backing bean with the information for OLS
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class OlsBean implements Serializable {

    /**
     * Sets up a logger for that class.
     */
    public static final Log log = LogFactory.getLog(OlsBean.class);

    private Term interactionTypeTerm;
    private List<Term> interactionTypeTerms;

    private Term detectionMethodTerm;
    private List<Term> detectionMethodTerms;

    private TreeModel interactionTypeTreeModel;
    private TreeModel detectionMethodTreeModel;

    public OlsBean() {
        ServletContext context = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();

        interactionTypeTerm = (Term) context.getAttribute(StartupListener.INTERACTION_TYPE_TERM);
        interactionTypeTerms = (List<Term>) context.getAttribute(StartupListener.INTERACTION_TYPES);
        detectionMethodTerm = (Term) context.getAttribute(StartupListener.DETECTION_METHOD_TERM);
        detectionMethodTerms = (List<Term>) context.getAttribute(StartupListener.DETECTION_METHODS);

        interactionTypeTreeModel = new TreeBuilder().createModel(interactionTypeTerm);
        detectionMethodTreeModel = new TreeBuilder().createModel(detectionMethodTerm);

    }

    public List<String> suggestInteractionTypes(String prefix) {
        if (log.isDebugEnabled()) log.debug("Interaction type Suggestions for: " + prefix);

        List<String> suggestions = new ArrayList<String>();

        for (Term t : interactionTypeTerms) {
            if (t.getName().startsWith(prefix)) {
                suggestions.add(t.getName());
            }
        }

        Collections.sort(suggestions);

        if (log.isDebugEnabled()) log.debug("Found "+suggestions.size()+" suggestions from a list of "+interactionTypeTerms.size()+" terms");

        return suggestions;
    }

    public List<String> suggestInteractionDetectionMethods(String prefix) {
        if (log.isDebugEnabled()) log.debug("Detection methods Suggestions for: " + prefix);

        List<String> suggestions = new ArrayList<String>();

        for (Term t : detectionMethodTerms) {
            if (t.getName().startsWith(prefix)) {
                suggestions.add(t.getName());
            }
        }

        Collections.sort(suggestions);

        return suggestions;
    }

    public static List<Term> childrenFor(Term term, List<Term> children) {
        if (term.getChildren() != null && !term.getChildren().isEmpty()) {
            for (Term child : term.getChildren()) {
                children.add(child);
                childrenFor(child, children);
            }
        }

        return children;
    }

    public Term getDetectionMethodTerm() {
        return detectionMethodTerm;
    }

    public void setDetectionMethodTerm(Term detectionMethodTerm) {
        this.detectionMethodTerm = detectionMethodTerm;
    }

    public List<Term> getDetectionMethodTerms() {
        return detectionMethodTerms;
    }

    public void setDetectionMethodTerms(List<Term> detectionMethodTerms) {
        this.detectionMethodTerms = detectionMethodTerms;
    }

    public Term getInteractionTypeTerm() {
        return interactionTypeTerm;
    }

    public void setInteractionTypeTerm(Term interactionTypeTerm) {
        this.interactionTypeTerm = interactionTypeTerm;
    }

    public List<Term> getInteractionTypeTerms() {
        return interactionTypeTerms;
    }

    public void setInteractionTypeTerms(List<Term> interactionTypeTerms) {
        this.interactionTypeTerms = interactionTypeTerms;
    }

    public TreeModel getInteractionTypeTreeModel()
    {
        return interactionTypeTreeModel;
    }

    public void setInteractionTypeTreeModel(TreeModel interactionTypeTreeModel)
    {
        this.interactionTypeTreeModel = interactionTypeTreeModel;
    }

    public TreeModel getDetectionMethodTreeModel()
    {
        return detectionMethodTreeModel;
    }

    public void setDetectionMethodTreeModel(TreeModel detectionMethodTreeModel)
    {
        this.detectionMethodTreeModel = detectionMethodTreeModel;
    }
}