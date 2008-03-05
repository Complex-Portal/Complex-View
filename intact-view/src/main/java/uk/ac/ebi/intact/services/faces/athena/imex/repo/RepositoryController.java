package uk.ac.ebi.intact.services.faces.athena.imex.repo;

import uk.ac.ebi.intact.dataexchange.imex.repository.Repository;
import uk.ac.ebi.intact.dataexchange.imex.repository.ImexRepositoryContext;
import uk.ac.ebi.intact.dataexchange.imex.repository.RepositoryStatistics;
import uk.ac.ebi.intact.services.faces.athena.imex.ftp.ImexFTPFileWrapper;
import uk.ac.ebi.intact.services.faces.athena.imex.ftp.FtpController;
import uk.ac.ebi.intact.services.faces.athena.BaseController;

import javax.faces.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class RepositoryController extends BaseController {

    private Log log = LogFactory.getLog(RepositoryController.class);

    private FtpController ftpController;

    private String repositoryPath;
    private Repository repository;
    private RepositoryStatistics statistics;

    public RepositoryController() {

    }

    public void openRepository(ActionEvent evt) {
        if (log.isInfoEnabled()) log.info("Opening local repository: "+repositoryPath);
        this.repository = ImexRepositoryContext.openRepository(repositoryPath);

        this.statistics = ImexRepositoryContext.getInstance().getRepositoryStatistics();

        addInfoMessage("Local repository is now OPEN.", null);
    }

    public void closeRepository(ActionEvent evt) {
        if (repository != null) {
            if (log.isInfoEnabled()) log.info("Closing local repository: "+repositoryPath);
            repository.close();
        }
        repository = null;

        addInfoMessage("Local repository is now CLOSED.", null);
    }

    public void importAllEntriesFromFTP(ActionEvent evt) throws IOException {
        for (ImexFTPFileWrapper fileWrapper : ftpController.getIntactFtpFiles()) {
            repository.storeEntrySet(fileWrapper.getFtpFile(), "intact");
        }

        for (ImexFTPFileWrapper fileWrapper : ftpController.getMintFtpFiles()) {
            repository.storeEntrySet(fileWrapper.getFtpFile(), "mint");
        }

        for (ImexFTPFileWrapper fileWrapper : ftpController.getDipFtpFiles()) {
            repository.storeEntrySet(fileWrapper.getFtpFile(), "dip");
        }

        statistics.refresh();
    }

    public boolean isRepositoryOpen() {
        return repository != null && repository.isOpen();
    }

    public String getRepositoryPath() {
        return repositoryPath;
    }

    public void setRepositoryPath(String repositoryPath) {
        this.repositoryPath = repositoryPath;
    }

    public boolean isRepositoryPathCorrect() {
        return repositoryPath != null;
    }

    public boolean isRepositoryPathExists() {
        if (!isRepositoryPathCorrect()) {
            return false;
        }

        return new File(repositoryPath).exists();
    }

    public RepositoryStatistics getStatistics() {
        return statistics;
    }

    public void setFtpController(FtpController ftpController) {
        this.ftpController = ftpController;
    }
}
