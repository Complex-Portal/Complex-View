/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.confidence.old;

import uk.ac.ebi.intact.confidence.util.GenerateAlignmentFile;

/**
 * TODO comment that
 *
 * @author Iain Bancarz
 * @version $Id$
 * @since 14-Aug-2006
 */
public class AlignTest03 implements TestConstants {


    public static void main(String[] args) {
        AlignTest03 test = new AlignTest03();

    }

    public AlignTest03() {

            String[] args = {
//                    "-h", "/tmp/highconf_all.txt",
//                    "-a", "/tmp/all_alignments2.txt",
//                    "-o", "/tmp/all_align_attribs2.txt"};
                    "-h", "/tmp/highconf_all.txt",
                    "-a", "/tmp/lc_alignments2.txt",
                    "-o", "/tmp/lc_align_attribs2.txt"};
          GenerateAlignmentFile generator = new GenerateAlignmentFile(args);
    }

}
