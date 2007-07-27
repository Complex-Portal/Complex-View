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
 * @since 09-Aug-2006
 *        <p/>
 *        <p/>
 *        Input proteins in FASTA format
 *        For each protein in list
 *        output significant BLAST hits (sequence alignments) in a reference file
 *        (reference file example -- all IntAct proteins)
 *        <p/>
 *        Output format: UniProt IDs delimited by commas
 *        Protein,Hit1,Hit2,Hit3 ...
 *        <p/>
 *        Later use this file to find attributes of protein pairs
 *        If P;Q is a pair, attributes will be:
 *        P;Q,HP1;HQ1,HP2;HQ2, ...  HP1 is first hit to P, HQ1 first hit to Q, etc.
 */
public class AlignmentFileMaker
{

    private String fastaRefPath = "/scratch/blast/intact.fasta";
    private String blastPath = "/scratch/blast/blast-2.2.14/bin/blastall";

    public AlignmentFileMaker(String protListPath)
    {


    }


    private void getBlastHits()
    {
        // run a BLAST search for a given FASTA file
        // searches the FASTA file at fastaRefPath for significant hits
        // requires a correctly formatted BLAST database
        // (see the formatdb utility in the BLAST application)
        // write BLAST output to a temporary file, then read significant hits from the file
        // return string representation of hits
        // (uniprot IDs delimited by commas?) (entry numbers in FASTA reference file?)


    }

    private void runBlast(String fastaInput, String blastDatabase)
    {
        // execute system call to run a BLAST search for a given FASTA file


    }


}
