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
 * @since 12-Jul-2006
 *        <p/>
 *        Specifies a context (binary interaction)
 *        in the Toolkit for Advanced Discriminative Modelling (TADM) MaxEnt modelling software
 *        Only has binary events (true/false) and binary-valued features in this implementation
 */
public class TadmContext
{
    // context is a data point, eg. binary interaction

    static int totalEvents = 2; // events in a given context are true/false
    int freq1; // frequency of 'true' event in this context
    int freq0; // frequency of 'false' event in this context
    int fvPairs; // total feature-value pairs present in context
    int[] features1; // array of feature indices for 'true' interaction
    // note (attribute)-(true interaction)
    //  and (attribute)-(false interaction) will be separate features
    // each feature will always have value 1 (if present)
    int[] features0;  // array of feature indices for 'false' interaction


    public TadmContext(int freq1, int freq0, int[] attribs)
    {
        // attribs is an array of attribute indices
        // feature index for (attribute, true) = attribute index * 2
        // feature index for (attribute, false) = (attribute index * 2) +1

        // note that features 0 and 1 are the default 'empty' features
        // -- present in all interactions

        this.freq1 = freq1;
        this.freq0 = freq0;
        fvPairs = attribs.length;
        features1 = new int[fvPairs];
        features0 = new int[fvPairs];

        int attribIndex;
        for (int i = 0; i < fvPairs; i++)
        {
            attribIndex = attribs[i];
            features1[i] = attribIndex * 2;
            features0[i] = (attribIndex * 2) + 1;
        }

    }

    public String getEventFileBlock()
    {
        // returns a three-line block to represent this context in an event file

        String fvPair, block;

        StringBuilder fvList1 = new StringBuilder();
        for (int i : features1)
        {
            fvPair = " " + i + " 1";
            fvList1.append(fvPair);
        }
        StringBuilder fvList0 = new StringBuilder();
        for (int i : features0)
        {
            fvPair = " " + i + " 1";
            fvList0.append(fvPair);
        }


        block = uk.ac.ebi.intact.confidence.attribute.TadmContext.totalEvents + "\n" +  // first line:  total events = 2
                freq1 + " " + fvPairs + fvList1.toString() + "\n" +
                // second line -- attributes for 'true' event
                freq0 + " " + fvPairs + fvList0.toString();
        // third line -- attributes for 'false' event
        return block;

    }

    public void incrFreq(boolean outcome) throws IllegalArgumentException
    {
        // increase frequency of a true/false outcome by 1
        if (outcome == true)
        {
            freq1++;
        }
        else
        {
            freq0++;
        }
    }


}
