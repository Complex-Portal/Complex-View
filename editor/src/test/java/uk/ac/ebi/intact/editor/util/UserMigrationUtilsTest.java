package uk.ac.ebi.intact.editor.util;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Assert;
import org.junit.Test;
import uk.ac.ebi.intact.core.users.model.Preference;
import uk.ac.ebi.intact.core.users.model.Role;
import uk.ac.ebi.intact.core.users.model.User;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

/**
 * UserMigrationUtils Tester.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 1.0
 */
public class UserMigrationUtilsTest {

    public File getParent() {
        final URL resource = UserMigrationUtilsTest.class.getResource( "" );
        return new File( resource.getFile() ).getParentFile();
    }

    private void assertUserEquals( User sam, User copy ) {
        Assert.assertEquals( copy.getLogin(), sam.getLogin() );
        Assert.assertEquals( copy.getPassword(), sam.getPassword() );
        Assert.assertEquals( copy.getEmail(), sam.getEmail() );
        Assert.assertEquals( copy.getOpenIdUrl(), sam.getOpenIdUrl() );
        Assert.assertEquals( copy.getFirstName(), sam.getFirstName() );
        Assert.assertEquals( copy.getLastName(), sam.getLastName() );

        Assert.assertTrue( CollectionUtils.isEqualCollection( copy.getRoles(), sam.getRoles() ) );
        Assert.assertTrue( CollectionUtils.isEqualCollection( copy.getPreferences(), sam.getPreferences() ) );
    }

    private User createSam() {
        User sam = new User( "skerrien", "Samuel", "Kerrien", "skerrien@example.com" );
        sam.setPassword( "############" );
        sam.setOpenIdUrl( "http://www.google.com" );
        sam.setDisabled( true );

        Role role = new Role( "ADMIN" );
        sam.addRole( role );

        Preference p = new Preference( sam, "note" );
        p.setValue( "some\nimportant information..." );
        sam.getPreferences().add( p );
        return sam;
    }

    private User createBruno() {
        User bruno = new User( "baranda", "Bruno", "Aranda", "baranda@example.com" );
        bruno.setPassword( "***************" );
        bruno.setOpenIdUrl( "http://www.yahoo.com" );
        bruno.setDisabled( false );

        Role role = new Role( "CURATOR" );
        bruno.addRole( role );

        Preference p = new Preference( bruno, "note" );
        p.setValue( "lalalalalalalala" );
        bruno.getPreferences().add( p );
        return bruno;
    }

    private User createMarine() {
        User marine = new User( "marine", "Marine", "Dumousseau", "marine@example.com" );
        marine.setPassword( "xxxxxxxxxxxxxxxx" );
        marine.setOpenIdUrl( "http://www.wanadoo.fr" );
        marine.setDisabled( false );
        return marine;
    }

    @Test
    public void complexSingleUser() throws Exception {

        User sam = createSam();

        final UserMigrationUtils migrationUtils = new UserMigrationUtils();
        final File output = new File( getParent(), "user.xml" );
        migrationUtils.exportUsers( Arrays.asList( sam ), new FileOutputStream( output ) );

        InputStream is = new FileInputStream( output );
        final Collection<User> users = migrationUtils.importUsers( is );
        User copy = users.iterator().next();

        assertUserEquals( sam, copy );
    }

    @Test
    public void threeUsers() throws Exception {

        User sam = createSam();
        User bruno = createBruno();
        User marine = createMarine();

        final UserMigrationUtils migrationUtils = new UserMigrationUtils();
        final File output = new File( getParent(), "users.xml" );
        migrationUtils.exportUsers( Arrays.asList( sam, bruno, marine ), new FileOutputStream( output ) );

        InputStream is = new FileInputStream( output );
        final Collection<User> users = migrationUtils.importUsers( is );
        Assert.assertNotNull( users );
        Assert.assertEquals( 3, users.size() );
        final Iterator<User> iterator = users.iterator();
        User copySam = iterator.next();
        User copyBruno = iterator.next();
        User copyMarine = iterator.next();

        assertUserEquals( sam, copySam );
        assertUserEquals( bruno, copyBruno );
        assertUserEquals( marine, copyMarine);
    }
}
