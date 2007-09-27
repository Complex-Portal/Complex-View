/*
Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/

package uk.ac.ebi.intact.application.editor.struts.framework.util;

import org.apache.log4j.Logger;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.application.editor.util.CvHelper;
import uk.ac.ebi.intact.application.editor.util.DaoProvider;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.persistence.dao.AnnotatedObjectDao;
import uk.ac.ebi.intact.context.IntactContext;

import java.util.*;

/**
 * The menu factory to generate various menus.
 *
 * @author Sugath Mudali (smudali@ebi.ac.uk)
 * @version $Id$
 */
public class EditorMenuFactory {


//    protected static final Logger LOGGER = Logger.getLogger(EditorConstants.LOGGER);
    protected static final Log LOGGER = LogFactory.getLog(EditorMenuFactory.class);

    // Those variables are used when a called of the function getMenus(String EditorPageName) from the Class
    // AbstractEditViexBean is done in order to tell for which page this method is called to remove the relevant
    // cvTopic from the annotation section
    public static final String FEATURE_PAGE = "featurePage";

    public static final String SEQUENCE= "Sequence";

    public static final String CV_PAGE = "Cv";

    public static final String BIOSOURCE_PAGE = "BioSource";
    // Static data

    /**
     * The list item to indicate a selection is required. This item is always
     * displayed as the first item in the list.
     */
    public static final String SELECT_LIST_ITEM = "--- Select ---";

    /**
     * The names for the topic list.
     */
    public static final String TOPIC = "Topic";

    /**
     * The name for the database list.
     */
    public static final String DATABASE = "Database";

    /**
     * The name for the qualifier list.
     */
    public static final String QUALIFIER = "Qualifier";

    /**
     *  The name for the host oragnism list.
     */
    public static final String ORGANISM = "Organism";

    /**
     * The name for the interaction list.
     */
    public static final String INTERACTION = "Interaction";

    /**
     * The name for the identification list.
     */
    public static final String IDENTIFICATION = "Identification";

    /**
     * The name for the interaction type list.
     */
    public static final String INTERACTION_TYPE = "InteractionType";

    /**
     * The name for the experiment list.
     */
    public static final String EXPERIMENT = "Experiment";

    /**
     * The name for the role list.
     */
    public static final String EXPROLE = "ExpRole";

    public static final String BIOROLE = "BioRole";

    /**
     * The name for the CV cell list.
     */
    public static final String CELL = "Cell";

    /**
     * The name for the Tissue list.
     */
    public static final String TISSUE = "Tissue";

    /**
     * The name for the CvFeatureType list.
     */
    public static final String FEATURE_TYPE = "FeatureType";

    /**
     * The name for the CvFeatureIdentification list.
     */
    public static final String FEATURE_IDENTIFICATION = "FeatureIdentification";

    /**
     * The name for the Protein list (used for exemple by the function getMenu(String type) to filter the CvTopic menu
     */
    public static final String PROTEIN= "Protein";

    /**
     * The only instance of this class.
     */
    private static final EditorMenuFactory ourInstance = new EditorMenuFactory();

    /**
     * Maps: Menu Name -> Menu type. Common to all the users and it is immutable.
     */
    private static final Map<String,Class> ourNameToType = new HashMap<String,Class>();

    /**
     * The criteria to retrieve nucleic acid types
     */
//    private static final Criteria ourNucleicAcidCriteria = new Criteria();
    private static Collection<String> ourNucleicAcidMiRefs = new ArrayList<String>();
    /**
     * The criteria to retrieve protein types
     */
//    private static final Criteria ourProteinCriteria = new Criteria();
    private static Collection<String> ourProteinMiRefs = new ArrayList<String>();


    // Static initializer.

