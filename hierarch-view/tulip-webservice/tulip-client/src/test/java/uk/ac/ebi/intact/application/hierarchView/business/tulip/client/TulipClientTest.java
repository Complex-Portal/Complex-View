package uk.ac.ebi.intact.application.hierarchView.business.tulip.client;

import junit.framework.TestCase;
import uk.ac.ebi.intact.tulip.client.generated.ProteinCoordinate;
import uk.ac.ebi.intact.tulip.client.TulipClient;

import java.rmi.RemoteException;

public class TulipClientTest extends TestCase
{

    public void testClient()
    {
        TulipClient client = new TulipClient();

                String content = "(nodes 1 2 3 4 5 6 7 8 9 )\n" +
                                 "(edge 1 7 2)\n" +
                                 "(edge 2 7 8)\n" +
                                 "(edge 3 7 9)\n" +
                                 "(edge 4 7 5)\n" +
                                 "(edge 5 7 1)\n" +
                                 "(edge 6 7 3)\n" +
                                 "(edge 7 7 4)\n" +
                                 "(edge 8 7 3)\n" +
                                 "(edge 9 7 6)";

                ProteinCoordinate[] proteins = null;

                try {
                    System.out.print( "Check if the web service is running ... \n" );
                    proteins = client.getComputedTlpContent( content );
                } catch ( RemoteException e ) {
                    e.printStackTrace();
                }

                if( null == proteins ) {
                    System.out.println( "Error during retreiving of proteins coordinates (null)." );

                    System.out.print( "Try to get service messages ... " );
                    String[] msg = client.getErrorMessages( true );
                    for( int i = 0; i < msg.length; i++ ) {
                        // display coordinates
                        System.out.println( "message " + i + ") " + msg[i] );
                    } // for

                }

                if( 0 == proteins.length ) {
                    System.out.println( "No protein retreived." );
                } else {
                    for( int i = 0; i < proteins.length; i++ ) {
                        // display coordinates
                        System.out.println( proteins[i].getId() +
                                            " \t X=" + proteins[i].getX() +
                                            " \t Y=" + proteins[i].getY() );
                    } // for
                } // else

    }

}
