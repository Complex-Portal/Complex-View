/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.confidence.attribute;

import uk.ac.ebi.intact.confidence.BinaryInteractionSet;
import uk.ac.ebi.intact.confidence.ProteinPair;
import uk.ac.ebi.intact.confidence.FileMethods;

import java.util.HashSet;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.io.*;

/**
 * TODO comment that
 *
 * @author Iain Bancarz
 * @version $Id$
 * @since 31-Jul-2006
 *        <p/>
 *        read attributes of multiple types from file
 *        combine into a single file
 */
public class FileCombiner implements AnnotationConstants
{


    private boolean verbose = true;

    private int maxBufferSize = 1000;

    private int rejected;  // count number of incorrectly formatted protein pairs rejected

    public FileCombiner(String[] attributePaths, String outPath)
            throws IOException
    {
        // this version finds attributes for blocks of 1000 protein pairs
        // avoids out-of-memory errors


        HashSet<ProteinPair> allPairs = new HashSet<ProteinPair>();
        HashSet<ProteinPair> pairBuffer = new HashSet<ProteinPair>();

        rejected = 0; // count number of incorrectly formatted protein pairs rejected

        for (String path : attributePaths)
        {
            FileReader fr = new FileReader(path);
            BufferedReader br = new BufferedReader(fr);
            String line;
            boolean firstBuffer = true;
            while ((line = br.readLine()) != null)
            {
                ProteinPair pair = FileMethods.getProteinPair(line);
                if (allPairs.contains(pair))
                {
                    continue;
                }
                // ignore pairs for which annotation has already been written
                if (!FileMethods.correctUniprotFormats(pair))
                {
                    // also ignore pairs containing incorrectly formatted uniprot names
                    rejected++;
                    if (verbose)
                    {
                        String comment = pair.toString() +
                                " rejected -- badly formatted UniProt ID.";
                        System.out.println(comment);
                        comment = rejected + " pairs of " + allPairs.size()
                                + " rejected so far.";
                        System.out.println(comment);
                    }
                    continue;
                }


                allPairs.add(pair);
                pairBuffer.add(pair);
                if (pairBuffer.size() == maxBufferSize)
                {
                    if (firstBuffer)
                    {
                        appendAttributeInfo(pairBuffer, attributePaths, outPath, false);
                        firstBuffer = false;
                        // open new output file instead of appending to old one
                    }
                    else
                    {
                        appendAttributeInfo(pairBuffer, attributePaths, outPath, true);
                    }
                    pairBuffer.clear();
                    if (verbose)
                    {
                        String comment =
                                "Annotation for " + allPairs.size() + " protein pairs found.";
                        System.out.println(comment);
                    }
                }
            }
            appendAttributeInfo(pairBuffer, attributePaths, outPath, true);

            fr.close();
        }
        if (verbose)
        {
            String comment = allPairs.size() + " protein pairs found.";
            System.out.println(comment);
        }
    }


    public int getRejected()
    {
        return rejected;
    }

    private void appendAttributeInfo(
            HashSet<ProteinPair> interactions, String[] attribPaths, String outPath, boolean append)
            throws IOException
    {

        HashMap<ProteinPair, HashSet<Attribute>> pairToAttribs =
                new HashMap<ProteinPair, HashSet<Attribute>>();
        for (String path : attribPaths)
        {
            FileReader fr = new FileReader(path);
            BufferedReader br = new BufferedReader(fr);
            String line;
            while ((line = br.readLine()) != null)
            {
                ProteinPair pair = FileMethods.getProteinPair(line);
                if (interactions.contains(pair))
                {
                    HashSet<Attribute> newAttribs = FileMethods.parseAttributeLine(line);
                    HashSet<Attribute> oldAttribs = pairToAttribs.get(pair);
                    ArrayList<Attribute> update = new ArrayList<Attribute>();
                    if (oldAttribs != null)
                    {
                        update.addAll(oldAttribs);
                    }
                    update.addAll(newAttribs);
                    pairToAttribs.put(pair, new HashSet<Attribute>(update));
                }
            }

            fr.close();
        }

        FileWriter fw = new FileWriter(outPath, append); // open FileWriter for appending
        PrintWriter pw = new PrintWriter(fw);
        StringBuilder out;
        for (ProteinPair pair : pairToAttribs.keySet())
        {
            out = new StringBuilder(pair.toString());
            HashSet<Attribute> attribs = pairToAttribs.get(pair);
            for (Attribute a : attribs)
            {
                out.append(",");
                out.append(a.toString());
            }
            pw.println(out.toString());
            //if (verbose) System.out.println(out.toString());
        }
        fw.close();

    }

    /* // old version of class -- very slow!!

        public FileCombiner(BinaryInteractionSet biSet, String[] attributePaths, String outPath)
                throws IOException {

            HashSet<ProteinPair> interactions = biSet.getSet();

            FileWriter fw = new FileWriter(outPath);
            PrintWriter pw = new PrintWriter(fw);

            String[] protNames, items;
            String name, inLine;
            StringBuilder combinedOutput;
            int pairCount = 0; // if debugging, count number of protein pairs read
            int rejects = 0;  // count number of illegal UniProt IDs rejected
            for (ProteinPair pair : interactions) {

                protNames = pair.getNames();
                name = protNames[0] + ";" + protNames[1];

                if (!Pattern.matches(uniprotTermExpr, protNames[0]) ||
                        !Pattern.matches(uniprotTermExpr, protNames[1])) {
                    if (verbose) {
                        String comment = "Rejected " + name + " -- badly formed UniProt ID.";
                        System.out.println(comment);
                        rejects++;
                    }
                    continue;
                }

                combinedOutput = new StringBuilder();
                combinedOutput.append(name);

                for (String path : attributePaths) {
                    // for each path to an attribute file
                    // find attributes of this interaction, if any
                    // append to combinedOutput
                    FileReader fr = new FileReader(path);
                    BufferedReader br = new BufferedReader(fr);
                    while ((inLine = br.readLine()) != null) {
                        items = inLine.split(",");
                        if (name.equals(items[0])) {

                            for (int i = 1; i < items.length; i++) {
                                String foo = "," + items[i];
                                combinedOutput.append(foo);
                            }
                            break;
                        }
                    }
                }
                pw.println(combinedOutput.toString());
                if (verbose) {
                    pairCount++;
                    String comment = "Combined annotation written for interaction " + pairCount + " of " +
                            interactions.size();
                    System.out.println(comment);

                }
            }


            String comment = rejects + " badly formed UniProt IDs rejected.";
            System.out.println(comment);

            fw.close();


        }

    */

}

