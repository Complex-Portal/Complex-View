package uk.ac.ebi.intact.search.wsclient;

import junit.framework.TestCase;
import uk.ac.ebi.intact.search.wsclient.generated.InteractionInfo;

import java.util.List;


public class SearchClientTest extends TestCase
{

    private static final String LOCALHOST_URL = "http://localhost:8080/search-ws-1.5.1-SNAPSHOT/search";

    private SearchServiceClient client;

    protected void tearDown() throws Exception
    {
        super.tearDown();
        client = null;
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        client = new SearchServiceClient();
    }

    public void testFindPartnersUsingUniprotIds() throws Exception
    {
        List<String> uniprotIds = client.findPartnersUsingUniprotIds("P52292");

        for (String id : uniprotIds)
        {
            System.out.println(id);
        }
    }

    public void testGetInteractionInfoUsingIntactIds() throws Exception
    {
        List<InteractionInfo> interInfos = client.getInteractionInfoUsingIntactIds("EBI-1004115","EBI-710997");

        for (InteractionInfo interInfo : interInfos)
        {
            System.out.println(interInfo.getIntactAc());
        }

    }

    public void testGetInteractionInfoUsingUniprotIds() throws Exception
    {
        List<InteractionInfo> interInfos = client.getInteractionInfoUsingUniprotIds("Q15691","P54274");

        for (InteractionInfo interInfo : interInfos)
        {
            System.out.println(interInfo.getIntactAc());
        }

    }
     

    public void testServiceVersion() throws Exception
    {
        System.out.println(client.getServiceVersion());
    }

    public void testCountExperiments() throws Exception
    {
        System.out.println(client.getSearchPort().countExperimentsUsingIntactQuery("brca2"));
    }

    public void testGetInteractionInfoForInteractionAc() throws Exception {

        InteractionInfo interactionInfo = client.getInteractionInfoForInteractionAc("EBI-1264463");
        
        System.out.println("Short name: "+interactionInfo.getShortName());
        System.out.println("Full name: "+interactionInfo.getFullName());
        System.out.println("IntAct AC: "+interactionInfo.getIntactAc());
    }
}
