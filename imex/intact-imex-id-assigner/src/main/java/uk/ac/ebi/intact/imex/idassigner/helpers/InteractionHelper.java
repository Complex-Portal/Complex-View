/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.imex.idassigner.helpers;

import uk.ac.ebi.intact.core.IntactException;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.core.persistence.dao.DaoFactory;
import uk.ac.ebi.intact.core.persistence.dao.XrefDao;
import uk.ac.ebi.intact.model.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Utility methods for Interactions.
 * <p/>
 * <b><u>Note</u></b>: the difference between an IMEx ID and an IMEx primary ID is the following: <br/> <b>IMEx ID</b>:
 * id retreived from the IMEx key assigner, it will be shared across all IMEx partner (eg. IM-12345). <br/> <b>IMEx
 * primary ID</b>: the local identifier of the interaction in the originating database. (eg, in IntAct: EBI-983747)
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since <pre>11-May-2006</pre>
 */
public class InteractionHelper {

    ////////////////////////////
    // IMEx ID

    /**
     * Add an IMEx ID onto an Interaction.
     *
     * @param interaction the interaction
     * @param imexId      the IMEx ID
     *
     * @return true if the IMEx ID was added successfully, false otherwise (eg. there was already one IMEx id).
     *
     * @throws uk.ac.ebi.intact.core.IntactException
     *
     */
    public static boolean addIMExId( Interaction interaction, String imexId ) throws IntactException {
        CvDatabase imex = CvHelper.getImex();

        String id = getIMExId( interaction );

        if ( id == null ) {
            // add a new Xref
            Institution owner = IntactContext.getCurrentInstance().getInstitution();
            InteractorXref xref = new InteractorXref( owner, imex, imexId, null );
            interaction.addXref( xref );

            DaoFactory daoFactory = IntactContext.getCurrentInstance().getDataContext().getDaoFactory();
            XrefDao xdao = daoFactory.getXrefDao();
            xdao.persist( xref );
            System.out.println( "Added IMEx ID( " + imexId + " ) to interaction " + interaction.getAc() + " " + interaction.getShortLabel() );

            return true;

        } else {
            System.out.println( "Interaction " + interaction.getAc() + " " + interaction.getShortLabel() +
                                " had already an IMEx ID: " + id + ". skip update." );
        }

        return false;
    }

    /**
     * Search for an IMEx id.
     *
     * @param interaction the interaction that may hold an IMEx Xref.
     *
     * @return the IMEx id, or null if not found.
     */
    public static String getIMExId( Interaction interaction ) {

        CvDatabase imex = CvHelper.getImex( );

        for ( Xref xref : interaction.getXrefs() ) {
            if ( imex.equals( xref.getCvDatabase() ) ) {
                return xref.getPrimaryId();
            }
        }

        return null; // not found
    }

    /**
     * Answers the question: "has the given interaction got an IMEx ID ?".
     *
     * @param interaction the interaction
     *
     * @return true if the interaction has an IMEx ID.
     */
    public static boolean hasIMExId( Interaction interaction ) {
        return ( null != getIMExId( interaction ) );
    }

    ////////////////////////////
    // IMEx Primary ID

    /**
     * Search and return the first Xref having a CvXrefQualifier( imex-primary ).
     *
     * @param interaction the interaction we are searching on.
     *
     * @return an Xref or null if not found.
     */
    public static Xref getIMExPrimary( Interaction interaction ) {

        CvXrefQualifier imexPrimary = CvHelper.getImexPrimary( );

        for ( Xref xref : interaction.getXrefs() ) {
            if ( imexPrimary.equals( xref.getCvXrefQualifier() ) ) {
                return xref;
            }
        }

        return null; // not found
    }

    /**
     * Answers the question: "has the given interaction got an IMEx primary ID ?".
     *
     * @param interaction the interaction
     *
     * @return true if the interaction has an IMEx primary ID.
     */
    public static boolean hasIMExPrimary( Interaction interaction ) {

        return ( null != getIMExPrimary( interaction ) );
    }

    /**
     * Add an Xref(CvDatabase(current), XcXrefQualifier( imex-primary ) ) if there's none.
     *
     * @param interaction the interaction on which we'll try to add an Xref
     *
     * @return true if the Xref was added, false otherwise.
     *
     * @throws IntactException
     */
    public static boolean addIMExPrimary( Interaction interaction ) throws IntactException {

        // first, find which database we are.
        CvDatabase currentDatabase = IMExHelper.whoAmI( );
        CvXrefQualifier imexPrimary = CvHelper.getImexPrimary( );

        Xref xref = getIMExPrimary( interaction );

        if ( xref == null ) {
            // add a new Xref
            String id = interaction.getAc();
            Institution owner = IntactContext.getCurrentInstance().getInstitution();
            xref = new InteractorXref( owner, currentDatabase, id, imexPrimary );
            interaction.addXref( (InteractorXref ) xref );

            DaoFactory daoFactory = IntactContext.getCurrentInstance().getDataContext().getDaoFactory();
            XrefDao xdao = daoFactory.getXrefDao();
            xdao.persist( xref );

            System.out.println( "Added IMEx Primary ID from " + currentDatabase.getShortLabel() + "( " + id +
                                " ) to interaction " + interaction.getAc() + " " + interaction.getShortLabel() );
            return true;

        } else {
            System.out.println( "Interaction " + interaction.getAc() + " " + interaction.getShortLabel() +
                                " had already an IMEx Primary ID from " + xref.getCvDatabase().getShortLabel() +
                                ": " + xref.getPrimaryId() + ". skip update." );
        }

        return false;
    }

    ///////////////////////////
    // Interactors

    /**
     * Answers the question: "Has the given interaction got only interactors of the given type ?".
     * <p/>
     * note: an interaction without interactor is not valid.
     *
     * @param interaction the interaction.
     * @param type        the interactor type.
     *
     * @return true if all interactor are of the given type, false otherwise.
     */
    public static boolean hasOnlyInteractorOfType( Interaction interaction, CvInteractorType type ) {

        if ( type == null ) {
            throw new IllegalArgumentException( "You must give a non null CvInteractorType." );
        }

        if ( interaction == null ) {
            throw new IllegalArgumentException( "You must give a non null Interaction." );
        }

        if ( interaction.getComponents().isEmpty() ) {
            return false;
        }

        for ( Component component : interaction.getComponents() ) {
            Interactor interactor = component.getInteractor();
            if ( ! type.equals( interactor.getCvInteractorType() ) ) {
                return false;
            }
        }

        return true;
    }

    /**
     * Collect a distinct set of Interactor associated to an Interaction.
     *
     * @param interaction the interaction
     *
     * @return a non null Set of Interactor.
     */
    public static Set<Interactor> selectDistinctInteractors( Interaction interaction ) {

        Set<Interactor> interactors = new HashSet<Interactor>( interaction.getComponents().size() );

        for ( Component component : interaction.getComponents() ) {
            interactors.add( component.getInteractor() );
        }

        return interactors;
    }
}