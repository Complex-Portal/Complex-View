/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.confidence.old;

import uk.ac.ebi.intact.confidence.attribute.FileMaker;
import uk.ac.ebi.intact.confidence.BinaryInteractionSet;

import java.io.IOException;

/**
 * TODO comment that
 *
 * @author Iain Bancarz
 * @version $Id$
 * @since 11-Aug-2006
 */
public class AlignTest01 implements TestConstants {

    static String myDir = dir + "alignment/";      // ~/data/alignment
    static String lowConfBi = myDir + "lowconf.txt";
    static String hiConfBi = myDir + "highconf_all.txt";
    static String medConfBi = myDir + "medconf_all.txt";

    public static void main(String[] args) throws IOException {
        AlignTest01 test = new AlignTest01();
    }

    public AlignTest01() throws IOException {

        //getLowConfFile();
        //getHighConfFile();
        getMedConfFile();
    }

    private void getLowConfFile() throws IOException {

         FileMaker fm = new FileMaker(new BinaryInteractionSet(lowConfBi));
         fm.writeAlignmentAttributes(
                 myDir+"lc_alignments.txt",
                 myDir+"highconf_all.txt",
                 myDir+"lowconf_alignment_attribs.txt");
    }


    private void getHighConfFile() throws IOException {

         FileMaker fm = new FileMaker(new BinaryInteractionSet(hiConfBi));
         fm.writeAlignmentAttributes(
                 myDir+"hc_alignments.txt",
                 myDir+"highconf_all.txt",
                 myDir+"highconf_alignment_attribs.txt");
    }

    private void getMedConfFile() throws IOException {

          FileMaker fm = new FileMaker(new BinaryInteractionSet(hiConfBi));
          fm.writeAlignmentAttributes(
                  myDir+"mc_alignments.txt",
                  myDir+"highconf_all.txt",
                  myDir+"medconf_alignment_attribs.txt");
     }


}
