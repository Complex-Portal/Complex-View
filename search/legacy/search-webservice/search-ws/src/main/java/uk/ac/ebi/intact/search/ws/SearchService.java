package uk.ac.ebi.intact.search.ws;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import java.util.List;

/**
 * TODO comment that class header
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@WebService(targetNamespace = "http://ebi.ac.uk/intact/search/wsclient/generated")
public interface SearchService {
    @WebMethod()
    InteractionInfo[] getInteractionInfoUsingUniprotIds(String uniprotId1, String uniprotId2);

    @WebMethod()
    InteractionInfo[] getInteractionInfoUsingIntactIds(String id1,String id2);

    @WebMethod()
    PartnerResult[] findPartnersUsingUniprotIds(String[] proteinIds);

    @WebMethod
    int countExperimentsUsingIntactQuery(String query);

    @WebMethod
    int countProteinsUsingIntactQuery(String query);

    @WebMethod
    int countNucleicAcidsUsingIntactQuery(String query);

    @WebMethod
    int countSmallMoleculesUsingIntactQuery(String query);

    @WebMethod
    int countInteractionsUsingIntactQuery(String query);

    @WebMethod
    int countCvObjectsUsingIntactQuery(String query);

    @WebMethod
    int countAllBinaryInteractions();

    @WebMethod
    List<SimpleResult> searchExperimentsUsingQuery(String query, Integer firstResult, Integer maxResults);

    @WebMethod
    List<SimpleResult> searchProteinsUsingQuery(String query, Integer firstResult, Integer maxResults);

    @WebMethod
    List<SimpleResult> searchNucleicAcidsUsingQuery(String query, Integer firstResult, Integer maxResults);

    @WebMethod
    List<SimpleResult> searchSmallMoleculesUsingQuery(String query, Integer firstResult, Integer maxResults);

    @WebMethod
    List<SimpleResult> searchInteractionsUsingQuery(String query, Integer firstResult, Integer maxResults);

    @WebMethod
    List<SimpleResult> searchCvObjectsUsingQuery(String query, Integer firstResult, Integer maxResults);

    @WebMethod
    List<SimpleResult> searchUsingQuery(String query, String[] searchableTypes, Integer firstResult, Integer maxResults);

    @WebMethod()
    String getVersion();
}
