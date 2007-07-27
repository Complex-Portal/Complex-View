/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.confidence.attribute;

import uk.ac.ebi.intact.confidence.ProteinPair;

/**
 * TODO comment that
 *
 * @author Iain Bancarz
 * @version $Id$
 * @since 09-Aug-2006
 */
public class AlignmentAttribute extends Attribute
{

    private ProteinPair pair;

    public AlignmentAttribute(ProteinPair pair)
    {

        // type and name are instance variables inherited from superclass
        type = Attribute.ALIGNMENT_TYPE;
        this.pair = pair;
        this.name = pair.toString();
    }


    public int compareTo(Attribute other)
    {

        if (other instanceof AlignmentAttribute)
        {
            AlignmentAttribute otherAtt = (AlignmentAttribute) other;
            return pair.compareTo(otherAtt.getProteinPair());
        }
        else
        {
            Integer typeInt = type;
            return typeInt.compareTo(other.getType());
        }

    }

    public boolean equals(Object other)
    {
        // override standard equals() method
        if (other == null)
        {
            return false;
        }
        else if (other instanceof AlignmentAttribute)
        {
            AlignmentAttribute otherAtt = (AlignmentAttribute) other;
            ProteinPair otherPair = otherAtt.getProteinPair();
            return pair.equals(otherPair);
        }
        else
        {
            return false;
        }
    }

    public int hashCode()
    {
        // need to override hashCode()  because equals()  has been overridden
        return pair.hashCode();
    }

    public ProteinPair getProteinPair()
    {
        return pair;
    }

    public String toString()
    {
        return name;
    }

    public String getTypeName()
    {
        return Attribute.ALIGNMENT_TYPENAME;
    }


}
