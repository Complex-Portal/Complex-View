/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.confidence.model;

import java.util.Collection;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.business.IntactTransactionException;
import uk.ac.ebi.intact.config.impl.AbstractHibernateDataConfig;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.Component;
import uk.ac.ebi.intact.model.Interaction;
import uk.ac.ebi.intact.model.Interactor;
import uk.ac.ebi.intact.model.InteractorXref;
import uk.ac.ebi.intact.model.Protein;
import uk.ac.ebi.intact.model.util.ProteinUtils;
import uk.ac.ebi.intact.persistence.dao.DaoFactory;
import uk.ac.ebi.intact.persistence.dao.InteractionDao;

/**
 * TODO comment this
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version
 * @since <pre>14-Aug-2007</pre>
 */
public class DBcon {
	public static void main(String[] args) {
		  DaoFactory daoFactory = IntactContext.getCurrentInstance().getDataContext().getDaoFactory();

	       InteractionDao interactionDao = daoFactory.getInteractionDao();
	       Interaction interaction = interactionDao.getByAc("EBI-987097");
	       System.out.println("Shortlabel : " +interaction.getShortLabel());
	       Collection<Component> components = interaction.getComponents();
	       for (Component comp : components){
	           Interactor interactor = comp.getInteractor();
	            //this is because an interactor could be a small molecule, you want to make sure you have a protein
	           if(Protein.class.isAssignableFrom(Interactor.class)){
	               Protein protein = (Protein) interactor;
	               String seq = protein.getSequence();
	               InteractorXref uniprotXref = ProteinUtils.getUniprotXref(protein);
	               String uniprotPrymaryAc = uniprotXref.getPrimaryId();
	           }
	       }

	       try {
	                   IntactContext.getCurrentInstance().getDataContext().commitTransaction();
	               } catch (IntactTransactionException e) {
	                   //If commiting the transaction failed (ex : a shortlabel was longer then 20 characters), try to rollback.
	                   try {
	                       IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getCurrentTransaction().rollback();
	                   } catch (IntactTransactionException e1) {
	                       // If rollback was not successfull do what you want : printStackTrace, throw Exception...
	                       throw new IntactException("Problem at commit time, couldn't rollback : " + e1);
	                   }
	                   // If commit is it could not commit do what you want : printStackTrace, throw Exception...
	                   throw new IntactException("Problem at commit time, rollback done : " + e);
	               }finally{
	                   // Commiting the transaction close as well the session if everything goes fine but in case of an exception
	                   // sent at commit time then the session would not be closed, so it's really important that you close it here
	                   // otherwise you might get again this fishy connection and have very nasty bugs.
	                   Session hibernateSession = getSession();
	                   if ( hibernateSession.isOpen() ) {
	                       hibernateSession.close();
	                   }
	               } 
	}
	
	private static Session getSession() {
	       AbstractHibernateDataConfig abstractHibernateDataConfig = ( AbstractHibernateDataConfig ) IntactContext.getCurrentInstance().getConfig().getDefaultDataConfig();
	       SessionFactory factory = abstractHibernateDataConfig.getSessionFactory();
	       return factory.getCurrentSession();
	   } 
}
