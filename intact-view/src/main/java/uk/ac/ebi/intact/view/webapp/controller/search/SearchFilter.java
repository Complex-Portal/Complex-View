package uk.ac.ebi.intact.view.webapp.controller.search;

/**
 * Represents a filter in a query.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public interface SearchFilter {
    String getField();

    String getValue();

    boolean isClearable();

    String getDisplayValue();

    String toLuceneSyntax();

    String toDisplay();

    boolean isNegated();
}
