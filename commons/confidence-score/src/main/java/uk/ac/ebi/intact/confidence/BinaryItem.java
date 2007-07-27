/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.confidence;


import java.util.Arrays;
import java.util.HashSet;

/**
 * TODO comment that
 *
 * @author Iain Bancarz
 * @version $Id$
 * @since 14-Jun-2006
 *        <p/>
 *        General-purpose class to represent pairs of terms
 *        Each term is identified by a unique string
 *        Eg. pairs of GO annotation terms or UniProt proteins
 *        <p/>
 *        Extended by:
 *        <ul><li>GoTermPair
 *        <li>IpTermPair
 *        <li>ProteinPair
 *        </ul>
 */
public abstract class BinaryItem implements Comparable<BinaryItem>
{

    private String name1;
    private String name2;

    public BinaryItem() throws IllegalArgumentException
    {
        // need this constructor to allow subclassing
    }

    public BinaryItem(String name1, String name2)
    {
        String[] names = new String[]{name1, name2};
        Arrays.sort(names);
        this.name1 = names[0];
        this.name2 = names[1];
    }

    public int compareTo(BinaryItem other)
    {
        // comparison between term pairs
        // first sort their term strings
        // then return comparison between first sorted strings
        // if first sorted strings are equal, compare second sorted strings

        String[] names1 = new String[]{name1, name2};
        String[] names2 = other.getNames();
        Arrays.sort(names1);
        Arrays.sort(names2);
        if (names1[0].equals(names2[0]))
        {
            return names1[1].compareTo(names2[1]);
        }
        else
        {
            return names1[0].compareTo(names2[0]);
        }

    }


    public boolean equals(Object other)
    {
        // override standard equals() method
        // two binary interactions are equal if they have the same interactors -- in any order
        if (other == null)
        {
            return false;
        }
        else if (other instanceof uk.ac.ebi.intact.confidence.BinaryItem)
        {
            uk.ac.ebi.intact.confidence.BinaryItem otherBi = (uk.ac.ebi.intact.confidence.BinaryItem) other;
            String[] otherNames = otherBi.getNames();
            if (name1.equals(otherNames[0]) && name2.equals(otherNames[1]))
            {
                return true;
            }
            else if (name2.equals(otherNames[0]) && name1.equals(otherNames[1]))
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
        int hash = name1.hashCode() ^ name2.hashCode(); // use XOR operator to combine hashcodes
        //int hash = 0;
        return hash;
    }

    public String[] getNames()
    {
        return new String[]{name1, name2};
    }


    public String toString()
    {
        return name1 + ";" + name2;
    }

    public boolean containsTerm(String term)
    {
        if (name1.equals(term) || name2.equals(term))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

}
