/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.confidence.old;

import uk.ac.ebi.intact.confidence.BinaryInteractionSet;

import java.io.IOException;

/**
 * TODO comment that
 *
 * @author Iain Bancarz
 * @version $Id$
 * @since 01-Aug-2006
 */
public class FileCombinerTest01 implements TestConstants {

    public static void main(String[] args) throws IOException {
        FileCombinerTest01 test = new FileCombinerTest01();
    }

    public FileCombinerTest01() throws IOException {

        BinaryInteractionSet biSet = new BinaryInteractionSet(highConfPairs);
        String[] paths = {hiConfGoAttribs, hiConfIpAttribs};
        //FileCombiner fc = new FileCombiner(biSet, paths, dir+"highconf_all_attribs.txt");

    }

}
