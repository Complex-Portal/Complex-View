package uk.ac.ebi.intact.services.search;

import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.persistence.dao.DaoFactory;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public abstract class SearchBaseController extends BaseController {

    protected DaoFactory getDaoFactory() {
        return getIntactContext().getDataContext().getDaoFactory();
    }

    protected IntactContext getIntactContext() {
        return IntactContext.getCurrentInstance();
    }

    
}
