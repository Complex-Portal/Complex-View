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
 * @since 14-Jun-2006
 *        <p/>
 *        denotes some identifiable attribute of a binary interaction
 *        eg. presence of a GO term pair
 *        need to sort attributes into a consistent order
 *        eg. GO ancestor terms, GO term pairs, then InterPro term pairs, then coexp distances
 */
public abstract class Attribute implements Comparable<uk.ac.ebi.intact.confidence.attribute.Attribute>
{

    // assign a constant integer to each type of attribute
    // eg. 0 = null, 1 = InterPro term pair
    // allows consistent ordering of attributes by type

    // constants to define type integers
    public static final int NULL_TYPE = 0;
    public static final int INTERPRO_TYPE = 1;
    public static final int GO_PAIR_TYPE = 2;

    public static final int ALIGNMENT_TYPE = 3;

    // similarly, assign name strings to each type (more readable than numbers)
    public static final String NULL_TYPENAME = "NULL";
    public static final String INTERPRO_TYPENAME = "INTERPRO";
    public static final String GO_PAIR_TYPENAME = "GO TERM PAIR";

    public static final String ALIGNMENT_TYPENAME = "SEQUENCE ALIGNMENT";

    int type; // variable to hold type of a given subclass
    // assign one of the above integer constants to this variable
    String typename; // similarly to TYPE, but for name of attribute type
    String name; // String to identify this particular attribute instance,
    // eg. InterproID1:InterproID2


    public Attribute()
    {
        // need this constructor to allow subclassing
    }

    public int compareTo(Attribute other)
    {

        int otherType = other.getType();
        if (type != otherType)
        {
            // attributes are of different types
            Integer typeInt = type;
            // wrap int variable in Integer object -- allows comparison
            return typeInt.compareTo(otherType);
        }
        else
        {
            return compareSameType(other);
        }


    }

    private int compareSameType(Attribute other) throws IllegalArgumentException
    {
        // compare two attributes of the same type (for ordering in compareTo() above)
        // can override this for a particular Attribute subclass
        // eg. for InterPro term pairs, use compareTo method from BinaryItem class

        if (this.getType() != other.getType())
        {
            throw new IllegalArgumentException(
                    "Attribute of different type supplied to compareSameType method.");
        }
        else
        {
            return this.toString().compareTo(other.toString());
        }
    }

    public boolean equals(Object other)
    {
        // override standard equals() method
        // further overridden by IpAttribute subclass
        if (other == null)
        {
            return false;
        }
        else if (this instanceof NullAttribute && other instanceof NullAttribute)
        {
            return true;
        }
        else if (other instanceof uk.ac.ebi.intact.confidence.attribute.Attribute)
        {
            uk.ac.ebi.intact.confidence.attribute.Attribute otherAtt = (uk.ac.ebi.intact.confidence.attribute.Attribute) other;
            if (type == otherAtt.getType() && name.equals(otherAtt.toString()))
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        else
        {
            return false;
        }
    }

    public int hashCode()
    {
        // need to override hashCode()  because equals()  has been overridden
        int hash = name.hashCode();
        return hash;
    }


    public int getType()
    {
        return type;
    }

    public String toString()
    {
        return name;
    }

    public String getTypeName()
    {
        return typename;
    }


}
