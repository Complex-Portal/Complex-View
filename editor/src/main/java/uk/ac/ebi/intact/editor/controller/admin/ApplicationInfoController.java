package uk.ac.ebi.intact.editor.controller.admin;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.core.persistence.dao.DbInfoDao;
import uk.ac.ebi.intact.model.meta.DbInfo;
import uk.ac.ebi.kraken.uuw.services.remoting.UniProtJAPI;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller("applicationInfo")
@Lazy
public class ApplicationInfoController {

    private String uniprotJapiVersion;
    private String schemaVersion;
    private String lastUniprotUpdate;
    private String lastCvUpdate;

    public ApplicationInfoController() {
    }

    @PostConstruct
    public void init() {
        uniprotJapiVersion = UniProtJAPI.factory.getVersion();

        final DbInfoDao infoDao = IntactContext.getCurrentInstance().getDaoFactory().getDbInfoDao();

        schemaVersion = getDbInfoValue(infoDao, DbInfo.SCHEMA_VERSION);
        lastUniprotUpdate = getDbInfoValue(infoDao, DbInfo.LAST_PROTEIN_UPDATE);
        lastCvUpdate = getDbInfoValue(infoDao, DbInfo.LAST_CV_UPDATE_PSIMI);
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
}
