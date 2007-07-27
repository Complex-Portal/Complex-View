package uk.ac.ebi.intact.confidence.old;

/**
 * TODO comment that
 *
 * @author Iain Bancarz
 * @version $Id$
 * @since 01-Aug-2006
 */
public interface TestConstants {
    // interface to store constants for testing classes
    // eg. pathnames

    public static String dir = "/net/nfs6/vol1/homes/ibancarz/data/";
    public static String highConfPairs = dir + "highconf_all.txt";
    public static String medConfPairs = dir + "medconf_all.txt";
    public static String lowConfPairs = dir + "lowconf.txt";

    public static String uniprotPath = "/scratch/UniProt/uniprot_all.dat";
    public static String swissprotPath = "/scratch/UniProt/uniprot_sprot.dat";

    public static String lowConfGoAttribs = dir + "lowconf_go_attribs.txt";
    public static String lowConfIpAttribs = dir + "lowconf_ip_attribs.txt";
    public static String medConfGoAttribs = dir + "medconf_go_attribs.txt";
    public static String medConfIpAttribs = dir + "medconf_ip_attribs.txt";
    public static String hiConfGoAttribs = dir + "highconf_go_attribs.txt";
    public static String hiConfIpAttribs = dir + "highconf_ip_attribs.txt";

    public static String hiConfAll = dir + "highconf_all_attribs.txt";
    public static String medConfAll = dir + "medconf_all_attribs.txt";
    public static String lowConfAll = dir + "lowconf_all_attribs.txt";

    public static String lowConfFiltered = dir + "lowconf_filtered_attribs.txt";

    // contents of data directory
//    highconf_all.txt         lowconf_go.txt          medconf_go.txt
//    highconf_go_attribs.txt  lowconf_interpro.txt    medconf_interpro.txt
//    highconf_go.txt          lowconf_ip_attribs.txt  medconf_ip_attribs.txt
//    highconf_interpro.txt    lowconf.txt             perl
//    highconf_ip_attribs.txt  medconf_all.txt         swissprot_yeast_proteins.txt
//    lowconf_go_attribs.txt   medconf_go_attribs.txt


}
