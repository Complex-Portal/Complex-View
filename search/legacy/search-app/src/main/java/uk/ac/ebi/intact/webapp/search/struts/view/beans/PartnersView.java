/**
 * Copyright (c) 2002-2006 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.webapp.search.struts.view.beans;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.Interactor;
import uk.ac.ebi.intact.model.NucleicAcid;
import uk.ac.ebi.intact.model.Protein;
import uk.ac.ebi.intact.model.SmallMolecule;
import uk.ac.ebi.intact.persistence.dao.DaoFactory;
import uk.ac.ebi.intact.persistence.dao.ProteinDao;
import uk.ac.ebi.intact.searchengine.SearchClass;
import uk.ac.ebi.intact.util.SearchReplace;
import uk.ac.ebi.intact.webapp.search.SearchWebappContext;
import uk.ac.ebi.intact.webapp.search.struts.util.SearchConstants;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * This class represent the partners view. It contains the main Interactor and all the partners.
 * When instantiated, it loads all the information using the hibernate Daos and stores it in
 * collections. Then, the jsp page will access to this bean to show its content.
 * The view contains PartnersViewBean to represent each of the entries.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id:PartnersView.java 6452 2006-10-16 17:09:42 +0100 (Mon, 16 Oct 2006) baranda $
 * @since <pre>04-May-2006</pre>
 */
public class PartnersView extends AbstractView
{
    private static Log log = LogFactory.getLog(PartnersView.class);

    /**
     * The main interactor, the query
     */
    private PartnersViewBean interactorCandidate;

    /**
     * Url template to access uniprot
     */
    private String uniprotUrlTemplate;


    private Interactor interactor;


    /**
     * The partners of the main interactor
     */
    private List<PartnersViewBean> interactionPartners;

    public PartnersView(HttpServletRequest request, Interactor interactor)
    {
        super(request);
        this.interactor = interactor;

        ProteinDao proteinDao = getDaoFactory().getProteinDao();

        // Using the ac, we retrieve the uniprot url template (by using the identity "Xref")
        List<String> templates = proteinDao.getUniprotUrlTemplateByProteinAc(interactor.getAc());
        uniprotUrlTemplate = "";
        if (!templates.isEmpty())
        {
            uniprotUrlTemplate = proteinDao.getUniprotUrlTemplateByProteinAc(interactor.getAc()).get(0);
        }

        // pagination preparation here
        int totalItems = getTotalItems();
        int maxResults = getItemsPerPage();
        if (getCurrentPage() == 0)
        {
            if (totalItems > getItemsPerPage())
            {
                setCurrentPage(1);
            }
        }

        int firstResult = (getCurrentPage()-1)*getItemsPerPage();

        if (firstResult > totalItems)
        {
            throw new RuntimeException("Page out of bounds: "+getCurrentPage()+" ("+firstResult+"/"+maxResults+")");
        }

        if (totalItems <= getItemsPerPage()) firstResult = 0;

        // Load the list of partners ACs, each partner has a list with the interaction ACs
        Map<String,List<String>> partnersWithInteractionAcs = getPartnerInteractorsAcsPage(firstResult, maxResults);

        // This set will contain all the ACs for the main interactor
        // Iterates throug all the interactions from the previous query, to create a collection with unique ACs
        Set<String> interactionAcsForMainInteractor = new HashSet<String>(partnersWithInteractionAcs.size());

        for (List<String> allInteractionsAc : partnersWithInteractionAcs.values())
        {
            interactionAcsForMainInteractor.addAll(allInteractionsAc);
        }

        // Creates the main interactor PartnersViewBean
        interactorCandidate = createPartnersViewBean(interactor, true, interactionAcsForMainInteractor);

        // We iterate through the map to retrieve all the partners, with their interaction ACs,
        // and create a PArtnerViewBean with each entry
        interactionPartners = new ArrayList<PartnersViewBean>();

        for (Map.Entry<String,List<String>> entry : partnersWithInteractionAcs.entrySet())
        {
            String partnerInteractorAc = entry.getKey();

            if (!partnerInteractorAc.equals(interactor.getAc()))
            {
                // We retrieve each interactor from the database, to create the bean
                Interactor partnerInteractor = getDaoFactory().getInteractorDao().getByAc(partnerInteractorAc);
                PartnersViewBean bean = createPartnersViewBean(partnerInteractor, false, entry.getValue());
                interactionPartners.add(bean);
            }
        }

    }

