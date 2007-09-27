/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.application.editor.util;

import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.persistence.dao.CvObjectDao;
import uk.ac.ebi.intact.persistence.dao.DaoFactory;
import uk.ac.ebi.intact.persistence.dao.IntactTransaction;

import java.util.Collection;
import java.util.List;

/**
 * This class is an Helper to
 *
 * @author Catherine Leroy (cleroy@ebi.ac.uk)
 * @version $Id$
 */
public class CvHelper {
    /**
     * psi-mi CvDatabase.
     */
    private static CvDatabase psiMi;
    private static CvDatabase newt;
    /**
     * Identity CvXrefQualifier.
     */
    private static CvXrefQualifier identity;
    /**
     * Obsolete CvTopic.
     */
    private static CvTopic obsolete;
    /**
     * Interaction CvInteractorType.
     */
    private static CvInteractorType interaction;
    /**
     * nucleic acid CvInteractorType.
     */
    private static CvInteractorType nucleicAcid;
    /**
     * protein CvInteractorType.
     */
    private static CvInteractorType protein;

    /**
     * This class is used in the Editor.
     */
    public CvHelper(){
    }


    /**
     *  Protein CvInteractorType getter.
     * @return the CvInteractorType protein.
     * @throws IntactException
     */
    public static CvInteractorType getProtein() throws IntactException {
        if (protein == null)
            protein = (CvInteractorType) getCvUsingItsMiRef(CvInteractorType.PROTEIN_MI_REF);
        return protein;
    }

    /**
     * NucleicAcid CvInteractorType getter.
     * @return the CvInteractorType protein.
     * @throws IntactException
     */
    public static CvInteractorType getNucleicAcid() throws IntactException {
        if (nucleicAcid == null)
            nucleicAcid = (CvInteractorType) getCvUsingItsMiRef(CvInteractorType.NUCLEIC_ACID_MI_REF);
        return nucleicAcid;
    }

    /**
     *  Interaction CvInteractorType getter.
     * @return the CvInteractorType interaction.
     * @throws IntactException
     */
    public static CvInteractorType getInteraction() throws IntactException {
        if (interaction == null)
            interaction = (CvInteractorType) getCvUsingItsMiRef(CvInteractorType.INTERACTION_MI_REF);
        return interaction;
    }

    /**
     *  Obsolete CvTopic getter.
     * @return the obsolete CvTopic.
     * @throws IntactException
     */
    public static CvTopic getObsolete() throws IntactException {
        if (obsolete == null)
            obsolete = (CvTopic) getCvUsingItsMiRef(CvTopic.OBSOLETE_MI_REF);
        return obsolete;
    }

    /**
     * PsiMi CvDatabase getter.
     * @return the CvDatabase psi-mi.
     * @throws IntactException
     */

    public static CvDatabase getPsiMi() throws IntactException {
        if (psiMi == null){
            CvObjectDao<CvDatabase> cvObjectDao = DaoProvider.getDaoFactory().getCvObjectDao(CvDatabase.class);
            psiMi = cvObjectDao.getByXref(CvDatabase.PSI_MI_MI_REF);
        }
        return psiMi;
    }


    public static CvDatabase getNewt() throws IntactException {
        if(newt == null){
            newt = (CvDatabase) getCvUsingItsMiRef(CvDatabase.NEWT_MI_REF);
        }
        return newt;
    }

    /**
     * identity CvXrefQualifier getter.
     * @return the CvXrefQualifier identity.
     * @throws IntactException
     */
    public  static CvXrefQualifier getIdentity() throws IntactException {

        if (identity == null){
            CvObjectDao<CvXrefQualifier> cvObjectDao = DaoProvider.getDaoFactory().getCvObjectDao(CvXrefQualifier.class);
            identity = (CvXrefQualifier) cvObjectDao.getByXref(CvXrefQualifier.IDENTITY_MI_REF);
        }
        return identity;
    }

