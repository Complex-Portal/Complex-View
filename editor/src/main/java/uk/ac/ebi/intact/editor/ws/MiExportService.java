/**
 * Copyright 2011 The European Bioinformatics Institute, and others.
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
package uk.ac.ebi.intact.editor.ws;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Path("/mi")
public interface MiExportService {

    String FORMAT_XML254 = "xml254";
    String FORMAT_MITAB25 = "tab25";
    String FORMAT_MITAB26 = "tab26";
    String FORMAT_MITAB27 = "tab27";
    String FORMAT_HTML = "html";
    String FORMAT_JSON = "json";
    String FORMAT_GRAPHML = "graphml";
    String FORMAT_FEBS_SDA = "sda";

    @GET
    @Path("/publication")
    Object exportPublication(@QueryParam("ac") String id,
                             @DefaultValue("tab25") @QueryParam("format") String format);

    @GET
    @Path("/experiment")
    Object exportExperiment(@QueryParam("ac") String id,
                            @DefaultValue("tab25") @QueryParam("format") String format);

    @GET
    @Path("/interaction")
    Object exportInteraction(@QueryParam("ac") String id,
                             @DefaultValue("tab25") @QueryParam("format") String format);

}
