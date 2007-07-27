/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.confidence;

import uk.ac.ebi.intact.confidence.BinaryInteractionSet;
import uk.ac.ebi.intact.confidence.ProteinPair;
import uk.ac.ebi.intact.confidence.attribute.*;

import java.util.HashSet;
import java.util.TreeSet;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.io.IOException;
import java.io.FileReader;
import java.io.BufferedReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * TODO comment that
 *
 * @author Iain Bancarz
 * @version $Id$
 * @since 31-Jul-2006
 *        <p/>
 *        contains static methods to process attribute files in standard format
 *        <p/>
 *        File format is as follows:
 *        Comment lines start with >
 *        Data lines are delimited by commas
 *        The two components of binary items (eg. protein pairs) are separated by semicolons
 *        Lines are of the form:
 *        Proteinpair,attribute1,attribute2, ... , attributeN
 */
public class FileMethods implements AnnotationConstants
{


    public static String getFirstItem(String line)
    {
        String[] items = line.split(",");
        return items[0];
    }

    public static ProteinPair getProteinPair(String line)
    {
        String pairString = getFirstItem(line);
        String[] prots = pairString.split(";");
        return new ProteinPair(prots[0], prots[1]);
    }

    public static boolean correctUniprotFormats(ProteinPair pair)
    {
        // checks if protein names in a ProteinPair are correctly formatted

        String[] protNames = pair.getNames();
        // String name = protNames[0] + ";" + protNames[1];

        if (!Pattern.matches(uniprotTermExpr, protNames[0]) ||
                !Pattern.matches(uniprotTermExpr, protNames[1]))
        {
            return false;
        }
        else
        {
            return true;
        }
    }

    public static boolean correctUniprotFormats(String[] names)
    {
        // as above, but checks if all names in a list of strings are correctly formatted

        for (String name : names)
        {
            if (!Pattern.matches(uniprotTermExpr, name))
            {
                return false;
            }
        }
        return true;
    }


    public static HashMap<Attribute, Integer> attribFreqs(String inPath) throws IOException
    {

        FileReader fr = new FileReader(inPath);
        BufferedReader br = new BufferedReader(fr);
        String line;

        HashMap<Attribute, Integer> allCounts = new HashMap<Attribute, Integer>();
        Integer count;
        HashSet<Attribute> someAttribs;
        while ((line = br.readLine()) != null)
        {
            if (Pattern.matches(commentExpr, line))
            {
                continue; // skip comment lines
            }
            someAttribs = parseAttributeLine(line);
            for (Attribute a : someAttribs)
            {
                if (allCounts.containsKey(a))
                {
                    count = allCounts.get(a) + 1;
                    allCounts.put(a, count);
                }
                else
                {  // first time attribute has been observed
                    allCounts.put(a, 1);
                }
            }
        }
        fr.close();
        return allCounts;

    }

    public static int[] findCountProfile(String inPath) throws IOException
    {
        // print number of attributes with each frequency to standard output

        HashMap<Attribute, Integer> attribFreqs = attribFreqs(inPath);
        int freq, max = 0;

        // find greatest frequency
        for (Attribute a : attribFreqs.keySet())
        {
            freq = attribFreqs.get(a);
            if (freq > max)
            {
                max = freq;
            }
        }

        // now populate array of frequency counts
        int[] profile = new int[max];
        for (Attribute a : attribFreqs.keySet())
        {
            freq = attribFreqs.get(a);
            profile[freq - 1]++;
        }

        return profile;
    }

    public static int findTotalInteractions(String inPath) throws IOException
    {
        // count number of non-comment lines in an attribute file

        int i = 0;

        FileReader fr = new FileReader(inPath);
        BufferedReader br = new BufferedReader(fr);
        String line;

        while ((line = br.readLine()) != null)
        {
            if (Pattern.matches(commentExpr, line))
            {
                continue; // skip comment lines
            }
            else
            {
                i++;
            }
        }
        fr.close();

        return i;
    }


    public static double findCoverage(String inPath) throws IOException
    {
        // count proportion of interactions without attributes in an attribute file

        double total = 0.0;
        double empty = 0.0;

        FileReader fr = new FileReader(inPath);
        BufferedReader br = new BufferedReader(fr);
        String line;

        while ((line = br.readLine()) != null)
        {
            if (Pattern.matches(commentExpr, line))
            {
                continue; // skip comment lines
            }
            else
            {
                total++;
                HashSet<Attribute> attribs = parseAttributeLine(line);
                if (attribs.isEmpty())
                {
                    empty++;
                }
            }
        }
        fr.close();

        return empty / total;
    }

