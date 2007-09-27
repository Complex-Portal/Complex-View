/*
Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.editor.struts.framework.util;

import uk.ac.ebi.intact.model.AnnotatedObject;
import uk.ac.ebi.intact.model.Editable;
import uk.ac.ebi.intact.model.AnnotatedObjectImpl;
import uk.ac.ebi.intact.persistence.util.CgLibUtil;

import java.io.*;
import java.lang.reflect.Modifier;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This utility class generates EditorTopics.properties file using the
 * classes in the AnnotatedObject package. An editiable class must follow
 * the following conditions<ul>:
 * <li>It must implement the Editable interface.</li>
 * <li>It must have AnnotatedObject has it super class in the class hierarchy</li>
 * <li>It must not be an abstract class or an interface.</li>
 * </ul>
 * The property key is the abbreviated class name and full class name is the
 * value. For example,
 * uk.ac.ebi.intact.model.CvTopic saved under CvTopic.
 *
 * <b>Note</b> This program assumes that all the editable objects
 * are in the package of AnnotatedObject and classes already exist in this
 * location (ie., compile sources before running this tool).
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 */
public class EditorTopicsGenerator {

    protected static final Log LOGGER = LogFactory.getLog(EditorTopicsGenerator.class);

    /**
     * The root of the classpath. The package uk.ac.ebi.intact.model
     * should be able to access from this value.
     */
    private String myRoot;

    /**
     * The name of the resource file to write Intact types.
     */
    private String myResourceName;

    /**
     * Constructor that accepts root to the class directory to search for
     * AnnotatedObject and the destination to write the properties.
     *
     * @param root the root of the classpath to access AnnotatedObject.
     * @param dest the name of the resource file to write Intact editor types.
     * The postfix '.properties' is appended to the name if it is not present.
     * This shouldn't be null.
     */
    public EditorTopicsGenerator(String root, String dest) {
        myRoot = root;
        // Check for properties extension.
        myResourceName = dest.endsWith(".properties") ? dest :
                dest + ".properties";
    }

    // Instance Methods

    /**
     * This method does the actual writing of the resource file.
     * @throws ClassNotFoundException unable to find a class.
     * @throws IOException for errors in writing to the properties file.
     */
    public void doIt() throws ClassNotFoundException, IOException {
        // The super class for all the editable objects.
        Class superClazz = AnnotatedObject.class;

        // The name of the super's package.
        String packname = superClazz.getPackage().getName();

        // The path to the super class from the root.
        String path = myRoot + packageToFile(packname);

        // Filter to include only the classes.
        FilenameFilter filter = new FilenameFilter() {
            // Implements FilenameFiler interface.
            public boolean accept(File dir, String name) {
                // Only accept the classes.
                if (name.endsWith(".class")) {
                    return true;
                }
                return false;
            }
        };
        // List of files in the package matching the filter.
        String[] files = (new File(path)).list(filter);

        // The proprties to hold CV class names; the key is the CV class name
        // and the value is the full classpath name.
        Properties props = new Properties();

        for (int i = 0; i < files.length; i++) {
            String classname = stripExtension(files[i]);
            String fullname = packname + "." + classname;
            Class clazz = Class.forName(fullname);

            if (!implementsEditable(clazz)) {
                // Does not implement the ditable interface.
                continue;
            }
            if (!hasAnnotatedObjetAsSuper(clazz)) {
                // Has no AnnotatedObject as super.
                continue;
            }
            if (isAbstractOrInterface(clazz)) {
                // An abstract or an interface.
                continue;
            }

            classname = CgLibUtil.getDisplayableClassName( classname );

            props.put(classname, fullname);
        }
        writeToProperties(props);
    }

    // Helper Methods

    /**
     * True if the given class implements the Editable interface.
     * @param clazz the class object to check.
     * @return <code>triue</code> if <code>clazz</code> implements
     * {@link uk.ac.ebi.intact.model.Editable} interface.
     */
    private boolean implementsEditable(Class clazz) {
        Class editClass = Editable.class;
        Class[] intfs = clazz.getInterfaces();
        for (int i = 0; i < intfs.length; i++) {
            if (intfs[i].equals(editClass)) {
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @param clazz the class object to check.
     * @return true if <code>clazz</code>is derived from
     * AnnotatedObject (super class); false is for otherwise.
     */
    private boolean hasAnnotatedObjetAsSuper(Class clazz) {
        Class superclass = clazz.getSuperclass();

        // Assume we hvaen't found the super class.
        boolean superFound = false;
        // Loop till Object is reached or found AnnotatedObject in the
        // class hierarchy.
        while (!superclass.equals(Object.class)) {
            if (superclass.equals(AnnotatedObjectImpl.class)) {
                // Found the annotated object as the super class.
                superFound = true;
                break;
            }
            superclass = superclass.getSuperclass();
        }
        // Found a type but not inhertited from AnnotatedObject.
        if (!superFound) {
            return false;
        }
        return true;
    }

    /**
     * @param clazz the class object to check.
     * @return true if <code>clazz</code> is an abstract or an interface.
     */
    private boolean isAbstractOrInterface(Class clazz) {
        // AnnotatedObject is the super class. Check for abstract classes.
        int modifiers = clazz.getModifiers();
        // Must be a non abstract and non interface.
        if (Modifier.isAbstract(modifiers) || Modifier.isInterface(modifiers)) {
            return true;
        }
        return false;
    }

    /**
     * Converts a given package name to the platform dependent file path.
     * @param packName the name of the package.
     * @return the patform dependent file path.
     */
    private String packageToFile(String packName) {
        // The platform dependent file separator.
        char sep = System.getProperty("file.separator").charAt(0);

        // Replace all the package separators with file separators.
        return sep + packName.replace(".".charAt(0), sep) + sep;
    }

    /**
     * Strips extension bit from a filename.
     * @param filename the name of the file.
     * @return the filename after removing the extension; if there is no
     * extension then this equals to <tt>filename</tt>.
     */
    private String stripExtension(String filename) {
        // The index position to chop off the .class extension.
        int pos = filename.indexOf('.');

        // Only remove an extension if we have one.
        if (pos != -1) {
            return filename.substring(0, pos);
        }
        return filename;
    }

    /**
     * Writes the given properties contents to a properties file.
     * @param props the properties file to write contents.
     * @exception IOException throws for any I/O errors.
     */
    private void writeToProperties(Properties props) throws IOException {
        // The output stream to write the props contents.
        BufferedOutputStream out = null;
        try {
            out = new BufferedOutputStream(new FileOutputStream(myResourceName));
            props.store(out, "Editor Topics");
        }
        finally {
            // Ensure that we close the stream regardless.
            if (out != null) {
                try {
                    out.close();
                }
                catch (IOException ioe) {
                    LOGGER.error("", ioe);
                }
            }
        }
    }

    // ------------------------------------------------------------------------

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println(
                    "usage: EditorTopicsGenerator {class root} {property file to write}");
            return;
        }
        EditorTopicsGenerator topicsGen = new EditorTopicsGenerator(args[0], args[1]);
        try {
            topicsGen.doIt();
            System.out.println("Wrote to " + args[1]);
        }
        catch (Exception ex) {
            LOGGER.error("", ex);
            ex.printStackTrace();
        }
    }
}
