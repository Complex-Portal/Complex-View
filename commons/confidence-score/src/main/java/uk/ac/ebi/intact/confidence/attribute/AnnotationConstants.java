package uk.ac.ebi.intact.confidence.attribute;

import java.util.HashSet;

/**
 * TODO comment that
 *
 * @author Iain Bancarz
 * @version $Id$
 * @since 31-Jul-2006
 *        <p/>
 *        Stores various constants related to protein annotation
 *        Useful for finding attributes
 *        Eg. regular expressions to denote GO and InterPro terms
 */
public interface AnnotationConstants
{

    // identify protein accession number line in uniprot flatfile
    static String uniprotNameExpr = "^AC.*";

    // identify uniprot accession term
    static String uniprotTermExpr = "\\w{6,6}";  // this also matches _ but should be OK

    // regexps to identify InterPro and GO accession numbers
    static String ipTermExpr = "IPR\\d{6,6}"; // IPR followed by exactly 6 digits
    static String goTermExpr = "GO:\\d{7,7}"; // GO: followed by exactly 7 digits

    static String commentExpr = "^>.*"; // standard comment line starts with >

    // regexps to identify GO/Interpro lines in UniProt flatfiles
    static String ipLineExprUniProt = "^DR\\s+InterPro.*";
    static String goLineExprUniProt = "^DR\\s+GO.*";
    static String endExprUniProt = "^//.*";      // marks end of a UniProt entry

    static String forbiddenGoTerm = "GO:0005515"; // GO term for protein binding
    static String[] forbiddenGoTerms = {
            forbiddenGoTerm, // GO term for protein binding
            // the following are all children of "GO:0005515" as of 31/07/2006
            // grandchildren etc. omitted for now -- can add these later
            "GO:0048185",
            "GO:0045294",
            "GO:0043532",
            "GO:0045152",
            "GO:0043008",
            "GO:0030881",
            "GO:0001540",
            "GO:0008013",
            "GO:0048306",
            "GO:0005516",
            "GO:0050839",
            "GO:0051087",
            "GO:0030276",
            "GO:0005518",
            "GO:0001848",
            "GO:0030332",
            "GO:0042980",
            "GO:0019955",
            "GO:0008092",
            "GO:0031249",
            "GO:0045502",
            "GO:0019899",
            "GO:0001918",
            "GO:0001968",
            "GO:0048184",
            "GO:0001965",
            "GO:0031681",
            "GO:0031683",
            "GO:0031682",
            "GO:0045295",
            "GO:0051021",
            "GO:0001948",
            "GO:0019838",
            "GO:0030742",
            "GO:0031720",
            "GO:0031072",
            "GO:0030492",
            "GO:0031423",
            "GO:0042393",
            "GO:0042802",
            "GO:0019865",
            "GO:0008262",
            "GO:0043560",
            "GO:0019215",
            "GO:0030984",
            "GO:0017170",
            "GO:0005521",
            "GO:0043236",
            "GO:0008034",
            "GO:0051787",
            "GO:0042043",
            "GO:0002046",
            "GO:0001846",
            "GO:0002039",
            "GO:0051219",
            "GO:0008267",
            "GO:0005522",
            "GO:0043495",
            "GO:0030674",
            "GO:0008022",
            "GO:0008320",
            "GO:0046983",
            "GO:0019904",
            "GO:0047485",
            "GO:0043621",
            "GO:0005102",
            "GO:0042153",
            "GO:0048155",
            "GO:0048154",
            "GO:0046332",
            "GO:0043221",
            "GO:0000149",
            "GO:0045545",
            "GO:0046977",
            "GO:0046980",
            "GO:0017025",
            "GO:0045569",
            "GO:0008134",
            "GO:0031369",
            "GO:0043130",
            "GO:0051082",
            "GO:0017147",
            "GO:0042988",
    };


}
