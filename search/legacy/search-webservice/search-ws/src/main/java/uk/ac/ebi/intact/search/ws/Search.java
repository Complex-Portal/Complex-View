/**
 * Copyright (c) 2002-2006 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.search.ws;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.intact.core.IntactException;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.core.context.IntactSession;
import uk.ac.ebi.intact.core.persistence.dao.DaoFactory;
import uk.ac.ebi.intact.core.persistence.dao.InteractionDao;
import uk.ac.ebi.intact.core.persistence.dao.ProteinDao;
import uk.ac.ebi.intact.model.*;

import javax.annotation.Resource;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.xml.ws.WebServiceContext;
import java.io.IOException;
import java.util.*;

/**
 * TODO comment this!
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 * @since <pre>08-Aug-2006</pre>
 */
@Controller
public class Search implements SearchService {

    private static final Log log = LogFactory.getLog(Search.class);

    @Autowired
    private SearchConfig config;

    @Autowired
    private ProteinDao proteinDao;

    @Autowired
    private InteractionDao interactionDao;
    
    @Autowired
    private uk.ac.ebi.intact.core.persistence.svc.SearchService simpleSearchService;

    public Search()
    {
        log.info("Initializing Search (new instance)...");
    }

    @WebMethod
    @Transactional(readOnly = true)
    public InteractionInfo[] getInteractionInfoUsingUniprotIds(@WebParam(name = "interactorAc1")String uniprotId1,
                                                               @WebParam(name = "interactorAc2")String uniprotId2)
    {
        //ProteinDao proteinDao = getProteinDao();

        List<ProteinImpl> protsForId1 = proteinDao.getByUniprotId(uniprotId1);
        List<ProteinImpl> protsForId2 = proteinDao.getByUniprotId(uniprotId2);

        System.out.println("PROTS: "+protsForId1);

        List<InteractionInfo> interInfos = new ArrayList<InteractionInfo>();

        for (Protein prot1 : protsForId1)
        {
            for (Protein prot2 : protsForId2)
            {
                InteractionInfo[] results = getInteractionInfoUsingIntactIds(prot1.getAc(), prot2.getAc());
                interInfos.addAll(Arrays.asList(results));
            }
        }

        

        return interInfos.toArray(new InteractionInfo[interInfos.size()]);
    }

    @WebMethod
    @Transactional(readOnly = true)
    public InteractionInfo[] getInteractionInfoUsingIntactIds(@WebParam(name = "interactorAc1")String id1,
                                                              @WebParam(name = "interactorAc2")String id2)
    {
        //InteractionDao interactionDao = getInteractionDao();

        List<Interaction> interactions = interactionDao.getInteractionsForProtPairAc(id1, id2);

        List<InteractionInfo> interInfos = new ArrayList<InteractionInfo>();

        for (Interaction inter : interactions)
        {
            String intactAc = inter.getAc();
            String shortName = inter.getShortLabel();
            String fullName = inter.getFullName();

            CvInteractionType type = inter.getCvInteractionType();
            String interactionType = type.getShortLabel();
            String description = type.getFullName();

            // annotation definition
            String definition = null;

            for (Annotation annotation : type.getAnnotations())
            {
                if (annotation.getCvTopic().getShortLabel().equals(CvTopic.DEFINITION))
                {
                    definition = annotation.getAnnotationText();
                    break;
                }
            }

            InteractionInfo interInfo = new InteractionInfo(intactAc, shortName, fullName, interactionType, description, definition);
            interInfos.add(interInfo);
        }
        
        return interInfos.toArray(new InteractionInfo[interInfos.size()]);
    }

    /**
     * Finds all interaction partners for given list of protein IDs (only UniProt IDs are supported at present)
     *
     * @param proteinIds
     * @return
     */
    @WebMethod()
    @Transactional(readOnly = true)
    public PartnerResult[] findPartnersUsingUniprotIds(String[] proteinIds)
    {
        if (log.isDebugEnabled())
        {
            if (proteinIds.length == 1)
            {
                log.debug("Finding partners for: " + proteinIds[0]);
            }
            else
            {
                log.debug("Finding partners for an array of " + proteinIds.length + " protein IDs");
            }
        }

        

        List<PartnerResult> results = new ArrayList<PartnerResult>(proteinIds.length);

        int totalFound = 0;
        int intactProtsFound = 0;

        for (String uniprotId : proteinIds)
        {
            List<ProteinImpl> protsWithThisUniprotId = proteinDao.getByUniprotId(uniprotId);

            intactProtsFound = protsWithThisUniprotId.size();

            for (ProteinImpl prot : protsWithThisUniprotId)
            {
                List<String> protIds;

                if (prot != null)
                {
                    protIds = proteinDao.getPartnersUniprotIdsByProteinAc(prot.getAc());
                }
                else
                {
                    protIds = Collections.EMPTY_LIST;
                }

                totalFound = totalFound + protIds.size();

                results.add(new PartnerResult(uniprotId, protIds.toArray(new String[protIds.size()])));
            }
        }

        

        if (log.isDebugEnabled())
        {
            log.debug("Total IntAct prots found: " + intactProtsFound);
            log.debug("Total partners found: " + totalFound);
        }

        return results.toArray(new PartnerResult[results.size()]);
    }

