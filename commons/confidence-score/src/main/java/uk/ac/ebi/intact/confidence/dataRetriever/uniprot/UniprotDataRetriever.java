/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others. All
 * rights reserved. Please see the file LICENSE in the root directory of this
 * distribution.
 */
package uk.ac.ebi.intact.confidence.dataRetriever.uniprot;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.bridges.blast.model.Sequence;
import uk.ac.ebi.intact.bridges.blast.model.UniprotAc;
import uk.ac.ebi.intact.confidence.dataRetriever.AnnotationRetrieverStrategy;
import uk.ac.ebi.intact.confidence.model.*;
import uk.ac.ebi.intact.uniprot.model.UniprotProtein;
import uk.ac.ebi.intact.uniprot.model.UniprotXref;
import uk.ac.ebi.intact.uniprot.service.UniprotRemoteService;
import uk.ac.ebi.intact.uniprot.service.UniprotService;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Retireving protein information form Uniprot (http://beta.uniprot.org/)
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version
 * @since
 *
 * <pre>
 * 22 Oct 2007
 * </pre>
 */
public class UniprotDataRetriever implements AnnotationRetrieverStrategy {
	/**
	 * Sets up a logger for that class.
	 */
	public static final Log	log	= LogFactory.getLog(UniprotDataRetriever.class);

	private UniprotService	uniprot;

	public UniprotDataRetriever() {
		uniprot = new UniprotRemoteService();
	}

    public Sequence getSeq(Identifier id){
        if (id instanceof UniprotIdentifierImpl){
            Collection<UniprotProtein> prots = getUniprotProtein( id.getId());
            if(prots.size() >1  && log.isInfoEnabled()){
                log.info("Found more than one UniprotProtein for " + id.getId());
            }
            return new Sequence(prots.iterator().next().getSequence());
        }
        return null;
    }

    public Sequence getSeq(UniprotAc ac){
        Collection<UniprotProtein> uniprotProts = getUniprotProtein( ac.getAcNr());
        if(uniprotProts.size() >1  && log.isInfoEnabled()){
            log.info("Found more than one UniprotProtein for " + ac);
        }
        for(UniprotProtein prot : uniprotProts){
                return new Sequence(prot.getSequence());
        }
        return null;
    }

    /**
     *
     * @param id
     * @return can be null
     */
    public Set<Identifier> getGOs(Identifier id){
        if (id instanceof UniprotIdentifierImpl){
           return getGOs(id.getId());
        }
        return null;
    }

    /**
     *
     * @param ac
     * @return :  not null
     */
    public Set<Identifier> getGOs(UniprotAc ac) {
		return getGOs(ac.getAcNr());
	}

    /**
     *
     * @param id
     * @return : can be null
     */
    public Set<Identifier> getIps(Identifier id){
        if (id instanceof UniprotIdentifierImpl){
            return getIps(id.getId());
        } else {
            return null;
        }
    }

    /**
     *
     * @param ac
     * @return  : not null
     */
    public Set<Identifier> getIps(UniprotAc ac) {
        return getIps(ac.getAcNr());
	}

    public void getSequences(List<ProteinSimplified> proteins){
        for( ProteinSimplified proteinSimplified : proteins){
            Sequence seq =  getSeq( proteinSimplified.getUniprotAc());
            if (seq != null){
            proteinSimplified.setSequence( seq);
            } else {
                if (log.isInfoEnabled()){
                    log.info("No sequence found for ac: "+ proteinSimplified.getUniprotAc());
                }
            }
        }
    }

    public void getGOs(List<ProteinSimplified> proteins){
		for (ProteinSimplified proteinSimplified : proteins) {
			proteinSimplified.setGoSet(getGOs(proteinSimplified.getUniprotAc()));
		}
	}

	public void getIps(List<ProteinSimplified> proteins){
		for (ProteinSimplified proteinSimplified : proteins) {
			proteinSimplified.setInterProSet(getIps(proteinSimplified.getUniprotAc()));
		}
	}

    ///////
    // private methods

     private Collection<UniprotProtein> getUniprotProtein(String ac){
           return uniprot.retrieve( ac);
    }

      private Set<Identifier> getGOs(String ac){
        Set<Identifier> gos = new HashSet<Identifier>();
        Collection<UniprotProtein> uniprotProts = getUniprotProtein( ac );
        if ( uniprotProts.size() > 1 && log.isInfoEnabled() ) {
            log.info( "Found more than one UniprotProtein for " + ac );
        }
        if (uniprotProts.size() != 0){
            Collection<UniprotXref> xrefs = ( ( UniprotProtein ) uniprotProts.toArray()[0] ).getCrossReferences();
            for ( UniprotXref uniprotXref : xrefs ) {
                if ( uniprotXref.getDatabase().equalsIgnoreCase( "Go" ) ) {
                    gos.add( new GoIdentifierImpl( uniprotXref.getAccession() ) );
                }
            }
        }
        return gos;
    }

    private Set<Identifier> getIps(String ac){
        Set<Identifier> ips = new HashSet<Identifier>();
        Collection<UniprotProtein> uniprotProts = uniprot.retrieve( ac );
        if ( uniprotProts.size() > 1 && log.isInfoEnabled() ) {
            log.info( "Found more than one UniprotProtein for " + ac );
        }
        if (uniprotProts.size() != 0){
            Collection<UniprotXref> xrefs = ( ( UniprotProtein ) uniprotProts.toArray()[0] ).getCrossReferences();
            for ( UniprotXref uniprotXref : xrefs ) {
                if ( uniprotXref.getDatabase().equalsIgnoreCase( "InterPro" ) ) {
                    ips.add( new InterProIdentifierImpl( uniprotXref.getAccession() ) );
                }
            }
        }else if (log.isInfoEnabled()){
            log.info("UniprotAc " + ac + " not found in UniprotKB");    
        }
        
        return ips;
    }
}
