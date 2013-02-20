package uk.ac.ebi.intact.service;

import psidev.psi.mi.xml.stylesheets.XslTransformerUtils;

import java.io.File;

/**
 * TODO comment this
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>23/07/12</pre>
 */

public class ExportIntactPublicationToHtml {

    public static void main(String[] args) throws Exception {

        String pubFile=null;

        if (args.length != 1) {
            System.err.println("Usage: ExportIntactPublicationToHtml <publication xml file>");
            System.exit(1);
        } else {
            pubFile = args[0];

            System.out.println("--------------------------");
            System.out.println("Publication identifier: "+pubFile);
            System.out.println("--------------------------");
        }

        File inputFile = new File(pubFile);

        if (!inputFile.exists()) {
            System.err.println("File does not exist or has wrong permissions: "+inputFile);
            System.exit(-1);
        }

        File outputFile = new File(inputFile.getCanonicalPath()+".html");

        try {
            // we transform the xml to html using an utility class that returns
            // the output stream with the html content

            XslTransformerUtils.viewPsiMi25(inputFile, outputFile);
        }
        catch ( Exception e ) {
            System.err.println("Failed to produce the HTML view");
        }
    }
}
