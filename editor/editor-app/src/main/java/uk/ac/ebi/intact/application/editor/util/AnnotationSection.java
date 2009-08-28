/**
 * Copyright 2009 The European Bioinformatics Institute, and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.intact.application.editor.util;

import uk.ac.ebi.intact.core.IntactException;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.model.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
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

}