    static {
        // Fill the maps with list names and their associated classes.
        ourNameToType.put(TOPIC, CvTopic.class);
        ourNameToType.put(DATABASE, CvDatabase.class);
        ourNameToType.put(QUALIFIER, CvXrefQualifier.class);
        ourNameToType.put(ORGANISM, BioSource.class);
        ourNameToType.put(INTERACTION, CvInteraction.class);
        ourNameToType.put(IDENTIFICATION, CvIdentification.class);
        ourNameToType.put(INTERACTION_TYPE, CvInteractionType.class);
        ourNameToType.put(EXPERIMENT, Experiment.class);
        ourNameToType.put(EXPROLE, CvExperimentalRole.class);
        ourNameToType.put(BIOROLE, CvBiologicalRole.class);
        ourNameToType.put(CELL, CvCellType.class);
        ourNameToType.put(TISSUE, CvTissue.class);
        ourNameToType.put(FEATURE_TYPE, CvFeatureType.class);
        ourNameToType.put(FEATURE_IDENTIFICATION, CvFeatureIdentification.class);

        // Fill the criterias
        buildProteinAndNucleicAcidCriteria();
    }

    private static void buildProteinAndNucleicAcidCriteria(){
        CvHelper cvHelper = null;
        Collection<String> nucleicAcidMIs = new ArrayList();
        try {
            cvHelper = new CvHelper();

            // Get the interactor type menu list for the NucleicAcid - Editor
            CvInteractorType nucleicAcid = IntactContext.getCurrentInstance().getCvContext().getByMiRef(CvInteractorType.class, CvInteractorType.NUCLEIC_ACID_MI_REF);
            ourNucleicAcidMiRefs = cvHelper.getChildrenMiRefs(nucleicAcid, nucleicAcidMIs);
            ourNucleicAcidMiRefs.add(CvInteractorType.NUCLEIC_ACID_MI_REF);

            // Get the interactor type menu list for the Protein - Editor
            List<String> proteinMis = CvInteractorType.getProteinMIs();
            for( String proteinMi : proteinMis){
                ourProteinMiRefs.add(proteinMi);
            }

        } catch (IntactException e) {
            LOGGER.error("Problem trying to load the MI numbers for the CvInteractorType and children of protein " +
                    "and nucleic acid : ", e);
            e.printStackTrace();
        }
    }

    // No instantiation from outside.
    private EditorMenuFactory() {}

    /**
     * Returns the only instance of this class.
     */
    public static EditorMenuFactory getInstance() {
        return ourInstance;
    }

    /**
     * A utility method to normalizes a menu item
     * @param item the item to normalize.
     * @return if give <code>item</code> equals to {@link #SELECT_LIST_ITEM} null
     * is returned. For all other times, given <code>item</code> is returned.
     */
    public static String normalizeMenuItem(String item) {
        return SELECT_LIST_ITEM.equals(item) ? null : item;
    }

    /**
     * Converts the given list to add menu.
     * @param menu the menu to convert (not modified).
     * @return converted menu; {@link #SELECT_LIST_ITEM} is added as the first
     * item to the list.
     */
    public List<String> convertToAddMenu(List<String> menu) {
        // The menu to return.
        List<String> modMenu = new LinkedList<String>(menu);
        // The default value for add menu.
        String  defvalue = SELECT_LIST_ITEM;
        // Add as the first item in the list.
        modMenu.add(0, defvalue);
        return modMenu;
    }

    public List<String> convertToAddMenu(List<String> menu, String firstPositionCvShortlabel) {

        //todo:check firstPosistionShortlabel not null
        // The menu to return.
        Iterator<String> modMenuIterator = menu.iterator();
        while(modMenuIterator.hasNext()){
            String shortLabel = modMenuIterator.next();
            LOGGER.debug("shortLabel = " + shortLabel);
            if(firstPositionCvShortlabel.equals(shortLabel)){
                LOGGER.debug("removing " + shortLabel + " from list");
                //MODIF-2007-9-19
                //modMenuIterator.remove();
                //todo : if not found, send error message.
            }
        }
        List<String> modMenu = new LinkedList<String>(menu);
        // The default value for add menu.
        String  defvalue = SELECT_LIST_ITEM;
        // Add as the first item in the list.
        modMenu.add(0, defvalue);
        modMenu.add(1,firstPositionCvShortlabel);
        return modMenu;
    }

