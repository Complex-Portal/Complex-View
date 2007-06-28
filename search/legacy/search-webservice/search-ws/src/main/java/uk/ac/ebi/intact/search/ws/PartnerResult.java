/**
 * Copyright (c) 2002-2006 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.search.ws;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * TODO comment this!
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 * @since <pre>08-Aug-2006</pre>
 */
public class PartnerResult
{

    private String uniprotAc;
    private String[] partnerUniprotAcs;

    public PartnerResult()
    {

    }

    public PartnerResult(String uniprotAc, String[] partnerUniprotAcs)
    {
        this.uniprotAc = uniprotAc;
        this.partnerUniprotAcs = partnerUniprotAcs;
    }

    public String getUniprotAc()
    {
        return uniprotAc;
    }

    public void setUniprotAc(String uniprotAc)
    {
        this.uniprotAc = uniprotAc;
    }

    public String[] getPartnerUniprotAcs()
    {
        return partnerUniprotAcs;
    }

    public void setPartnerUniprotAcs(String[] partnerUniprotAcs)
    {
        this.partnerUniprotAcs = partnerUniprotAcs;
    }
}
