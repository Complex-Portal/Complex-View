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
 * @since 31-Jul-2006
 */
public class IpTermPair extends BinaryItem implements AnnotationConstants
{


    public IpTermPair(String name1, String name2) throws IllegalArgumentException
    {

        super(name1, name2);

        if (
                !(Pattern.matches(AnnotationConstants.ipTermExpr, name1) ||
                        !(Pattern.matches(AnnotationConstants.ipTermExpr, name2)))
            // if either name1 or name2 is not a correctly formatted IP term
                )
        {
            throw new IllegalArgumentException(
                    "Incorrect arguments to IpTermPair constructor: " + name1 + ", " + name2);
        }


    }


}
