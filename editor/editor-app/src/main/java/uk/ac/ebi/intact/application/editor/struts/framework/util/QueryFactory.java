/*
 Copyright (c) 2002-2005 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.editor.struts.framework.util;

import uk.ac.ebi.intact.model.Alias;
import uk.ac.ebi.intact.model.ExperimentXref;
import uk.ac.ebi.intact.model.InteractorAlias;
import uk.ac.ebi.intact.persistence.dao.XrefDao;
import uk.ac.ebi.intact.persistence.dao.DaoFactory;
import uk.ac.ebi.intact.persistence.dao.AliasDao;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.application.editor.util.DaoProvider;

import java.util.Collection;
import java.util.ArrayList;

/**
 * This factory class builds queries for the editor.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 */
public class QueryFactory {

    /** Only instance of this class */
    private static final QueryFactory OUR_INSTANCE = new QueryFactory();

    /**
     * @return returns the only instance of this class.
     */
    public static final QueryFactory getInstance() {
        return OUR_INSTANCE;
    }


    /**
     * Returns the query to get gene names for a Protein
     * @param parent the AC of the parent (AC of the Protein)
     * @return the query to extract the gene name for given protein AC
     */
    public Collection<InteractorAlias> getGeneNameQuery(String aliasAc, String parent) {

        Collection<InteractorAlias> aliasesToReturn = new ArrayList<InteractorAlias>();
        AliasDao aliasDao = DaoProvider.getDaoFactory().getAliasDao(InteractorAlias.class);
        Collection<InteractorAlias> geneNames = aliasDao.getColByPropertyName("parentAc", parent);
        for(InteractorAlias alias : geneNames ){
            if(alias.getCvAliasType() != null && alias.getCvAliasType().getAc().equals(aliasAc)){
                aliasesToReturn.add(alias);
            }
        }
        return aliasesToReturn;
    }

    /**
     * Returns the query to get the primary xref qualifier
     * @param qualifier the AC of the primary xref qualifier.
     * @param parent the AC of the parent
     * @return the query to extract the primary Xref with given parent AC
     */
    public ExperimentXref getQualifierXrefQuery(String qualifier, String parent) {


        Collection<ExperimentXref> experimentXrefToReturn = new ArrayList<ExperimentXref>();

        XrefDao<ExperimentXref> xrefDao = DaoProvider.getDaoFactory().getXrefDao(ExperimentXref.class);

        Collection<ExperimentXref> experimentXrefs = xrefDao.getByParentAc(parent);
        for(ExperimentXref experimentXref : experimentXrefs){
            if(experimentXref.getCvXrefQualifier() != null && experimentXref.getCvXrefQualifier().getAc().equals(qualifier)){
                experimentXrefToReturn.add(experimentXref);
            }
        }

        if (experimentXrefToReturn.size() > 1){
            throw new IntactException("Find more then one experiment xref with parent_ac = " + parent +
                    " and qualifier " + qualifier );
        }else{
             for (ExperimentXref experimentXref : experimentXrefToReturn){
                 return experimentXref;
             }
            return null;
        }
    }
}
