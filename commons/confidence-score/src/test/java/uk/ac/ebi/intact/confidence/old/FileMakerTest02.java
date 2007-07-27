/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.confidence.old;

import uk.ac.ebi.intact.confidence.BinaryInteractionSet;
import uk.ac.ebi.intact.confidence.attribute.FileMaker;

import java.io.IOException;

/**
 * TODO comment that
 *
 * @author Iain Bancarz
 * @version $Id$
 * @since 01-Aug-2006
 */
public class FileMakerTest02 implements TestConstants {

    public static void main(String[] args) throws IOException {
        FileMakerTest02 test = new FileMakerTest02();
    }

    public FileMakerTest02() throws IOException {

//        BinaryInteractionSet biSet = new BinaryInteractionSet(highConfPairs);
//        FileMaker fm = new FileMaker(biSet);
//        fm.writeAnnotationAttributes(dir + "highconf_go.txt", dir + "highconf_go_attribs.txt");
//        fm.writeAnnotationAttributes(dir + "highconf_interpro.txt", dir + "highconf_ip_attribs.txt");
//
//
//        biSet = new BinaryInteractionSet(medConfPairs);
//        fm = new FileMaker(biSet);
//        fm.writeAnnotationAttributes(dir + "medconf_go.txt", dir + "medconf_go_attribs.txt");
//        fm.writeAnnotationAttributes(dir + "medconf_interpro.txt", dir + "medconf_ip_attribs.txt");


        BinaryInteractionSet biSet;
        FileMaker fm ;
        biSet = new BinaryInteractionSet(lowConfPairs);
        fm = new FileMaker(biSet);
        fm.writeAnnotationAttributes(dir + "lowconf2_go.txt", dir + "lowconf2_go_attribs.txt");
        fm.writeAnnotationAttributes(dir + "lowconf2_interpro.txt", dir + "lowconf2_ip_attribs.txt");

    }

}
