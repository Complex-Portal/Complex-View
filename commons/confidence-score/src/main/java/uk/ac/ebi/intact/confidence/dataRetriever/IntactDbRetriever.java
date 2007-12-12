/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others. All
 * rights reserved. Please see the file LICENSE in the root directory of this
 * distribution.
 */
package uk.ac.ebi.intact.confidence.dataRetriever;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import uk.ac.ebi.intact.bridges.blast.model.Sequence;
import uk.ac.ebi.intact.bridges.blast.model.UniprotAc;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.business.IntactTransactionException;
import uk.ac.ebi.intact.confidence.expansion.ExpansionStrategy;
import uk.ac.ebi.intact.confidence.model.*;
import uk.ac.ebi.intact.confidence.util.DataMethods;
import uk.ac.ebi.intact.config.impl.AbstractHibernateDataConfig;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.AnnotatedObjectUtils;
import uk.ac.ebi.intact.model.util.CvObjectUtils;
import uk.ac.ebi.intact.model.util.ProteinUtils;
import uk.ac.ebi.intact.persistence.dao.CvObjectDao;
import uk.ac.ebi.intact.persistence.dao.DaoFactory;
import uk.ac.ebi.intact.persistence.dao.InteractionDao;
import uk.ac.ebi.intact.persistence.dao.ProteinDao;

import java.io.*;
import java.util.*;

/**
 * Class to retrieve the high confidence set from IntAct.
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @since <pre>
 *        16 Aug 2007
 *        </pre>
 */
public class IntactDbRetriever implements DataRetrieverStrategy {
    /**
     * Sets up a logger for that class.
     */
    public static final Log log = LogFactory.getLog( IntactDbRetriever.class );
    private List<InteractionSimplified> highConfidenceSet;

    private DataMethods dataMethods;
    private DaoFactory daoFactory;
    private File workDir;
    private ExpansionStrategy expansionStrategy;
    private boolean dbNrForTest = false;

    public IntactDbRetriever( File workDir, ExpansionStrategy expansion ) {
        daoFactory = IntactContext.getCurrentInstance().getDataContext().getDaoFactory();
        dataMethods = new DataMethods();
      //  workDir = new File( tmpDirPath, "IntactDbRetriever" );
        this.workDir = workDir;
       // workDir.mkdir();
        this.expansionStrategy = expansion;
    }

    // //////////////////
    // // Public Methods
    /**
     * returns the list of the high confidence interactions
     *
     * @return List InteractionSimplified
     * @throws DataRetrieverException
     */
    public int retrieveHighConfidenceSet( Writer w ) throws DataRetrieverException {
        if ( highConfidenceSet == null ) {
            try {
                File file = new File( workDir.getPath(), "mediumConfidence.txt" );
                FileWriter fw = new FileWriter( file );
                readMediumConfidenceSet( null, fw );
                fw.close();
            } catch ( FileNotFoundException e ) {
                throw new DataRetrieverException( e );
            } catch ( IOException e ) {
                throw new DataRetrieverException( e );
            }
        }
        return highConfidenceSet.size();
    }

    /**
     * returns the list of the medium confidence interactions
     *
     * @return List InteractionSimplified
     * @throws DataRetrieverException
     */
    public void retrieveMediumConfidenceSet( Writer w ) throws DataRetrieverException {
        readMediumConfidenceSet( w, null );
    }

    /**
     * reads only the protein information(uniprotAc and sequence and role) out
     * of the DB
     *
     * @param uniportAcs list with uniportACs
     * @return List {@link ProteinSimplified}
     */
    public List<ProteinSimplified> readSeq( List<String> uniportAcs ) {
        List<ProteinSimplified> proteins = new ArrayList<ProteinSimplified>();
        ProteinDao proteinDao = daoFactory.getProteinDao();
        for ( String ac : uniportAcs ) {
            Protein protein = proteinDao.getByAc( ac );
            ProteinSimplified proteinS = saveProteinInformation( protein );
            proteins.add( proteinS );
        }

        closeDb();

        return proteins;
    }

    /**
     * saves out of DB the interactionAc, and components -uniprotAc into
     * InteractionSimplified structures
     *
     * @param ebiACs
     * @return List {@link InteractionSimplified}
     */
    public List<InteractionSimplified> read( Set<String> ebiACs ) {
        List<InteractionSimplified> interactions = new ArrayList<InteractionSimplified>();
        InteractionDao interactionDao = daoFactory.getInteractionDao();
        for ( String ac : ebiACs ) {
            Interaction interaction = interactionDao.getByAc( ac );
            if ( interaction != null ) {
                InteractionSimplified intS = saveInteractionInformation( interaction );
                log.info( "read: " + intS.getAc() );
                interactions.add( intS );
            } else
                log.debug( "interaction accession nr not found : " + ac );
        }

        closeDb();

        return interactions;
    }