    private PartnersViewBean createPartnersViewBean(Interactor interactor, boolean mainInteractor, Collection<String> interactionAcs)
    {
         PartnersViewBean bean = new PartnersViewBean(interactor);

        // Bean creation
        List<String> geneNames = getDaoFactory().getInteractorDao().getGeneNamesByInteractorAc(interactor.getAc());
        bean.setGeneNames(geneNames);

        // All this setters set the information necessary for the view
        bean.setNumberOfInteractions(interactionAcs.size());
        bean.setUniprotAc(getUniprotAc(interactor));
        bean.setIdentityXrefURL(getIdentityXrefUrl(interactor));
        bean.setInteractorSearchURL(getInteractorSearchURL(interactor));
        bean.setInteractorPartnerUrl(getInteractorPartnerURL(interactor));
        bean.setInteractionsSearchURL(getInteractionsSearchURL(interactionAcs));

        return bean;
    }

    /**
     * Provides this Protein's Uniprot AC (ie its Xref to Uniprot).
     *
     * @return String the Protein's Uniprot AC.
     */
    public String getUniprotAc(Interactor interactor) {
       return getDaoFactory().getProteinDao().getUniprotAcByProteinAc(interactor.getAc());
    }

    /**
     * Gets the identity Xref url, replacing the accession number with the current value
     */
    private String getIdentityXrefUrl(Interactor interactor)
    {
        String primaryIdXrefIdentity = getPrimaryIdXrefIdentity(interactor);

        if (uniprotUrlTemplate == null || primaryIdXrefIdentity == null)
        {
            return "#";
        }

        log.debug("AC: "+interactor.getAc() +"; Url template: "+uniprotUrlTemplate+"; Primary Id: "+primaryIdXrefIdentity);

        return SearchReplace.replace(uniprotUrlTemplate, "${ac}", primaryIdXrefIdentity);
    }

    private String getPrimaryIdXrefIdentity(Interactor interactor)
    {
         return  getDaoFactory().getProteinDao().getUniprotAcByProteinAc(interactor.getAc());
    }


    /**
     * Returns a fully populated URL to perform a search for all this Protein's Interactions. ISSUE: This is used in the
     * mockups to fdisplay DETAIL. This would be unmanageable for large Interaction lists spread across Experiments.
     * DESIGN DECISION: make th link go to the main 'simple' page to list the Interactions *there*, so users can then
     * choose which detail they want.
     *
     * @return String The complete search URL to perform a (possibly multiple) search for this Protein's Interactions
     */
    private String getInteractionsSearchURL(Collection<String> interactionAcs) {

        String interactionSearchURL = "";

        // now create the URL-based searchquery
        StringBuffer sb = new StringBuffer();
        for ( Iterator<String> it = interactionAcs.iterator(); it.hasNext(); ) {
            String interactionAc = it.next();
            sb.append( interactionAc );
            if ( it.hasNext() ) {
                sb.append( "," );
            }

            interactionSearchURL = SearchWebappContext.getCurrentInstance().getSearchUrl() + sb.toString();
        }
        return interactionSearchURL;
    }


    private String getInteractorSearchURL(Interactor interactor) {
        return getInteractorURL(interactor, "single");
    }

    private String getInteractorPartnerURL(Interactor interactor) {
        return getInteractorURL(interactor, "partner");
    }

