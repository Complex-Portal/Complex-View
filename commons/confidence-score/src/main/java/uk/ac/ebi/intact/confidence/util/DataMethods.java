/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others. All
 * rights reserved. Please see the file LICENSE in the root directory of this
 * distribution.
 */
package uk.ac.ebi.intact.confidence.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.bridges.blast.model.Sequence;
import uk.ac.ebi.intact.bridges.blast.model.UniprotAc;
import uk.ac.ebi.intact.confidence.BinaryInteractionSet;
import uk.ac.ebi.intact.confidence.ProteinPair;
import uk.ac.ebi.intact.confidence.attribute.AnnotationConstants;
import uk.ac.ebi.intact.confidence.dataRetriever.IntactDbRetriever;
import uk.ac.ebi.intact.confidence.expansion.ExpansionStrategy;
import uk.ac.ebi.intact.confidence.expansion.SpokeExpansion;
import uk.ac.ebi.intact.confidence.model.*;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * TODO comment this ... someday
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since <pre>
 *               14 - Aug - 2007
 *               </pre>
 */
public class DataMethods implements AnnotationConstants {

    /**
     * Sets up a logger for that class.
     */
    public static final Log log = LogFactory.getLog( DataMethods.class );

    // Constructor
    public DataMethods() {
    }

    /**
     * given a file with one ebi-accession number per line, this method will
     * return a list containing the ebi-accession numbers TODO: check the format
     * of the string
     *
     * @param filepath
     * @return List of strings
     */
    public List<String> readExact( String filepath, boolean ebiAcExpected ) throws IOException {
        if ( filepath == null )
            throw new NullPointerException();

        List<String> ebiACs = new ArrayList<String>();
        List<String> uniprotAcs = new ArrayList<String>();

        File file = new File( filepath );
        System.out.println( file.getAbsolutePath() );
        FileReader fr = null;
        try {
            fr = new FileReader( file );
        } catch ( FileNotFoundException e ) {
            System.out.println( "file not found : " + filepath );
        }

        BufferedReader br = new BufferedReader( fr );
        String line = "";
        while ( ( line = br.readLine() ) != null ) {
            if ( ebiAcExpected && Pattern.matches( ebiAcExpr, line ) ) {
                ebiACs.add( line );
            } else if ( Pattern.matches( uniprotTermExpr, line ) ) {
                uniprotAcs.add( line );
            } else {
                log.debug( "format incorect: " + line );
            }
        }

        return ( ebiAcExpected ? ebiACs : uniprotAcs );
    }

