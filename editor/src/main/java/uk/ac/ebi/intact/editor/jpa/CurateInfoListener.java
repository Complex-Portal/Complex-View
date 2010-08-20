package uk.ac.ebi.intact.editor.jpa;

import org.hibernate.event.*;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.core.util.DebugUtil;
import uk.ac.ebi.intact.editor.controller.curate.CuratorContextController;
import uk.ac.ebi.intact.model.IntactObject;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class CurateInfoListener implements PostUpdateEventListener, PostInsertEventListener, PostDeleteEventListener {

    @Override
    public void onPostUpdate(PostUpdateEvent event) {
        final Object entity = event.getEntity();

        if (entity instanceof IntactObject) {
            IntactObject io = (IntactObject) entity;
            getCuratorContextController()
                .addInfoMessage( getCuratorContextController().intactObjectSimpleName(io) +" updated", "- "+DebugUtil.intactObjectToString(io, false) );

            getCuratorContextController().removeFromUnsaved(io);
        }
    }

    @Override
    public void onPostDelete(PostDeleteEvent event) {
         final Object entity = event.getEntity();

        if (entity instanceof IntactObject) {
            IntactObject io = (IntactObject) entity;
            getCuratorContextController()
                .addInfoMessage( getCuratorContextController().intactObjectSimpleName(io) +" deleted", "- "+DebugUtil.intactObjectToString(io, false) );

            getCuratorContextController().removeFromUnsaved(io);
        }
    }

    @Override
    public void onPostInsert(PostInsertEvent event) {
        final Object entity = event.getEntity();

        if (entity instanceof IntactObject) {
            IntactObject io = (IntactObject) entity;
            getCuratorContextController()
                .addInfoMessage( getCuratorContextController().intactObjectSimpleName(io) +" created", "- "+DebugUtil.intactObjectToString(io, false) );

            getCuratorContextController().removeFromUnsaved(io);
        }
    }

    public CuratorContextController getCuratorContextController() {
        return (CuratorContextController) IntactContext.getCurrentInstance().getSpringContext().getBean("curatorContextController");
    }
}
