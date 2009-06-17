package uk.ac.ebi.intact.view.webapp.controller.search;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.Map;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public interface IconGenerator {
    @PostConstruct
    @Transactional(readOnly = true)
    void prepareColours();

    Map<String, IconGeneratorImpl.ColouredCv> getColourMap();
}
