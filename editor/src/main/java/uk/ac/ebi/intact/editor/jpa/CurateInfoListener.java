package uk.ac.ebi.intact.editor.jpa;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.event.spi.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.core.util.DebugUtil;
import uk.ac.ebi.intact.editor.controller.curate.ChangesController;
import uk.ac.ebi.intact.editor.controller.curate.CuratorContextController;
import uk.ac.ebi.intact.model.AnnotatedObject;
import uk.ac.ebi.intact.model.IntactObject;

import java.util.Collections;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class CurateInfoListener implements PostUpdateEventListener, PostInsertEventListener, PostDeleteEventListener {

    private static final Log log = LogFactory.getLog( CurateInfoListener.class );

    @Override
    public void onPostUpdate(PostUpdateEvent event) {
        if (!isHttpSessionAvailable()) return;

        final Object entity = event.getEntity();

        if (entity instanceof AnnotatedObject) {
            IntactObject io = (IntactObject) entity;

            if (io.getAc() == null || !getChangesController().isDeletedAc(io.getAc())) {
                getCuratorContextController()
                    .addInfoMessage( getCuratorContextController().intactObjectSimpleName(io) +" updated", "- "+DebugUtil.intactObjectToString(io, false) );

                getChangesController().removeFromUnsaved(io, Collections.EMPTY_LIST);
            }

            if (log.isDebugEnabled()) log.debug("Updated: "+DebugUtil.intactObjectToString(io, false));
        }
    }



    @Override
    public void onPostDelete(PostDeleteEvent event) {
        if (!isHttpSessionAvailable()) return;

         final Object entity = event.getEntity();

        if (entity instanceof IntactObject) {

            IntactObject io = (IntactObject) entity;
            getChangesController()
                .addInfoMessage( getCuratorContextController().intactObjectSimpleName(io) +" deleted", "- "+DebugUtil.intactObjectToString(io, false) );

            getChangesController().removeFromUnsaved(io, Collections.EMPTY_LIST);

            if (log.isDebugEnabled()) log.debug("Deleted: "+DebugUtil.intactObjectToString(io, false));
        }
    }

    @Override
    public void onPostInsert(PostInsertEvent event) {
        if (!isHttpSessionAvailable()) return;

        final Object entity = event.getEntity();

        if (entity instanceof IntactObject) {
            IntactObject io = (IntactObject) entity;
            getChangesController()
                .addInfoMessage( getCuratorContextController().intactObjectSimpleName(io) +" created", "- "+DebugUtil.intactObjectToString(io, false) );

            getChangesController().removeFromUnsaved(io, Collections.EMPTY_LIST);

            if (log.isDebugEnabled()) log.debug("Created: "+DebugUtil.intactObjectToString(io, false));
        }
    }

    public ChangesController getChangesController() {
        return (ChangesController) IntactContext.getCurrentInstance().getSpringContext().getBean("changesController");
    }


    public CuratorContextController getCuratorContextController() {
        return (CuratorContextController) IntactContext.getCurrentInstance().getSpringContext().getBean("curatorContextController");
    }

    public boolean isHttpSessionAvailable() {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return (attr != null);
    }
}
