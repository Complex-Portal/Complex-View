package uk.ac.ebi.intact.services.faces.athena;

import uk.ac.ebi.intact.dataexchange.imex.repository.model.RepoEntry;

import javax.faces.context.FacesContext;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import java.io.Serializable;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

import org.apache.myfaces.trinidad.component.UIXCollection;
import org.apache.myfaces.trinidad.component.UIXTable;
import org.apache.myfaces.trinidad.component.UIXTree;
import org.apache.myfaces.trinidad.model.RowKeySet;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class BaseController implements Serializable {

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

    protected List getSelected(UIComponent component) {
        UIXCollection table = (UIXCollection) component;

        final RowKeySet state;
        if (table instanceof UIXTable) {
            state = ((UIXTable) table).getSelectedRowKeys();
        } else {
            state = ((UIXTree) table).getSelectedRowKeys();
        }

        Iterator<Object> selection = state.iterator();
        Object oldKey = table.getRowKey();

        List<RepoEntry> selectedEntries = new ArrayList<RepoEntry>();
        while (selection.hasNext()) {
            table.setRowKey(selection.next());
            selectedEntries.add((RepoEntry) table.getRowData());
        }
        table.setRowKey(oldKey);
        return selectedEntries;
    }
}
