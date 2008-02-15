package uk.ac.ebi.intact.service.psicquic.wsclient;

import org.hupo.psi.mi.psicquic.PsicquicService;

/**
 *
 */
public class SearchClient {

    public static void main(String[] args) throws Exception {
        PsicquicService service = new PsicquicService();

        System.out.println(service.getPsicquic().getVersion());
    }
}
