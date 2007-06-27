/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.application.hierarchview.business.graph.test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import uk.ac.ebi.intact.application.hierarchview.business.graph.InteractionNetwork;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.searchengine.CriteriaBean;
import uk.ac.ebi.intact.util.simplegraph.Edge;
import uk.ac.ebi.intact.util.simplegraph.EdgeI;
import uk.ac.ebi.intact.util.simplegraph.Node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * A template for a test class.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 */
public class InteractionNetworkTest extends TestCase {

    Institution institution;
    BioSource bio1, bio2;
    Experiment exp1, exp2;
    CvInteractionType interactionType;
    Interaction int1, int2;
    Component comp1, comp2, comp3, comp4, comp5, comp6, comp7, comp8, comp9;
    CvComponentRole bait, prey;
    Protein   prot1, prot2, prot3, prot4, prot5, prot6, prot7, prot8, prot9;

    InteractionNetwork network1, network2;
    CriteriaBean criteria1, criteria2;


    /**
     * Constructs a InteractionNetworkTest instance with the specified name.
     *
     * @param name the name of the test.
     */
    public InteractionNetworkTest(String name) {
        super(name);
    }

    private Protein createProtein (Institution institution, BioSource bio, String ac, String shortLabel,
                                   String fullname, String crc64, CvInteractorType protType) {
        Protein prot = new ProteinImpl( institution, bio, shortLabel, protType);
        // TODO I have to set the AC to make the fusion work
        prot.setAc( ac );
        prot.setOwnerAc( institution.getAc() );
        prot.setFullName( fullname );
        prot.setCrc64( crc64);
        return prot;
    }

    private void createEdge ( InteractionNetwork network, Component comp1, Component comp2 ) {
        Protein prot1 = (Protein) comp1.getInteractor(),
                prot2 = (Protein) comp2.getInteractor();
        EdgeI edge = new Edge();
        edge.setComponent1( comp1 );
        edge.setComponent2( comp2 );
        Node node1 = new Node( prot1 );
        Node node2 = new Node( prot2 );
        edge.setNode1( node1 );
        edge.setNode2( node2 );

        network.addNode( node1 );
        network.addNode( node1 );
        network.addEdge( edge );
    }

    /**
     * Sets up the test fixture. Called before every test case method.
     */
    protected void setUp() throws Exception {
        super.setUp();

        // Write setting up code for each test.

        institution = new Institution("Boss");

        //NB if Institution is not to extend BasicObject, its created/updated need setting also
        institution.setFullName("The Owner Of Everything");
        institution.setPostalAddress("1 AnySreet, AnyTown, AnyCountry");
        institution.setUrl("http://www.dummydomain.org");

        bio1 = new BioSource(institution, "bio1", "1");
        bio1.setOwnerAc(institution.getAc());
        bio1.setFullName("test biosource 1");

        bio2 = new BioSource(institution, "bio2", "2");
        bio2.setOwnerAc(institution.getAc());
        bio2.setFullName("test biosource 2");

        exp1 = new Experiment(institution, "exp1", bio1);
        exp1.setOwnerAc(institution.getAc());
        exp1.setFullName("test experiment 1");

        Collection colexp1 = new ArrayList(1);
        colexp1.add( exp1 );

        CvInteractorType interactorType = new CvInteractorType( institution, "interaction" );

        interactionType = new CvInteractionType( institution, "type" );
        int1 = new InteractionImpl( colexp1, new ArrayList(), interactionType,
                interactorType, "int1", institution  );

        exp2 = new Experiment(institution, "exp2", bio2);
        exp2.setOwnerAc(institution.getAc());
        exp2.setFullName("test experiment 2");

        Collection colexp2 = new ArrayList(1);
        colexp2.add( exp2 );

        int2 = new InteractionImpl( colexp2, new ArrayList(), interactionType,
                interactorType, "int1", institution  );

        bait = new CvComponentRole( institution, "bait");
        prey = new CvComponentRole( institution, "prey");

        CvInteractorType protType = new CvInteractorType( institution, "protein" );

        prot1 = createProtein( institution, bio1, "1", "P1", "test protein 1", "dummy 1 crc64", protType );
        prot2 = createProtein( institution, bio1, "2", "P2", "test protein 2", "dummy 2 crc64", protType );
        prot3 = createProtein( institution, bio1, "3", "P3", "test protein 3", "dummy 3 crc64", protType );
        prot4 = createProtein( institution, bio1, "4", "P4", "test protein 4", "dummy 4 crc64", protType );
        prot5 = createProtein( institution, bio1, "5", "P5", "test protein 5", "dummy 5 crc64", protType );
        prot6 = createProtein( institution, bio1, "6", "P6", "test protein 6", "dummy 6 crc64", protType );
        prot7 = createProtein( institution, bio1, "7", "P7", "test protein 7", "dummy 7 crc64", protType );
        prot8 = createProtein( institution, bio1, "8", "P8", "test protein 8", "dummy 8 crc64", protType );
        prot9 = createProtein( institution, bio1, "9", "P9", "test protein 9", "dummy 9 crc64", protType );

        comp1 = new Component( institution, int1, prot1, bait);
        comp2 = new Component( institution, int1, prot2, prey);
        comp3 = new Component( institution, int1, prot3, prey);
        comp4 = new Component( institution, int1, prot4, prey);
        comp5 = new Component( institution, int1, prot5, prey);
        comp6 = new Component( institution, int1, prot6, prey);
        comp7 = new Component( institution, int1, prot7, prey);
        comp8 = new Component( institution, int1, prot8, prey);
        comp9 = new Component( institution, int1, prot9, prey);

        // Build networks
        network1 = new InteractionNetwork( prot1 );
        createEdge( network1, comp1, comp2 );
        createEdge( network1, comp1, comp3 );
        createEdge( network1, comp1, comp4 );
        createEdge( network1, comp2, comp3 );
        createEdge( network1, comp4, comp5 );
        createEdge( network1, comp4, comp6 );
        createEdge( network1, comp4, comp7 );
        criteria1 = new CriteriaBean( "prot1", "shortlabel" );
        network1.addCriteria( criteria1 );

        comp1 = new Component( institution, int2, prot1, prey);
        comp2 = new Component( institution, int2, prot2, prey);
        comp3 = new Component( institution, int2, prot3, prey);
        comp4 = new Component( institution, int2, prot4, prey);
        comp5 = new Component( institution, int2, prot5, prey);
        comp6 = new Component( institution, int2, prot6, prey);
        comp7 = new Component( institution, int2, prot7, prey);
        comp8 = new Component( institution, int2, prot8, prey);
        comp9 = new Component( institution, int2, prot9, bait);

        network2 = new InteractionNetwork( prot9 );
        createEdge( network2, comp9, comp8 );
        createEdge( network2, comp9, comp7 );
        createEdge( network2, comp8, comp2 );
        createEdge( network2, comp8, comp3 );
        createEdge( network2, comp7, comp4 );
        criteria2 = new CriteriaBean( "prot9", "shortlabel" );
        network2.addCriteria( criteria2 );

    }



