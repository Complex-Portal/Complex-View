package uk.ac.ebi.intact.services.faces.athena.dev;

import org.springframework.beans.factory.InitializingBean;
import uk.ac.ebi.intact.model.Experiment;
import uk.ac.ebi.intact.core.unit.IntactMockBuilder;
import uk.ac.ebi.intact.core.persister.PersisterHelper;
import uk.ac.ebi.intact.context.IntactContext;

import java.util.Random;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class DataPopulatorInitializingBean implements InitializingBean {

    public void afterPropertiesSet() throws Exception {
        IntactContext.getCurrentInstance().getUserContext().setUserId("bruno");
        
        Experiment[] experiments = new Experiment[5];
        for (int i=0; i<5; i++) {
            experiments[i] = new IntactMockBuilder().createExperimentRandom(new Random().nextInt(10));
        }

        PersisterHelper.saveOrUpdate(experiments);
    }
}