    @WebMethod
    public int countExperimentsUsingIntactQuery(String query)
    {
        

        int count = simpleSearchService.count(Experiment.class, query);

        

        return count;
    }

    @WebMethod
    public int countProteinsUsingIntactQuery(String query)
    {
        int count = simpleSearchService.count(ProteinImpl.class, query);

        return count;
    }

    @WebMethod
    public int countNucleicAcidsUsingIntactQuery(String query)
    {
        

        int count = simpleSearchService.count(NucleicAcidImpl.class, query);

        

        return count;
    }

    @WebMethod
    public int countSmallMoleculesUsingIntactQuery(String query)
    {
        

        int count = simpleSearchService.count(SmallMoleculeImpl.class, query);

        

        return count;
    }

    @WebMethod
    public int countInteractionsUsingIntactQuery(String query)
    {
        

        int count = simpleSearchService.count(InteractionImpl.class, query);

        

        return count;
    }

    @WebMethod
    public int countCvObjectsUsingIntactQuery(String query)
    {
        

        int count = simpleSearchService.count(CvObject.class, query);

        

        return count;
    }

    @WebMethod
    public int countAllBinaryInteractions()
    {
        

        DaoFactory daoFactory = IntactContext.getCurrentInstance().getDataContext().getDaoFactory();
        int componentCount = daoFactory.getComponentDao().countAll();
        int interactionCount = daoFactory.getInteractionDao().countAll();

        int count = (componentCount - interactionCount);

        

        return count;
    }

    @WebMethod
    public List<SimpleResult> searchExperimentsUsingQuery(String query, Integer firstResult, Integer maxResults)
    {
        

        List<SimpleResult> results = searchUsingQuery(query, new Class[]{Experiment.class}, firstResult, maxResults);

        

        return results;
    }

    @WebMethod
    public List<SimpleResult> searchProteinsUsingQuery(String query, Integer firstResult, Integer maxResults)
    {
        

        List<SimpleResult> results = searchUsingQuery(query, new Class[]{ProteinImpl.class}, firstResult, maxResults);

        

        return results;
    }

    @WebMethod
    public List<SimpleResult> searchNucleicAcidsUsingQuery(String query, Integer firstResult, Integer maxResults)
    {
        

        List<SimpleResult> results = searchUsingQuery(query, new Class[]{NucleicAcidImpl.class}, firstResult, maxResults);

        

        return results;
    }

    @WebMethod
    public List<SimpleResult> searchSmallMoleculesUsingQuery(String query, Integer firstResult, Integer maxResults)
    {
        

        List<SimpleResult> results = searchUsingQuery(query, new Class[]{SmallMoleculeImpl.class}, firstResult, maxResults);

        

        return results;
    }

    @WebMethod
    public List<SimpleResult> searchInteractionsUsingQuery(String query, Integer firstResult, Integer maxResults)
    {
        

        List<SimpleResult> results = searchUsingQuery(query, new Class[]{InteractionImpl.class}, firstResult, maxResults);

        

        return results;
    }

    @WebMethod
    public List<SimpleResult> searchCvObjectsUsingQuery(String query, Integer firstResult, Integer maxResults)
    {
        

        List<SimpleResult> results = searchUsingQuery(query, new Class[]{CvObject.class}, firstResult, maxResults);

        

        return results;
    }

    @WebMethod
    public List<SimpleResult> searchUsingQuery(String query, String[] searchableTypes, Integer firstResult, Integer maxResults)
    {
        

        Class<? extends Searchable>[] searchables = new Class[searchableTypes.length];

        for (int i = 0; i < searchableTypes.length; i++)
        {
            try
            {
                searchables[i] = (Class<? extends Searchable>) Class.forName(searchableTypes[i]);
            }
            catch (ClassNotFoundException e)
            {
                throw new IntactException(e);
            }
            //finally
            //{
                //getDataContext().getDaoFactory().getCurrentSession().close();
            //}
        }

        List<SimpleResult> results = searchUsingQuery(query, searchables, firstResult, maxResults);

        

        return results;
    }

    private List<SimpleResult> searchUsingQuery(String query, Class<? extends Searchable>[] searchables, Integer firstResult, Integer maxResults)
    {
        

        List<SimpleResult> results = new ArrayList<SimpleResult>();
        if (firstResult == null) firstResult = 0;
        if (maxResults == null) maxResults = Integer.MAX_VALUE;

        int currentResultsNum = 0;

        for (Class<? extends Searchable> searchable : searchables)
        {
            results.addAll(searchUsingQuery(query, searchable, firstResult, maxResults - currentResultsNum));
            currentResultsNum = currentResultsNum + results.size();

            if (currentResultsNum == maxResults)
            {
                break;
            }
        }

        

        return results;
    }

    private List<SimpleResult> searchUsingQuery(String query, Class<? extends Searchable> searchable, Integer firstResult, Integer maxResults)
    {
        

        List<SimpleResult> results = new ArrayList<SimpleResult>();

        List res = simpleSearchService.search(searchable, query, firstResult, maxResults);

        for (Object result : res)
        {
            AnnotatedObject ao = (AnnotatedObject) result;
            results.add(new SimpleResult(ao.getAc(), ao.getShortLabel(), ao.getFullName(), ao.getClass().getName()));
        }

        

        return results;
    }

    @WebMethod()
    public String getVersion()
    {
        return config.getVersion();
    }


}
