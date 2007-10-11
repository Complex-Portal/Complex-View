/*
Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.

This class take care of all the actions when pressing on the auto-completion button on the Experiment-Editor page.

All experiment are linked to a pubmed Id. Once you have it, you can via a web service (cdbWebservices.jar developped by
Mark Rijnbeek markr@ebi.ac.uk) retrieve some information from pubmed.

Then, you can automatically :
- add the shortlabel of the experiment
- add the fullname of the experiment
- add an annotation with (cvTopic = author-list) and (description = [name of the author])
- add an annotation with (cvTopic = contact-email) and (description = [email]) (if possible, not all articles are
associated to a contact-email
- add a crossreference with (primary Id = pubmedId) and (qualifier = primary-reference)

If an anotation author-list already exists and is different, it will update the description with the new author list
If an xref exists with qualifier = primary-reference but with a different primaryId, it will update the primaryId
If an annotation contact-email already exists it will add a new annotation, with the new email. It will not erase the
previous one.


*/

package uk.ac.ebi.intact.application.editor.struts.action.experiment;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.*;
import uk.ac.ebi.intact.application.editor.business.EditUserI;
import uk.ac.ebi.intact.application.editor.struts.framework.AbstractEditorDispatchAction;
import uk.ac.ebi.intact.application.editor.struts.framework.EditorFormI;
import uk.ac.ebi.intact.application.editor.struts.view.CommentBean;
import uk.ac.ebi.intact.application.editor.struts.view.XreferenceBean;
import uk.ac.ebi.intact.application.editor.struts.view.experiment.ExperimentViewBean;
import uk.ac.ebi.intact.application.editor.util.DaoProvider;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.persistence.dao.AnnotationDao;
import uk.ac.ebi.intact.persistence.dao.CvObjectDao;
import uk.ac.ebi.intact.persistence.dao.ExperimentDao;
import uk.ac.ebi.intact.persistence.dao.XrefDao;
import uk.ac.ebi.intact.util.cdb.ExperimentAutoFill;
import uk.ac.ebi.intact.util.cdb.PublicationNotFoundException;
import uk.ac.ebi.intact.util.cdb.UnexpectedException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * The action class to auto complete a part of the Experiment Editor form from a given pubmedId
 * (after pressing the auto-completion button on Editor - Experiment 
 *
 * @author Catherine Leroy (cleroy@ebi.ac.uk)
 * @version $Id: AutocompDispatchAction.java, 2005/08/24
 *
 * @struts.action
 *      path="/exp/autocomp"
 *      name="expForm"
 *      input="edit.layout"
 *      scope="session"
 *      validate="false"
 *      parameter="dispatch"
 */

public class AutocompDispatchAction extends AbstractEditorDispatchAction {

    private static final Log log = LogFactory.getLog(AutocompDispatchAction.class);


    // Implements super's abstract methods.

    /**
     * Provides the mapping from resource key to method name.
     * @return Resource key / method name map.
     */
    protected Map getKeyMethodMap() {
        Map map = new HashMap();
        map.put("exp.button.autocompletion", "autocomp");

        return map;
    }

    /**
     * Process the specified HTTP request, and create the corresponding
     * HTTP response (or forward to another web component that will create
     * it). Return an ActionForward instance describing where and how
     * control should be forwarded, or null if the response has
     * already been completed.
     *
     * @param mapping - The <code>ActionMapping</code> used to select this instance
     * @param form - The optional <code>ActionForm</code> bean for this request (if any)
     * @param request - The HTTP request we are processing
     * @param response - The HTTP response we are creating
     *
     * @return - represents a destination to which the action servlet,
     * <code>ActionServlet</code>, might be directed to perform a RequestDispatcher.forward()
     * or HttpServletResponse.sendRedirect() to, as a result of processing
     * activities of an <code>Action</code> class
     */
    public ActionForward autocomp(ActionMapping mapping,
                                  ActionForm form,
                                  HttpServletRequest request,
                                  HttpServletResponse response)
            throws Exception {
        // Handler to the Intact User.

        EditorFormI editorForm = (EditorFormI) form;

        EditUserI user = getIntactUser(request);

        // The current view of the edit session.
        ExperimentViewBean view = (ExperimentViewBean) user.getView();

        String pubmedId=view.getPubmedId();
        if(pubmedId!=null){
            log.debug("The pubmed is : " + pubmedId);
            pubmedId = pubmedId.trim();
        } else{
            log.debug("The pubmed Id was null");
        }

        /*
        Instantiate the object ExperimentAutoFill (eaf), which use the webService.
        Once this object is created you can call some of its methods like :
        eaf.getFullname()           =====> it will return a valide fullname for the experiment
        eaf.getShortlabel(helper)   =====> it will return a valide shortlabel for the experiment
        eaf.getAuthorList()         =====> it will return the author list
        eaf.getAuthorEmail()        =====> it will return the author email
        */

        //reset first
        view.setFullName(null);
        view.setShortLabel(null);
        view.getAnnotations().clear();

        try{

            ExperimentAutoFill eaf = new ExperimentAutoFill(pubmedId);

            AnnotationDao annotationDao = DaoProvider.getDaoFactory().getAnnotationDao();
            XrefDao xrefDao = DaoProvider.getDaoFactory().getXrefDao();
            ExperimentDao experimentDao = DaoProvider.getDaoFactory().getExperimentDao();

            // The ac of the experiment
            String expAc=view.getAc();

            /*********************************************************************************************
            C r e a t i n g   t h e   f u l l n a m e   a n d   a d d i n g   i t   t o   t h e   v i e w
            *********************************************************************************************/
            String fullname = eaf.getFullname();
            view.setFullName(fullname);

            /*************************************************************************************************
            C r e a t i n g   t h e   s h o r t l a b e l   a n d   a d d i n g   i t   t o   t h e   v i e w
            **************************************************************************************************/
            String shortlabel = eaf.getShortlabel();
            view.setShortLabel(shortlabel);

            /******************************************************************************************
            C r e a t i n g   a u t h o r - l i s t   a n n o t a t i o n   a n d   a d d i n g   i t
            ******************************************************************************************/

            String authorList = eaf.getAuthorList();
            if(!("".equals(authorList) || null==authorList)){

                //Create an object Annotation with author-list cvTopic containing the list of authors
                Annotation authorListAnnotation = authorListAnnotation(authorList);

                //Create a CommentBean from this Annotation
                CommentBean authorListCb = new CommentBean(authorListAnnotation);

                /*
                Work to do on the view :
                If the view already contains an author-list CommentBean we have to update its description  with the new
                list of author
                */
                boolean annotationUpdated=false;
                List<CommentBean> annotsAlreadyInView=view.getAnnotations();
                for (int i = 0; i < annotsAlreadyInView.size(); i++) {
                    CommentBean cb =  annotsAlreadyInView.get(i);
                    /*
                    If cb's cvTopic is authorList cvTopic and if the list of authors is not the one corresponding to the
                    pubmed Id just entered then set the description of cb with the new author list
                    */
                    if(CvTopic.AUTHOR_LIST.equals(cb.getTopic()) && false==authorListCb.getDescription().equals(cb.getDescription())){
                        cb.setDescription(authorListCb.getDescription());
                        annotationUpdated=true;
                    }
                }
                /*
                Work to do on the database :
                If this experiment is already attached to an author-list annotation which is in the database, we
                update the annotationText of the annotation with the new list of author.
                */
                if(false=="".equals(expAc) && null != expAc){
                    Experiment exp = experimentDao.getByAc(expAc);
                    //get all the annotations contained in the database linked to this experiment
                    Collection annotations = exp.getAnnotations();
                    for (Iterator iterator = annotations.iterator(); iterator.hasNext();) {
                        Annotation annot =  (Annotation) iterator.next();
                        if(CvTopic.AUTHOR_LIST.equals(annot.getCvTopic().getShortLabel()) && false==authorListCb.getDescription().equals(annot.getAnnotationText())){
                            annot.setAnnotationText(authorListCb.getDescription());
                            annotationDao.saveOrUpdate(annot);
//                            if(helper.isPersistent(annot)){
//                                annot.setAnnotationText(authorListCb.getDescription());
//                                helper.update(annot);
//                            }
                        }
                    }
                }
                /*
                If the authorListCb is not in the view and if the modification hadn't been done by update then add it
                properly to the view.
                */
                if(!view.annotationExists(authorListCb) && annotationUpdated==false){
                    view.addAnnotation(authorListCb);
                }

            }


            /*********************************************************************************************
            C r e a t i n g   e m a i l - c o n t a c t   a n n o t a t i o n   a n d   a d d i n g   i t
            **********************************************************************************************/

            /*
            An email is never deleted automatically, so we just add any new email which is not already in the view.
            */
            String authorEmail = eaf.getAuthorEmail();
            if(!("".equals(authorEmail) || null==authorEmail)){
                Annotation authorEmailAnnotation = authorEmailAnnotation(authorEmail);
                CommentBean authorEmailCb = new CommentBean(authorEmailAnnotation);
                if(!view.annotationExists(authorEmailCb)) {
                    view.addAnnotation(authorEmailCb);
                }
            }

            /*********************************************************************************************
            C r e a t i n g   j o u r n a l   a n n o t a t i o n   a n d   a d d i n g   i t
            **********************************************************************************************/
            String journal = eaf.getJournal();
            if(!("".equals(journal) || null==journal)){
                Annotation journalAnnotation = createJournalAnnotation(journal);
                CommentBean journalCb = new CommentBean(journalAnnotation);



                /*
                Work to do on the view :
                If the view already contains an author-list CommentBean we have to update its description  with the new
                list of author
                */
                boolean annotationUpdated=false;
                List<CommentBean> annotsAlreadyInView=view.getAnnotations();
                for (CommentBean cb : annotsAlreadyInView) {
                    /*
                    If cb's cvTopic is authorList cvTopic and if the list of authors is not the one corresponding to the
                    pubmed Id just entered then set the description of cb with the new author list
                    */
                    if(CvTopic.JOURNAL.equals(cb.getTopic()) && false==journalCb.getDescription().equals(cb.getDescription())){
                        cb.setDescription(journalCb.getDescription());
                        annotationUpdated=true;
                    }
                }

                if(!view.annotationExists(journalCb) && annotationUpdated==false){
                    view.addAnnotation(journalCb);
                }
            }
            /***************************************************************************************************
            C r e a t i n g   p u b l i c a t i o n   y e a r   a n n o t a t i o n   a n d   a d d i n g   i t
            ****************************************************************************************************/

            String pubYear = Integer.toString(eaf.getYear());
            if(!("".equals(pubYear) || null==pubYear)){
                Annotation pubYearAnnotation = createPubYearAnnotation(pubYear);
                CommentBean pubYearCb = new CommentBean(pubYearAnnotation);



                /*
                Work to do on the view :
                If the view already contains an author-list CommentBean we have to update its description  with the new
                list of author
                */
                boolean annotationUpdated=false;
                List annotsAlreadyInView=view.getAnnotations();
                for (int i = 0; i < annotsAlreadyInView.size(); i++) {
                    CommentBean cb =  (CommentBean) annotsAlreadyInView.get(i);
                    /*
                    If cb's cvTopic is authorList cvTopic and if the list of authors is not the one corresponding to the
                    pubmed Id just entered then set the description of cb with the new author list
                    */
                    if(CvTopic.PUBLICATION_YEAR.equals(cb.getTopic()) && false==pubYearCb.getDescription().equals(cb.getDescription())){
                        cb.setDescription(pubYearCb.getDescription());
                        annotationUpdated=true;
                    }
                }

                /*
                Work to do on the database :
                If this experiment is already attached to an author-list annotation which is in the database, we
                update the annotationText of the annotation with the new list of author.
                */
                if(false=="".equals(expAc) && null != expAc){
                    Experiment exp = experimentDao.getByAc(expAc);
                    //get all the annotations contained in the database linked to this experiment
                    Collection<Annotation> annotations = exp.getAnnotations();
                    for (Annotation annot : annotations){
                        if(CvTopic.PUBLICATION_YEAR.equals(annot.getCvTopic().getShortLabel()) && false==pubYearCb.getDescription().equals(annot.getAnnotationText())){
                            annot.setAnnotationText(pubYearCb.getDescription());
                            annotationDao.saveOrUpdate(annot);

//                            if(helper.isPersistent(annot)){
//                                annot.setAnnotationText(pubYearCb.getDescription());
//                                helper.update(annot);
//                            }
                        }
                    }
                }

                if(!view.annotationExists(pubYearCb) && annotationUpdated==false){
                    view.addAnnotation(pubYearCb);
                }

            }

            /******************************************************************************
            C r e a t i n g   p u b m e d   x r e f e r e n ce   a n d   a d d i n g   i t
            ******************************************************************************/

            boolean xrefUpdated=false;
            //An xref object with primaryId = pubmedId and qualifier=primary-reference
            Xref pubmedXref = pubmedXref(pubmedId);

            //The XreferenceBean corresponding to to the pubmedXref
            XreferenceBean pubmedXb = new XreferenceBean(pubmedXref);

            //The list of all the XreferenceBean contained in the view
            List<XreferenceBean> xrefsAlreadyInView = view.getXrefs();

            /*
            Work to do on the view :
            If the view already contains an xreferenceBean with database=pubmed and qualifier=primary-reference but with
            primaryId !=pubmedId we update its primaryid  with the new pubmed Id
            */
            for (XreferenceBean xrefBean : xrefsAlreadyInView) {
                if(CvDatabase.PUBMED.equals(xrefBean.getDatabase()) && CvXrefQualifier.PRIMARY_REFERENCE.equals(xrefBean.getQualifier()) && false==pubmedId.equals(xrefBean.getPrimaryId())){
                    xrefBean.setPrimaryId(pubmedId);
                    xrefUpdated=true;
                }
            }
            /*
            Work to do on the database :
            If this experiment is already attached to a pubmed xreference (with qualifier = primary-reference) which is
            in the database, we update the primaryId with the new one.
            */
            if(false=="".equals(expAc) && null != expAc){
                Experiment exp = experimentDao.getByAc(expAc);
                Collection<ExperimentXref> xrefs = exp.getXrefs();

                for (ExperimentXref xref : xrefs) {
                    if(CvDatabase.PUBMED.equals(xref.getCvDatabase().getShortLabel()) && CvXrefQualifier.PRIMARY_REFERENCE.equals(xref.getCvXrefQualifier().getShortLabel()) && false==pubmedId.equals(xref.getPrimaryId())){
                        xref.setPrimaryId(pubmedId);
                        xrefDao.saveOrUpdate(xref);
//                        if(helper.isPersistent(xref)){
//                            xref.setPrimaryId(pubmedId);
//                            helper.update(xref);
//                        }
                    }
                }
            }
            if(!view.xrefExists(pubmedXb) && xrefUpdated==false){
                view.addXref(pubmedXb);
            }

            view.copyPropertiesTo(editorForm);

        }catch (NumberFormatException e){  //If the pubmed Id do not have the good format
            LOGGER.error("The given pubmed id is not an integer : ", e);
            ActionMessages errors = new ActionMessages();
            errors.add("autocomp", new ActionMessage("error.exp.autocomp.wrong.format"));
            saveErrors(request, errors);
            setAnchor(request, editorForm);
            // Display the error in the edit page.
            return mapping.getInputForward();
        }catch (PublicationNotFoundException e){  //If the publication is not found
            LOGGER.error(" The publication corresponding to pubmedId " + pubmedId + "couldn't be found : ", e);
            ActionMessages errors = new ActionMessages();
            errors.add("autocomp", new ActionMessage("error.exp.autocomp.publication.not.found",pubmedId));
            saveErrors(request, errors);
            setAnchor(request, editorForm);
            // Display the error in the edit page.
            return mapping.getInputForward();
        }catch(UnexpectedException e){ //Unexpected exception
            LOGGER.error("", e);
            ActionMessages errors = new ActionMessages();
            errors.add("autocomp", new ActionMessage("error.exp.autocomp", pubmedId));
            saveErrors(request, errors);
            setAnchor(request, editorForm);
            // Display the error in the edit page.
            return mapping.getInputForward();
        }catch(Throwable t){ //Any other kind of exception
            LOGGER.error("",t);
            ActionMessages errors = new ActionMessages();
            errors.add("autocomp", new ActionMessage("error.exp.autocomp"));
            saveErrors(request, errors);
            setAnchor(request, editorForm);
            // Display the error in the edit page.
            return mapping.getInputForward();
        }

        return mapping.getInputForward();
    }

    /**
     * Given an authorList it creates an "author-list" Annotation
     *
     * @param authorList a String containing the name of the author separated by a coma
     * ex : Ho Y., Gruhler A., Heilbut A., Bader GD., Moore L., Adams SL., Millar A., Taylor P., Bennett K.
     * @return The author-list Annotation
     * @throws IntactException
     */
    public Annotation authorListAnnotation(String authorList) throws IntactException {

        Annotation authorListAnnot;
        CvObjectDao<CvTopic> cvObjectDao = DaoProvider.getDaoFactory().getCvObjectDao(CvTopic.class);
        CvTopic authorListTopic = cvObjectDao.getByXref(CvTopic.AUTHOR_LIST_MI_REF );
        if ( authorListTopic == null ) {
            System.err.println( "Could not find CvTopic(" + CvTopic.AUTHOR_LIST +
                                ")... no author list will be attached/updated to the experiment." );
        }

        authorListAnnot = new Annotation(IntactContext.getCurrentInstance().getConfig().getInstitution(), authorListTopic ,authorList);

        return authorListAnnot;
    }

    /**
     * Given an publication year String it creates an "author-list" Annotation
     *
     * @param pubYear a String containing the year of publication of the article
     * @return The publication-year Annotation
     * @throws IntactException
     */
    public Annotation createPubYearAnnotation (String pubYear) throws IntactException {
        Annotation pubYearAnnot;

        CvObjectDao<CvTopic> cvObjectDao = DaoProvider.getDaoFactory().getCvObjectDao(CvTopic.class);
        CvTopic publicationYear = cvObjectDao.getByPsiMiRef(CvTopic.PUBLICATION_YEAR_MI_REF );

        if (publicationYear == null) {
            throw new IllegalStateException("Could not find CvTopic: " + CvTopic.PUBLICATION_YEAR_MI_REF);
        }

        pubYearAnnot = new Annotation(IntactContext.getCurrentInstance().getConfig().getInstitution(), publicationYear ,pubYear);

        return pubYearAnnot;

    }

    /**
         * Given an authorList it creates an "author-list" Annotation
         *
         * @param journal a String containing the name of the journal
         * @return The journal Annotation
         * @throws IntactException
         */
        public Annotation createJournalAnnotation(String journal) throws IntactException {

            Annotation journalAnnot;

            CvObjectDao<CvTopic> cvObjectDao = DaoProvider.getDaoFactory().getCvObjectDao(CvTopic.class);
            CvTopic journalTopic = cvObjectDao.getByPsiMiRef(CvTopic.JOURNAL_MI_REF );

            if (journalTopic == null) {
                throw new IllegalStateException("CvTopic not found: "+journalTopic);
            }

            journalAnnot = new Annotation(IntactContext.getCurrentInstance().getConfig().getInstitution(), journalTopic ,journal);

            return journalAnnot;
        }

    /**
     * Given an authorEmail it creates an "contact-email" Annotation
     * @param authorEmail a String containing the email of the author
     * ex : bcrosby@uwindsor.ca
     * @return The contact-email annotation
     * @throws IntactException
     */
    public Annotation authorEmailAnnotation(String authorEmail) throws IntactException {

        Annotation authorEmailAnnot;

        CvObjectDao<CvTopic> cvObjectDao = DaoProvider.getDaoFactory().getCvObjectDao(CvTopic.class);
        CvTopic authorEmailTopic = cvObjectDao.getByPsiMiRef(CvTopic.CONTACT_EMAIL_MI_REF );

        if (authorEmailTopic == null) {
            throw new IllegalStateException("CvTopic not found: "+authorEmailTopic);
        }

        authorEmailAnnot = new Annotation(IntactContext.getCurrentInstance().getConfig().getInstitution(), authorEmailTopic ,authorEmail);

        return authorEmailAnnot;
    }

    /**
     *  Given a pubmedId it create a Xref (pubmed, primary-reference)
     * @param pubmedId a pubmed Id
     * @return the pubmed Xref
     * @throws IntactException
     */

    public Xref pubmedXref (String pubmedId) throws IntactException {
        Xref pubmedXref;
        CvObjectDao<CvObject> cvObjectDao = DaoProvider.getDaoFactory().getCvObjectDao(CvObject.class);
        CvXrefQualifier primaryRefQualifier = (CvXrefQualifier) cvObjectDao.getByXref(CvXrefQualifier.PRIMARY_REFERENCE_MI_REF);
        CvDatabase pubmedDatabase= (CvDatabase) cvObjectDao.getByXref(CvDatabase.PUBMED_MI_REF);
        pubmedXref=new ExperimentXref(IntactContext.getCurrentInstance().getConfig().getInstitution(),pubmedDatabase,pubmedId,"","",primaryRefQualifier);
        return pubmedXref;
    }


}