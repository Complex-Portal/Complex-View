package uk.ac.ebi.intact.service.psicquic.wsclient;

import org.hupo.psi.mi.psicquic.DbRefRequestType;
import org.hupo.psi.mi.psicquic.DbRefType;
import org.hupo.psi.mi.psicquic.PsicquicService;

/**
 *
 */
public class SearchClient {

    public static void main(String[] args) throws Exception {
        PsicquicService service = new PsicquicService();

        DbRefRequestType dbRegRequestType = new DbRefRequestType();
        DbRefType refType = new DbRefType();
        refType.setAc("UNK-3041");
        dbRegRequestType.setDbRef(refType);
        
        System.out.println(service.getPsicquic().getByInteractor(dbRegRequestType).getResultSet().getEntrySet());
    }
}
