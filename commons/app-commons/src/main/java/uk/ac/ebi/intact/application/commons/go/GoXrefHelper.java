/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.application.commons.go;

import uk.ac.ebi.intact.model.CvXrefQualifier;

import java.io.IOException;

/**
 * TODO comment it.
 *
 * @author Catherine Leroy (cleroy@ebi.ac.uk)
 * @version $Id$
 */
public class GoXrefHelper {

    private GoServerProxy goServer;
    private GoServerProxy.GoResponse goResponse;
    private String qualifier = null;
    private String secondaryId = null;

    public GoXrefHelper(String goId){
        if (goId == null){
            throw new IllegalArgumentException("The goId shouldn't be null");
        }
        goServer = new GoServerProxy();
        try {
            goResponse = goServer.query(goId);
            qualifier = goResponse.getCategory();
            secondaryId = goResponse.getName();

        } catch (IOException e) {
            System.out.println("The qualifier and the secondaryId corresponding to GoId " + goId + " couldn't be found automatically");
        } catch (GoServerProxy.GoIdNotFoundException e) {
            System.out.println("The qualifier and the secondaryId corresponding to GoId " + goId + " couldn't be found automatically");
        }
    }

    public String getQualifier () {
        return this.qualifier;
    }

    public String getSecondaryId () {
        String secondaryId = null;
        if(qualifier != null ){
            if ( qualifier.equals(CvXrefQualifier.COMPONENT) ){
                secondaryId = "C:" + this.secondaryId;
            }else if (qualifier.equals(CvXrefQualifier.FUNCTION) ){
                secondaryId = "F:" + this.secondaryId;
            }else if (qualifier.equals(CvXrefQualifier.PROCESS) ){
                secondaryId = "P:" + this.secondaryId;
            }
        }

        return secondaryId;
    }

    public static void main(String[] args) {
        //C
        GoXrefHelper goXrefHelper = new GoXrefHelper("GO:0005737");
        System.out.println(goXrefHelper.getQualifier());
        System.out.println(goXrefHelper.getSecondaryId());
        //F
        goXrefHelper = new GoXrefHelper("GO:0005520");
        System.out.println(goXrefHelper.getQualifier());
        System.out.println(goXrefHelper.getSecondaryId());

        //P
        goXrefHelper = new GoXrefHelper("GO:0045663");
        System.out.println(goXrefHelper.getQualifier());
        System.out.println(goXrefHelper.getSecondaryId());

        goXrefHelper = new GoXrefHelper("GO:0005856");
        System.out.println(goXrefHelper.getQualifier());
        System.out.println(goXrefHelper.getSecondaryId());
    }

}
