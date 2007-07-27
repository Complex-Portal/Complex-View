/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.confidence.attribute;


import uk.ac.ebi.intact.confidence.FileMethods;

import java.util.*;
import java.util.regex.Pattern;
import java.io.*;

/**
 * TODO comment that
 *
 * @author Iain Bancarz
 * @version $Id$
 * @since 31-Jul-2006
 *        <p/>
 *        writes input for classifier programs
 *        eg. TADM event files, c4.5 format for jBNC and decision trees
 */
public class ClassifierInputWriter implements AnnotationConstants
{

    private HashMap<Attribute, Integer> attribIndices;
    // mapping from attributes to their indices

    private TreeSet<Attribute> sortedAttribs;
    // attributes present in data, sorted in natural comparison order

    private String hcPath, lcPath, outPath;

    public ClassifierInputWriter(String hcPath, String lcPath, String outPath, String type)
            throws IllegalArgumentException, IOException
    {

        this.hcPath = hcPath;
        this.lcPath = lcPath;
        this.outPath = outPath;

        ArrayList<Attribute> allAttribsList = new ArrayList<Attribute>();
        allAttribsList.addAll(FileMethods.getAttribSet(hcPath));
        allAttribsList.addAll(FileMethods.getAttribSet(lcPath));
        sortedAttribs = new TreeSet<Attribute>(allAttribsList);
        //   attributes present in data, sorted in their natural comparison order
        //   include ALL attributes in both high-conf and low-conf data files
        //   if we want to use only attributes found in high-conf data --
        // must pre-filter the lowconf file (using the Filter class)

        // assign an integer index to each attribute present
        attribIndices = new HashMap<Attribute, Integer>();
        attribIndices.put(new NullAttribute(), 0);
        int i = 1;
        for (Attribute a : sortedAttribs)
        {
            if (a.getType() == Attribute.NULL_TYPE)
            {
                continue; // null attribute already added
            }
            attribIndices.put(a, i);
            i++;
        }


        if (type.equals("TADM"))
        {
            writeTadmFile(hcPath, lcPath, outPath);
        }
        else if (type.equals("c4.5"))
        {
            writeC45File();
        }
        else
        {
            throw new IllegalArgumentException(
                    "Incorrect classifier type supplied to ClassifierInputWriter: " + type);
        }

    }

    public void writeAttribList(String path) throws IOException
    {

        FileWriter fw = new FileWriter(path);
        PrintWriter pw = new PrintWriter(fw);

        String header = "> Attributes present in HC file " + hcPath + " and LC file " + lcPath + "\n" +
                "> written to TADM file " + outPath + " on " + FileMethods.getDateTime();
        pw.println(header);
        for (Attribute a : sortedAttribs)
        {
            pw.println(a.toString());
        }

        fw.close();
    }


    private void writeTadmFile(String hcPath, String lcPath, String outPath) throws IOException
    {
        /* write an input file for the Toolkit for Advanced Discriminative Modelling (TADM)
         * TADM is MaxEnt modelling software
         * also need two BinaryInteractionSet objects -- respectively positive and negative examples
         * each vector of feature values, corresponding to one or more binary
         interactions, is an 'event'

         * introduce a default 'empty' attribute -- present for all protein pairs

         * output format:
         [total outcomes in context -- true/false in this case]
         [freq of 'true'] [total feature-value pairs] [feature1] [value1] [feature2] [value2] ...
         [freq of 'false'] [total feature-value pairs] [feature1] [value1] [feature2] [value2] ...

         * frequencies may be zero

         * feature designations -- [feature1], [feature2], etc -- refer to a consistently
         ordered list of features.  See TadmContext class for explanation of feature indexing
         * see also separate TADM documentation for more detailed description of file format

         * want to create a TadmContext object for each positive/negative interaction &
         write to file

        */

        // initialize output objects
        FileWriter fw = new FileWriter(outPath);
        PrintWriter pw = new PrintWriter(fw);

        // write event file header
        String header = "&header " + FileMethods.getDateTime() + "/";
        pw.println(header);

        // find and print event file blocks

        // define objects to read/write data
        FileReader fr;
        BufferedReader br;
        String line, block;
        TreeSet<Attribute> sorted;

        // read high-confidence interactions
        fr = new FileReader(hcPath);
        br = new BufferedReader(fr);
        while ((line = br.readLine()) != null)
        {
            if (Pattern.matches(commentExpr, line))
            {
                continue;  // skip comment lines
            }
            sorted = new TreeSet<Attribute>(FileMethods.parseAttributeLine(line));
            sorted.add(new NullAttribute());
            block = eventFileBlock(sorted, true);
            pw.println(block);
        }
        fr.close();      // close FileReader

        // repeat for low-confidence interactions
        fr = new FileReader(lcPath);
        br = new BufferedReader(fr);
        while ((line = br.readLine()) != null)
        {
            if (Pattern.matches(commentExpr, line))
            {
                continue;  // skip comment lines
            }
            sorted = new TreeSet<Attribute>(FileMethods.parseAttributeLine(line));
            block = eventFileBlock(sorted, false);
            pw.println(block);
        }
        fr.close();     // close FileReader

        fw.close();    // close FileWriter
    }


    private String eventFileBlock(TreeSet<Attribute> sorted, boolean outcome)
    {
        // find event file block for a given context (binary interaction)


        int[] attribIndicesPresent = findAttribIndicesPresent(sorted);

        // generate object to represent this context (binary interaction) for TADM
        TadmContext tc;
        if (outcome == true)
        {
            tc = new TadmContext(1, // frequency of 'true' interaction
                    0, // frequency of 'false' interaction
                    attribIndicesPresent);
        }
        else
        {
            tc = new TadmContext(0, // frequency of 'true' interaction
                    1, // frequency of 'false' interaction
                    attribIndicesPresent);
        }

        return tc.getEventFileBlock();
    }

    private int[] findAttribIndicesPresent(TreeSet<Attribute> sortedAttribs)
    {
        int[] indices = new int[sortedAttribs.size()];
        int i = 0;
        for (Attribute a : sortedAttribs)
        {
            indices[i] = attribIndices.get(a);
            i++;
        }
        return indices;

    }


    private void writeC45File() throws IllegalArgumentException
    {
        throw new IllegalArgumentException("c4.5 method not defined yet!");
    }


}
