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
public class FileCombinerTest03 implements TestConstants {

    public static void main(String[] args) throws IOException {
        FileCombinerTest03 test = new FileCombinerTest03();
    }

    public FileCombinerTest03() throws IOException {

        BinaryInteractionSet biSet = new BinaryInteractionSet(dir + "lowconf2.txt");
        String[] paths = {dir + "lowconf2_go_attribs.txt", dir + "lowconf2_ip_attribs.txt"};
        //FileCombiner fc = new FileCombiner(biSet, paths, dir+"lowconf_all_attribs.txt");
        //FileCombiner fc = new FileCombiner(biSet, paths, dir+"lowconf2_all_attribs.txt");

    }

}
