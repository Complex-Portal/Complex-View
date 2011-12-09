package uk.ac.ebi.intact.editor;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.jdbc.datasource.init.DatabasePopulator;
import org.springframework.stereotype.Controller;
import psidev.psi.mi.tab.PsimiTabWriter;
import psidev.psi.mi.tab.converter.xml2tab.TabConversionException;
import psidev.psi.mi.tab.converter.xml2tab.Xml2Tab;
import psidev.psi.mi.tab.expansion.SpokeWithoutBaitExpansion;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.xml.PsimiXmlReader;
import psidev.psi.mi.xml.converter.ConverterException;
import psidev.psi.mi.xml.model.EntrySet;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.core.lifecycle.LifecycleManager;
import uk.ac.ebi.intact.core.persistence.dao.DaoFactory;
import uk.ac.ebi.intact.core.persister.CorePersister;
import uk.ac.ebi.intact.core.unit.IntactMockBuilder;
import uk.ac.ebi.intact.dataexchange.cvutils.CvUpdater;
import uk.ac.ebi.intact.dataexchange.psimi.xml.exchange.PsiExchange;
import uk.ac.ebi.intact.model.Publication;
import uk.ac.ebi.intact.model.user.User;
import uk.ac.ebi.intact.psimitab.IntactPsimiTabWriter;
import uk.ac.ebi.intact.psimitab.IntactXml2Tab;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
@DependsOn("intactInitializer")
public class DataPopulator implements InitializingBean {

    @Autowired
    private IntactContext intactContext;
    
    @Autowired
    private CorePersister corePersister;

    @Autowired
    private DaoFactory daoFactory;

    @Autowired
    private CvUpdater cvUpdater;

    @Autowired
    private LifecycleManager lifecycleManager;


    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("POPULATING DATA...");

        User admin = daoFactory.getUserDao().getByLogin("admin");
        intactContext.getUserContext().setUser(admin);

        cvUpdater.executeUpdateWithLatestCVs();

        IntactMockBuilder mockBuilder = new IntactMockBuilder(intactContext.getInstitution());
        
        Publication publicationRandom = mockBuilder.createPublicationRandom();
        lifecycleManager.getNewStatus().claimOwnership(publicationRandom);
        lifecycleManager.getAssignedStatus().startCuration(publicationRandom);
        corePersister.saveOrUpdate(publicationRandom);


    }


}
