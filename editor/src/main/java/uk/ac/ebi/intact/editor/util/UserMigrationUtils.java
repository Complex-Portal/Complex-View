package uk.ac.ebi.intact.editor.util;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import uk.ac.ebi.intact.core.users.model.Preference;
import uk.ac.ebi.intact.core.users.model.Role;
import uk.ac.ebi.intact.core.users.model.User;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Utility to import/export IntAct users to XML.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 1.0
 */
public class UserMigrationUtils {

    private static final String LOGIN_ATTRIBUTE = "login";
    private static final String PASSWORD_ATTRIBUTE = "password";
    private static final String EMAIL_ATTRIBUTE = "email";
    private static final String DISABLED_ATTRIBUTE = "disabled";
    private static final String FIRST_NAME_ATTRIBUTE = "firstName";
    private static final String LAST_NAME_ATTRIBUTE = "lastName";
    private static final String OPEN_ID_URL_ATTRIBUTE = "openIdUrl";
    private static final String KEY_ATTRIBUTE = "key";
    private static final String TRUE_VALUE = "true";

    private static final String USERS_TAG = "users";
    private static final String USER_TAG = "user";
    private static final String ROLES_TAG = "roles";
    private static final String ROLE_TAG = "role";
    private static final String PREFERENCES_TAG = "preferences";
    private static final String PREFERENCE_TAG = "preference";

    public Document buildUsersDocument( Collection<User> users ) throws ParserConfigurationException {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder parser = factory.newDocumentBuilder();
        Document doc = parser.newDocument();

        Element root = doc.createElement( USERS_TAG );
        doc.appendChild( root );

        for ( User user : users ) {
            Element xmlUser = doc.createElement( USER_TAG );
            root.appendChild( xmlUser );

            // user
            if ( user.getLogin() != null ) xmlUser.setAttribute( LOGIN_ATTRIBUTE, user.getLogin() );
            if ( user.getPassword() != null ) xmlUser.setAttribute( PASSWORD_ATTRIBUTE, user.getPassword() );
            if ( user.getEmail() != null ) xmlUser.setAttribute( EMAIL_ATTRIBUTE, user.getEmail() );
            if ( user.isDisabled() ) xmlUser.setAttribute( DISABLED_ATTRIBUTE, TRUE_VALUE );
            if ( user.getFirstName() != null ) xmlUser.setAttribute( FIRST_NAME_ATTRIBUTE, user.getFirstName() );
            if ( user.getLastName() != null ) xmlUser.setAttribute( LAST_NAME_ATTRIBUTE, user.getLastName() );
            if ( user.getOpenIdUrl() != null ) xmlUser.setAttribute( OPEN_ID_URL_ATTRIBUTE, user.getOpenIdUrl() );

            // roles
            if ( !user.getRoles().isEmpty() ) {
                Element xmlRoles = doc.createElement( ROLES_TAG );
                xmlUser.appendChild( xmlRoles );

                for ( Role role : user.getRoles() ) {
                    Element xmlRole = doc.createElement( ROLE_TAG );
                    xmlRoles.appendChild( xmlRole );
                    xmlRole.setTextContent( role.getName() );
                }
            }

            // preferences
            if ( !user.getPreferences().isEmpty() ) {
                Element xmlPrefs = doc.createElement( PREFERENCES_TAG );
                xmlUser.appendChild( xmlPrefs );

                for ( Preference pref : user.getPreferences() ) {
                    Element xmlPref = doc.createElement( PREFERENCE_TAG );
                    xmlPrefs.appendChild( xmlPref );
                    xmlPref.setAttribute( KEY_ATTRIBUTE, pref.getKey() );
                    xmlPref.setTextContent( pref.getValue() );
                }
            }
        }

        return doc;
    }

    public Collection<User> readUsersDocument( Document document ) {

        Collection<User> users = new ArrayList<User>();

        // convert document to users
        NodeList root = document.getElementsByTagName( USERS_TAG );
        final NodeList xmlUsers = root.item( 0 ).getChildNodes();
        for ( int i = 0; i < xmlUsers.getLength(); i++ ) {
            final Element xmlUser = ( Element ) xmlUsers.item( i );
            final User user = new User();
            users.add( user );

            user.setLogin( xmlUser.getAttribute( LOGIN_ATTRIBUTE ) );
            user.setPassword( xmlUser.getAttribute( PASSWORD_ATTRIBUTE ) );
            user.setEmail( xmlUser.getAttribute( EMAIL_ATTRIBUTE ) );
            final String disabled = xmlUser.getAttribute( DISABLED_ATTRIBUTE );
            user.setDisabled( ( disabled == null ? false : ( disabled.equals( TRUE_VALUE ) ? true : false ) ) );
            user.setFirstName( xmlUser.getAttribute( FIRST_NAME_ATTRIBUTE ) );
            user.setLastName( xmlUser.getAttribute( LAST_NAME_ATTRIBUTE ) );
            user.setOpenIdUrl( xmlUser.getAttribute( OPEN_ID_URL_ATTRIBUTE ) );

            // roles
            final NodeList rolesNode = xmlUser.getElementsByTagName( ROLES_TAG );
            if ( rolesNode != null && rolesNode.getLength() == 1 ) {
                for ( int r = 0; r < rolesNode.getLength(); r++ ) {
                    String role = rolesNode.item( r ).getTextContent();
                    user.addRole( new Role( role ) );
                }
            }

            // preferences
            final NodeList preferencesNode = xmlUser.getElementsByTagName( PREFERENCES_TAG );
            if ( preferencesNode.getLength() > 0 ) {
                final NodeList xmlPrefs = preferencesNode.item( 0 ).getChildNodes();
                for ( int p = 0; p < xmlPrefs.getLength(); p++ ) {
                    final Element xmlPref = ( Element ) xmlPrefs.item( p );
                    assert ( xmlPref.getNodeName().equals( PREFERENCE_TAG ) );
                    String key = xmlPref.getAttribute( KEY_ATTRIBUTE );
                    String value = xmlPref.getTextContent();

                    Preference pr = new Preference( user, key );
                    pr.setValue( value );

                    user.getPreferences().add( pr );
                }
            }
        }

        return users;
    }

    public void exportUsers( Collection<User> users, OutputStream os ) throws Exception {

        Document doc = buildUsersDocument( users );

        OutputFormat format = new OutputFormat( doc );
        XMLSerializer output = new XMLSerializer( os, format );
        output.serialize( doc );
    }

    public Collection<User> importUsers( InputStream userInputStream ) throws Exception {

        DocumentBuilderFactory dBF = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = dBF.newDocumentBuilder();
        // builder.setErrorHandler(new MyErrorHandler());
        InputSource is = new InputSource( userInputStream );
        Document doc = builder.parse( is );

        return readUsersDocument( doc );
    }
}
