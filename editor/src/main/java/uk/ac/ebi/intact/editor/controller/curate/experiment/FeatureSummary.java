package uk.ac.ebi.intact.editor.controller.curate.experiment;

/**
 * Summary for a feature
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>21/10/14</pre>
 */

public class FeatureSummary {

    private String feature;
    private String biundDomain;

    public FeatureSummary(String feature, String bind){
        this.feature = feature;
        this.biundDomain = bind;
    }

    public String getFeature() {
        return feature;
    }

    public String getBindDomain() {
        return biundDomain;
    }
}