    /**
     * retrieves the medium confidence set, which is defined by all the
     * interactions in IntAct which do not belong to the high confidence set, or
     * the low confidence set (sets defined through curators)
     *
     * @param mcWriter, hcWriter
     * @return List {@link InteractionSimplified}
     * @throws DataRetrieverException
     */
    public List<InteractionSimplified> readMediumConfidenceSet( Writer mcWriter, Writer hcWriter )
            throws DataRetrieverException {
        highConfidenceSet = new ArrayList<InteractionSimplified>();
        List<InteractionSimplified> medconf = new ArrayList<InteractionSimplified>();

        Set<UniprotAc> medconfProteins = new HashSet<UniprotAc>();
        Set<UniprotAc> highconfProteins = new HashSet<UniprotAc>();

        // File ipFile = new File(workDir, "medconf_db_ip.txt");
        // File goFile = new File(workDir, "medconf_db_go.txt");
        // File seqFile = new File(workDir, "medconf_db_seq.txt");
        Writer ipMcWriter = null;
        Writer goMcWriter = null;
        Writer seqMcWriter = null;

        Writer ipHcWriter = null;
        Writer goHcWriter = null;
        Writer seqHcWriter = null;
        try {

            if ( mcWriter == null ) {
                mcWriter = new FileWriter( new File( workDir, "medconf_all.txt" ) );
            }

            ipMcWriter = new FileWriter( new File( workDir, "medconf_db_ip.txt" ) );
            goMcWriter = new FileWriter( new File( workDir, "medconf_db_go.txt" ) );
            seqMcWriter = new FileWriter( new File( workDir, "medconf_db_seq.txt" ) );

            if ( hcWriter == null ) {
                hcWriter = new FileWriter( new File( workDir, "highconf_all.txt" ) );
            }
            ipHcWriter = new FileWriter( new File( workDir, "highconf_db_ip.txt" ) );
            goHcWriter = new FileWriter( new File( workDir, "highconf_db_go.txt" ) );
            seqHcWriter = new FileWriter( new File( workDir, "highconf_db_seq.txt" ) );
        } catch ( IOException e ) {
            throw new DataRetrieverException( e );
        }

        InteractionDao interactionDao = daoFactory.getInteractionDao();

        int totalNr = 0;
        // only for test purpose

        beginTransaction();
        totalNr = interactionDao.countAll();
        commitTransaction();
        if ( log.isInfoEnabled() ) {
            log.info( "\tGoing to process: " + totalNr );
        }

        if ( dbNrForTest ) {
            totalNr = 10; // TODO: ask it there is a more elegant way
        }

        int firstResult = 0;
        final int maxResults = 50;

        List<InteractionImpl> interactions = null;
        for ( int i = 0; i < totalNr; i += 50 ) {
            // do {
            beginTransaction();
            interactionDao = daoFactory.getInteractionDao();
            interactions = interactionDao.getAll( firstResult, maxResults );

            medconf = checkInteractions( interactions );

            commitTransaction();

            if ( log.isInfoEnabled() ) {
                log.info( "\t\tProcessed medium confidence " + medconf.size() );
            }

            // exports medium confidence set
            medconf = dataMethods.expand( medconf, expansionStrategy );
            dataMethods.export( medconf, mcWriter, true );
            Set<ProteinSimplified> newProts = getNewProteinList( medconf, medconfProteins );
            dataMethods.exportInterPro( newProts, ipMcWriter );
            dataMethods.exportGO( newProts, goMcWriter );
            dataMethods.exportSeq( newProts, seqMcWriter );
            medconfProteins.addAll( getUniprotAc( newProts ) );

            // exports high confidence set
            if ( highConfidenceSet.size() != 0 ) {
                highConfidenceSet = dataMethods.expand( highConfidenceSet, expansionStrategy );
                dataMethods.export( highConfidenceSet, hcWriter, true );
                newProts = getNewProteinList( highConfidenceSet, highconfProteins );
                dataMethods.exportInterPro( newProts, ipHcWriter );
                dataMethods.exportGO( newProts, goHcWriter );
                dataMethods.exportSeq( newProts, seqHcWriter );
                highconfProteins.addAll( getUniprotAc( newProts ) );
                highConfidenceSet.clear();
            }

            if ( log.isInfoEnabled() ) {
                int processed = firstResult + interactions.size();

                if ( firstResult != processed ) {
                    log.info( "\t\tProcessed " + ( firstResult + interactions.size() ) );
                }
            }

            firstResult = firstResult + maxResults;

        }// while (!interactions.isEmpty());

        try {
            mcWriter.close();
            ipMcWriter.close();
            goMcWriter.close();
            seqMcWriter.close();

            hcWriter.close();
            ipHcWriter.close();
            goHcWriter.close();
            seqHcWriter.close();
        } catch ( IOException e ) {
            throw new DataRetrieverException( e );
        }
        return medconf;
    }

