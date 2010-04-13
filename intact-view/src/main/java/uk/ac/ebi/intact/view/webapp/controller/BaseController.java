package uk.ac.ebi.intact.view.webapp.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.orchestra.conversation.ConversationBindingEvent;
import org.apache.myfaces.orchestra.conversation.ConversationBindingListener;
import uk.ac.ebi.intact.core.context.IntactContext;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;

/**
 * IntAct JSF Base Controller.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public abstract class BaseController implements Serializable, ConversationBindingListener {

    private static final Log log = LogFactory.getLog( BaseController.class );

    protected void addMessage(String message, String detail) {
        FacesContext context = FacesContext.getCurrentInstance();
        FacesMessage facesMessage = new FacesMessage(message, detail);
        context.addMessage(null, facesMessage);
    }

    protected void addInfoMessage(String message, String detail) {
        FacesContext context = FacesContext.getCurrentInstance();
        FacesMessage facesMessage = new FacesMessage(FacesMessage.SEVERITY_INFO, message, detail);
        context.addMessage(null, facesMessage);
    }

    protected void addWarningMessage(String message, String detail) {
        FacesContext context = FacesContext.getCurrentInstance();
        FacesMessage facesMessage = new FacesMessage(FacesMessage.SEVERITY_WARN, message, detail);
        context.addMessage(null, facesMessage);
    }

    protected void addErrorMessage(String message, String detail) {
        FacesContext context = FacesContext.getCurrentInstance();
        FacesMessage facesMessage = new FacesMessage(FacesMessage.SEVERITY_ERROR, message, detail);
        context.addMessage(null, facesMessage);
    }

   
    /**
     * Use this method to get a value using a list of parameter names. The names are iterated in order
     * and if a value is found, that value is return. This method is useful to create synonym parameters.
     * @param paramNames The parameter names, which are synonyms
     * @return The value
     */
    protected String getParameterValue(String ... paramNames) {
        for (String paramName : paramNames) {
            String value = FacesContext.getCurrentInstance()
                    .getExternalContext().getRequestParameterMap().get(paramName);

            if (value != null) {
                return value;
            }
        }

        return null;
    }

    protected Object getBean(String name) {
        return IntactContext.getCurrentInstance().getSpringContext().getBean(name);
    }

    public void valueBound(ConversationBindingEvent event) {

        if (log.isDebugEnabled()) {
            HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
            log.debug("Conversation event (value bound): conversation="+event.getConversation().getName()+
                ", name="+event.getName()+", session: "+request.getSession().getId());
        }
    }

    public void valueUnbound(ConversationBindingEvent event) {
        if (log.isDebugEnabled()) {
            HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
            log.debug("Conversation event (value unbound): conversation="+event.getConversation().getName()+
                    ", name="+event.getName()+", session: "+request.getSession().getId());
        }
    }
}

