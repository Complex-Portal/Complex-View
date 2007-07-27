/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.confidence.old;

import uk.ac.ebi.intact.confidence.attribute.FileCombiner;
import uk.ac.ebi.intact.confidence.FileMethods;

import java.io.IOException;

/**
 * TODO comment that
 *
 * @author Iain Bancarz
 * @version $Id$
 * @since 02-Aug-2006
 */
public class FileCombinerTest04 implements TestConstants {

    public static void main(String[] args) {

        FileCombinerTest04 test = new FileCombinerTest04();

    }

    public FileCombinerTest04() {

        String[] input = {medConfGoAttribs, medConfIpAttribs};
        try {
            //FileCombiner fc = new FileCombiner(input, dir+"bigcombinertest.txt");

            String startTime = FileMethods.getDateTime();
            FileCombiner fc = new FileCombiner(input, dir+"bigcombinertest.txt");
            String endTime = FileMethods.getDateTime();
            String comment = "Test complete:  Start time "+startTime+", end time "+endTime;
            System.out.println(comment);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(e.toString());
            System.exit(0);
        }

    }

}
