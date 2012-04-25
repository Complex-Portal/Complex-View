package uk.ac.ebi.intact.editor.controller.curate.publication;

import edu.ucla.mbi.imex.central.ws.v20.Publication;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.bridges.imexcentral.*;
import uk.ac.ebi.intact.bridges.imexcentral.mock.MockImexCentralClient;
import uk.ac.ebi.intact.editor.ApplicationInitializer;

import java.util.List;

/**
 * Wrapper of the Imex central client
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>16/04/12</pre>
 */

public class ImexCentralClientWrapper implements ImexCentralClient{

    private static final Log log = LogFactory.getLog(ApplicationInitializer.class);
    private ImexCentralClient imexCentralClient;
    
    public ImexCentralClientWrapper(String username, String password, String endpoint) throws ImexCentralException {
        String localTrustStore = System.getProperty( "javax.net.ssl.trustStore" );
        String localTrustStorePwd = System.getProperty( "javax.net.ssl.keyStorePassword" );
        if(localTrustStore==null) {
            log.error( "It appears you haven't setup a local trust store (other than the one embedded in the JDK)." +
                    "\nShould you want to specify one, use: -Djavax.net.ssl.trustStore=<path.to.keystore> " +
                    "\nAnd if it is password protected, use: -Djavax.net.ssl.keyStorePassword=<password>" );
        } else {
            log.info( "Using local trust store: " + localTrustStore + (localTrustStorePwd == null ? " (no password set)" : " (with password set)" ) );
        }

        if (username != null && password != null && endpoint != null && !username.isEmpty() && !password.isEmpty() && !endpoint.isEmpty()){
            imexCentralClient = new DefaultImexCentralClient(username, password, endpoint);
        }
        else {
            imexCentralClient = new MockImexCentralClient();
        }
    }
    
    @Override
    public String getEndpoint() {
        return imexCentralClient.getEndpoint();
    }

    @Override
    public Publication getPublicationById(String identifier) throws ImexCentralException {
        return imexCentralClient.getPublicationById(identifier);
    }

    @Override
    public List<Publication> getPublicationByOwner(String owner, int first, int max) throws ImexCentralException {
        return imexCentralClient.getPublicationByOwner(owner, first, max);
    }

    @Override
    public List<Publication> getPublicationByStatus(String status, int first, int max) throws ImexCentralException {
        return imexCentralClient.getPublicationByStatus(status, first, max);
    }

    @Override
    public Publication updatePublicationStatus(String identifier, PublicationStatus status) throws ImexCentralException {
        return imexCentralClient.updatePublicationStatus(identifier, status);
    }

    @Override
    public Publication updatePublicationAdminGroup(String identifier, Operation operation, String group) throws ImexCentralException {
        return imexCentralClient.updatePublicationAdminGroup(identifier, operation, group);
    }

    @Override
    public Publication updatePublicationAdminUser(String identifier, Operation operation, String user) throws ImexCentralException {
        return imexCentralClient.updatePublicationAdminUser(identifier, operation, user);
    }

    @Override
    public Publication updatePublicationIdentifier(String oldIdentifier, String newIdentifier) throws ImexCentralException {
        return imexCentralClient.updatePublicationIdentifier(oldIdentifier, newIdentifier);
    }

    @Override
    public void createPublication(Publication publication) throws ImexCentralException {
       imexCentralClient.createPublication(publication);
    }

    @Override
    public Publication createPublicationById(String identifier) throws ImexCentralException {
        return imexCentralClient.createPublicationById(identifier);
    }

    @Override
    public Publication getPublicationImexAccession(String identifier, boolean aBoolean) throws ImexCentralException {
        return imexCentralClient.getPublicationImexAccession(identifier, aBoolean);
    }
}
