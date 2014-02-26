package uk.ac.ebi.intact.view.webapp.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.core.persistence.dao.DaoFactory;

/**
 * Abstract controller giving access to IntAct database access via JPA.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public abstract class JpaBaseController extends BaseController {

    private static final Log log = LogFactory.getLog( JpaBaseController.class );

    IntactContext intactContext;

    @Autowired
    private DaoFactory daoFactory;

    protected DaoFactory getDaoFactory() {
        return this.daoFactory;
    }

    protected IntactContext getIntactContext() {
        if (intactContext == null){
           intactContext = IntactContext.getCurrentInstance();
        }
        return intactContext;
    }

    
}