    /**
     * Returns a menu for given name.
     * @param key the name of the menu; the valid values are: {@link #TOPIC},
     * {@link #DATABASE}, {@link #QUALIFIER}, {@link #ORGANISM},
     * {@link #INTERACTION}, {@link #IDENTIFICATION}, {@link #INTERACTION_TYPE},
     * {@link #EXPERIMENT} and {@link #EXPROLE}.
     * @param mode 0 for and edit menu and 1 for an add menu; the difference is
     * {@link #SELECT_LIST_ITEM} is added as the first entry for an add menu.
     * @return a list of menu items for given <code>name</code>.
     * @throws IntactException for errors in contructing the menu or unable to
     * create an Intact helper to access persistent system.
     */
    public List<String> getMenu(String key, int mode) throws IntactException {
        // The class associated with the key.
        Class clazz = ourNameToType.get(key);
        List<String> menu = getMenuList(clazz);
        if (menu.isEmpty()) {
            // Special list when we don't have any menu items.
            menu.add(SELECT_LIST_ITEM);
            return menu;
        }
        if (mode == 1) {
            menu = convertToAddMenu(menu);
        }
        return menu;
    }

    public List<String> getMenu(String key, int mode, String firstCvShortlabel) throws IntactException {
        // The class associated with the key.
        Class clazz = ourNameToType.get(key);
        List<String> menu = getMenuList(clazz);
        Iterator iterator = menu.iterator();

        if (menu.isEmpty()) {
            // Special list when we don't have any menu items.
            menu.add(SELECT_LIST_ITEM);
            return menu;
        }
        if (mode == 1) {
            menu = convertToAddMenu(menu,firstCvShortlabel);
        }
        return menu;
    }

    /**
     * Returns a menu for given name.
     * @param key the name of the menu; the valid values are: {@link #TOPIC},
     * {@link #DATABASE}, {@link #QUALIFIER}, {@link #ORGANISM},
     * {@link #INTERACTION}, {@link #IDENTIFICATION}, {@link #INTERACTION_TYPE},
     * {@link #EXPERIMENT} and {@link #ROLE}.
     * @param editedClass, the class of the Object beeing edited, to know how to filter the menu (Experiment.class,
     * Protein.class, NucleicAcid.class, Feature class, Interaction class, Experiment class, CvObject class).
     * @param mode 0 for and edit menu and 1 for an add menu; the difference is
     * {@link #SELECT_LIST_ITEM} is added as the first entry for an add menu.
     * @return a list of menu items for given <code>name</code>.
     * @throws IntactException for errors in contructing the menu or unable to
     * create an Intact helper to access persistent system.
     */
    /**
     *
     * @param key
     * @param mode
     * @param editedClass
     * @return
     * @throws IntactException
     */
     public List<String> getTopicMenu(String key, int mode, Class editedClass) throws IntactException {
        // The class associated with the key.
        Class clazz = ourNameToType.get(key);
//        List<String> menu = getMenuList(clazz, menuClazz);
        List<String> menu = getTopicMenuList(clazz, editedClass);
        if (menu.isEmpty()) {
            // Special list when we don't have any menu items.
            menu.add(SELECT_LIST_ITEM);
            return menu;
        }
        if (mode == 1) {
            menu = convertToAddMenu(menu);
        }
        return menu;
    }

    /**
     * Returns a list of menu itsm for the NucleicAcid editor.
     * @param mode 0 for and edit menu and 1 for an add menu; the difference is
     * {@link #SELECT_LIST_ITEM} is added as the first entry for an add menu.
     * @return a list of menu items for hte NucleicAcid editor.
     * @throws IntactException for errors in contructing the menu or unable to
     * create an Intact helper to access persistent system.
     */
    public List<String> getNucleicAcidMenu(int mode) throws IntactException {
        CvHelper cvHelper = new CvHelper();
        CvInteractorType nucleicAcid = IntactContext.getCurrentInstance().getCvContext().getByMiRef(CvInteractorType.class, CvInteractorType.NUCLEIC_ACID_MI_REF);
        Collection<String> nucleicAcidMIs = new ArrayList<String>();
        ourNucleicAcidMiRefs = cvHelper.getChildrenMiRefs(nucleicAcid, nucleicAcidMIs);
                    ourNucleicAcidMiRefs.add(CvInteractorType.NUCLEIC_ACID_MI_REF);

        return getPolymerMenu(mode, ourNucleicAcidMiRefs);
    }

