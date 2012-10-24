/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.application.commons.util;

import uk.ac.ebi.intact.core.IntactException;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.model.*;

import java.util.*;
import java.util.regex.Pattern;

/**
 *
 * W H E R E    I T    I S    U S E D
 * -----------------------------------
 *
 * In the SanityCheck
 * In the editor to display only the relevant CvTopic in the Annotation section for each Editor page
 *
 * H O W    T O    U S E    I T
 * ----------------------------
 *
 * If you want to have the List of all the CvTopic you can use to annotate a Protein for exemple just do like that
 *
 *  AnnotationSetion annotationSection = new annotationSection();
 *  List usableTopicsForProtein = annotationSetion.getUsableTopics(EditorMenuFactory.PROTEIN)
 *
 * H O W    I T    W O R K S
 * ---------------------------
 *
 * For each cvTopic
 *      search for the annotation having its CvTopic equal to used-in-class CvTopic
 *
 *          take the description field which is a string containing the classes where this cvTopic can be used.
 *          It is not really classes but more editor page names.
 *          ex : the cvTopic having shortlabel = to "function" can be used to annotate a Protein in the Protein Editor
 *               and an Interaction in the Interaction Editor. So the field description of the annotation used-in-class
 *               will contain the string : "Interaction, Protein"
 *
 *          the description is split using the coma separtor and all terms are put in an array
 *          ex : In our previous example here is what would happen :
 *               classes[0] ====> Interaction
 *               classes[1] ====> Protein
 *
 *          for each term of the array (classes[]), add in the list corresponding to this term the topic's shortlabel
 *              ex :In our previous example here is what would happen :
 *                  In the map annotationSection ask for the list linked to the key "Interaction" add the term "function"
 *                  Then, ask for the list linked to the key "Protein" and add the term "function"
 *
 *
 * @author Catherine Leroy (cleroy@ebi.ac.uk)
 * @version $Id$
 */
public class AnnotationSection {

    HashMap<String,List<String>> annotationSection = new HashMap<String,List<String>>();

    public HashMap getAnnotationSection() {
        return annotationSection;
    }

    public void setAnnotationSection(HashMap<String,List<String>> annotationSection) {
        this.annotationSection = annotationSection;
    }

    public List<String> getUsableTopics(String editorPage){
        return annotationSection.get(editorPage);
    }



    public AnnotationSection () throws IntactException {
         //   IntactHelper intactHelper = new IntactHelper();
        /*
            Experiment.class.getName() ===> Editor - Experiment
            Interaction.class.getName() ===> Editor - Interaction
            Protein.class.getName() ===> Editor - Protein
                                      ===> Editor - NucleicAcid
            CvObject.class.getName() ===> Editor - CvAlliasType
                                      ===> CvCellType
                                      ===> CvComponentRole
                                      ===> CvDatabase
                                      ===> CvFuzzyType
                                      ===> CvTissue
                                      ===> CvTopic
                                      ===> CvXrefQualifier
            BioSource.class.getName() ===> Editor - BioSource
        */

        annotationSection.put(Experiment.class.getName(),new ArrayList<String>());
        annotationSection.put(Interaction.class.getName(),new ArrayList<String>());
        annotationSection.put(Protein.class.getName(),new ArrayList<String>());
        annotationSection.put(CvObject.class.getName(),new ArrayList<String>());
        annotationSection.put(BioSource.class.getName(), new ArrayList<String>());
        Object object = annotationSection.get(Experiment.class.getName());

        List<String> usableTopics = new ArrayList<String>();

        //IntactHelper helper = new IntactHelper();

        Collection<CvTopic> cvTopics = IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getCvObjectDao(CvTopic.class).getAll();

        for (CvTopic cvTopic : cvTopics)
        {
            Collection<Annotation> cvTopicAnnotations = cvTopic.getAnnotations();

            for (Annotation annotation : cvTopicAnnotations)
            {
                if (CvTopic.USED_IN_CLASS.equals(annotation.getCvTopic().getShortLabel()))
                {
                    String usedInClass = annotation.getAnnotationText();
                    if (usedInClass != null)
                    {
                        Pattern pattern = Pattern.compile(",");
                        String[] classes = pattern.split(usedInClass);

                        for (String className : classes)
                        {
                            String editorPageName = className.trim();
                            usableTopics =  annotationSection.get(editorPageName);
                            if (usableTopics != null)
                            {
                                usableTopics.add(cvTopic.getShortLabel());
                                //usableTopics.add(cvTopic.getShortLabel());
                                annotationSection.put(editorPageName, usableTopics);
                            }
                        }
                    }
                }
            }
        }
        //intactHelper.closeStore();
    }

    public static void main(String[] args) throws IntactException {
        AnnotationSection annotationSection = new AnnotationSection();
        annotationSection.getAnnotationSection();


        Set<String> keySet = annotationSection.annotationSection.keySet();
        for (String editorPage : keySet)
        {
            System.out.println("editor page " + editorPage + " : ");
            List<String> usableTopics = annotationSection.annotationSection.get(editorPage);
            //System.out.println(editorPage + " : ");
            for (String cvTopicShortLabel : usableTopics)
            {
                System.out.println("\t" + cvTopicShortLabel + " ");
            }
        }
        List<String> bioSourceList = annotationSection.getUsableTopics(BioSource.class.getName());
        for (String o : bioSourceList)
        {
            System.out.println("topic for bisource " + o);
        }
    }

}