    /**
     * Reads a list of proteins (one prot er line) into a list of uniprotAc
     * objects.
     *
     * @param reader
     * @return
     */
    public Set<UniprotAc> readProtListToUniprotAc( Reader reader ) {
        Set<UniprotAc> proteins = new HashSet<UniprotAc>();

        BufferedReader br = new BufferedReader( reader );
        String line = "";
        try {
            while ( ( line = br.readLine() ) != null ) {
                line.trim();
                proteins.add( new UniprotAc( line ) );

            }
        } catch ( IOException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return proteins;
    }

    /**
     * extracts the uniprotAcs contained in a file into a list
     *
     * @param reader
     * @return
     */
    public List<String> readProteinParisIntoList( Reader reader ) {
        List<String> proteins = new ArrayList<String>();

        BufferedReader br = new BufferedReader( reader );
        String line = "";
        try {
            while ( ( line = br.readLine() ) != null ) {
                String[] strs = line.split( "," );
                for ( String str : strs ) {
                    if ( Pattern.matches( uniprotTermExpr, str ) && !proteins.contains( str ) ) {
                        proteins.add( str );
                    }
                }

            }
        } catch ( IOException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return proteins;
    }

    /**
     * Retrieves the proteins form the list of interactions
     *
     * @param interactions
     * @return
     */

    /**
     * Given a file with one EBI-accession number per line, this method
     * retrieves the interaction information from IntAct and stores it into a
     * list.
     *
     * @param filepath
     * @return List of simplified interaction objects
     */
    public List<InteractionSimplified> read( String filepath, String tmpDirPath ) throws IOException {
        HashSet<String> ebiACs = new HashSet<String>( readExact( filepath, true ) );
        List<InteractionSimplified> interactions = getInteractions( tmpDirPath, ebiACs );
        return interactions;
    }

    /*
      * gets the information out of the DB for each EBI-xxxx it retrievs the
      * uniprotAcs
      */
    private List<InteractionSimplified> getInteractions( String tmpDirPath, Set<String> ebiACs ) {
        ebiACs = checkFormat( ebiACs, true );
        return ( new IntactDbRetriever( new File(tmpDirPath), new SpokeExpansion() ) ).read( ebiACs );
    }

    /**
     * checks if the strings represent valid EBI accession numbers or uniprot
     * accession numbers. If ebiACs = true it will check the format for EBI
     * accession numbers.
     *
     * @param ebiACs true or false
     * @param acs    list of strings
     * @return List of checked strings
     */
    public Set<String> checkFormat( Set<String> acs, boolean ebiACs ) {
        Set<String> checkedACs = new HashSet<String>( acs.size() );
        if ( ebiACs ) {

            for ( String ac : acs ) {
                if ( Pattern.matches( ebiAcExpr, ac ) ) {
                    checkedACs.add( ac );
                } else {
                    log.debug( "format incorect: " + ac );
                }
            }
        } else { // checkFormat for uniprotACs
            for ( String ac : acs ) {
                if ( Pattern.matches( uniprotTermExpr, ac ) ) {
                    checkedACs.add( ac );
                } else {
                    log.debug( "format incorect: " + ac );
                }
            }
        }
        return checkedACs;
    }

    /**
     * Given a fasta file containing uniprotACs, this method extracts the
     * accession numbers.
     *
     * @param inFile  the fasta file to be read
     * @param outFile where the list of uniprotACs will be written
     * @return List of uniprotACs
     */
    public Set<String> readFastaToProts( File inFile, File outFile ) {
        Set<String> proteins = new HashSet<String>();

        try {
            FileReader fr = new FileReader( inFile );
            BufferedReader br = new BufferedReader( fr );
            String line = "";
            String uniprotAcLine = "^>(.*|.*)";
            while ( ( line = br.readLine() ) != null ) {
                if ( Pattern.matches( uniprotAcLine, line ) ) {
                    String[] pices = line.split( "\\|" );
                    log.debug( "found uniportAc:	" + pices[0].substring( 1 ) + "\n" );
                    proteins.add( pices[0].substring( 1 ) );
                }

            }
        } catch ( FileNotFoundException e ) {
            e.printStackTrace();
        } catch ( IOException e ) {
            e.printStackTrace();
        }

        // writing it to outFile
        if ( outFile != null && proteins.size() != 0 ) {
            writeToFile( proteins, outFile );
        }

        return proteins;
    }

    /**
     * Parses a fasta file (">P12345-1|description\nmfg.....") intro a list of ProteinSimplified objects.
     *
     * @param inFile the fasta file to be read
     * @return Set<ProteinSimplified>
     */
    public Set<ProteinSimplified> readExactFasta( File inFile ) {
        Set<ProteinSimplified> proteins = new HashSet<ProteinSimplified>();
        try {
            FileReader fr = new FileReader( inFile );
            BufferedReader br = new BufferedReader( fr );
            String line = "";
            String uniprotAcLine = "^>(.*|.*)";
            UniprotAc ac = null;
            String seqStr = "";
            while ( ( line = br.readLine() ) != null ) {
                if ( Pattern.matches( uniprotAcLine, line ) ) {
                    // save previous prot
                    if ( ac != null ) {
                        if ( !seqStr.equalsIgnoreCase( "" ) ) {
                            proteins.add( new ProteinSimplified( ac, new Sequence( seqStr ) ) );
                        } else {
                            if ( log.isDebugEnabled() ) {
                                log.debug( "Seq not found for ac: " + ac );
                            }
                        }
                    }
                    // start collecting info for new protein
                    // split by non word characters
                    String[] pices = line.split( "\\|" );
                    ac = new UniprotAc( pices[0].substring( 1 ) );
                    seqStr = "";
                    if ( log.isDebugEnabled() ) {
                        log.debug( "found uniportAc:	" + pices[1] + "\n" );
                    }

                } else {
                    seqStr += line.trim();
                }
            }
            if ( ac != null ) {
                if ( !seqStr.equalsIgnoreCase( "" ) ) {
                    proteins.add( new ProteinSimplified( ac, new Sequence( seqStr ) ) );
                } else {
                    if ( log.isDebugEnabled() ) {
                        log.debug( "Seq not found for ac: " + ac );
                    }
                }
            }
        } catch ( FileNotFoundException e ) {
            e.printStackTrace();
        } catch ( IOException e ) {
            e.printStackTrace();
        }

        return proteins;
    }

    /**
     * Given a list of interactions(binary and complex interactions) it returns
     * a list containing only binary interactions. The complex interactions are
     * expanded according to the private field expansionStrategy.
     *
     * @param interactions
     * @return List of binary interactions
     */
    public List<InteractionSimplified> expand( List<InteractionSimplified> interactions,
                                               ExpansionStrategy expansionStrategy ) {
        if ( interactions == null || expansionStrategy == null ) {
            throw new NullPointerException( "check that the interactions and expansionStrategy are not null!" );
        }
        List<InteractionSimplified> expanded = new ArrayList<InteractionSimplified>();
        for ( InteractionSimplified interaction : interactions ) {
            expanded.addAll( expansionStrategy.expand( interaction ) );
        }
        return expanded;
    }
    
    /**
     * Generates a random number of interactions that are not present in the
     * highconf, medconf or lowconf list. The random interactions are build out
     * of the list of proteins (yeastProtACs).
     *
     * @param yeastProtACs
     * @param highconf     list of high confidence interactions
     * @param medconf      list of medium confidence interactions
     * @param lowconf      list of low confidence interactions
     * @param nr           the number of random interactions to be generated
     * @return List of simplified interactions
     */
    public List<InteractionSimplified> generateLcInteractions( Set<String> yeastProtACs,
                                                               List<InteractionSimplified> highconf, List<InteractionSimplified> medconf,
                                                               List<InteractionSimplified> lowconf, int nr ) {

        InteractionGenerator intGen = new InteractionGenerator();
        intGen.setHighconfidence( highconf );
        intGen.setLowconfidence( lowconf );
        intGen.setMediumconfidence( medconf );
        yeastProtACs = checkFormat( yeastProtACs, false );
        intGen.setProteinACs( yeastProtACs );

        List<InteractionSimplified> generatedLC = intGen.generate( nr );

        return generatedLC;
    }

    /**
     * Generates random interactions that are not present in the forbidden list
     *
     * @param yeastProt
     * @param forbidden
     * @param nr        number of created interactions
     * @return BinaryInteractionSet
     */
    public BinaryInteractionSet generateLowConf( Set<String> yeastProt, BinaryInteractionSet forbidden, int nr ) {
        InteractionGenerator intGen = new InteractionGenerator();
        return intGen.generate( yeastProt, forbidden, nr );
    }

    /**
     * Exports the interaction and uniprot - accession numbers to an output
     * stream
     *
     * @param interactions   the exported list of interactions
     * @param file           the output file
     * @param uniprotAcsOnly a flag for the export
     */
    public void export( List<InteractionSimplified> interactions, File file, boolean uniprotAcsOnly ) {
        try {
            FileOutputStream fos = new FileOutputStream( file );
            OutputStreamWriter osw = new OutputStreamWriter( fos );
            export( interactions, osw, uniprotAcsOnly );
            osw.close();
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    /**
     * Exports a BinaryInteractonSet to a file
     *
     * @param biSet
     * @param file
     */
    public void export( BinaryInteractionSet biSet, File file ) {
        FileWriter fw;
        try {
            fw = new FileWriter( file );
            for ( ProteinPair pp : biSet.getSet() ) {
                fw.append( pp.toString() + "\n" );
            }
            fw.close();
        } catch ( IOException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Exports the interaction and uniprot - accession numbers to an output
     * stream
     *
     * @param interactions   the exported list of interactions
     * @param writer         the Writer
     * @param uniprotAcsOnly a flag for exporting
     */
    public void export( List<InteractionSimplified> interactions, Writer writer, boolean uniprotAcsOnly ) {
        if ( log.isDebugEnabled() ) {
            log.debug( "\t\tGoing to export : " + interactions.size() );
        }
        for ( InteractionSimplified item : interactions ) {

            if ( item.getInteractors().size() != 2 && log.isDebugEnabled() ) {
                log.debug( "interaction : " + item.getAc() + " is not binary!!!" );
            }

            StringBuilder sb = new StringBuilder();
            if ( !uniprotAcsOnly )
                sb.append( item.getAc() + ": " );
            ProteinSimplified prot1 = ( ProteinSimplified ) item.getInteractors().toArray()[0];
            ProteinSimplified prot2 = ( ProteinSimplified ) item.getInteractors().toArray()[1];
            sb.append( prot1.getUniprotAc() + ";" + prot2.getUniprotAc() + "\n" );

            try {
                writer.append( sb.toString() );
            } catch ( IOException e ) {
                if ( log.isErrorEnabled() ) {
                    log.error( "could not export : " + sb.toString() );
                }
                e.printStackTrace();
            }
        }
    }

    public void exportGO( Set<ProteinSimplified> proteins, Writer writer ) {
        for ( ProteinSimplified proteinSimplified : proteins ) {
            exportGO( proteinSimplified, writer );
        }
    }

    private void exportGO( ProteinSimplified proteinS, Writer writer ) {
        if (proteinS == null  || proteinS.getUniprotAc() == null){
            log.info("UniprotAc must not be null!");
            throw new IllegalArgumentException( "ProteinSimplified must not be null, nor its uniprotAc!");
        }
        HashSet<String> forbiddenGo = new HashSet<String>( forbiddenGoTerms.length );
        for ( String goTerm : forbiddenGoTerms ) {
            // use static array of forbidden GO terms, recorded in
            // AnnotationConstants interface
            forbiddenGo.add( goTerm );
        }

        Set<Identifier> gos = proteinS.getGoSet();
        String ac = proteinS.getUniprotAc().getAcNr();
        try {
            writer.append( ac + "," );
            if ( gos != null ) {
                for ( Identifier goId : gos ) {
                    if ( !forbiddenGo.contains( goId ) ) {
                        writer.append( goId + "," );
                    }
                }
            }
            writer.append( "\n" );
        } catch ( IOException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void exportInterPro( Set<ProteinSimplified> proteins, Writer writer ) {
        for ( ProteinSimplified proteinSimplified : proteins ) {
            exportInterPro( proteinSimplified, writer );
        }
    }

    private void exportInterPro( ProteinSimplified proteinS, Writer writer ) {
        if (proteinS == null || proteinS.getUniprotAc() == null){
            log.info("UniprotAc is null!");
            throw new IllegalArgumentException( "ProteinSimplified must not be null, nor its uniprotAc!");
        }
        Set<Identifier> ips = proteinS.getInterProSet();
        if ( proteinS.getUniprotAc() == null ) {
            log.info( "protein withouth ac!: " + proteinS );
        }
        String ac = proteinS.getUniprotAc().getAcNr();
        try {
            writer.append( ac + "," );
            if ( ips != null ) {
                for ( Identifier ipId : ips ) {
                    writer.append( ipId + "," );
                }
            }
            writer.append( "\n" );
        } catch ( IOException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void exportSeq( Set<ProteinSimplified> proteins, Writer seqWriter ) {
        for ( ProteinSimplified proteinSimplified : proteins ) {
            exportSeq( proteinSimplified, seqWriter );
        }
    }

    private void exportSeq( ProteinSimplified proteinS, Writer seqWriter ) {
        if ( proteinS == null ) {
            throw new IllegalArgumentException( "ProteinSimplified mus not be null!" );
        }
        if(proteinS.getUniprotAc() == null){
            throw new IllegalArgumentException("UniprotAc must not be null!");
        }
//        if (proteinS.getSequence() == null){
//            throw new IllegalArgumentException("ProteinSimplified mus have a sequence! >"+ proteinS.getUniprotAc()+"<");
//        }
        try {
            seqWriter.append( ">" + proteinS.getUniprotAc() + "|description \n" );
            if ( proteinS.getSequence() != null ) {
                seqWriter.append( proteinS.getSequence().getSeq() );
            } else {
                seqWriter.append( "noSequence" );
            }
            seqWriter.append( "\n" );
        } catch ( IOException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Exports the interaction and uniprot - accession numbers to an output
     * stream
     *
     * @param proteinPairs the exported list of protein pairs
     * @param writer       the Writer
     */
    public void export( List<ProteinPair> proteinPairs, Writer writer ) {
        log.info( "\t\tGoing to export : " + proteinPairs.size() );
        for ( ProteinPair pp : proteinPairs ) {
            StringBuilder sb = new StringBuilder();
            sb.append( pp.getFirstId() + ";" + pp.getSecondId() + "\n" );
            try {
                writer.append( sb.toString() );
            } catch ( IOException e ) {
                log.error( "could not export : " + sb.toString() );
                e.printStackTrace();
            }
        }
        try {
            writer.close();
        } catch ( IOException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * retrieves from a file of the form <EBI-AC: p1, p2> a List of
     * InteractionSimplified
     *
     * @param inFile
     * @return List InteractionSimplified
     */
    public List<InteractionSimplified> readImport( File inFile ) {
        FileReader fr;
        List<InteractionSimplified> interactions = new ArrayList<InteractionSimplified>();
        try {
            fr = new FileReader( inFile );
            BufferedReader br = new BufferedReader( fr );

            String line = "";
            while ( ( line = br.readLine() ) != null ) {
                String[] strs = line.split( ":" );

                InteractionSimplified intS = new InteractionSimplified();
                intS.setAc( strs[0] );

                String[] uniprotAcs = strs[1].split( ";" );

                List<ProteinSimplified> proteins = new ArrayList<ProteinSimplified>();

                for ( String uniprot : uniprotAcs ) {
                    ProteinSimplified protS = new ProteinSimplified();
                    protS.setUniprotAc( new UniprotAc( uniprot.trim() ) );
                    log.info( "found ebi-ac:	" + intS.getAc() + " uniprotAc: " + uniprot + "\n" );
                    proteins.add( protS );
                }
                intS.setInteractors( proteins );
                interactions.add( intS );
            }

        } catch ( FileNotFoundException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch ( IOException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return interactions;
    }

    /**
     * retrieves a list of Protein Pairs form a fileF
     *
     * @param inFile
     * @return List of ProteinPairs
     */
    public List<ProteinPair> readImportProteinPairs( File inFile ) {
        List<ProteinPair> proteins = new ArrayList<ProteinPair>();
        FileReader fr;
        try {
            fr = new FileReader( inFile );
            BufferedReader br = new BufferedReader( fr );

            String line = "";
            while ( ( line = br.readLine() ) != null ) {
                String[] uniprotAcs = line.split( ";" );
                if ( Pattern.matches( uniprotTermExpr, uniprotAcs[0] ) && Pattern.matches( uniprotTermExpr, uniprotAcs[1] ) ) {
                    ProteinPair pp = new ProteinPair( uniprotAcs[0], uniprotAcs[1] );
                    proteins.add( pp );
                }
            }
        } catch ( FileNotFoundException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch ( IOException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return proteins;
    }

    /**
     * given a list of interactions it retrieves the list of proteins
     *
     * @param interactions
     * @return List of uniprotAc
     */
    public List<String> getProteins( List<InteractionSimplified> interactions ) {
        List<String> proteins = new ArrayList<String>();

        for ( InteractionSimplified intS : interactions ) {
            for ( ProteinSimplified protS : intS.getInteractors() ) {
                if ( !proteins.contains( protS.getUniprotAc() ) ) {
                    proteins.add( protS.getUniprotAc().getAcNr() );
                }
            }
        }

        return proteins;
    }

    /*
      * Writes the list of proteins to a file. One protein per line.
      */
    private void writeToFile( Collection<String> proteins, File outFile ) {
        try {
            FileWriter fw = new FileWriter( outFile );
            PrintWriter pw = new PrintWriter( fw );

            for ( String uniprotAc : proteins ) {
                pw.println( uniprotAc );
            }
            fw.close();
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

}
