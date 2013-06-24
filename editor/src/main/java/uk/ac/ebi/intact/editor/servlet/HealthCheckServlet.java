/**
 * Copyright 2009 The European Bioinformatics Institute, and others.
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
package uk.ac.ebi.intact.editor.servlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.core.persistence.dao.DaoFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class HealthCheckServlet extends HttpServlet {

    private static final Log log = LogFactory.getLog(HealthCheckServlet.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ServletContext context = getServletContext();

        resp.setContentType("text/plain");
        resp.getWriter().write("Application: OK\n");

        // db check
        boolean dbOk = false;
        try {
            final IntactContext intactContext = IntactContext.getCurrentInstance();
            final DaoFactory daoFactory = intactContext.getDataContext().getDaoFactory();
            final int count = daoFactory.getInstitutionDao().countAll();
            dbOk = (count > 0);
        } catch ( Throwable t ) {
            log.error( "Health Check failed on database.", t );
        }
        resp.getWriter().write("Database: " + (dbOk ? "OK" : "FAILED"));
        resp.getWriter().write("\n");


        // The EBI load balancer looks for the keyword ALL_OK
        boolean allOk = dbOk;
        resp.getWriter().write("\nGlobal status: "+(allOk? "ALL_OK" : "FAILED"));
    }
}
