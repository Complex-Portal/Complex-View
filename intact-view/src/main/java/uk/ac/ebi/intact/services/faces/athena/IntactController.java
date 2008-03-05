package uk.ac.ebi.intact.services.faces.athena;


import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.Experiment;
import uk.ac.ebi.intact.persistence.dao.DaoFactory;

import javax.persistence.EntityManagerFactory;

import org.springframework.stereotype.Controller;
import org.springframework.context.annotation.Scope;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Iterator;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller("intact")
@Scope("request")
public class IntactController {

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    public String getInstitution() {
        return IntactContext.getCurrentInstance().getInstitution().getShortLabel();
    }

    public int getInteractionCount() {
        return getDaoFactory().getInteractionDao().countAll();
    }

    private DaoFactory getDaoFactory() {
        return IntactContext.getCurrentInstance().getDataContext().getDaoFactory();
    }

    public String getChartUrl() {
        String baseUrl = "http://chart.apis.google.com/chart?cht=p3&chs=250x100";

        StringBuilder values = new StringBuilder();
        StringBuilder titles = new StringBuilder();

        for (Iterator<Experiment> iterator = getDaoFactory().getExperimentDao().getAll().iterator(); iterator.hasNext();) {
            Experiment exp =  iterator.next();

            values.append(exp.getInteractions().size());
            titles.append(exp.getShortLabel());

            if (iterator.hasNext()) {
                values.append(",");
                titles.append("|");
            }
        }

        return baseUrl+"&chd=t:"+values+"&chl="+titles;
    }

}