    // ///////////////////
    // // Private Methods

    private Set<UniprotAc> getUniprotAc( Set<ProteinSimplified> newProts ) {
        Set<UniprotAc> prots = new HashSet<UniprotAc>( newProts.size() );
        for ( ProteinSimplified proteinSimplified : newProts ) {
            prots.add( proteinSimplified.getUniprotAc() );
        }
        return prots;
    }

    private Set<ProteinSimplified> getNewProteinList( List<InteractionSimplified> interactions,
                                                      Set<UniprotAc> alreadyProcessed ) {
        Set<ProteinSimplified> newProteins = new HashSet<ProteinSimplified>();
        for ( InteractionSimplified interaction : interactions ) {
            List<ProteinSimplified> prots = interaction.getComponents();
            for ( ProteinSimplified protein : prots ) {
                if ( !alreadyProcessed.contains( protein.getUniprotAc() ) ) {
                    newProteins.add( protein );
                }
            }
        }
        return newProteins;
    }

    private Set<ProteinSimplified> getProteinList( List<InteractionSimplified> medconf ) {
        Set<ProteinSimplified> proteinList = new HashSet<ProteinSimplified>();
        for ( InteractionSimplified interaction : medconf ) {
            proteinList.addAll( interaction.getComponents() );
        }
        return proteinList;
    }

    private void commitTransaction() {
        try {
            IntactContext.getCurrentInstance().getDataContext().commitTransaction();
        } catch ( IntactTransactionException e ) {
            throw new IntactException( e );
        }
    }

    private void beginTransaction() {
        IntactContext.getCurrentInstance().getDataContext().beginTransaction();
        daoFactory = IntactContext.getCurrentInstance().getDataContext().getDaoFactory();
    }

    /*
      * the interactions are separated into: highconf, lowconf and medconf
      */
    private List<InteractionSimplified> checkInteractions( List<InteractionImpl> interactionsAll ) {
        List<InteractionSimplified> medconf = new ArrayList<InteractionSimplified>();
        for ( InteractionImpl interaction : interactionsAll ) {

            if ( isInteractionEligible( interaction ) ) {
                if ( isHighConfidenceOrComplexes( interaction ) || isEnzymeOrFluorescenceRole( interaction ) ) {
                    highConfidenceSet.add( saveInteractionInformation( interaction ) );
                } else {
                    medconf.add( saveInteractionInformation( interaction ) );
                }
            }
        }
        return medconf;
    }

    /**
     * checks if the interaction contains at least 2 proteins from uniprot
     *
     * @param interaction
     * @return true or false
     */
    private boolean isInteractionEligible( InteractionImpl interaction ) {
        int nr = 0;

        for ( Component comp : interaction.getComponents() ) {
            Interactor interactor = comp.getInteractor();
            if ( interactor != null && Protein.class.isAssignableFrom( interactor.getClass() )
                 && ProteinUtils.isFromUniprot( ( Protein ) interactor ) && fromUniprot( interactor ) ) {
                nr += 1;
            }
            if ( nr == 2 ) {
                return true;
            }
        }

        return false;
    }

