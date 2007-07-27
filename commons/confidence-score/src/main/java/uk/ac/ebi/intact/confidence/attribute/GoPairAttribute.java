/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.confidence.attribute;


/**
 * TODO comment that
 *
 * @author Iain Bancarz
 * @version $Id$
 * @since 21-Jun-2006
 */
public class GoPairAttribute extends Attribute
{

    private GoTermPair pair;

    public GoPairAttribute(GoTermPair pair)
    {
        // type and name are instance variables inherited from superclass
        type = Attribute.GO_PAIR_TYPE;
        this.pair = pair;
        this.name = pair.toString();
    }

    public int compareTo(Attribute other)
    {

        if (other instanceof GoPairAttribute)
        {
            GoPairAttribute otherAtt = (GoPairAttribute) other;
            return pair.compareTo(otherAtt.getTermPair());
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
        else if (other instanceof GoPairAttribute)
        {
            GoPairAttribute otherAtt = (GoPairAttribute) other;
            GoTermPair otherTermPair = otherAtt.getTermPair();
            return pair.equals(otherTermPair);
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

    public GoTermPair getTermPair()
    {
        return pair;
    }

    public String toString()
    {
        return name;
    }

    public String getTypeName()
    {
        return Attribute.GO_PAIR_TYPENAME;
    }


}
