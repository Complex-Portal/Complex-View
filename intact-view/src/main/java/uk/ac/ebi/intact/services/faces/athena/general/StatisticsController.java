package uk.ac.ebi.intact.services.faces.athena.general;

import uk.ac.ebi.intact.services.faces.athena.BaseController;
import uk.ac.ebi.intact.persistence.dao.DaoFactory;
import uk.ac.ebi.intact.context.IntactContext;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class StatisticsController extends BaseController {

    private int publicationsCount;
    private int experimentsCount;
    private int interactionsCount;
    private int proteinsCount;
    private int bioSourcesCount;
    private int cvObjectsCount;

    public StatisticsController() {
        DaoFactory daoFactory = IntactContext.getCurrentInstance().getDataContext().getDaoFactory();

        this.publicationsCount = daoFactory.getPublicationDao().countAll();
        this.experimentsCount = daoFactory.getExperimentDao().countAll();
        this.interactionsCount = daoFactory.getInteractionDao().countAll();
        this.proteinsCount = daoFactory.getProteinDao().countAll();
       // this.bioSourcesCount = daoFactory.getBioSourceDao().countAll();
        this.cvObjectsCount = daoFactory.getCvObjectDao().countAll();
    }

    public int getPublicationsCount() {
        return publicationsCount;
    }

    public int getExperimentsCount() {
        return experimentsCount;
    }

    public int getInteractionsCount() {
        return interactionsCount;
    }

    public int getProteinsCount() {
        return proteinsCount;
    }

    public int getBioSourcesCount() {
        return bioSourcesCount;
    }

    public int getCvObjectsCount() {
        return cvObjectsCount;
    }
}
