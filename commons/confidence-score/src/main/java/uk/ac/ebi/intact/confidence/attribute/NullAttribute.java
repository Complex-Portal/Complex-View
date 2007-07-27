/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.confidence.attribute;

import uk.ac.ebi.intact.confidence.attribute.Attribute;


/**
 * TODO comment that
 *
 * @author Iain Bancarz
 * @version $Id$
 * @since 14-Jul-2006
 */
public class NullAttribute extends Attribute
{
    // 'null' attribute present in all binary interactions
    // needed as a placeholder for the maxent classifier


    public NullAttribute()
    {
        type = NULL_TYPE;
        typename = NULL_TYPENAME;
        this.name = NULL_TYPENAME;
    }


}
