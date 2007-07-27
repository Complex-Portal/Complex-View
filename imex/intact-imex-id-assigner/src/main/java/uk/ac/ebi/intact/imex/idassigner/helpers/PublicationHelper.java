/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.imex.idassigner.helpers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.imex.idassigner.id.IMExIdTransformer;
import uk.ac.ebi.intact.imex.idassigner.id.IMExRange;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.AnnotatedObjectUtils;
import uk.ac.ebi.intact.persistence.dao.AnnotationDao;
import uk.ac.ebi.intact.persistence.dao.CvObjectDao;
import uk.ac.ebi.intact.persistence.dao.DaoFactory;
import uk.ac.ebi.intact.persistence.dao.PublicationDao;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Utility methods for a publication.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since <pre>11-May-2006</pre>
 */
public class PublicationHelper {

    public static final Log log = LogFactory.getLog( PublicationHelper.class );

    /**
     * Simple representation of a Date.
     * <p/>
     * Will be used to name our IMEx files.
     */
    private static final SimpleDateFormat SIMPLE_DATE_FORMATER = new SimpleDateFormat( "yyyy-MM-dd" );

    public static final String IMEX_EXPORT_SEPARATOR = ":";

    public static Publication loadPublication( String pmid ) throws IntactException {

        DaoFactory daoFactory = IntactContext.getCurrentInstance().getDataContext().getDaoFactory();
        PublicationDao dao = daoFactory.getPublicationDao();

        log.debug( "Searching for Publication by pmid: " + pmid );
        Collection<Publication> publications = dao.getByXrefLike( CvHelper.getPubmed(), pmid );

        log.debug( publications.size() + " publication(s) found." );
        if ( publications.size() > 1 ) {

            StringBuilder sb = new StringBuilder( 512 );
            for ( Publication publication : publications ) {
                sb.append( "\n" ).append( publication );
            }

            throw new IllegalStateException( "More than one publication matching pubmed id " + pmid +
                                             " were found. Details below:" + sb.toString() );

        } else if ( publications.size() == 1 ) {

            Publication publication = publications.iterator().next();
            log.debug( publication );
            return publication;
        }

        return null;
    }

    /**
     * Add an annotation To a Publication. It decribes the requested range of IMEx ID from Key Assigner.
     *
     * @param publication the publication onto which we add a new annotation
     * @param imexRange   the range
     *
     * @throws uk.ac.ebi.intact.business.IntactException
     *
     */
    public static void addRequestedAnnotation( Publication publication, IMExRange imexRange ) throws IntactException {

        CvTopic imexRangeRequested = CvHelper.getImexRangeRequested();

        Institution owner = IntactContext.getCurrentInstance().getConfig().getInstitution();
        String simpleRange = IMExIdTransformer.formatSimpleRange( imexRange );
        Annotation requested = new Annotation( owner, imexRangeRequested, simpleRange );

        DaoFactory daoFactory = IntactContext.getCurrentInstance().getDataContext().getDaoFactory();
        AnnotationDao aDao = daoFactory.getAnnotationDao();

        if ( publication.getAnnotations().contains( requested ) ) {

            System.out.println( "That publication had already the Annotation(" + imexRangeRequested.getShortLabel() +
                                ", " + simpleRange + ")." );

        } else {
            // not in yet, add it...
            aDao.persist( requested );

            PublicationDao pDao = daoFactory.getPublicationDao();

            publication.addAnnotation( requested );
            pDao.update( publication );

            System.out.println( "Added Annotation(" + imexRangeRequested.getShortLabel() + ", " + simpleRange +
                                ") to Publication(" + getPubmedId( publication ) + ")" );
        }
    }

    /**
     * Return a publication pubmed id if available.
     *
     * @param publication the publication we are searching on.
     *
     * @return a string representation of one or many pubmed ids or null if none id found.
     */
    public static String getPubmedId( Publication publication ) {
        DaoFactory daoFactory = IntactContext.getCurrentInstance().getDataContext().getDaoFactory();
        CvObjectDao<CvObject> cvObjectDao = daoFactory.getCvObjectDao();
        CvDatabase pubmed = CvHelper.getPubmed();
        Collection<Xref> pubmeds = AnnotatedObjectUtils.searchXrefs( publication, pubmed );
        if ( pubmeds.isEmpty() ) {
            return null;
        } else {
            StringBuilder sb = new StringBuilder();
            for ( Iterator<Xref> iterator = pubmeds.iterator(); iterator.hasNext(); ) {
                Xref xref = iterator.next();
                sb.append( xref.getPrimaryId() );
                if ( iterator.hasNext() ) {
                    sb.append( ", " );
                }
            }
            return sb.toString();
        }
    }

    /**
     * Collect all requested IDs on a specific publication and build a collection of IMExRange.
     *
     * @param publication the publication
     *
     * @return a non null collection of IMExRange, may be empty.
     *
     * @throws IntactException
     */
    public static List<IMExRange> getRequestedRanges( Publication publication ) throws IntactException {
        CvTopic imexRangeRequested = CvHelper.getImexRangeRequested();

        List<IMExRange> ranges = new ArrayList<IMExRange>( 2 );

        for ( Annotation annotation : publication.getAnnotations() ) {
            if ( imexRangeRequested.equals( annotation.getCvTopic() ) ) {
                // found one
                IMExRange range = IMExIdTransformer.parseSimpleRange( annotation.getAnnotationText() );
                ranges.add( range );
            }
        }

        return ranges;
    }

