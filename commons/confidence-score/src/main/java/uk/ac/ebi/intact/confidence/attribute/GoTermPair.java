/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.confidence.attribute;

import uk.ac.ebi.intact.confidence.BinaryItem;

import java.util.regex.Pattern;


/**
 * TODO comment that
 *
 * @author Iain Bancarz
 * @version $Id$
 * @since 21-Jun-2006
 */
public class GoTermPair extends BinaryItem implements AnnotationConstants
{

    public GoTermPair(String name1, String name2) throws IllegalArgumentException
    {

        super(name1, name2);

        if (
                !(Pattern.matches(AnnotationConstants.goTermExpr, name1) ||
                        !(Pattern.matches(AnnotationConstants.goTermExpr, name2)))
            // if either name1 or name2 is not a correctly formatted GO term
                )
        {
            throw new IllegalArgumentException(
                    "Incorrect arguments to GoTermPair constructor: " + name1 + ", " + name2);
        }


    }

}