    public static int[] findCountProfile(String inPath, int threshold) throws IOException
    {
        // print number of attributes with each frequency to standard output

        HashMap<Attribute, Integer> attribFreqs = attribFreqs(inPath);
        int freq, max = 0;

        // find greatest frequency
        for (Attribute a : attribFreqs.keySet())
        {
            freq = attribFreqs.get(a);
            if (freq > max)
            {
                max = freq;
            }
        }

        String comment = "Printing attributes with frequencies greater than " + threshold;
        System.out.println(comment);

        // now populate array of frequency counts
        int[] profile = new int[max];
        for (Attribute a : attribFreqs.keySet())
        {
            freq = attribFreqs.get(a);
            profile[freq - 1]++;
            if (freq > threshold)
            {
                System.out.println(a.toString());
            }
        }

        return profile;
    }

    public static void printCountProfile(String inPath) throws IOException
    {

        int[] profile = findCountProfile(inPath);

        int freq;
        String out = "Count profiles for input file " + inPath;
        System.out.println(out);
        System.out.println(getDateTime());
        out = "[Frequency] [Number of attributes]";
        System.out.println(out);
        for (int i = 0; i < profile.length; i++)
        {
            freq = i + 1;
            out = freq + "\t" + profile[i];
            System.out.println(out);
        }


    }

    public static HashSet<Attribute> getAttribSet(String inPath) throws IOException
    {

        FileReader fr = new FileReader(inPath);
        BufferedReader br = new BufferedReader(fr);
        String line;
        HashSet<Attribute> allAttribSet = new HashSet<Attribute>();
        HashSet<Attribute> someAttribs;
        while ((line = br.readLine()) != null)
        {
            if (Pattern.matches(commentExpr, line))
            {
                continue; // skip comment lines
            }
            someAttribs = parseAttributeLine(line);
            for (Attribute a : someAttribs)
            {
                allAttribSet.add(a);
            }
        }
        fr.close();
        return allAttribSet;

    }

    public static String getAttribString(HashSet<Attribute> set) throws IOException
    {
        // generate string of attributes to append to existing line
        // starts with a comma
        // attributes separated by commas

        StringBuilder sb = new StringBuilder();
        for (Attribute a : set)
        {
            sb.append(",");
            sb.append(a.toString());
        }
        return sb.toString();

    }

    public static String getAttribString(String line) throws IOException
    {

        HashSet<Attribute> set = parseAttributeLine(line);
        return getAttribString(set);


    }

    public static TreeSet<Attribute> getSortedAttribSet(String inPath) throws IOException
    {
        HashSet<Attribute> unsorted = getAttribSet(inPath);
        return new TreeSet<Attribute>(unsorted);
    }

    public static BinaryInteractionSet getInteractionSet(String inPath) throws IOException
    {
        // get binary interactions from an interaction/attribute file in standard format

        FileReader fr = new FileReader(inPath);
        BufferedReader br = new BufferedReader(fr);
        String line, pair;
        String[] items, proteins;
        HashSet<ProteinPair> interactions = new HashSet<ProteinPair>();
        while ((line = br.readLine()) != null)
        {
            if (Pattern.matches(commentExpr, line))
            {
                continue; // skip comment lines
            }
            items = line.split(",");
            pair = items[0];
            proteins = pair.split(";");
            interactions.add(new ProteinPair(proteins[0], proteins[1]));
        }
        fr.close();

        return new BinaryInteractionSet(interactions);
    }


    public static HashSet<Attribute> parseAttributeLine(String line)
    {

        HashSet<Attribute> attribSet = new HashSet<Attribute>();

        String[] items = line.split(",");
        for (int i = 1; i < items.length; i++)
        {
            // first item is protein pair -- ignore it and find attributes
            attribSet.add(parseAttribute(items[i]));
        }
        return attribSet;

    }


    public static Attribute parseAttribute(String input) throws IllegalArgumentException
    {

        String ipAttribExpr = ipTermExpr + ";" + ipTermExpr;
        String goPairAttribExpr = goTermExpr + ";" + goTermExpr;
        //String alignAttribExpr = uniprotTermExpr + ";" + uniprotTermExpr;
        String alignAttribExpr = "\\w+;\\w+";

        if (Pattern.matches(ipAttribExpr, input))
        {
            String[] terms = input.split(";");
            IpAttribute ipa = new IpAttribute(new IpTermPair(terms[0], terms[1]));
            return ipa;
        }
        else if (Pattern.matches(goPairAttribExpr, input))
        {
            String[] terms = input.split(";");
            GoPairAttribute gpa = new GoPairAttribute(new GoTermPair(terms[0], terms[1]));
            return gpa;
        }
        else if (Pattern.matches(alignAttribExpr, input))
        {
            String[] terms = input.split(";");
            AlignmentAttribute aa = new AlignmentAttribute(
                    new ProteinPair(terms[0], terms[1]));
            return aa;
        }
        else
        {
            throw new IllegalArgumentException(
                    "Cannot parse attribute from input string " + input);
        }
    }


    public static String getDateTime()
    {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }


}