    private boolean fromUniprot( Interactor interactor ) {
        if ( interactor == null ) {
            return false;
        }
        Collection<InteractorXref> xrefs = interactor.getXrefs();
        for ( InteractorXref interactorXref : xrefs ) {
            CvDatabase db = interactorXref.getCvDatabase();
            CvObjectXref dbXref = CvObjectUtils.getPsiMiIdentityXref( db );
            if ( dbXref == null ) {
                log.info( "dbXref == null, db: " + db + " interactor ac: " + interactor.getAc() );
                return false;
            }
            if ( CvDatabase.UNIPROT_MI_REF.equals( dbXref.getPrimaryId() ) ) {
                CvXrefQualifier qualifier = interactorXref.getCvXrefQualifier();
                CvObjectXref qualifierXref = CvObjectUtils.getPsiMiIdentityXref( qualifier );
                // if the uniprotAc are marked for removal
                if ( qualifierXref == null ) {
                    if ( log.isWarnEnabled() ) {
                        log.warn( "interactor :" + interactor.getAc() + " db qualifier: " + qualifier.getAc() );
                    }
                    return false;
                }

                if ( CvXrefQualifier.IDENTITY_MI_REF.equals( qualifierXref.getPrimaryId() ) ) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * checks if the interaction belongs to a complex curated or if the author
     * confidence is high
     *
     * @param interaction
     * @return true or false
     */
    private boolean isHighConfidenceOrComplexes( InteractionImpl interaction ) {
        for ( Annotation item : interaction.getAnnotations() ) {
            CvTopic authorConf = IntactContext.getCurrentInstance().getCvContext().getByMiRef( CvTopic.class,
                                                                                               CvTopic.AUTHOR_CONFIDENCE_MI_REF );
            CvTopic curatedComplex = IntactContext.getCurrentInstance().getCvContext().getByLabel( CvTopic.class,
                                                                                                   CvTopic.CURATED_COMPLEX );
            String authorConfDesc = "high";
            if ( curatedComplex.equals( item.getCvTopic() ) ) {
                return true;
            }
            if ( authorConf.equals( item.getCvTopic() ) && authorConfDesc.equals( item.getAnnotationText() ) ) {
                return true;
            }
        }

        return false;
    }

    /**
     * checks if the interaction contains enzymes, enzymes targets, fluorescence
     * acceptors / donors
     *
     * @param interaction
     * @return true or false
     */
    private boolean isEnzymeOrFluorescenceRole( InteractionImpl interaction ) {
        CvBiologicalRole enzymeRole = IntactContext.getCurrentInstance().getCvContext().getByMiRef(
                CvBiologicalRole.class, CvBiologicalRole.ENZYME_PSI_REF );
        CvBiologicalRole enzymeTargetRole = IntactContext.getCurrentInstance().getCvContext().getByMiRef(
                CvBiologicalRole.class, CvBiologicalRole.ENZYME_TARGET_PSI_REF );
        CvExperimentalRole fluorescenceAcceptorRole = IntactContext.getCurrentInstance().getCvContext().getByMiRef(
                CvExperimentalRole.class, CvExperimentalRole.FLUROPHORE_ACCEPTOR_MI_REF );
        CvExperimentalRole fluorescenceDonorRole = IntactContext.getCurrentInstance().getCvContext().getByMiRef(
                CvExperimentalRole.class, CvExperimentalRole.FLUROPHORE_DONOR_MI_REF );

        int enzymeNr = 0;
        int enzymeTargetNr = 0;
        int fluorescenceAcceptorNr = 0;
        int fluorescenceDonorNr = 0;

        for ( Component component : interaction.getComponents() ) {
            if ( enzymeRole.equals( component.getCvBiologicalRole() ) ) {
                enzymeNr++;
            }
            if ( enzymeTargetRole.equals( component.getCvBiologicalRole() ) ) {
                enzymeTargetNr++;
            }
            if ( fluorescenceAcceptorRole.equals( component.getCvExperimentalRole() ) ) {
                fluorescenceAcceptorNr++;
            }
            if ( fluorescenceDonorRole.equals( component.getCvExperimentalRole() ) ) {
                fluorescenceDonorNr++;
            }
        }

        // TODO: ask if it is possible to have an interaction between an enzyme
        // and another protein (but not a target enzyme), if yes, are these
        // considered as high confidence?
        if ( enzymeNr + enzymeTargetNr >= 2 ) {
            return true;
        }
        if ( fluorescenceAcceptorNr + fluorescenceDonorNr >= 2 ) {
            return true;
        }

        return false;
    }

    /**
     * saves the interaction information: interactionAc, the protein, the
     * proteins role into an InteractionSimplified object
     *
     * @param interaction
     * @return InteractionSimplified
     */
    private InteractionSimplified saveInteractionInformation( Interaction interaction ) {
        InteractionSimplified interactionS = new InteractionSimplified();

        interactionS.setAc( interaction.getAc() );

        Collection<Component> components = interaction.getComponents();
        List<ProteinSimplified> proteins = new ArrayList<ProteinSimplified>();
        for ( Component comp : components ) {
            Interactor interactor = comp.getInteractor();

            String role = "-";
            CvExperimentalRole expRole = comp.getCvExperimentalRole();
            CvObjectXref psiMiXref = CvObjectUtils.getPsiMiIdentityXref( expRole );
            if ( CvExperimentalRole.BAIT_PSI_REF.equals( psiMiXref.getPrimaryId() ) ) {
                role = "bait";
            }
            if ( CvExperimentalRole.PREY_PSI_REF.equals( psiMiXref.getPrimaryId() ) ) {
                role = "prey";
            }

            // this is because an interactor could be a small molecule, you want
            // to make sure you have a protein
            if ( Protein.class.isAssignableFrom( interactor.getClass() ) && fromUniprot( interactor )
                 && ProteinUtils.isFromUniprot( ( Protein ) interactor ) ) {
                ProteinSimplified protein = saveProteinInformation( ( Protein ) interactor );
                if ( protein != null ) {
                    protein.setRole( role );
                    proteins.add( protein );
                }
            }
        }
        interactionS.setInteractors( proteins );

        return interactionS;
    }

    /**
     * saves the uniprotAc and the sequence into the new protein object
     *
     * @param protein
     * @return ProteinSimplified object
     */
    private ProteinSimplified saveProteinInformation( Protein protein ) {
        ProteinSimplified proteinS = new ProteinSimplified();
        InteractorXref uniprotXref = ProteinUtils.getUniprotXref( protein );
        if ( uniprotXref != null ) {
            try {
                proteinS.setUniprotAc( new UniprotAc( uniprotXref.getPrimaryId() ) );
            } catch ( IllegalArgumentException e ) {
                if ( log.isDebugEnabled() ) {
                    log.debug( "UniprotAc not recognized as a valid one:>" + uniprotXref.getPrimaryId() + "<" );
                }
            }
        } else {
            proteinS.setUniprotAc( null );
        }
        if ( protein.getSequence() != null ) {
            proteinS.setSequence( new Sequence( protein.getSequence() ) );
        } else {
            if (log.isInfoEnabled()){
                log.info( "seq was null for: " + protein.getAc() );
            }
        }

        CvObjectDao<CvDatabase> cvDatabase = daoFactory.getCvObjectDao( CvDatabase.class );
        CvDatabase cvGo = cvDatabase.getByPsiMiRef( CvDatabase.GO_MI_REF );
        if ( cvGo == null ) {
            throw new NullPointerException( "CvDatabase GO must not be null, check that the it exists in the database!" );
        }
        Collection<Xref> goRefs = AnnotatedObjectUtils.searchXrefs( protein, cvGo );
        for ( Xref xref : goRefs ) {
            GoIdentifierImpl go = new GoIdentifierImpl( xref.getPrimaryId() );
            proteinS.addGo( go );
        }

        CvDatabase cvIp = cvDatabase.getByPsiMiRef( CvDatabase.INTERPRO_MI_REF );
        if ( cvIp == null ) {
            throw new NullPointerException(
                    "CvDatabase InterPro must not be null, check that the it exists in the database!" );
        }
        Collection<Xref> ipRefs = AnnotatedObjectUtils.searchXrefs( protein, cvIp );
        for ( Xref xref : ipRefs ) {
            InterProIdentifierImpl ip = new InterProIdentifierImpl( xref.getPrimaryId() );
            proteinS.addInterProId( ip );
        }

        return proteinS;
    }

    private void closeDb() {
        try {
            IntactContext.getCurrentInstance().getDataContext().commitTransaction();
        } catch ( IntactTransactionException e ) {
            // If committing the transaction failed (ex : a shortlabel was
            // longer
            // then 20 characters), try to rollback.
            try {
                IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getCurrentTransaction().rollback();
            } catch ( IntactTransactionException e1 ) {
                // If rollback was not successful do what you want :
                // printStackTrace, throw Exception...
                throw new IntactException( "Problem at commit time, couldn't rollback : ", e1 );
            }
            // If commit is it could not commit do what you want :
            // printStackTrace, throw Exception...
            throw new IntactException( "Problem at commit time, rollback done : ", e );
        } finally {
            // Committing the transaction close as well the session if
            // everything
            // goes fine but in case of an exception
            // sent at commit time then the session would not be closed, so it's
            // really important that you close it here
            // otherwise you might get again this fishy connection and have very
            // nasty bugs.
            Session hibernateSession = getSession();
            if ( hibernateSession.isOpen() ) {
                hibernateSession.close();
            }
        }
    }

    private static Session getSession() {
        AbstractHibernateDataConfig abstractHibernateDataConfig = ( AbstractHibernateDataConfig ) IntactContext
                .getCurrentInstance().getConfig().getDefaultDataConfig();
        SessionFactory factory = abstractHibernateDataConfig.getSessionFactory();
        return factory.getCurrentSession();
	}

	/**
     * @param dbNrForTest the dbNrForTest to set
     */
	public void setDbNrForTest(boolean dbNrForTest) {
		this.dbNrForTest = dbNrForTest;
	}
}
