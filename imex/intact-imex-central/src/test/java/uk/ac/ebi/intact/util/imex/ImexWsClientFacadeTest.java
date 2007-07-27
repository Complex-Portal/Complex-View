package uk.ac.ebi.intact.util.imex;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;
import uk.ac.ebi.intact.util.imex.status.ImexStatusStateDiagram;
import uk.ac.ebi.intact.util.imex.status.ImexPublicationStatus;
import uk.ac.ebi.intact.util.imex.exception.ImexFacadeException;
import uk.ac.ebi.intact.util.imex.model.ImexPublication;
import uk.ac.ebi.imexcentral.wsclient.generated.PublicationUtil;
import uk.ac.ebi.imexcentral.wsclient.generated.ImexCentralWebserviceException_Exception;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by IntelliJ IDEA.
 * User: CatherineLeroy
 * Date: 10-Feb-2007
 * Time: 12:20:09
 * To change this template use File | Settings | File Templates.
 */
public class ImexWsClientFacadeTest extends TestCase {
    private ImexWsClientFacade imexWsClientFacade = new ImexWsClientFacade();
    // This is the first id of the first publication to be created. Each time you run those tests
    // it create if successfull (publicationId, publicationId+1, publicationId+2 and publicationId+3)
    // How can I know they are not already created?
    // Well I can't as the database is not emptied after each time I run the test.
    // So each time i run the test, I need to change the value of publication. Taking it small but bigger than the actual
    //on I have good chance it has not been created.
    //todo : improve the mecanism to select pubmed id to create 
    private String publicationId = "29";
    private String author = "author " + publicationId;
    private String login = "cleroy";
    private String title = "title " + publicationId;

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public ImexWsClientFacadeTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( ImexWsClientFacadeTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testCreatePublication()
    {

        // Make sure that somebody not registered in Imex can not create a publication
        System.out.println("publicationId = " + publicationId);
        try {
            ImexPublication publication = imexWsClientFacade.createPublication(ImexPublication.PUBMED_TYPE, publicationId, ImexStatusStateDiagram.getPostPublication(),title, author, null,null,null,"vercingetorix");
            fail("Vercengertorix is probably not an imex user, he shouldn't have been allowed to create a publication");
        } catch (Exception e) {
            assert(true);
        }

        // Test that one can create a publication with status postpublication.
        System.out.println("publicationId = " + publicationId);
        try {
            ImexPublication publication = imexWsClientFacade.createPublication(ImexPublication.PUBMED_TYPE, publicationId, ImexStatusStateDiagram.getPostPublication(),title, author, null,null,null,login);
        } catch (Exception e) {
            fail("Couldn't create a publication " + e);

        }
        // Test that one can create a publication with status prepublication.
        System.out.println("publicationId = " + publicationId);
        try {
            //We need to get the pubmed id following publicationId as publicationId has already been created.
            int nextPublicationId = new Integer(publicationId);
            nextPublicationId ++;
            System.out.println("nextPublicationId = " + nextPublicationId);
            ImexPublication publication = imexWsClientFacade.createPublication(ImexPublication.PUBMED_TYPE, Integer.toString(nextPublicationId), ImexStatusStateDiagram.getPrePublication(),title, author, null,null,null,login);
        } catch (Exception e) {
            fail("Couldn't create a publication " + e);

        }

        // Make sure that if a publication has already been created, you can not create is twice.
        try {
            ImexPublication publication = imexWsClientFacade.createPublication(ImexPublication.PUBMED_TYPE, publicationId, ImexStatusStateDiagram.getPostPublication(),title, author, null,null,null,login);
            System.out.println("bonjour");
            fail("This publication has already earlier been created, it should have return an error message");
        } catch (Exception e) {
            assert(true);
        }

        // Make sure that if a the type is not valid (is not one of the following : PUBMED, DOI, JOURNAL_SPECIFIC, IMEX)
        // you can not create a publication.
        try {
            int nextPublicationId = new Integer(publicationId);
            nextPublicationId ++;

            ImexPublication publication = imexWsClientFacade.createPublication("WrongType", Integer.toString(nextPublicationId), ImexStatusStateDiagram.getPostPublication(),title, author, null,null,null,login);
            fail("This publication has just been created, it should have return an error message as the type is not a valid type");
        } catch (Exception e) {
            assert(true);
        }

        // Make sure that if a the status is not valid (is not one of the following : postpublication, prepublication, reserved, inprogress, released,discarded,incomplete)
        // you can not create a publication.
        try {
            int nextPublicationId = new Integer(publicationId);
            nextPublicationId ++;
            ImexPublication publication = imexWsClientFacade.createPublication(ImexPublication.PUBMED_TYPE, Integer.toString(nextPublicationId), new ImexPublicationStatus("WrongStatus", Collections.EMPTY_LIST),title, author, null,null,null,login);
            fail("This publication has just been created, it should have return an error message as the status is not a valid one");
        } catch (Exception e) {
            assert(true);
        }

        //Check that one can not create a publication with status discarded
        try {
            int nextPublicationId = new Integer(publicationId);
            nextPublicationId ++;
            ImexPublication publication = imexWsClientFacade.createPublication(ImexPublication.PUBMED_TYPE, Integer.toString(nextPublicationId), ImexStatusStateDiagram.getDiscarded(),title, author, null,null,null,login);
            fail("A publication has been created with status discarded. Status should be post or prepublication");
        } catch (Exception e) {
            assert(true);
        }
        //Check that one can not create a publication with status imcomplete
        try {
            int nextPublicationId = new Integer(publicationId);
            nextPublicationId ++;
            ImexPublication publication = imexWsClientFacade.createPublication(ImexPublication.PUBMED_TYPE, Integer.toString(nextPublicationId), ImexStatusStateDiagram.getIncomplete(),title, author, null,null,null,login);
            fail("A publication has been created with status incomplete. Status should be post or prepublication");
        } catch (Exception e) {
            assert(true);
        }
        //Check that one can not create a publication with status InProgress
        try {
            int nextPublicationId = new Integer(publicationId);
            nextPublicationId ++;
            ImexPublication publication = imexWsClientFacade.createPublication(ImexPublication.PUBMED_TYPE, Integer.toString(nextPublicationId), ImexStatusStateDiagram.getInProgress(),title, author, null,null,null,login);
            fail("A publication has been created with status inprogress. Status should be post or prepublication");
        } catch (Exception e) {
            assert(true);
        }
        //Check that one can not create a publication with status released
        try {
            int nextPublicationId = new Integer(publicationId);
            nextPublicationId ++;
            ImexPublication publication = imexWsClientFacade.createPublication(ImexPublication.PUBMED_TYPE, Integer.toString(nextPublicationId), ImexStatusStateDiagram.getReleased(),title, author, null,null,null,login);
            fail("A publication has been created with status released. Status should be post or prepublication");
        } catch (Exception e) {
            assert(true);
        }
        //Check that one can not create a publication with status reserved
        try {
            int nextPublicationId = new Integer(publicationId);
            nextPublicationId ++;
            ImexPublication publication = imexWsClientFacade.createPublication(ImexPublication.PUBMED_TYPE, Integer.toString(nextPublicationId), ImexStatusStateDiagram.getReserved(),title, author, null,null,null,login);
            fail("A publication has been created with status reserved. Status should be post or prepublication");
        } catch (Exception e) {
            assert(true);
        }

    }

    public void testGetPublicationByStatus(){
        Collection<PublicationUtil> publicationUtils = new ArrayList();
        try {
            publicationUtils = imexWsClientFacade.getPublicationByStatus(ImexStatusStateDiagram.getPostPublication());
            System.out.println("imexPublications.size() = " + publicationUtils.size());
        } catch (ImexFacadeException e) {

            fail("Couldn't get the Publications  for status PostPublication");
        }
        if(publicationUtils.size() <= 0){
            fail ("Didn't get any Publications for status PostPublication, possible but very surprising, to check manually");
        }

    }

    public void testGetPublicationById(){
        Collection<PublicationUtil> publicationUtils = new ArrayList();

        try {
            publicationUtils = imexWsClientFacade.getPublicationById(publicationId);
        } catch (ImexFacadeException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        PublicationUtil createdPublication = null;
        for (PublicationUtil publicationUtil : publicationUtils){
            // todo: when Nisha will have add the getType in PublicationUtil, I will do the test on the type as well
            if(publicationId.equals(publicationUtil.getIdentifier())/* && ImexPublication.PUBMED_TYPE.equals(publicationUtil.getType())*/){
                createdPublication = publicationUtil;
                break;
            }
        }
        if(createdPublication == null){
            fail("The publication created in the testCreatePublication() has not been found");
        }else{
            assertEquals(title, createdPublication.getTitle());
            assertEquals(author, createdPublication.getAuthor());
            //todo: when Nisha will have add the getLoginId in PublicationUtil, I will uncomment the following line.
//            assertEquals(login, createdPublication.getLoginId());
            assertEquals(ImexStatusStateDiagram.getPostPublication(),createdPublication.getStatus());
        }
    }

    public void testUpdatePublicationStatus(){

        int count = new Integer(publicationId);
        count = count + 2;
        String firstPublication = Integer.toString(count);
        count = count + 1;
        String secondPublication = Integer.toString(count);


        //FirstPublication is created with status postpublication.
        /*---------------------------------------
         * FirstPublication : postpublication
         * ---------------------------------------*/
        try {
            imexWsClientFacade.createPublication(ImexPublication.PUBMED_TYPE, firstPublication, ImexStatusStateDiagram.getPostPublication(),"title", "author", null,null,null,login);
        } catch (ImexFacadeException e) {
            fail("Can't do the test if we couldn't create the publication");
        }

        try {
            imexWsClientFacade.updatePublicationStatus(firstPublication, ImexStatusStateDiagram.getPrePublication(),login);
            fail("postPublication ==> prepublication FORBIDEN");
        } catch (ImexCentralWebserviceException_Exception e) {
            assert(true);
        }

        //postPublication ==> discarded FORBIDEN
        try{
            imexWsClientFacade.updatePublicationStatus(firstPublication, ImexStatusStateDiagram.getDiscarded(),login);
            fail("postPublication ==> discarded FORBIDEN");
        } catch (ImexCentralWebserviceException_Exception e) {
            assert(true);
        }

        //postPublication ==> inprogress FORBIDEN
        try{
            imexWsClientFacade.updatePublicationStatus(firstPublication, ImexStatusStateDiagram.getInProgress(),login);
            fail("postPublication ==> inprogress FORBIDEN");
        } catch (ImexCentralWebserviceException_Exception e) {
            assert(true);
        }

        //postPublication ==> incomplete FORBIDEN
        try{
            imexWsClientFacade.updatePublicationStatus(firstPublication, ImexStatusStateDiagram.getIncomplete(),login);
            fail("postPublication ==> incomplete FORBIDEN");
        } catch (ImexCentralWebserviceException_Exception e) {
            assert(true);
        }

        //postPublication ==> released FORBIDEN
        try{
            imexWsClientFacade.updatePublicationStatus(firstPublication, ImexStatusStateDiagram.getReleased(),login);
            fail("postPublication ==> released FORBIDEN");
        } catch (ImexCentralWebserviceException_Exception e) {
            assert(true);
        }

        /*---------------------------------------
         * FirstPublication : reserved
         * ---------------------------------------*/
        //postPublication ==> reserved ALLOWED
        try {
            imexWsClientFacade.updatePublicationStatus(firstPublication, ImexStatusStateDiagram.getReserved(),login);
            assert true;
        } catch (ImexCentralWebserviceException_Exception e) {
            fail("postPublication ==> reserved ALLOWED");
        }

        //reserved ==> prepublication FORBIDEN
        try {
            imexWsClientFacade.updatePublicationStatus(firstPublication, ImexStatusStateDiagram.getPrePublication(),login);
            fail("reserved ==> prepublication FORBIDEN");
        } catch (ImexCentralWebserviceException_Exception e) {
            assert(true);
        }

        //reserved ==> postpublcation FORBIDEN
        try{
            imexWsClientFacade.updatePublicationStatus(firstPublication, ImexStatusStateDiagram.getPostPublication(),login);
            fail("//reserved ==> postpublcation FORBIDEN");
        } catch (ImexCentralWebserviceException_Exception e) {
            assert(true);
        }

        //reserved ==> released FORBIDEN
        try{
            imexWsClientFacade.updatePublicationStatus(firstPublication, ImexStatusStateDiagram.getReleased(),login);
            fail("reserved ==> released FORBIDEN");
        } catch (ImexCentralWebserviceException_Exception e) {
            assert(true);
        }

        /*---------------------------------------
         * FirstPublication : incomplete
         * ---------------------------------------*/
        //reserved ==> incomplete ALLOWED
        try {
            imexWsClientFacade.updatePublicationStatus(firstPublication, ImexStatusStateDiagram.getIncomplete(),login);
        } catch (ImexCentralWebserviceException_Exception e) {
            fail("reserved ==> incomplete ALLOWED");
        }




        //incomplete ==> reserved : FORBIDEN
        try {
            imexWsClientFacade.updatePublicationStatus(firstPublication, ImexStatusStateDiagram.getReserved(),login);
            fail("//incomplete ==> reserved : FORBIDEN");
        } catch (ImexCentralWebserviceException_Exception e) {
            assert true;
        }

        //incomplete ==> released : FORBIDEN
        try {
            imexWsClientFacade.updatePublicationStatus(firstPublication, ImexStatusStateDiagram.getReleased(),login);
            fail("incomplete ==> released : FORBIDEN");
        } catch (ImexCentralWebserviceException_Exception e) {
            assert true;
        }

        //incomplete ==> prepublication FORBIDEN
        try {
            imexWsClientFacade.updatePublicationStatus(firstPublication, ImexStatusStateDiagram.getPrePublication(),login);
            fail("incomplete ==> prepublication FORBIDEN");
        } catch (ImexCentralWebserviceException_Exception e) {
            assert true;
        }

        //incomplete ==> postpublication FORBIDEN
        try {
            imexWsClientFacade.updatePublicationStatus(firstPublication, ImexStatusStateDiagram.getPostPublication(),login);
            fail("incomplete ==> postpublication FORBIDEN");
        } catch (ImexCentralWebserviceException_Exception e) {
            assert true;
        }

        /**---------------------------------------
         * FirstPublication : inprogress
         * ---------------------------------------*/
        //incomplete ==> inprogress  ALLOWED
        try {
            imexWsClientFacade.updatePublicationStatus(firstPublication, ImexStatusStateDiagram.getInProgress(),login);
        } catch (ImexCentralWebserviceException_Exception e) {
            fail("incomplete ==> inprogress  ALLOWED");
        }

        //inprogress ==> reserved FORBIDEN
        try{
            imexWsClientFacade.updatePublicationStatus(firstPublication, ImexStatusStateDiagram.getReserved(),login);
            fail("inprogress ==> reserved FORBIDEN");
        } catch (ImexCentralWebserviceException_Exception e) {
            assert(true);
        }

        //inprogress ==> prepublication FORBIDEN
        try{
            imexWsClientFacade.updatePublicationStatus(firstPublication, ImexStatusStateDiagram.getPrePublication(),login);
            fail("inprogress ==> prepublication FORBIDEN");
        } catch (ImexCentralWebserviceException_Exception e) {
            assert(true);
        }

        //inprogress ==> postpublication FORBIDEN
        try{
            imexWsClientFacade.updatePublicationStatus(firstPublication, ImexStatusStateDiagram.getPostPublication(),login);
            fail("inprogress ==> postpublication FORBIDEN");
        } catch (ImexCentralWebserviceException_Exception e) {
            assert(true);
        }


        /**---------------------------------------
         * FirstPublication : released
         * ---------------------------------------*/
        //inprogress ==>released ALLOWED
        try {
            imexWsClientFacade.updatePublicationStatus(firstPublication, ImexStatusStateDiagram.getReleased(),login);
        } catch (ImexCentralWebserviceException_Exception e) {
            fail("inprogress ==>released ALLOWED");
        }

        //released==>discarded  FORBIDEN
        try{
            imexWsClientFacade.updatePublicationStatus(firstPublication, ImexStatusStateDiagram.getDiscarded(),login);
            fail("released==>discarded  FORBIDEN");
        } catch (ImexCentralWebserviceException_Exception e) {
            assert(true);
        }
        //released==>inprogress FORBIDEN
        try{
            imexWsClientFacade.updatePublicationStatus(firstPublication, ImexStatusStateDiagram.getInProgress(),login);
            fail("released==>inprogress FORBIDEN");
        } catch (ImexCentralWebserviceException_Exception e) {
            assert(true);
        }
        //released==>incomplete FORBIDEN
        try{
            imexWsClientFacade.updatePublicationStatus(firstPublication, ImexStatusStateDiagram.getIncomplete(),login);
            fail("released==>incomplete FORBIDEN");
        } catch (ImexCentralWebserviceException_Exception e) {
            assert(true);
        }
        //released==>reserved FORBIDEN
        try{
            imexWsClientFacade.updatePublicationStatus(firstPublication, ImexStatusStateDiagram.getReserved(),login);
            fail("released==>reserved FORBIDEN");
        } catch (ImexCentralWebserviceException_Exception e) {
            assert(true);
        }
        //released==>postpublication FORBIDEN
        try{
            imexWsClientFacade.updatePublicationStatus(firstPublication, ImexStatusStateDiagram.getPostPublication(),login);
            fail("released==>postpublication FORBIDEN");
        } catch (ImexCentralWebserviceException_Exception e) {
            assert(true);
        }
        //released==>prepublication FORBIDEN
        try{
            imexWsClientFacade.updatePublicationStatus(firstPublication, ImexStatusStateDiagram.getPrePublication(),login);
            fail("released==>prepublication FORBIDEN");
        } catch (ImexCentralWebserviceException_Exception e) {
            assert(true);
        }


        /*---------------------------------------
         * SecondPublication : prePublication
         * ---------------------------------------*/
        //Create secondPublication with status prePublicaiton
        try {
            imexWsClientFacade.createPublication(ImexPublication.PUBMED_TYPE, secondPublication, ImexStatusStateDiagram.getPrePublication(),"title", "author", null,null,null,login);
        } catch (ImexFacadeException e) {
            fail("Can't do the test if we couldn't create the publication");
        }

        //prepublication ==> postpublication FORBIDEN
        try{
            imexWsClientFacade.updatePublicationStatus(secondPublication, ImexStatusStateDiagram.getPostPublication(),login);
            fail("prepublication ==> postpublication FORBIDEN");
        } catch (ImexCentralWebserviceException_Exception e) {
            assert(true);
        }

        //prepublication ==> discarded FORBIDEN
        try{
            imexWsClientFacade.updatePublicationStatus(secondPublication, ImexStatusStateDiagram.getDiscarded(),login);
            fail("prepublication ==> discarded FORBIDEN");
        } catch (ImexCentralWebserviceException_Exception e) {
            assert(true);
        }

        //prepublication ==> inprogress FORBIDEN
        try{
            imexWsClientFacade.updatePublicationStatus(secondPublication, ImexStatusStateDiagram.getInProgress(),login);
            fail("prepublication ==> inprogress FORBIDEN");
        } catch (ImexCentralWebserviceException_Exception e) {
            assert(true);
        }

        //prepublication ==> incomplete FORBIDEN
        try{
            imexWsClientFacade.updatePublicationStatus(secondPublication, ImexStatusStateDiagram.getIncomplete(),login);
            fail("prepublication ==> incomplete FORBIDEN");
        } catch (ImexCentralWebserviceException_Exception e) {
            assert(true);
        }

        //prepublication ==> released FORBIDEN
        try{
            imexWsClientFacade.updatePublicationStatus(secondPublication, ImexStatusStateDiagram.getReleased(),login);
            fail("Changing the status from reserved to released should not have been possible");
        } catch (ImexCentralWebserviceException_Exception e) {
            assert(true);
        }

        /*---------------------------------------
         * SecondPublication : reserved
         * ---------------------------------------*/
        //prepublication ==> reserved ALLOWED
         try {
            imexWsClientFacade.updatePublicationStatus(secondPublication, ImexStatusStateDiagram.getReserved(),login);
        } catch (ImexCentralWebserviceException_Exception e) {
            fail("prepublication ==> reserved ALLOWED");
        }

        /*---------------------------------------
         * SecondPublication : inprogress
         * ---------------------------------------*/
        try {
            imexWsClientFacade.updatePublicationStatus(secondPublication, ImexStatusStateDiagram.getInProgress(),login);
        } catch (ImexCentralWebserviceException_Exception e) {
            fail("We couldn't bring it from reserved to inprogress, we won't be able to test the rest");
        }

        /*---------------------------------------
         * SecondPublication : discarded
         * ---------------------------------------*/
        // inprogress ==> discarded ALLOWED
        try {
            imexWsClientFacade.updatePublicationStatus(secondPublication, ImexStatusStateDiagram.getDiscarded(),login);
        } catch (ImexCentralWebserviceException_Exception e) {
            fail("inprogress ==> discarded ALLOWED");
        }

        // discarded ==> released FORBIDEN
        try{
            imexWsClientFacade.updatePublicationStatus(secondPublication, ImexStatusStateDiagram.getReleased(),login);
            fail("Changing the status from reserved to released should not have been possible");
        } catch (ImexCentralWebserviceException_Exception e) {
            assert(true);
        }

        // discarded ==> postpublication FORBIDEN
        try{
            imexWsClientFacade.updatePublicationStatus(secondPublication, ImexStatusStateDiagram.getPostPublication(),login);
            fail("Changing the status from reserved to released should not have been possible");
        } catch (ImexCentralWebserviceException_Exception e) {
            assert(true);
        }

        // discarded ==> prepublication FORBIDEN
        try{
            imexWsClientFacade.updatePublicationStatus(secondPublication, ImexStatusStateDiagram.getPrePublication(),login);
            fail("Changing the status from reserved to released should not have been possible");
        } catch (ImexCentralWebserviceException_Exception e) {
            assert(true);
        }

        // discarded ==> incomplete FORBIDEN
        try{
            imexWsClientFacade.updatePublicationStatus(secondPublication, ImexStatusStateDiagram.getIncomplete(),login);
            fail("Changing the status from reserved to released should not have been possible");
        } catch (ImexCentralWebserviceException_Exception e) {
            assert(true);
        }

        /*---------------------------------------
         * SecondPublication : inprogress
         * ---------------------------------------*/
        // discarded ==> inprogress ALLOWED
        try {
            imexWsClientFacade.updatePublicationStatus(secondPublication, ImexStatusStateDiagram.getInProgress(),login);
        } catch (ImexCentralWebserviceException_Exception e) {
            fail("inprogress ==> discarded ALLOWED");
        }

        /*---------------------------------------
         * SecondPublication : incomplete
         * ---------------------------------------*/
        // inprogress ==> incomplete ALLOWED
        try {
            imexWsClientFacade.updatePublicationStatus(secondPublication, ImexStatusStateDiagram.getIncomplete(),login);
        } catch (ImexCentralWebserviceException_Exception e) {
            fail("inprogress ==> discarded ALLOWED");
        }

        /*---------------------------------------
         * SecondPublication : inprogress
         * ---------------------------------------*/
        // incomplete ==> inprogress ALLOWED
        try {
            imexWsClientFacade.updatePublicationStatus(secondPublication, ImexStatusStateDiagram.getIncomplete(),login);
        } catch (ImexCentralWebserviceException_Exception e) {
            fail("inprogress ==> discarded ALLOWED");
        }

        //todo incomplete ==>  discarded ALLOWED ?
        // todo: discarded ==> reserved ?

    }


}
