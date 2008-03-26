package uk.ac.ebi.intact.services.search;

import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.persistence.dao.DaoFactory;
import uk.ac.ebi.intact.business.IntactTransactionException;
import org.apache.myfaces.orchestra.conversation.ConversationBindingListener;
import org.apache.myfaces.orchestra.conversation.ConversationBindingEvent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public abstract class JpaBaseController extends BaseController implements ConversationBindingListener{

    private static final Log log = LogFactory.getLog( JpaBaseController.class );

    protected DaoFactory getDaoFactory() {
        return getIntactContext().getDataContext().getDaoFactory();
    }

    protected IntactContext getIntactContext() {
        return IntactContext.getCurrentInstance();
    }

    public void valueBound(ConversationBindingEvent event) {
        if (log.isDebugEnabled()) log.debug("Conversation event (value bound): conversation="+event.getConversation().getName()+", name="+event.getName());
       // IntactContext.getCurrentInstance().getDataContext().beginTransaction();
    }

    public void valueUnbound(ConversationBindingEvent event) {
        if (log.isDebugEnabled()) log.debug("Conversation event (value unbound): conversation="+event.getConversation().getName()+", name="+event.getName());
//        try {
//            IntactContext.getCurrentInstance().getDataContext().commitTransaction();
//        } catch (IntactTransactionException e) {
//            e.printStackTrace();
//        }
    }
}
