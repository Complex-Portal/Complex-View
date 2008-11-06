/**
 * Copyright 2008 The European Bioinformatics Institute, and others.
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
package uk.ac.ebi.intact.view.webapp.util;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.trinidad.render.ExtendedRenderKitService;
import org.apache.myfaces.trinidad.util.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import psidev.psi.mi.tab.model.Alias;
import uk.ac.ebi.intact.psimitab.IntactBinaryInteraction;
import uk.ac.ebi.intact.view.webapp.controller.config.IntactViewConfiguration;

import javax.faces.context.FacesContext;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Utility class for linking to external database resources
 *
 * @author Prem Anand (prem@ebi.ac.uk)
 * @version $Id$
 * @since 2.0.1-SNAPSHOT
 */
@Controller
@Scope( "conversation.access" )
public class ExternalDbLinker {

    @Autowired
    private IntactViewConfiguration intactViewConfiguration;

    public ExternalDbLinker(){

    }

    private static final Log log = LogFactory.getLog( ExternalDbLinker.class );

    //URL Links
    public static final String INTERPROURL = "http://www.ebi.ac.uk/interpro/ISpy?ac=";
    public static final String CHROMOSOMEURL = "http://www.ensembl.org/Homo_sapiens/featureview?type=ProteinAlignFeature;id=";
    public static final String EXPRESSIONURL_PREFIX = "http://www.ebi.ac.uk/microarray-as/atlas/qr?q_gene=";
    public static final String EXPRESSIONURL_SUFFIX = "&q_updn=updn&q_expt=%28all+conditions%29&q_orgn=HOMO+SAPIENS&view=heatmap&expand_efo=on&view=";
    public static final String REACTOMEURL = "http://www.reactome.org/cgi-bin/skypainter2";

    //identifier seperators
    public static final String INTERPRO_SEPERATOR = ",";
    public static final String CHROMOSOME_SEPERATOR = ";id=";
    public static final String EXPRESSION_SEPERATOR = ",+";
    public static final String REACTOME_SEPERATOR = "\n";


    public void goExternalLink( String baseUrl, String seperator, String[] selected ) {
        goExternalLink( baseUrl, "", seperator, selected );
    }

    public void goExternalLink( String baseUrl, String urlSuffix, String seperator, String[] selected ) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExtendedRenderKitService service = Service.getRenderKitService( facesContext, ExtendedRenderKitService.class );

        if ( selected.length > 0 ) {
            String url = baseUrl + StringUtils.join( selected, seperator ) + urlSuffix;
            service.addScript( facesContext, "window.open('" + url + "');" );
        } else {
            service.addScript( facesContext, "alert('Selection is empty');" );
        }
    }

    //linking to reactome needs a form submit
    public void reactomeLinker( String baseUrl, String separator, String[] selected, String forwardPage ) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExtendedRenderKitService service = Service.getRenderKitService( facesContext, ExtendedRenderKitService.class );

        if ( selected.length > 0 ) {
            service.addScript( facesContext, getReactomeForm( baseUrl,
                                                              StringUtils.join( selected, separator ),
                                                              forwardPage ) );
        } else {
            service.addScript( facesContext, "alert('Selection is empty');" );
        }
    }

    private String getReactomeForm( String baseUrl, String selectedIds, String forwardPage ) {

        StringBuilder sb = new StringBuilder( 1024 );
        sb.append( "reactomeform = document.createElement('form');" );
        sb.append( "reactomeform.method='post';\n" );
        sb.append( "reactomeform.action='" ).append( baseUrl ).append( "';\n" );
        sb.append( "reactomeform.enctype='multipart/form-data';\n" );
        sb.append( "reactomeform.name='skypainter';\n" );
        sb.append( "reactomeform.target='_blank';" );

        createInputHidden(sb, "reactomeform", "QUERY", selectedIds);
        createInputHidden(sb, "reactomeform", "DB", "gk_current");
        createInputHidden(sb, "reactomeform", "SUBMIT", "1");

        sb.append("document.forms[0].parentNode.appendChild(reactomeform);");
        sb.append( "reactomeform.submit();\n" );

        if ( log.isDebugEnabled() ) {
            log.debug("JavaScript to link to  Reactome: \n" +sb.toString() );
        }

        return sb.toString();
    }

    private void createInputHidden( StringBuilder sb, String formName, String name, String value ) {
        sb.append(name).append("Input = document.createElement('input');");
        sb.append(name).append("Input.type = 'hidden';");
        sb.append(name).append("Input.name = '").append(name).append("';");
        sb.append(name).append("Input.value = '").append(value).append("';");
        sb.append(formName).append(".appendChild(");
        sb.append(name).append("Input");
        sb.append(");\n");
    }

    public static Set<String> getUniqueGeneNames( List<IntactBinaryInteraction> interactions ) {
        Set<String> geneNames = new HashSet<String>();

        for ( IntactBinaryInteraction interaction : interactions ) {
            final Collection<Alias> aliases = interaction.getInteractorA().getAliases();

            for ( Alias alias : aliases ) {
                geneNames.add( alias.getName() );
            }
        }
        return geneNames;
    }

    public static Set<String> getUniqueUniprotIds( List<IntactBinaryInteraction> interactions ) {
        Set<String> uniprotIds = new HashSet<String>();

        for ( IntactBinaryInteraction interaction : interactions ) {
            String uniprotId = MitabFunctions.getUniprotIdentifierFromCrossReferences( interaction.getInteractorA().getIdentifiers() );

            if ( uniprotId != null ) {
                uniprotIds.add( uniprotId );
            }
        }
        return uniprotIds;
    }
}