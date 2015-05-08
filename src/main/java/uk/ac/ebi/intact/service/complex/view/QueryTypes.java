package uk.ac.ebi.intact.service.complex.view;

/**
 * @author Oscar Forner (oforner@ebi.ac.uk)
 * @version $Id$
 * @since 04/12/13
 */
public enum QueryTypes {
    DEFAULT("search"),
    EXPORT("export"),
    DETAILS("details");

    String value;

    QueryTypes( String value ) { this.value = value; }

    public static QueryTypes getByValue( String value ) {
        for( QueryTypes type : values() )
            if (type.value.equals(value))
                return type;
        return null;
    }
}
