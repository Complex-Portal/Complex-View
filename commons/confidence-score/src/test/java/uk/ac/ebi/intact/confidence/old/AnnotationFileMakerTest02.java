/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.confidence.old;

import uk.ac.ebi.intact.confidence.BinaryInteractionSet;
import uk.ac.ebi.intact.confidence.attribute.AnnotationFileMaker;

import java.io.IOException;

/**
 * TODO comment that
 *
 * @author Iain Bancarz
 * @version $Id$
 * @since 01-Aug-2006
 */
public class AnnotationFileMakerTest02 implements TestConstants {


    public static void main(String[] args) throws IOException {
        AnnotationFileMakerTest02 test = new AnnotationFileMakerTest02();
    }

    public AnnotationFileMakerTest02() throws IOException {

        BinaryInteractionSet biSet = new BinaryInteractionSet(dir + "lowconf2.txt");
        //AnnotationFileMaker afm = new AnnotationFileMaker(biSet, uniprotPath);
        AnnotationFileMaker afm = new AnnotationFileMaker(biSet, swissprotPath);
        System.out.println("Finding Interpro annotation:");
        afm.writeInterproAnnotation(dir + "lowconf2_interpro.txt");
        System.out.println("Finding GO annotation:");
        afm.writeGoAnnotation(dir + "lowconf2_go.txt");

    }


}