    /**
     * Returns a list of menu itsm for the Protein editor.
     * @param mode 0 for and edit menu and 1 for an add menu; the difference is
     * {@link #SELECT_LIST_ITEM} is added as the first entry for an add menu.
     * @return a list of menu items for hte Protein editor.
     * @throws IntactException for errors in contructing the menu or unable to
     * create an Intact helper to access persistent system.
     */
    public List<String> getProteinMenu(int mode) throws IntactException {
        ourProteinMiRefs.add("MI:0327");
        ourProteinMiRefs.add("MI:0326");
        return getPolymerMenu(mode, ourProteinMiRefs);
    }

    /**
     * True if <code>clazz</code> is a menu type
     * @param clazz the <code>Class</code> to compare.
     * @return true if <code>clazz</code> is one of designated menu type.
     */
    public boolean isMenuType(Class clazz) {
        return ourNameToType.containsValue(clazz);
    }

    // Helper methods

    /**
     * Return a List of all shortLabels of the class, e.g. for menus.
     *
     * @return a List of short labels as Strings.
     */
    private List<String> getMenuList(Class targetClass) throws IntactException {
        // The menu to return.
        List<String> menu = new ArrayList<String>();

        // The query factory to get a query.
        AnnotatedObjectDao annotatedObjectDao = DaoProvider.getDaoFactory(targetClass);
        Collection<AnnotatedObject> annotatedObjects = annotatedObjectDao.getAll(true, true);
        for(AnnotatedObject annobj : annotatedObjects){
            menu.add(annobj.getShortLabel());
        }
        return menu;
    }

    /**
     * Return a List of all shortLabels of the class, e.g. for menus.
     *
     * @return a List of short labels as Strings.
     */
    private List<String> getTopicMenuList(Class targetClass, Class menuClass) throws IntactException {
        // The menu to return.
        List<String> menu = new ArrayList<String>();

        // The query factory to get a query.
        AnnotatedObjectDao annotatedObjectDao = DaoProvider.getDaoFactory(targetClass);
        Collection<AnnotatedObject> annotatedObjects = annotatedObjectDao.getAll(true, true);
        for(AnnotatedObject annobj : annotatedObjects){
            CvTopic topic = (CvTopic) annobj;
            Annotation annotation = getUsedInClassAnnotation(topic);
            if(annotation != null && annotation.getAnnotationText() != null && annotation.getAnnotationText().contains(menuClass.getName())){
                menu.add(annobj.getShortLabel());
            }
        }
        return menu;
    }

    public Annotation getUsedInClassAnnotation(CvTopic cvTopic){
        Collection<Annotation> annotations = cvTopic.getAnnotations();
        for(Annotation annotation : annotations){
            CvTopic topic = annotation.getCvTopic();
            if(topic != null && CvTopic.USED_IN_CLASS.equals(topic.getShortLabel())){
                return annotation;
            }
        }
        return null;
    }

    /**
     * Returns a list of menu itsm for the Polymer editor.
     * @param mode 0 for and edit menu and 1 for an add menu; the difference is
     * {@link #SELECT_LIST_ITEM} is added as the first entry for an add menu.
     * @return a list of menu items for hte Polymer editor.
     * @throws IntactException for errors in contructing the menu or unable to
     * create an Intact helper to access persistent system.
     */
    private List<String> getPolymerMenu(int mode, Collection<String> ourPolymerMiRefs/*Criteria criteria*/) throws IntactException {
        // The menu to return.
        List<String> menu = new ArrayList<String>();

        Collection<CvInteractorType> cvInteractorTypes = DaoProvider.getDaoFactory().getCvObjectDao(CvInteractorType.class).getByPsiMiRefCollection(ourPolymerMiRefs);

        for(CvInteractorType cvInteractorType : cvInteractorTypes){
            menu.add(cvInteractorType.getShortLabel());
        }
        if (menu.isEmpty()) {
            // Special list when we don't have any menu items.
            menu.add(SELECT_LIST_ITEM);
            return menu;
        }
        if (mode == 1) {
            menu = convertToAddMenu(menu);
        }
        return menu;
    }

}