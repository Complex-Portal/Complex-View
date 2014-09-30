package uk.ac.ebi.intact.view.webapp.io;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.response.FacetField;
import org.hupo.psi.mi.psicquic.model.PsicquicSolrServer;
import uk.ac.ebi.intact.dataexchange.psimi.solr.FieldNames;
import uk.ac.ebi.intact.dataexchange.psimi.solr.IntactSolrSearchResult;
import uk.ac.ebi.intact.dataexchange.psimi.solr.IntactSolrSearcher;
import uk.ac.ebi.intact.view.webapp.IntactViewException;
import uk.ac.ebi.intact.view.webapp.controller.search.UserQuery;

import java.io.*;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Exporter of molecule list
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>29/09/14</pre>
 */

public class MoleculeListExporter {

    private int maxSize = 200;
    private static final Log log = LogFactory.getLog(MoleculeListExporter.class);

    private IntactSolrSearcher solrSearcher;

    public MoleculeListExporter(SolrServer solrServer){
        this.solrSearcher = new IntactSolrSearcher(solrServer);
    }

    public Set<String> collectMoleculeAcs(String userQuery,
                                         boolean filterSpoke, boolean filterNegative) {
        int numberUniprotProcessed = 0;
        int first = 0;
        Set<String> uniprotAcs = new HashSet<String>();
        final String uniprotFieldNameA = FieldNames.ID_A_FACET;
        final String uniprotFieldNameB = FieldNames.ID_B_FACET;

        boolean hasMoreAcs = true;
        while (hasMoreAcs){
            try {
                String [] facetFields = buildFacetFields(uniprotFieldNameA, uniprotFieldNameB);
                String query = userQuery != null ? userQuery : UserQuery.STAR_QUERY;

                String [] queryFilters = null;
                if (filterNegative && filterSpoke){
                    queryFilters = new String[]{FieldNames.NEGATIVE+":false", FieldNames.COMPLEX_EXPANSION+":\"-\""};
                }
                else if (filterNegative){
                    queryFilters = new String[]{FieldNames.NEGATIVE+":false"};
                }
                else if (filterSpoke){
                    queryFilters = new String[]{FieldNames.COMPLEX_EXPANSION+":\"-\""};
                }

                IntactSolrSearchResult result = solrSearcher.searchWithFacets(query, 0, 0, PsicquicSolrServer.RETURN_TYPE_COUNT, queryFilters, facetFields, first, maxSize);

                List<FacetField> facetFieldList = result.getFacetFieldList();
                if (facetFieldList == null || facetFieldList.isEmpty()){
                    hasMoreAcs = false;
                }

                int numberOfFacets=0;
                for (FacetField facet : facetFieldList){
                    if (facet.getValueCount() > 0){
                        // collect uniprot ids
                        for (FacetField.Count count : facet.getValues()){
                            // only process uniprot ids
                            if (count.getName().contains(":")){
                                if (uniprotAcs.add(count.getName().substring(count.getName().indexOf(":")+1))){
                                    numberUniprotProcessed++;
                                }
                            }
                        }
                        numberOfFacets++;
                    }
                }

                if (numberOfFacets == 0){
                    hasMoreAcs = false;
                }

                first+=maxSize;

            } catch (Exception e) {
                log.error("Problem loading uniprot ACs",e);
                throw new IntactViewException("Problem loading Molecule ACs", e);
            }
        }

        return uniprotAcs;
    }

    public void writeMoleculeAcs(OutputStream output, String userQuery,
                                          boolean filterSpoke, boolean filterNegative) throws IOException {
        Set<String> moleculeAcs = collectMoleculeAcs(userQuery, filterSpoke, filterNegative);

        Writer out = new BufferedWriter(new OutputStreamWriter(output));
        try{
            Iterator<String> molAcsIterator = moleculeAcs.iterator();
            while (molAcsIterator.hasNext()){
                out.write(molAcsIterator.next());
                if (molAcsIterator.hasNext()){
                    out.write("\n");
                }
            }
        }
        finally {
            // close writer
            out.close();
        }
    }

    private String[] buildFacetFields(String uniprotFieldNameA, String uniprotFieldNameB) {
        String[] facetFields = new String[]{uniprotFieldNameA, uniprotFieldNameB};

        return facetFields;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }
}
