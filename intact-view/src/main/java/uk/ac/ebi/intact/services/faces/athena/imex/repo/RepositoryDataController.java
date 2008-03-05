package uk.ac.ebi.intact.services.faces.athena.imex.repo;

import uk.ac.ebi.intact.dataexchange.imex.repository.ImexRepositoryContext;
import uk.ac.ebi.intact.dataexchange.imex.repository.RepositoryHelper;
import uk.ac.ebi.intact.dataexchange.imex.repository.model.RepoEntry;
import uk.ac.ebi.intact.dataexchange.psimi.xml.exchange.PsiExchange;
import uk.ac.ebi.intact.services.faces.athena.BaseController;

import javax.faces.component.UIComponent;
import javax.faces.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class RepositoryDataController extends BaseController {

    private List<RepoEntry> entries;

    private UIComponent entriesTable;

    public RepositoryDataController() {
        
    }

     public void importSelected(ActionEvent action) {
         List<RepoEntry> selectedEntries = getSelected(entriesTable);

         RepositoryHelper helper = new RepositoryHelper(ImexRepositoryContext.getInstance().getRepository());

         for (RepoEntry repoEntry : selectedEntries) {
             File file = helper.getEntryFile(repoEntry);
             try {
                 PsiExchange.importIntoIntact(new FileInputStream(file));
             }
             catch (Exception e) {
                 addInfoMessage("Failed to import: "+repoEntry.getPmid()+" ("+file+")", e.getMessage());
             }
         }

         addInfoMessage("Imported "+selectedEntries.size()+" entries.", null);
     }

    public List<RepoEntry> getEntries() {
        if (entries == null) {
            entries = ImexRepositoryContext.getInstance().getImexServiceProvider().getRepoEntryService().findAllRepoEntries();
        }
        return entries;
    }

    public void setEntries(List<RepoEntry> entries) {
        this.entries = entries;
    }

    public UIComponent getEntriesTable() {
        return entriesTable;
    }

    public void setEntriesTable(UIComponent entriesTable) {
        this.entriesTable = entriesTable;
    }
}
