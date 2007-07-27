/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.confidence;


import java.util.HashSet;
import java.util.TreeSet;
import java.util.Collection;
import java.util.regex.Pattern;
import java.io.IOException;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;

/**
 * TODO comment that
 *
 * @author Iain Bancarz
 * @version $Id$
 * @since 02-Jun-2006
 */
public class BinaryInteractionSet
{

    private HashSet<ProteinPair> biSet;  // set of non-identical binary interactions
    private static boolean verbose = false; // debugging switch

    public BinaryInteractionSet(String path) throws IOException
    {

        // read set from a file that simply lists protein pairs in lines of the form:
        // UniProtID1,UniProtID2

        biSet = new HashSet<ProteinPair>();

        File infile = new File(path);
        BufferedReader in = new BufferedReader(new FileReader(infile));
        String line, prot1, prot2;
        String[] items;  // proteins in each line
        int totalProts;  // length of items array
        while ((line = in.readLine()) != null)
        {
            if (BinaryInteractionSet.verbose)
            {
                System.out.println("Input line: " + line);
            }
            if (Pattern.matches("^>.+", line))
            {
                continue;  // skip comment lines
            }
            items = line.split("\\W+"); // split by nonword characters
            totalProts = items.length;
            if (totalProts <= 1)
            {
                continue; // ignore empty lines & proteins with no listed partners
            }
            prot1 = items[0];
            prot2 = items[1];
            ProteinPair bi = new ProteinPair(prot1, prot2);
            if (verbose)
            {
                System.out.println(bi.toString() + " added to binary interaction set.");
            }
            biSet.add(bi);
        }
        if (verbose)
        {
            System.out.println(biSet.size() + " binary interactions found.");
        }
        in.close();
    }

    public HashSet<String> getInteractionPartners(String prot)
    {

        HashSet<String> partners = new HashSet<String>();
        String[] names;
        for (ProteinPair pair : biSet)
        {
            names = pair.getNames();
            if (names[0].equals(prot))
            {
                partners.add(names[1]);
            }
            else if (names[1].equals(prot))
            {
                partners.add(names[0]);
            }
        }
        return partners;

    }

    public BinaryInteractionSet(Collection<ProteinPair> collection)
    {
        biSet = new HashSet<ProteinPair>();
        for (ProteinPair pair : collection)
        {
            biSet.add(pair);
        }
    }

    public HashSet<String> getAllProtNames()
    {
        // get all UniProt IDs of proteins participating in this binary interaction set

        HashSet<String> allNames = new HashSet<String>();
        for (ProteinPair bi : biSet)
        {
            String[] names = bi.getNames();
            allNames.add(names[0]);
            allNames.add(names[1]);
        }
        return allNames;
    }

    public HashSet<ProteinPair> getSet()
    {
        return biSet;
    }

    public TreeSet<ProteinPair> getSortedInteractions()
    {
        return new TreeSet<ProteinPair>(biSet);
    }

    public int size()
    {
        return biSet.size();
    }


}
