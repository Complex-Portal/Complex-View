/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.imex.idassigner.helpers;

import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.CvDatabase;
import uk.ac.ebi.intact.model.CvInteractorType;
import uk.ac.ebi.intact.model.CvTopic;
import uk.ac.ebi.intact.model.CvXrefQualifier;
import uk.ac.ebi.intact.persistence.dao.CvObjectDao;
import uk.ac.ebi.intact.persistence.dao.DaoFactory;

/**
 * Utility methods for CVs.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since <pre>15-May-2006</pre>
 */
public class CvHelper {

    //////////////////////////////
    // CvDatabase


    protected static CvDatabase getCvDatabase( String mi ) {
        DaoFactory daoFactory = IntactContext.getCurrentInstance().getDataContext().getDaoFactory();
        CvObjectDao<CvDatabase> dao = daoFactory.getCvObjectDao( CvDatabase.class );
        CvDatabase pubmed = dao.getByPsiMiRef( mi );
        if ( pubmed == null ) {
            throw new IllegalStateException( "Could not find Database by MI reference: " + mi );
        }
        return pubmed;
    }

    public static CvDatabase getPubmed() {
        return getCvDatabase( CvDatabase.PUBMED_MI_REF );
    }

    public static CvDatabase getImex() {
        return getCvDatabase( CvDatabase.IMEX_MI_REF );
    }

    public static CvDatabase getIntact() {
        return getCvDatabase( CvDatabase.INTACT_MI_REF );
    }

    public static CvDatabase getPsi() {
        return getCvDatabase( CvDatabase.PSI_MI_MI_REF );
    }

    //////////////////////////////
    // CvXrefQualifier

    protected static CvXrefQualifier getCvXrefQualifier( String mi ) {
        DaoFactory daoFactory = IntactContext.getCurrentInstance().getDataContext().getDaoFactory();
        CvObjectDao<CvXrefQualifier> dao = daoFactory.getCvObjectDao( CvXrefQualifier.class );
        CvXrefQualifier primaryReference = dao.getByPsiMiRef( mi );
        if ( primaryReference == null ) {
            throw new IllegalStateException( "Could not find CvXrefQualifier by MI reference: " + mi );
        }
        return primaryReference;
    }

    public static CvXrefQualifier getPrimaryReference() {
        return getCvXrefQualifier( CvXrefQualifier.PRIMARY_REFERENCE_MI_REF );
    }

    public static CvXrefQualifier getImexPrimary() {
        return getCvXrefQualifier( CvXrefQualifier.IMEX_PRIMARY_MI_REF );
    }

    public static CvXrefQualifier getIdentity() {
        return getCvXrefQualifier( CvXrefQualifier.IDENTITY_MI_REF );
    }

    ///////////////////////////////
    // CvInteractorType

    public static CvInteractorType getProteinType() {

        DaoFactory daoFactory = IntactContext.getCurrentInstance().getDataContext().getDaoFactory();
        CvObjectDao<CvInteractorType> dao = daoFactory.getCvObjectDao( CvInteractorType.class );
        CvInteractorType proteinType = dao.getByPsiMiRef( CvInteractorType.getProteinMI() );
        if ( proteinType == null ) {
            throw new IllegalStateException( "Could not find CvInteractorType by MI reference: " + CvInteractorType.getProteinMI() );
        }
        return proteinType;
    }

    ////////////////////////////////
    // CvTopic

    protected static CvTopic getCvTopic( String shortlabel ) {
        DaoFactory daoFactory = IntactContext.getCurrentInstance().getDataContext().getDaoFactory();
        CvObjectDao<CvTopic> dao = daoFactory.getCvObjectDao( CvTopic.class );
        CvTopic imexRangeRequested = dao.getByShortLabel( shortlabel );
        if ( imexRangeRequested == null ) {
            throw new IllegalStateException( "Could not find CvTopic by name: " + shortlabel );
        }
        return imexRangeRequested;
    }

    public static CvTopic getImexRangeRequested() throws IntactException {
        return getCvTopic( CvTopic.IMEX_RANGE_REQUESTED );
    }

    public static CvTopic getImexRangeAssigned() throws IntactException {
        return getCvTopic( CvTopic.IMEX_RANGE_ASSIGNED );
    }

    public static CvTopic getImexExported() throws IntactException {
        return getCvTopic( CvTopic.IMEX_EXPORTED );
    }

    public static CvTopic getAccepted() throws IntactException {
        return getCvTopic( CvTopic.ACCEPTED );
    }
}