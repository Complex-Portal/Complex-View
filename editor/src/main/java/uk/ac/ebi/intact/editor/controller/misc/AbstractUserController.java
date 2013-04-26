package uk.ac.ebi.intact.editor.controller.misc;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.editor.controller.JpaAwareController;
import uk.ac.ebi.intact.editor.controller.curate.institution.InstitutionService;
import uk.ac.ebi.intact.model.Institution;
import uk.ac.ebi.intact.model.user.Preference;
import uk.ac.ebi.intact.model.user.User;
import uk.ac.ebi.intact.model.util.UserUtils;

import java.util.ArrayList;

/**
 * Controller used when administrating users.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 2.0
 */
public abstract class AbstractUserController extends JpaAwareController {

    private static final Log log = LogFactory.getLog(AbstractUserController.class);

    private static final String CURATION_DEPTH = "curation.depth";
    public static final String RAW_NOTES = "editor.notes";
    public static final String GOOGLE_USERNAME = "google.username";

    public static final String INSTITUTION_AC = "editor.institution.ac";
    public static final String INSTITUTION_NAME = "editor.institution.name";

    private User user;

    /////////////////
    // Users

    public String getInstitutionNameForUser(User user) {
        return findPreference(user, INSTITUTION_NAME, null);
    }

    public String getInstitutionAcForUser(User user) {
        return findPreference(user, INSTITUTION_AC, null);
    }


    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getCurationDepth() {
        return findPreference(CURATION_DEPTH, getEditorConfig().getDefaultCurationDepth());
    }

    public void setCurationDepth(String curationDepth) {
        setPreference(CURATION_DEPTH, curationDepth);
    }

    public String getRawNotes() {
        return findPreference(RAW_NOTES, null);
    }

    public void setRawNotes(String notes) {
        setPreference(RAW_NOTES, notes);
    }

    public String getGoogleUsername() {
        return findPreference(GOOGLE_USERNAME, null);
    }

    public void setGoogleUsername(String notes) {
        setPreference(GOOGLE_USERNAME, notes);
    }

    public Institution getInstitution() {
        return getInstitution(getUser());
    }

    public Institution getInstitution(User user) {
        String ac = findPreference(user, INSTITUTION_AC, null);

        if (ac != null) {
            InstitutionService institutionService = (InstitutionService) getSpringContext().getBean("institutionService");
            return institutionService.findInstitutionByAc(ac);
        }

        return null;
    }

    public void setInstitution(Institution institution) {
        if (institution != null) {
            setPreference(INSTITUTION_AC, institution.getAc());
            setPreference(INSTITUTION_NAME, institution.getShortLabel());
        }
    }

    public User getMentorReviewer() {
        return UserUtils.getMentorReviewer(getIntactContext(), getUser());
    }

    public void setMentorReviewer(User mentor) {
        UserUtils.setMentorReviewer(user, mentor);
    }

    private String findPreference(String prefKey) {
        return findPreference(getUser(), prefKey, null);
    }

    private String findPreference(String prefKey, String defaultValue) {
        return findPreference(getUser(), prefKey, defaultValue);
    }

    private String findPreference(User user, String prefKey, String defaultValue) {
        if (user.getPreferences() == null) {
            user.setPreferences(new ArrayList<Preference>());
        }

        for (Preference pref : user.getPreferences()) {
            if (prefKey.equals(pref.getKey())) {
                return pref.getValue();
            }
        }
        return defaultValue;
    }

    private void setPreference(String prefKey, String prefValue) {
        if (user.getPreferences() == null) {
            user.setPreferences(new ArrayList<Preference>());
        }

        Preference preference = null;

        for (Preference pref : user.getPreferences()) {
            if (prefKey.equals(pref.getKey())) {
                preference = pref;
            }
        }

        if (preference == null) {
            preference = new Preference(user, prefKey);
            user.getPreferences().add(preference);
        }

        preference.setValue(prefValue);
    }


}
