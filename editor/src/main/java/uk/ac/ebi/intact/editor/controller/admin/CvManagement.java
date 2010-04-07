package uk.ac.ebi.intact.editor.controller.admin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.dataexchange.cvutils.CvUpdater;
import uk.ac.ebi.intact.dataexchange.cvutils.CvUpdaterStatistics;
import uk.ac.ebi.intact.editor.controller.BaseController;

import java.io.IOException;

/**
 * Controller used when managing cv objects.
 *
 * @author Bruno Aranda (skerrien@ebi.ac.uk)
 * @version $Id$
 */
@Controller
@Scope( "conversation.access" )
@ConversationName( "admin" )
public class CvManagement extends BaseController {

    private static final Log log = LogFactory.getLog( CvManagement.class );

    @Autowired
    private CvUpdater cvUpdater;

    private CvUpdaterStatistics cvUpdaterStatistics;

    public void updateCvs() {
        if ( log.isInfoEnabled() ) log.info( "Updating CVs" );

        try {
            cvUpdaterStatistics = cvUpdater.executeUpdateWithLatestCVs();

        } catch ( IOException e ) {
            addErrorMessage( "Problem updating CVs", e.getMessage() );
        }

        if ( log.isInfoEnabled() ) log.info( "Finished CV Update" );
    }

    public CvUpdaterStatistics getCvUpdaterStatistics() {
        return cvUpdaterStatistics;
    }
}
