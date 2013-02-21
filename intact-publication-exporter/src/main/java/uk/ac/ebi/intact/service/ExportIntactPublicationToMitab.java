package uk.ac.ebi.intact.service;

import org.springframework.transaction.TransactionStatus;
import psidev.psi.mi.tab.PsimiTabWriter;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.builder.PsimiTabVersion;
import uk.ac.ebi.intact.core.context.DataContext;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.core.persistence.dao.DaoFactory;
import uk.ac.ebi.intact.model.Interaction;
import uk.ac.ebi.intact.model.Publication;
import uk.ac.ebi.intact.psimitab.converters.Intact2BinaryInteractionConverter;
import uk.ac.ebi.intact.psimitab.converters.expansion.ExpansionStrategy;
import uk.ac.ebi.intact.psimitab.converters.expansion.SpokeWithoutBaitExpansion;

import javax.persistence.Query;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * This class will export Intact publication to MITAB
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>23/07/12</pre>
 */

public class ExportIntactPublicationToMitab {

    public static void main(String[] args) throws Exception {

        String pubId=null;
        String version=null;
        PsimiTabWriter writer=null;

        if (args.length != 2) {
            System.err.println("Usage: ExportIntactPublicationToMitab <publication id> <mitab version>");
            System.exit(1);
        } else {
            pubId = args[0];
            version = args[1];

            System.out.println("--------------------------");
            System.out.println("Publication identifier: "+pubId);
            System.out.println("MITAB version: "+ version);
            System.out.println("--------------------------");

            if (version.equals("2.5")){
                writer = new PsimiTabWriter(PsimiTabVersion.v2_5);
            }
            else if (version.equals("2.6")){
                writer = new PsimiTabWriter(PsimiTabVersion.v2_6);
            }
            else if (version.equals("2.7")){
                writer = new PsimiTabWriter(PsimiTabVersion.v2_7);
            }
            else {
                throw new IllegalArgumentException("The version " + version + " is not a valid mitab version. It has to be 2.5, 2.6 or 2.7");
            }
        }

        IntactContext.initContext(new String[]{"/META-INF/jpa-exporter.spring.xml"});

        final DataContext dataContext = IntactContext.getCurrentInstance().getDataContext();
        final DaoFactory daoFactory = dataContext.getDaoFactory();

        TransactionStatus transactionStatus = dataContext.beginTransaction();

        Publication publication = daoFactory.getPublicationDao().getByPubmedId(pubId);

        if (publication == null){
            throw new IllegalArgumentException("The publication " + pubId + " does not exist in the database.");
        }

        File outputFile = new File(pubId+".txt");
        BufferedWriter outputFileWriter = new BufferedWriter(new FileWriter(outputFile));

        try{
            String sql2 = "select distinct i from InteractionImpl i join i.experiments as e join e.publication as p " +
                    "where p.ac = :pubAc ";

            final Query query2 = daoFactory.getEntityManager().createQuery( sql2 );
            query2.setParameter("pubAc", publication.getAc());

            List<Interaction> interactions1 = query2.getResultList();

            System.out.println(interactions1.size() + " interactions to export.");

            for (Interaction interaction : interactions1){
                Collection<BinaryInteraction> binaryInteractions = process(interaction);

                writer.write(binaryInteractions, outputFileWriter);
            }
        }
        finally {
            outputFileWriter.close();
            dataContext.rollbackTransaction(transactionStatus);
        }
    }

    public static Collection<BinaryInteraction> process(Interaction item) throws Exception {
        ExpansionStrategy expansionStategy = new SpokeWithoutBaitExpansion();

        Intact2BinaryInteractionConverter interactionConverter = new Intact2BinaryInteractionConverter(expansionStategy);

        if (item == null){
            return null;
        }

        Collection<BinaryInteraction> binaryInteractions = interactionConverter.convert(item);

        System.out.println("Processing interaction : " + item.getAc());

        return binaryInteractions;
    }
}
