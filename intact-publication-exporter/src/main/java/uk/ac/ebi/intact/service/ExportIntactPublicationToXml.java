package uk.ac.ebi.intact.service;

import org.springframework.transaction.TransactionStatus;
import psidev.psi.mi.tab.PsimiTabWriter;
import psidev.psi.mi.tab.model.builder.PsimiTabVersion;
import psidev.psi.mi.xml.PsimiXmlForm;
import psidev.psi.mi.xml.PsimiXmlLightweightWriter;
import psidev.psi.mi.xml.PsimiXmlVersion;
import psidev.psi.mi.xml.converter.ConverterContext;
import psidev.psi.mi.xml.model.Source;
import uk.ac.ebi.intact.core.context.DataContext;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.core.persistence.dao.DaoFactory;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.shared.InstitutionConverter;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.shared.InteractionConverter;
import uk.ac.ebi.intact.model.Interaction;
import uk.ac.ebi.intact.model.Publication;

import javax.persistence.Query;
import java.io.File;
import java.util.List;

/**
 * Export an Intact publication to xml
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>23/07/12</pre>
 */

public class ExportIntactPublicationToXml {

    public static void main(String[] args) throws Exception {
        String pubId=null;

        if (args.length != 1) {
            System.err.println("Usage: ExportIntactPublicationToXml <publication id>");
            System.exit(1);
        } else {
            pubId = args[0];

            System.out.println("--------------------------");
            System.out.println("Publication identifier: "+pubId);
            System.out.println("--------------------------");
        }

        final DataContext dataContext = IntactContext.getCurrentInstance().getDataContext();
        final DaoFactory daoFactory = dataContext.getDaoFactory();

        TransactionStatus transactionStatus = dataContext.beginTransaction();

        Publication publication = daoFactory.getPublicationDao().getByPubmedId(pubId);

        if (publication == null){
            throw new IllegalArgumentException("The publication " + pubId + " does not exist in the database.");
        }

        InstitutionConverter institutionConverter = new InstitutionConverter();
        InteractionConverter interactionConverter = new InteractionConverter(null);

        File outputFile = new File(pubId+".xml");
        PsimiXmlLightweightWriter xmlWriter = new PsimiXmlLightweightWriter(outputFile, PsimiXmlVersion.VERSION_254);

        try{
            xmlWriter.writeStartDocument();

            String sql2 = "select distinct i from InteractionImpl i join i.experiments as e join e.publication as p " +
                    "where p.ac = :pubAc ";

            final Query query2 = daoFactory.getEntityManager().createQuery( sql2 );
            query2.setParameter("pubAc", publication.getAc());

            List<Interaction> interactions1 = query2.getResultList();

            System.out.println(interactions1.size() + " interactions to export.");

            uk.ac.ebi.intact.dataexchange.psimi.xml.converter.ConverterContext.getInstance().setDefaultInstitutionForAcs(interactions1.iterator().next().getOwner());
            uk.ac.ebi.intact.dataexchange.psimi.xml.converter.ConverterContext.getInstance().setGenerateExpandedXml(true);
            ConverterContext.getInstance().getConverterConfig().setXmlForm(PsimiXmlForm.FORM_EXPANDED);

            Source source = institutionConverter.intactToPsi(interactions1.iterator().next().getOwner());
            xmlWriter.writeStartEntry(source, null);

            for (Interaction interaction : interactions1){
                psidev.psi.mi.xml.model.Interaction convertedInteraction = interactionConverter.intactToPsi(interaction);

                xmlWriter.writeInteraction(convertedInteraction);
            }

            xmlWriter.writeEndEntry(null);

            xmlWriter.writeEndDocument();
        }
        finally{
            xmlWriter.closeOutputFile();
            dataContext.rollbackTransaction(transactionStatus);
        }
    }
}
