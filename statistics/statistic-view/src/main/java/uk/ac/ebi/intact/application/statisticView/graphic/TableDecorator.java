/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.statisticView.graphic;

import org.apache.taglibs.display.Decorator;
import uk.ac.ebi.intact.application.statisticView.struts.view.DisplayStatisticsBean;

/**
 * Allow the Display tag library to customize the table view.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @see uk.ac.ebi.intact.application.statisticView.struts.view.DisplayStatisticsBean
 * @since <pre>07-Jul-2005</pre>
 */
public class TableDecorator extends Decorator {

    /**
     * Display a link in the table that send the user to the correcponding graph down the current page.
     *
     * @return
     */
    public String getStatObject() {
        DisplayStatisticsBean bean = ( DisplayStatisticsBean ) this.getObject();
        return "<a href=\"#" + bean.getStatObject() + "\" class=\"red_bold_small\">" + bean.getStatObject() + "</a>";
    }
}