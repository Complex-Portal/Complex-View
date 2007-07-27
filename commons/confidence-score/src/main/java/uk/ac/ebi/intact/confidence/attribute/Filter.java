/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.confidence.attribute;

import uk.ac.ebi.intact.confidence.FileMethods;

import java.io.*;
import java.util.regex.Pattern;
import java.util.HashSet;

/**
 * TODO comment that
 *
 * @author Iain Bancarz
 * @version $Id$
 * @since 31-Jul-2006
 *        <p/>
 *        read a set of attributes from a file in standard format
 *        filter out undesired attributes
 *        eg. keep only those which appear in a given binary interaction set
 *        <p/>
 *        write interactions with filtered attributes to new file
 */
public class Filter implements AnnotationConstants
{

    private String inPath;
    private String outPath;

    private boolean verbose = true;

    public Filter(String inPath, String outPath)
    {
        this.inPath = inPath;
        this.outPath = outPath;
    }

    public void filterBySet(String filterSetPath) throws IOException
    {

        HashSet<Attribute> filterSet = FileMethods.getAttribSet(filterSetPath);

        // initialize i/o objects
        FileReader fr = new FileReader(inPath);
        BufferedReader br = new BufferedReader(fr);
        FileWriter fw = new FileWriter(outPath);
        PrintWriter pw = new PrintWriter(fw);

        String line, prot;
        HashSet<Attribute> someAttribs;
        StringBuilder sb;
        int lineCount = 0;
        while ((line = br.readLine()) != null)
        {
            if (Pattern.matches(commentExpr, line))
            {
                continue; // skip comment lines
            }
            prot = FileMethods.getFirstItem(line);
            someAttribs = FileMethods.parseAttributeLine(line);
            HashSet<Attribute> removal = new HashSet<Attribute>();
            // avoids ConcurrentModificationException
            for (Attribute a : someAttribs)
            {
                // remove all attributes which do not occur in the filter set
                if (!filterSet.contains(a))
                {
                    removal.add(a);
                }
            }
            for (Attribute a : removal)
            {
                someAttribs.remove(a);
            }

            sb = new StringBuilder();
            sb.append(prot);
            for (Attribute a : someAttribs)
            {
                sb.append(",");
                sb.append(a.toString());
            }
            pw.println(sb.toString());
            if (verbose)
            {
                lineCount++;
                String comment = "Attributes filtered for interaction " + lineCount;
                System.out.println(comment);
            }
        }

        fr.close();
        fw.close();


    }

/*
private HashMap<ProteinPair, HashSet<Attribute>> getAttribMap(String inPath)
        throws IOException {

    FileReader fr = new FileReader(inPath);
    BufferedReader br = new BufferedReader(fr);
    String line, prots;
    String[] items, protNames;
    HashSet<Attribute> attribSet;
    HashMap<ProteinPair, HashSet<Attribute>> attribMap
            = new HashMap<ProteinPair, HashSet<Attribute>>();
    while ((line = br.readLine()) != null) {
        if (Pattern.matches(commentExpr, line)) continue; // skip comment lines
        items = line.split(",");
        protNames = items[0].split(";");
        ProteinPair pair = new ProteinPair(protNames[0], protNames[1]);
        attribSet = new HashSet<Attribute>();
        for (int i=1;i<items.length;i++) {
            attribSet.add(parseAttribute(items[i]) );
        }
        attribMap.put(pair, attribSet);

    }
    fr.close();

    return attribMap;

}
*/

}
