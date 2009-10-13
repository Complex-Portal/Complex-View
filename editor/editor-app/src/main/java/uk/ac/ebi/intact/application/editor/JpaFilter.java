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
package uk.ac.ebi.intact.application.editor;

import org.springframework.transaction.TransactionStatus;
import uk.ac.ebi.intact.core.IntactException;
import uk.ac.ebi.intact.core.context.DataContext;
import uk.ac.ebi.intact.core.context.IntactContext;

import javax.servlet.*;
import java.io.IOException;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class JpaFilter implements Filter {

    public void init(FilterConfig filterConfig) throws ServletException {

    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        final DataContext dataContext = IntactContext.getCurrentInstance().getDataContext();
        final TransactionStatus transactionStatus = dataContext.beginTransaction();

        try {
            chain.doFilter(request, response);
        } catch (Throwable e) {
            if (!transactionStatus.isCompleted()) {
                dataContext.rollbackTransaction(transactionStatus);
            }
            
            throw new IntactException(e);
        }

        if (!transactionStatus.isCompleted()) {
            dataContext.commitTransaction(transactionStatus);
        }
    }

    public void destroy() {

    }
}
