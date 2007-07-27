/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.confidence;

import uk.ac.ebi.intact.confidence.attribute.Attribute;
import uk.ac.ebi.intact.confidence.attribute.NullAttribute;
import uk.ac.ebi.intact.confidence.attribute.AnnotationConstants;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.regex.Pattern;
import java.io.*;

/**
 * TODO comment that
 *
 * @author Iain Bancarz
 * @version $Id$
 * @since 04-Aug-2006
 *        <p/>
 *        Read attributes and corresponding feature weights from file
 *        Evaluate with held-out test data
 *        Find false positive/negative rates and output ROC points
 *        <p/>
 *        Also classify new data
 */
public class MaxEntClassifier
{

    private HashMap<Attribute, Double> trueWeightMap;
    private HashMap<Attribute, Double> falseWeightMap;

    private String attribPath, weightPath;

    private static boolean verbose = true; //debug switch

    public MaxEntClassifier(String attribPath, String weightPath)
            throws IOException, IllegalArgumentException
    {

        this.attribPath = attribPath;
        this.weightPath = weightPath;

        ArrayList<Attribute> attribList = readAttribs(attribPath);
        ArrayList<Double> weights = readWeights(weightPath);
        if ((attribList.size() * 2) != weights.size())
        {
            throw new IllegalArgumentException(
                    "Pair of weights not present for each attribute!\n" +
                            "Total attributes = " + attribList.size() + ", total weights = " + weights.size());
        }
        else if (verbose)
        {
            String comment = attribList.size() + " attributes, " + weights.size() +
                    " weights read from files.\n";
            System.out.println(comment);
        }

        trueWeightMap = new HashMap<Attribute, Double>();
        falseWeightMap = new HashMap<Attribute, Double>();
        int j = 0;
        for (int i = 0; i < attribList.size(); i++)
        {
            Attribute a = attribList.get(i);
            trueWeightMap.put(a, weights.get(j));
            j++;
            falseWeightMap.put(a, weights.get(j));
            j++;
        }
        if (verbose)
        {
            System.out.println("Weight maps successfully assigned.");
        }

    }

    public void printScoreProfile(String inPath) throws IOException
    {

        FileReader fr = new FileReader(inPath);
        BufferedReader br = new BufferedReader(fr);

        String line;
        double totalPairs = 0.0;
        double lowCount = 0.0;
        double medCount = 0.0;
        double hiCount = 0.0;
        while ((line = br.readLine()) != null)
        {
            totalPairs++;
            Double tScore = trueScoreFromLine(line);
            //sb.append(",");
            //sb.append(tScore);
            if (tScore < 0.5)
            {
                lowCount++;
            }
            else if (tScore > 0.5)
            {
                hiCount++;
            }
            else
            {
                medCount++;
            }
        }
        fr.close();

        String out = totalPairs + " total interactions.";
        System.out.println(out);
        double lowPercent = (lowCount / totalPairs) * 100;
        double medPercent = (medCount / totalPairs) * 100;
        double hiPercent = (hiCount / totalPairs) * 100;

        out = "Low-confidence interactions:  " + lowCount + " -- " + lowPercent + "%";
        System.out.println(out);
        out = "Medium-confidence interactions:  " + medCount + " -- " + medPercent + "%";
        System.out.println(out);
        out = "High-confidence interactions:  " + hiCount + " -- " + hiPercent + "%";
        System.out.println(out);

    }

    public double trueScoreFromLine(String line)
    {
        ProteinPair pair = FileMethods.getProteinPair(line);
        HashSet<Attribute> attribs = FileMethods.parseAttributeLine(line);
        Double[] probs = probs(attribs);
        Double tScore = probs[0];
        return tScore;
    }

    public void writeInteractionScores(String inPath, String outPath) throws IOException
    {
        // inPath = path to an interaction & attribute file in standard format
        // outPath = path for output


        FileReader fr = new FileReader(inPath);
        BufferedReader br = new BufferedReader(fr);
        FileWriter fw = new FileWriter(outPath);
        PrintWriter pw = new PrintWriter(fw);

        String out = "> " + FileMethods.getDateTime();
        pw.println(out);
        out = "> True-interaction probabilities: Input " + inPath;
        pw.println(out);
        out = "> Maxent weight file " + weightPath + ", attribute list " + attribPath;
        pw.println(out);
        String line;
        ProteinPair pair;
        HashSet<Attribute> attribs;
        while ((line = br.readLine()) != null)
        {
            pair = FileMethods.getProteinPair(line);
            StringBuilder sb = new StringBuilder(pair.toString());
            attribs = FileMethods.parseAttributeLine(line);
            Double[] probs = probs(attribs);
            Double tScore = probs[0];
            sb.append(",");
            sb.append(tScore);
            pw.println(sb.toString());
        }


        fr.close();
        fw.close();


    }

    public Double score(Collection<Attribute> attribs, boolean scoreType)
    {
        // un-normalised score for true/false outcome, given an attribute set

        Double sum = 0.0;
        if (attribs == null || attribs.isEmpty())
        {
            return 1.0;
        }

        if (scoreType == true)
        {
            for (Attribute a : attribs)
            {
                if (trueWeightMap.get(a) != null)
                {
                    sum = sum + trueWeightMap.get(a);
                }
            }
        }
        else
        {
            for (Attribute a : attribs)
            {
                if (falseWeightMap.get(a) != null)
                {
                    sum = sum + falseWeightMap.get(a);
                }
            }
        }

        Double score = Math.exp(sum);
        return score;

    }

    public Double[] probs(Collection<Attribute> attribs)
    {
        // return two-element array containing normalised probabilities

        Double tScore = score(attribs, true);
        Double fScore = score(attribs, false);
        Double normalizer = tScore + fScore;
        Double[] probs = new Double[2];
        probs[0] = tScore / normalizer;
        probs[1] = fScore / normalizer;
        return probs;


    }


    private ArrayList<Attribute> readAttribs(String attribPath) throws IOException
    {

        ArrayList<Attribute> attribs = new ArrayList<Attribute>();
        attribs.add(new NullAttribute());

        FileReader fr = new FileReader(attribPath);
        BufferedReader br = new BufferedReader(fr);
        String line;
        while ((line = br.readLine()) != null)
        {
            if (Pattern.matches(AnnotationConstants.commentExpr, line))
            {
                continue;
            }
            Attribute a = FileMethods.parseAttribute(line);
            if (a.getType() == Attribute.NULL_TYPE)
            {
                continue;
            }
            attribs.add(a);
        }
        fr.close();

        return attribs;
    }

    private ArrayList<Double> readWeights(String weightPath) throws IOException
    {

        ArrayList<Double> weights = new ArrayList<Double>();

        FileReader fr = new FileReader(weightPath);
        BufferedReader br = new BufferedReader(fr);
        String line;
        while ((line = br.readLine()) != null)
        {
            weights.add(new Double(line));
        }
        fr.close();

        return weights;
    }


}
