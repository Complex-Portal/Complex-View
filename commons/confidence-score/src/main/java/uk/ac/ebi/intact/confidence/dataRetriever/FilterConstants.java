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
 *  limitations under the License.
 */
package uk.ac.ebi.intact.confidence.dataRetriever;

import java.util.Arrays;
import java.util.List;

/**
 * Constant strings for the filtering of high confidences from IntAct.
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 1.6.0
 */
public class FilterConstants {


     public static final List<String> AUTHOR_CONFIDENCE_desc1 = Arrays.asList( new String[]{"core-1", "score = ++++",
                    "beta-galactosidase score = +++", "Interacted strongly.",
                    "Strong interaction.", "strong interaction","strong", "+++",
                    "GAI3 was found to interact strongly with GAIP - as measured by Beta-gal filter and liquid assays.",
                    "3", "LacZ4","very strong signal","Above 1.5", "A","B","ito-core",
                    "CORE_1","+++ (highest score given)","+++, strong signal","++; 22.5 beta gal units",
                    "+++, strong signal","high"} );


    public static final List<String> AUTHOR_CONFIDENCE_not_exp1 = Arrays.asList( new String[]{ "EBI-1104292",
                    "EBI-1104295","EBI-1265744","EBI-706691","EBI-727915","EBI-861848","EBI-929163","EBI-1256430",
                    "EBI-1256426"});


    public static final List<String> AUTHOR_CONFIDENCE_desc2 = Arrays.asList( new String[]{"A", "++++",
                    "very strong interaction", "very strong",
                    "score = +++"} );


    public static final List<String> AUTHOR_CONFIDENCE_exp2 = Arrays.asList( new String[]{"EBI-1104292",
                    "EBI-1104295","EBI-1265744","EBI-706691","EBI-727915","EBI-861848","EBI-929163","EBI-1256430",
                    "EBI-1256426"});


    public static final String IN_VITRO = "in vitro";
    public static final List<String> PARTICIPANTS_IN_VITRO = Arrays.asList( new String[]{IN_VITRO, "purified",
                     "chemical synthesis"});

    public static final String DISULFIDE_BOND = "disulfide bond";

    public static final String CROSSLINK = "crosslink";
}