    /**
     * Tears down the test fixture. Called after every test case method.
     */
    protected void tearDown() throws Exception {
        // Release resources for after running a test.
        super.tearDown();
    }

    /**
     * Returns this test suite. Reflection is used here to add all
     * the testXXX() methods to the suite.
     */
    public static Test suite() {
        return new TestSuite( InteractionNetworkTest.class );
    }

    private boolean containsNode ( InteractionNetwork network, Protein prot1, Protein prot2 ) {
        for ( Iterator iterator = network.getEdges().iterator (); iterator.hasNext (); ) {
            EdgeI edge = (EdgeI) iterator.next ();
            Node node1 = (Node) edge.getNode1();
            Node node2 = (Node) edge.getNode2();
            if ( ( node1.getInteractor() == prot1 ) && ( node2.getInteractor() == prot2 )
                  ||
                 ( node1.getInteractor() == prot2 ) && ( node2.getInteractor() == prot1 ) ) {
                return true;
            }
        }
        return false;
    }


    /**
     * In this method, you should use various assert
     * methods of the super class to test conditions. For example,
     * <pre>
     * assertTrue("Didn't generate IntactTypes resource files correctly",
     *    compareResources(newResources, expectedResources));
     * </pre>
     * You can also use fail method to indicate that the test is a failure.
     * This is normally used in a catch block if a method is not expected to
     * throw an exception as shown below:
     * <pre>
     *   try {
     *      // Run some method and catch the exception.
     *      objtotest.someMethod();
     *   }
     *   catch (AssertFailureException ex) {
     *      // Fatal error; we shouldn't get this.
     *      fail(ex.getMessage());
     *   }
     * </pre>
     */
    public void testFusion() {

        display( network1 );
        display( network2 );

        network1.fusion( network2 );

        display( network1 );

        assertTrue( "Fusion failed, P1 - P2 should be present", containsNode( network1, prot1, prot2 ) );
        assertTrue( "Fusion failed, P1 - P3 should be present", containsNode( network1, prot1, prot3 ) );
        assertTrue( "Fusion failed, P1 - P4 should be present", containsNode( network1, prot1, prot4 ) );
        assertTrue( "Fusion failed, P2 - P3 should be present", containsNode( network1, prot2, prot3 ) );
        assertTrue( "Fusion failed, P4 - P5 should be present", containsNode( network1, prot4, prot5 ) );
        assertTrue( "Fusion failed, P4 - P6 should be present", containsNode( network1, prot4, prot6 ) );
        assertTrue( "Fusion failed, P4 - P7 should be present", containsNode( network1, prot4, prot7 ) );
        assertTrue( "Fusion failed, P8 - P2 should be present", containsNode( network1, prot8, prot2 ) );
        assertTrue( "Fusion failed, P8 - P3 should be present", containsNode( network1, prot8, prot3 ) );
        assertTrue( "Fusion failed, P9 - P7 should be present", containsNode( network1, prot9, prot7 ) );
        assertTrue( "Fusion failed, P9 - P8 should be present", containsNode( network1, prot9, prot8 ) );

        assertTrue( "P1 should be central protein", network1.getCentralInteractors().contains( prot1 ));
        assertTrue( "P9 should be central protein", network1.getCentralInteractors().contains( prot9 ));

        assertTrue( "Original criteria removed",                   network1.getCriteria().contains( criteria1 ));
        assertTrue( "Criteria of the fusionned network not added", network1.getCriteria().contains( criteria2 ));

        assertTrue( "", network1.getEdges().size() == 11 );
    }

    private void display ( InteractionNetwork network ) {
        System.out.println ( "InteractionNetwork" );
        Collection edges = network.getEdges();
        for ( Iterator iterator = edges.iterator (); iterator.hasNext (); ) {
            EdgeI edge = (EdgeI) iterator.next ();
            Node node1 = (Node) edge.getNode1();
            Node node2 = (Node) edge.getNode2();
            System.out.println ( node1.getInteractor().getShortLabel() + " - " +
                                 node2.getInteractor().getShortLabel());
        }

    }
}
