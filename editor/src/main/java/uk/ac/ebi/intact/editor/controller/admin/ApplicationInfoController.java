package uk.ac.ebi.intact.editor.controller.admin;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.PlatformTransactionManager;
import uk.ac.ebi.intact.core.config.ConfigurationHandler;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.core.persistence.dao.DbInfoDao;
import uk.ac.ebi.intact.core.util.DebugUtil;
import uk.ac.ebi.intact.editor.controller.JpaAwareController;
import uk.ac.ebi.intact.model.Institution;
import uk.ac.ebi.intact.model.meta.Application;
import uk.ac.ebi.intact.model.meta.ApplicationProperty;
import uk.ac.ebi.intact.model.meta.DbInfo;
import uk.ac.ebi.kraken.uuw.services.remoting.UniProtJAPI;

import javax.annotation.PostConstruct;
import javax.faces.event.ActionEvent;
import javax.sql.DataSource;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller("applicationInfo")
@Lazy
public class ApplicationInfoController extends JpaAwareController {

    private String uniprotJapiVersion;
    private String schemaVersion;
    private String lastUniprotUpdate;
    private String lastCvUpdate;
    private String databaseCounts;

    private Application application;

    public ApplicationInfoController() {
    }

    @PostConstruct
    public void init() {
        uniprotJapiVersion = UniProtJAPI.factory.getVersion();

        final DbInfoDao infoDao = IntactContext.getCurrentInstance().getDaoFactory().getDbInfoDao();

        schemaVersion = getDbInfoValue(infoDao, DbInfo.SCHEMA_VERSION);
        lastUniprotUpdate = getDbInfoValue(infoDao, DbInfo.LAST_PROTEIN_UPDATE);
        lastCvUpdate = getDbInfoValue(infoDao, DbInfo.LAST_CV_UPDATE_PSIMI);

        ByteArrayOutputStream baos = new ByteArrayOutputStream(256);
        PrintStream ps = new PrintStream(baos);
        DebugUtil.printDatabaseCounts(ps);

        databaseCounts = baos.toString().replaceAll("\n","<br/>");

        application = IntactContext.getCurrentInstance().getApplication();
    }

    public void saveApplicationProperties(ActionEvent evt) {
        for (ApplicationProperty prop : application.getProperties()) {
            getDaoFactory().getApplicationPropertyDao().saveOrUpdate(prop);
        }

        ConfigurationHandler configurationHandler = getConfigurationHandler();
        configurationHandler.loadConfiguration(getApplication());

        addInfoMessage("Preferences saved", "");
    }

    public void persistConfig(ActionEvent evt) {
        getConfigurationHandler().persistConfiguration();
        application = getDaoFactory().getApplicationDao().getByAc(application.getAc());
    }

    private String getDbInfoValue(DbInfoDao infoDao, String key) {
        String value;
        DbInfo dbInfo = infoDao.get(key);
        if (dbInfo != null) {
            value = dbInfo.getValue();
        } else {
            value = "<unknown>";
        }
        return value;
    }

    public List<Institution> getAvailableInstitutions() {        
        return getDaoFactory().getInstitutionDao().getAll();
    }

    public List<Map.Entry<String,DataSource>> getDataSources() {
        return new ArrayList<Map.Entry<String,DataSource>>(
                IntactContext.getCurrentInstance().getSpringContext().getBeansOfType(DataSource.class).entrySet());
    }

    public List<Map.Entry<String,PlatformTransactionManager>> getTransactionManagers() {
        return new ArrayList<Map.Entry<String,PlatformTransactionManager>>(
                IntactContext.getCurrentInstance().getSpringContext().getBeansOfType(PlatformTransactionManager.class).entrySet());
    }

    public String[] getBeanNames() {
        return IntactContext.getCurrentInstance().getSpringContext().getBeanDefinitionNames();
    }

    public String getUniprotJapiVersion() {
        return uniprotJapiVersion;
    }

    public List<String> getSystemPropertyNames() {
        return new ArrayList<String>(System.getProperties().stringPropertyNames());
    }

    public String getSystemProperty(String propName) {
        return System.getProperty(propName);
    }

    public String getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(String schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    public String getLastUniprotUpdate() {
        return lastUniprotUpdate;
    }

    public void setLastUniprotUpdate(String lastUniprotUpdate) {
        this.lastUniprotUpdate = lastUniprotUpdate;
    }

    public String getLastCvUpdate() {
        return lastCvUpdate;
    }

    public void setLastCvUpdate(String lastCvUpdate) {
        this.lastCvUpdate = lastCvUpdate;
    }

    public String getDatabaseCounts() {
        return databaseCounts;
    }

    public void setDatabaseCounts(String databaseCounts) {
        this.databaseCounts = databaseCounts;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    private ConfigurationHandler getConfigurationHandler() {
        return (ConfigurationHandler) getSpringContext().getBean("configurationHandler");
    }
}
