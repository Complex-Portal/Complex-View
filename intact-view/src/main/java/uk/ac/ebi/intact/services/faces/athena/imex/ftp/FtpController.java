package uk.ac.ebi.intact.services.faces.athena.imex.ftp;

import uk.ac.ebi.intact.dataexchange.imex.repository.ftp.ImexFTPFile;
import uk.ac.ebi.intact.dataexchange.imex.repository.ftp.ImexFTPClient;
import uk.ac.ebi.intact.dataexchange.imex.repository.ftp.ImexFTPClientFactory;
import uk.ac.ebi.intact.services.faces.AthenaFacesException;
import uk.ac.ebi.intact.services.faces.athena.imex.ftp.ImexFTPFileWrapper;

import java.util.List;
import java.util.ArrayList;
import java.io.IOException;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class FtpController {

    private List<ImexFTPFileWrapper> intactFtpFiles;
    private List<ImexFTPFileWrapper> mintFtpFiles;
    private List<ImexFTPFileWrapper> dipFtpFiles;

    public FtpController() {
    }

    private List<ImexFTPFileWrapper> loadFtpFiles(ImexFTPClient client) {
        List<ImexFTPFileWrapper> files = new ArrayList<ImexFTPFileWrapper>();
        try {
            client.connect();
            for (ImexFTPFile file : client.listFiles()) {
               files.add(new ImexFTPFileWrapper(file));
            }
            client.disconnect();
        }
        catch (IOException e) {
            throw new AthenaFacesException("Problem listing "+client.getClass().getSimpleName()+" FTP Files", e);
        }

        return files;
    }

    // Getters and Setters
    /////////////////////////////////

    public List<ImexFTPFileWrapper> getIntactFtpFiles() {
        if (intactFtpFiles == null) {
            intactFtpFiles = loadFtpFiles(ImexFTPClientFactory.createIntactClient());
        }
        return intactFtpFiles;
    }

    public List<ImexFTPFileWrapper> getMintFtpFiles() {
        if (mintFtpFiles == null) {
            mintFtpFiles = loadFtpFiles(ImexFTPClientFactory.createMintClient());
        }
        return mintFtpFiles;
    }

    public List<ImexFTPFileWrapper> getDipFtpFiles() {
        if (dipFtpFiles == null) {
            dipFtpFiles = loadFtpFiles(ImexFTPClientFactory.createDipClient());
        }
        return dipFtpFiles;
    }
}
