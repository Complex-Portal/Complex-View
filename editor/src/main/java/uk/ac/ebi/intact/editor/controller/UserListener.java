package uk.ac.ebi.intact.editor.controller;

import uk.ac.ebi.intact.core.users.model.User;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public interface UserListener {

    void userLoggedIn(User user);

    void userLoggedOut(User user);

}
