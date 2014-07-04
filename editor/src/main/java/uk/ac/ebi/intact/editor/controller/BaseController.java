/**
 * Copyright 2010 The European Bioinformatics Institute, and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.intact.editor.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.intact.editor.config.EditorConfig;
import uk.ac.ebi.intact.jami.ApplicationContextProvider;
import uk.ac.ebi.intact.jami.dao.IntactDao;
import uk.ac.ebi.intact.model.user.User;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ExceptionQueuedEvent;
import javax.faces.event.ExceptionQueuedEventContext;
import java.io.Serializable;

/**
 * IntAct JSF Base Controller.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public abstract class BaseController implements Serializable {

    @Autowired
    private transient ApplicationContext applicationContext;

    public void addMessage( String message, String detail ) {
        FacesContext context = FacesContext.getCurrentInstance();
        FacesMessage facesMessage = new FacesMessage( message, detail );
        context.addMessage( null, facesMessage );
    }

    public void addInfoMessage( String message, String detail ) {
        FacesContext context = FacesContext.getCurrentInstance();
        FacesMessage facesMessage = new FacesMessage( FacesMessage.SEVERITY_INFO, message, detail );
        context.addMessage( null, facesMessage );
    }

    public void addWarningMessage( String message, String detail ) {
        FacesContext context = FacesContext.getCurrentInstance();
        FacesMessage facesMessage = new FacesMessage( FacesMessage.SEVERITY_WARN, message, detail );
        context.addMessage( null, facesMessage );
    }

    public void addErrorMessage( String message, String detail ) {
        FacesContext context = FacesContext.getCurrentInstance();
        FacesMessage facesMessage = new FacesMessage( FacesMessage.SEVERITY_ERROR, message, detail );
        context.addMessage( null, facesMessage );
    }

    protected ApplicationContext getSpringContext() {
        return applicationContext;
    }

    protected EditorConfig getEditorConfig() {
        return (EditorConfig) getSpringContext().getBean("editorConfig");
    }

    public User getCurrentUser() {
        UserSessionController userSessionController = getUserSessionController();
        return userSessionController.getCurrentUser();
    }

    @Transactional("jamiTransactionManager")
    public uk.ac.ebi.intact.jami.model.user.User getCurrentJamiUser() {
        UserSessionController userSessionController = getUserSessionController();
        User intactUser = userSessionController.getCurrentUser();

        IntactDao intactDao = ApplicationContextProvider.getBean("intactDao");
        return intactDao.getUserDao().getByAc(intactUser.getAc());
    }

    protected void handleException(Throwable e) {
        FacesContext ctx = FacesContext.getCurrentInstance();
        ExceptionQueuedEventContext eventContext = new ExceptionQueuedEventContext( ctx, e );
        ctx.getApplication().publishEvent( ctx, ExceptionQueuedEvent.class, eventContext );
        e.printStackTrace();
    }

    protected UserSessionController getUserSessionController() {
        return (UserSessionController) getSpringContext().getBean("userSessionController");
    }
}