    /**
     * Collect all assigned IDs on a specific publication and build a collection of IMExRange.
     * <p/>
     * This assumes the publication has been updated correctly.
     *
     * @param publication the publication
     *
     * @return a non null collection of IMExRange, may be empty.
     *
     * @throws IntactException
     */
    public static List<IMExRange> getAssignedRanges( Publication publication ) throws IntactException {
        CvTopic imexRangeAssigned = CvHelper.getImexRangeAssigned();

        List<IMExRange> ranges = new ArrayList<IMExRange>( 2 );

        for ( Annotation annotation : publication.getAnnotations() ) {
            if ( imexRangeAssigned.equals( annotation.getCvTopic() ) ) {
                // found one
                IMExRange range = IMExIdTransformer.parseSimpleRange( annotation.getAnnotationText() );
                ranges.add( range );
            }
        }

        return ranges;
    }

    /**
     * Add an annotation To a Publication. It decribes the requested range of IMEx ID from Key Assigner.
     *
     * @param publication the publication onto which we add a new annotation
     * @param imexRange   the range
     *
     * @throws IntactException
     */
    public static void addAssignedAnnotation( Publication publication, IMExRange imexRange ) throws IntactException {

        CvTopic imexRangeAssigned = CvHelper.getImexRangeAssigned();

        DaoFactory daoFactory = IntactContext.getCurrentInstance().getDataContext().getDaoFactory();
        AnnotationDao dao = daoFactory.getAnnotationDao();

        Institution owner = IntactContext.getCurrentInstance().getConfig().getInstitution();
        String simpleRange = IMExIdTransformer.formatSimpleRange( imexRange );
        Annotation requested = new Annotation( owner, imexRangeAssigned, simpleRange );

        if ( publication.getAnnotations().contains( requested ) ) {

            log.debug( "That publication had already the Annotation(" + imexRangeAssigned.getShortLabel() +
                       ", " + simpleRange + ")." );
        } else {
            // not in yet, add it...
            dao.persist( requested );

            PublicationDao pubDao = daoFactory.getPublicationDao();
            publication.addAnnotation( requested );
            pubDao.update( publication );

            log.debug( "Added Annotation(" + imexRangeAssigned.getShortLabel() + ", " + simpleRange +
                       ") to Publication(" + getPubmedId( publication ) + ")" );
        }
    }

    /**
     * Build a well formatted imex-exported annotation.
     *
     * @param text the text to append in the comment (format: YYYY-MM-DD: text).
     *
     * @return an annotation.
     *
     * @throws IntactException
     */
    public static Annotation buildImexExportedAnnotation( String text ) throws IntactException {
        CvTopic imexExported = CvHelper.getImexExported();
        Institution owner = IntactContext.getCurrentInstance().getConfig().getInstitution();
        Annotation annot = new Annotation( owner, imexExported );
        String today = getTodaySimpleDate();

        String annotText = today;
        if ( text != null ) {
            annotText += IMEX_EXPORT_SEPARATOR + " " + text.trim();
        }

        annot.setAnnotationText( annotText );
        return annot;
    }

    /**
     * Return today's date in a simple format.
     *
     * @return
     */
    public static String getTodaySimpleDate() {
        return SIMPLE_DATE_FORMATER.format( new Date() ); // YYYY-MM-DD;
    }

    /**
     * Get a list of Annotation having CvTopic( imex-exported ) and sort them chonologicaly.
     *
     * @param publication the publication of interrest.
     *
     * @return a sorted collection of exported date.
     *
     * @throws IntactException
     */
    public static List<Annotation> getExportHistory( Publication publication ) throws IntactException {
        CvTopic imexExported = CvHelper.getImexExported();

        List<Annotation> export = new ArrayList<Annotation>( publication.getAnnotations().size() );

        for ( Annotation annotation : publication.getAnnotations() ) {
            if ( imexExported.equals( annotation.getCvTopic() ) ) {
                export.add( annotation );
            }
        }

        // sort by date
        Collections.sort( export, new Comparator<Annotation>() {
            public int compare( Annotation o1, Annotation o2 ) {
                String t1 = o1.getAnnotationText();
                String t2 = o2.getAnnotationText();

                int idx1 = t1.indexOf( IMEX_EXPORT_SEPARATOR );
                if ( idx1 == -1 ) {
                    idx1 = t1.length();
                }
                String dt1 = t1.substring( 0, idx1 );

                int idx2 = t2.indexOf( IMEX_EXPORT_SEPARATOR );
                if ( idx2 == -1 ) {
                    idx2 = t2.length();
                }
                String dt2 = t2.substring( 0, idx2 );

                Date d1 = null;
                try {
                    d1 = SIMPLE_DATE_FORMATER.parse( dt1 );
                } catch ( ParseException e ) {
                    e.printStackTrace();
                }
                Date d2 = null;
                try {
                    d2 = SIMPLE_DATE_FORMATER.parse( dt2 );
                } catch ( ParseException e ) {
                    e.printStackTrace();
                }

                if ( d1 == null ) {
                    return -1;
                }

                if ( d2 == null ) {
                    return 1;
                }

                return d1.compareTo( d2 );
            }
        } );

        return export;
    }

    public static void showExportHistory( Publication publication ) throws IntactException {

        List<Annotation> export = getExportHistory( publication );

        // display
        for ( Annotation annotation : export ) {
            log.debug( annotation.getAnnotationText() );
        }
    }
}