package uk.ac.ebi.intact.editor.controller.misc;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.core.users.model.Preference;
import uk.ac.ebi.intact.core.users.model.User;
import uk.ac.ebi.intact.editor.controller.JpaAwareController;

import java.util.ArrayList;

/**
 * Controller used when administrating users.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 2.0
 */
public abstract class AbstractUserController extends JpaAwareController {

    private static final Log log = LogFactory.getLog( AbstractUserController.class );

    private static final String CURATION_DEPTH = "curation.depth";
    public static final String RAW_NOTES = "editor.notes";
    public static final String GOOGLE_USERNAME = "google.username";

    private User user;

    /////////////////
    // Users

    public User getUser() {
        return user;
    }

    public void setUser( User user ) {
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

    private String findPreference(String prefKey) {
        return findPreference(prefKey, null);
    }

    private String findPreference(String prefKey, String defaultValue) {
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
