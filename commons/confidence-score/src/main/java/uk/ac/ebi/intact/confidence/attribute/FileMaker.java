/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.confidence.attribute;

import uk.ac.ebi.intact.confidence.BinaryInteractionSet;
import uk.ac.ebi.intact.confidence.ProteinPair;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Pattern;

/**
 * TODO comment that
 *
 * @author Iain Bancarz
 * @version $Id$
 * @since 31-Jul-2006
 */
public class FileMaker
{

    BinaryInteractionSet biSet;
    private boolean verbose = false; // debug switch

    public FileMaker(BinaryInteractionSet biSet)
    {

        this.biSet = biSet;

    }

    public void writeAnnotationAttributes(String notePath, String outPath) throws IOException
    {
        // write attributes based on UniProt annotation -- eg. GO and InterPro terms

        // generate File objects for input/output

        File outFile = new File(outPath);

        HashMap<String, String[]> annotationMap = getProteinAnnotations(notePath);

        // initialize variables to read/write files
        FileWriter fw = new FileWriter(outFile);
        PrintWriter pw = new PrintWriter(fw);
        StringBuilder sb;
        String[] annotation1, annotation2;

        // now write output for each interaction
        for (ProteinPair bi : biSet.getSet())
        {
            String[] names = bi.getNames();
            annotation1 = annotationMap.get(names[0]);
            annotation2 = annotationMap.get(names[1]);
            if (annotation1 == null || annotation2 == null)
            {
                continue;
            }

            sb = new StringBuilder();
            sb.append(names[0] + ";" + names[1]); // append protein names, separated by semicolon
            for (String term1 : annotation1)
            {
                for (String term2 : annotation2)
                {
                    if (term1.equals(term2))
                    {
                        continue;
                    }
                    else
                    {
                        sb.append("," + term1 + ";" + term2); // comma-separated list of term pairs
                    }
                    // terms in pair separated by semicolon
                }
            }
            pw.println(sb.toString());
            if (verbose)
            {
                String comment = "Annotation written for protein pair " + bi.toString();
                System.out.println(comment);
            }
        }

        fw.close();


    }

    public void writeAlignmentAttributes(String hitPath, String hiconfPath, String outPath)
            throws IOException
    {
        // similar to writeAnnotationAttributes, but based on sequence alignment
        // hitPath is path to a file with:
        //   * (possible) interactors
        //   * proteins with high sequence similarity (BLAST hits)
        //   * lines of the form:  [Interactor ID],[Hit ID],[hit ID], ...
        // hiconfPath is path to a file with high-confidence binary interactions
        // outPath for output

        // populate map from protein IDs to their BLAST hits
        HashMap<String, String[]> hitMap = getAlignmentMap(hitPath);

        HashSet<ProteinPair> hiConfSet = new BinaryInteractionSet(hiconfPath).getSet();
        if (verbose)
        {
            String comment = hiConfSet.size() + " high-confidence protein pairs found.\n";
            System.out.println(comment);
        }

        FileWriter fw = new FileWriter(outPath);
        PrintWriter pw = new PrintWriter(fw);


        String[] prots, hits0, hits1;
        StringBuilder sb;
        for (ProteinPair pair : biSet.getSet())
        {
            sb = new StringBuilder(pair.toString());
            prots = pair.getNames();
            hits0 = hitMap.get(prots[0]);
            hits1 = hitMap.get(prots[1]);
            if (hits0 != null && hits1 != null)
            {
                for (String p0 : hits0)
                {
                    for (String p1 : hits1)
                    {
                        ProteinPair hitPair = new ProteinPair(p0, p1);
                        if (hiConfSet.contains(hitPair))
                        {
                            // this protein pair is related to a high-confidence pair
                            // (by significant sequence similarity)
                            sb.append(',');
                            sb.append(hitPair.toString());
                        }
                    }
                }
            }
            pw.println(sb.toString());

        }

        fw.close();


    }


    private HashMap<String, String[]> getProteinAnnotations(String notePath) throws IOException
    {

        File noteFile = new File(notePath);
        FileReader fr = new FileReader(noteFile);
        BufferedReader br = new BufferedReader(fr);
        HashMap<String, String[]> annotationMap = new HashMap<String, String[]>();

        String line, prot;
        String[] items, terms;
        while ((line = br.readLine()) != null)
        {
            items = line.split(","); // split by commas
            prot = items[0];  // first item is a protein name
            terms = new String[items.length - 1];
            for (int i = 1; i < items.length; i++)
            { // subsequent items are annotation terms
                terms[i - 1] = items[i];
            }
            annotationMap.put(prot, terms);
        }

        fr.close();
        return annotationMap;

    }

    private HashMap<String, String[]> getAlignmentMap(String hitPath) throws IOException
    {
        // similar to the above, but for sequence alignments


        HashMap<String, String[]> hitMap = new HashMap<String, String[]>();
        FileReader fr = new FileReader(hitPath);
        BufferedReader br = new BufferedReader(fr);
        String line, prot;
        String[] items, hits;
        while ((line = br.readLine()) != null)
        {
            items = line.split(","); // split by commas
            prot = items[0];
            //if (!Pattern.matches("\\w+", prot)) continue;
            if (items.length == 1)
            {
                hitMap.put(prot, null);
                if (verbose)
                {
                    String comment = "No alignments found for protein " + prot;
                    System.out.println(comment);
                }
            }
            else
            {
                hits = new String[items.length - 1];
                for (int i = 1; i < items.length; i++)
                {
                    hits[i - 1] = items[i];
                }
                hitMap.put(prot, hits);
                if (verbose)
                {
                    int some = items.length - 1;
                    String comment = some + " alignments found for protein " + prot;
                    System.out.println(comment);
                    comment = "\t" + line;
                    System.out.println(comment);
                    //comment = "\t" + hitPath;
                    //System.out.println(comment);
                }
            }
        }
        fr.close();

        return hitMap;

    }

}
