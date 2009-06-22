package uk.ac.ebi.intact.view.webapp.controller.search;

import java.util.Map;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public interface IconGenerator {

    void prepareColours();

    Map<String, IconGeneratorImpl.ColouredCv> getTypeColourMap();
    Map<String, IconGeneratorImpl.ColouredCv> getExpRoleColourMap();
    Map<String, IconGeneratorImpl.ColouredCv> getBioRoleColourMap();
}
