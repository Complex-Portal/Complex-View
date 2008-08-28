/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.view.webapp.controller.admin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.trinidad.model.UploadedFile;
import psidev.psi.mi.search.Searcher;
import psidev.psi.mi.tab.converter.txt2tab.MitabLineException;
import psidev.psi.mi.xml.converter.ConverterException;
import uk.ac.ebi.intact.binarysearch.webapp.generated.SearchConfig;
import uk.ac.ebi.intact.binarysearch.webapp.generated.Index;
import uk.ac.ebi.intact.binarysearch.webapp.generated.User;
import uk.ac.ebi.intact.view.webapp.util.WebappUtils;

import javax.faces.component.UIParameter;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

/**
 * Session scope backing bean for secure admin area
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class AdminBean implements Serializable {

    /**
     * Sets up a logger for that class.
     */
    public static final Log log = LogFactory.getLog(AdminBean.class);

    public static final String USER_AUTHENTICATED_TOKEN = AdminBean.class + ".USER_AUTHENTICATED";

    private User currentUser;
    private SearchConfig config;
    private String configFileLocation;

    private Index selectedIndex;
    private Index newIndex;

    private UploadedFile uploadedPsimitabFile;

    public AdminBean() {
        currentUser = anonymousUser();
        selectedIndex = new Index();
        newIndex = new Index();
    }

    /**
     * Method to login to the administrative area
     *
     * @return the action outcome after the login attempt
     */
    public String login() {
        if (log.isInfoEnabled()) log.info("User trying to log: " + currentUser.getName());

        FacesContext facesContext = FacesContext.getCurrentInstance();

        for (User user : config.getUsers()) {
            if (user.getName().equals(currentUser.getName())) {
                if (user.getPassword().equals(WebappUtils.encrypt(facesContext, currentUser.getPassword()))) {
                    if (log.isDebugEnabled()) log.debug("\tLogin successful");

                    // put the authentication token in the session
                    HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(false);
                    session.setAttribute(USER_AUTHENTICATED_TOKEN, Boolean.TRUE);

                    currentUser = user;

                    return "admin.index.manage";
                }
            }
        }

        if (log.isWarnEnabled()) {
            log.warn("User login failed!: " + currentUser.getName());
        }

        return null;
    }

    public void selectIndex(ActionEvent evt) {
        UIParameter param = (UIParameter) evt.getComponent().getChildren().get(0);
        this.selectedIndex = (Index) param.getValue();

        if (log.isDebugEnabled()) log.debug("Selected index: " + selectedIndex.getName());
    }

    public void saveIndex(ActionEvent evt) {
        if (log.isDebugEnabled())
            log.debug("Saving configuration file, after updating index: " + selectedIndex.getName());

        WebappUtils.writeConfiguration(config, new File(configFileLocation));
        deselectIndex(evt);
    }

    public void createIndex(ActionEvent evt) {
        if (log.isDebugEnabled()) log.debug("Creating index: " + newIndex.getName());

        try {
            newIndex.setSize(WebappUtils.countItemsInIndex(newIndex.getLocation()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        config.getIndices().add(newIndex);

        WebappUtils.writeConfiguration(config, new File(configFileLocation));
        deselectIndex(evt);

        newIndex = new Index();
    }

    public void deleteIndex(ActionEvent evt) {
        UIParameter param = (UIParameter) evt.getComponent().getChildren().get(0);
        Index indexToDelete = (Index) param.getValue();

        if (log.isInfoEnabled()) log.info("Deleting index: " + indexToDelete.getName());

        for (Iterator<Index> iterator = config.getIndices().iterator(); iterator.hasNext();)
        {
            Index index = iterator.next();

            if (index.equals(indexToDelete)) {
                iterator.remove();
            }
        }

        WebappUtils.writeConfiguration(config, new File(configFileLocation));
        deselectIndex(evt);
    }

    public void deselectIndex(ActionEvent evt) {
        this.selectedIndex = new Index();
    }

    public void defaultIndex(ActionEvent evt) {
        UIParameter param = (UIParameter) evt.getComponent().getChildren().get(0);
        Index defaultIndex = (Index) param.getValue();

        if (log.isInfoEnabled()) log.info("New default index: " + defaultIndex.getName());

        for (Iterator<Index> iterator = config.getIndices().iterator(); iterator.hasNext();)
        {
            Index index = iterator.next();

            index.setDefault(false);

            if (index.equals(defaultIndex)) {
                index.setDefault(true);
            }
        }
        WebappUtils.writeConfiguration(config, new File(configFileLocation));

    }

    /**
     * Upload a psimitab File
     */
    public void createIndexFromPsimitabFile(ActionEvent evt) {
        if (log.isInfoEnabled()) log.info("Uploading file: " + uploadedPsimitabFile.getFilename());

        newIndex.setLocation(new File("/tmp", currentUser.getName() + "_" + uploadedPsimitabFile.getFilename()).toString());

        try {
            Searcher.buildIndex(newIndex.getLocation(), uploadedPsimitabFile.getInputStream(), true, true);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ConverterException e) {
            e.printStackTrace();
        } catch (MitabLineException e) {
            e.printStackTrace();
        }

        createIndex(evt);

    }

    public boolean isUploaded() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        return facesContext.getExternalContext().getApplicationMap().get("fileupload_bytes") != null;
    }

    /**
     * Default anonymous user
     *
     * @return an anoymous user, used as the default user
     */
    private static User anonymousUser() {
        User user = new User();
        user.setName("Anonymous");

        user.getRoles().add("guest");

        return user;
    }

    public boolean isUserAdmin() {
        return this.currentUser.getRoles().contains("admin");
    }

    public Index getSelectedIndex() {
        return selectedIndex;
    }

    public void setSelectedIndex(Index selectedIndex) {
        this.selectedIndex = selectedIndex;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    public void setConfig(SearchConfig config) {
        this.config = config;
    }

    public String getConfigFileLocation() {
        return configFileLocation;
    }

    public void setConfigFileLocation(String configFileLocation) {
        this.configFileLocation = configFileLocation;
    }

    public Index getNewIndex() {
        return newIndex;
    }

    public void setNewIndex(Index newIndex) {
        this.newIndex = newIndex;
    }

    public UploadedFile getUploadedPsimitabFile() {
        return uploadedPsimitabFile;
    }

    public void setUploadedPsimitabFile(UploadedFile uploadedPsimitabFile) {
        this.uploadedPsimitabFile = uploadedPsimitabFile;
    }
}