    /**
     * Given a psi-mi id, it returns the corresponding cvObject.
     * @param psiMiId String containing the psi-mi id of the cvObject you want to get.
     * @return
     * @throws IntactException
     */
    private static CvObject getCvUsingItsMiRef(String psiMiId) throws IntactException {
        CvObjectDao<CvObject> cvObjectDao = DaoProvider.getDaoFactory().getCvObjectDao(CvObject.class);
        List<CvObject> cvObjects = cvObjectDao.getByXrefLike(getPsiMi(), getIdentity(), psiMiId);

        if (! (cvObjects.size()== 1) ){
            throw new IntactException("Retrieve " + cvObjects.size() + " CvObjects from database using the MI_REF "
                    + psiMiId);
        }
        return cvObjects.get(0);
    }


    /**
     * Given a CvDagObject, it returns a collection containing all it's children and grand children.
     * @param dag the CvDagObject you want to get the children from.
     * @param allChildrenMiRefs a collection in which the children will be put.
     * @return a Collection containing the children cvDagObject.
     * @throws IntactException
     */
    public Collection<String> getChildrenMiRefs(CvDagObject dag, Collection allChildrenMiRefs) throws IntactException {
        //Collection<String> miRefs = new ArrayList();
        Collection<CvDagObject> children = dag.getChildren();
        for (CvDagObject child : children){
            getChildrenMiRefs(child, allChildrenMiRefs);
            if(!isHiddenOrObsolete(child)){
                String miRef = getPsiMiRef(child);
                if( miRef != null){
                    allChildrenMiRefs.add(miRef);
                    System.out.println("adding " + miRef);
                }else throw new IntactException("Could not find any PSI-MI xref whith qualifier equal to identity for " +
                        "the CvDagObject[" + child.getAc() + "," + child.getShortLabel() + "] ");
            }
        }

        return allChildrenMiRefs;
    }


    /**
     * Return true if an annotatedObject is hidden or obsolete, false otherwise.
     * @param annotatedObj
     * @return a boolean.True if an annotatedObject is hidden or obsolete, false otherwise.
     * @throws IntactException
     */
    public boolean isHiddenOrObsolete(AnnotatedObject annotatedObj) throws IntactException {
        boolean hiddenOrObsolete = false;

        Collection<Annotation> annotations = annotatedObj.getAnnotations();
        for ( Annotation annotation : annotations ){
            if(CvTopic.HIDDEN.equals(annotation.getCvTopic().getShortLabel())
                    || getObsolete().getAc().equals(annotation.getCvTopic().getAc())){
                hiddenOrObsolete = true;
                break;
            }
        }
        return hiddenOrObsolete;
    }

    /**
     * Return a string containing the psi-mi id of a dag.
     * @param dag
     * @return
     * @throws IntactException
     */
    public String getPsiMiRef(CvDagObject dag) throws IntactException {
        String miRef = new String();
        boolean psiMiRefFound = false;
        Collection<CvObjectXref> xrefs = dag.getXrefs();
        for ( Xref xref : xrefs ){
            if(getPsiMi().getAc().equals(xref.getCvDatabase().getAc()) && getIdentity().getAc().equals(xref.getCvXrefQualifier().getAc())){
                psiMiRefFound = true;
                miRef = xref.getPrimaryId();
                break;
            }
        }
        if(psiMiRefFound = true){
            return miRef;
        }else return null;
    }

    public static void main(String[] args) throws IntactException {

        IntactTransaction tx = DaoProvider.getDaoFactory().beginTransaction();

        CvInteractorType newt = CvHelper.getNucleicAcid();//.getNewt();
                System.out.println("newt.getShortLabel() = " + newt.getShortLabel());
        //We should normally do a close session after that as in the IntactRequestSessionFilter but, as this 
        // main is just to show how the cvHelper works I haven't added the getSession() method and the close session...
        try{
            tx.commit();
        } catch(Exception e){
            System.out.println("Exception commiting " + e);    
        }

    }


}
