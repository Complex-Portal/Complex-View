package uk.ac.ebi.intact.view.webapp.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.orchestra.conversation.ConversationBindingEvent;
import org.apache.myfaces.orchestra.conversation.ConversationBindingListener;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.core.persistence.dao.DaoFactory;

/**
 * Abstract controller giving access to IntAct database access via JPA.
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