    private String getInteractorURL(Interactor interactor, String view)
    {
        String searchUrl = SearchWebappContext.getCurrentInstance().getSearchUrl();

        if (interactor instanceof Protein)
        {
            return getSimpleSearchURL(searchUrl,interactor) + "&amp;searchClass="+ SearchClass.PROTEIN.getShortName() +"&amp;view=" + view +
                    "&filter=ac";
        }
        else if (interactor instanceof NucleicAcid)
        {
            return getSimpleSearchURL(searchUrl, interactor) + "&amp;searchClass="+ SearchClass.NUCLEIC_ACID.getShortName() +"&amp;view=" + view +
                        "&filter=ac";
        }
        else if (interactor instanceof SmallMolecule)
        {
            return getSimpleSearchURL(searchUrl,interactor) + "&amp;searchClass="+ SearchClass.SMALL_MOLECULE.getShortName() +"&amp;view=" + view +
                    "&filter=ac";
        }

        return "";
    }

    /**
     * Provides a String representation of a URL to perform a basic search on this Protein's AC. Thus a general search
     * is performed using this Proterin's AC.
     *
     * @return String a String representation of a search URL link
     */
    private String getSimpleSearchURL(String searchUrl, Interactor interactor) {

        return searchUrl + interactor.getAc() + "&filter=ac";
    }

    public PartnersViewBean getInteractorCandidate()
    {
        return interactorCandidate;
    }

    public void setInteractorCandidate(PartnersViewBean interactorCandidate)
    {
        this.interactorCandidate = interactorCandidate;
    }

    public List<PartnersViewBean> getInteractionPartners()
    {
        return interactionPartners;
    }

    public void setInteractionPartners(List<PartnersViewBean> interactionPartners)
    {
        this.interactionPartners = interactionPartners;
    }


    @Override
    public int getTotalItems()
    {
        String prefix = getClass().getName()+"_";

        String attName = prefix+interactor.getAc();

        int totalItems;

        if (getSession().getAttribute(attName) == null)
        {
            totalItems = getPartnerInteractorsAcs().size();

            getSession().setAttribute(attName, totalItems);
        }
        else
        {
            totalItems = (Integer) getSession().getAttribute(attName);
        }

        getRequest().setAttribute(SearchConstants.TOTAL_RESULTS_ATT_NAME, totalItems);

        return totalItems;
    }

    public Map<String,List<String>> getPartnerInteractorsAcs()
    {
        final String CURRENT_AC_ATT = getClass().getName()+"_CURRENT_AC";

        String attName = getClass().getName()+"_partners";

        Map<String,List<String>> partnerAcs;

        // The ac of the previous interactor searched is already in the session? If so,
        // we can return the results directly. If the current ac is different than the one
        // in the session, we have to get the partner interactions again.
        if (getSession().getAttribute(CURRENT_AC_ATT) != null &&
                getSession().getAttribute(CURRENT_AC_ATT).equals(interactor.getAc()))
        {
            partnerAcs =  (Map<String,List<String>>) getSession().getAttribute(attName);
        }
        else
        {
            partnerAcs = getDaoFactory().getProteinDao().getPartnersWithInteractionAcsByProteinAc(interactor.getAc());

            getSession().setAttribute(attName, partnerAcs);
            getSession().setAttribute(CURRENT_AC_ATT, interactor.getAc());
        }

        return partnerAcs;
    }

    public Map<String,List<String>> getPartnerInteractorsAcsPage(int firstResult, int maxResults)
    {
        Map<String,List<String>> partnerAcs = getPartnerInteractorsAcs();

        int size = partnerAcs.size();

        if (firstResult > size)
        {
            throw new IllegalArgumentException("firstResult cannot be greater than the interactors size");
        }

        int to = firstResult+maxResults;

        if (to > size)
        {
            to = size;
        }

        List<String> keySublist = new ArrayList<String>(partnerAcs.keySet()).subList(firstResult, to);

        Map<String,List<String>> subMap = new HashMap<String,List<String>>();

        for (String ac : keySublist)
        {
            subMap.put(ac, partnerAcs.get(ac));
        }

        return subMap;
    }

    private DaoFactory getDaoFactory()
    {
        return IntactContext.getCurrentInstance().getDataContext().getDaoFactory();
    }
}
