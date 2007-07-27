package uk.ac.ebi.intact.util.imex.model;

/**
 * Created by IntelliJ IDEA.
 * User: CatherineLeroy
 * Date: 10-Feb-2007
 * Time: 14:08:15
 * To change this template use File | Settings | File Templates.
 */

//this could implement Publication...
public class ImexPublication {

    public static final String PUBMED_TYPE = "PUBMED";
    public static final String DOI_TYPE = "DOI";
    public static final String JOURNAL_SPECIFIC_TYPE = "JOURNAL_SPECIFIC";
    public static final String IMEX_TYPE = "IMEX";

    uk.ac.ebi.imexcentral.wsclient.generated.Publication publication;

    public ImexPublication(uk.ac.ebi.imexcentral.wsclient.generated.Publication publication) {
        this.publication = publication;
    }


}
