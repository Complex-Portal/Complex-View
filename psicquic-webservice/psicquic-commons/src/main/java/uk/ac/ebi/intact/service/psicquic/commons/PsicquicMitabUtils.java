/*
 * Copyright 2001-2007 The European Bioinformatics Institute.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.intact.service.psicquic.commons;

import psidev.psi.mi.search.column.ColumnSet;
import psidev.psi.mi.search.column.PsimiTabColumn;
import psidev.psi.mi.tab.formatter.LineFormatter;
import psidev.psi.mi.tab.model.BinaryInteraction;
import uk.ac.ebi.intact.service.psicquic.commons.mitab.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Utilities to deal with Psicquic, common for both web service and client
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public final class PsicquicMitabUtils {

    private PsicquicMitabUtils() {}

    public static Mitab convertToMitab(Collection<? extends BinaryInteraction> binaryInteractions, String database, ColumnSet columnSet, LineFormatter lineFormatter) {
        List<String> lines = new ArrayList(binaryInteractions.size());

        for (BinaryInteraction binaryInteraction : binaryInteractions) {
            String line = lineFormatter.format(binaryInteraction);
            lines.add(line);
        }

        return convertToMitab(lines, database, columnSet);
    }

    public static Mitab convertToMitab(Collection<String> lines, String database, ColumnSet columnSet) {
        ObjectFactory factory = new ObjectFactory();

        Mitab mitab = factory.createMitab();
        mitab.setDb(database);

        if (columnSet != null) {
            HeaderType header = factory.createHeaderType();
            mitab.setHeader(header);

            for (PsimiTabColumn psimiTabCol : columnSet.getColumns()) {
                mitab.getHeader().getCols().add(psimiTabCol.getColumnName());
            }
        }

        BodyType body = factory.createBodyType();
        mitab.setBody(body);

        int lineCount = 1;

        for (String line : lines) {
            RowType row = factory.createRowType();
            body.getRows().add(row);

            String[] colValues = line.split("\\t");

            if (columnSet != null && colValues.length != columnSet.getColumns().size()) {
                throw new IllegalArgumentException("Number of columns in line "+lineCount+" do not correspond with the number of columns in the header");
            }

            for (String colValue : colValues) {
                if ("-".equals(colValue)) {
                    colValue = "";
                }
                row.getCols().add(colValue);
            }

            lineCount++;
        }

        return mitab;
    }
}
