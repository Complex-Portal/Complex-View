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
package uk.ac.ebi.intact.psicquic.ws.aop;

import org.apache.cxf.transport.http.AbstractHTTPDestination;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.hupo.psi.mi.psicquic.DbRef;
import org.hupo.psi.mi.psicquic.QueryResponse;
import org.hupo.psi.mi.psicquic.RequestInfo;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import uk.ac.ebi.intact.psicquic.ws.jms.StatsProducer;
import uk.ac.ebi.intact.psicquic.ws.util.PsicquicStreamingOutput;

import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import java.util.Iterator;
import java.util.List;

/**
 * TODO comment that class header
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Component
@Aspect
public class StatisticsAspect  {

    @Autowired
    private StatsProducer statsProducer;

    @Autowired
    @Qualifier("javax.xml.ws.WebServiceContext")
    private WebServiceContext webServiceContext;

    @Around("execution(org.hupo.psi.mi.psicquic.QueryResponse getBy*(..))")
    private Object recordStats(ProceedingJoinPoint pjp) throws Throwable{
        StatsUnit statsUnit = createStatsUnit();

        populateStatsFromJoinpoint(statsUnit, pjp);

        statsUnit.setOperand(findOperand(pjp));

        long startInstant = System.currentTimeMillis();

        QueryResponse response = (QueryResponse) pjp.proceed();

        long endInstant = System.currentTimeMillis();

        statsUnit.setQueryResponse(response);
        statsUnit.setExecutionTime(new Interval(startInstant, endInstant));

        statsProducer.sendMessage(statsUnit);

        return response;
    }

     @Around("execution(uk.ac.ebi.intact.psicquic.ws.util.PsicquicStreamingOutput getBy*(..))")
     private Object recordRestStats(ProceedingJoinPoint pjp) throws Throwable {
         StatsUnit statsUnit = createStatsUnit();

         populateStatsFromJoinpoint(statsUnit, pjp);

         statsUnit.setOperand(findOperand(pjp));

         long startInstant = System.currentTimeMillis();

         PsicquicStreamingOutput response = (PsicquicStreamingOutput) pjp.proceed();

         long endInstant = System.currentTimeMillis();

         statsUnit.setQueryResponse(response.getQueryResponse());
         statsUnit.setExecutionTime(new Interval(startInstant, endInstant));

         statsProducer.sendMessage(statsUnit);

         return response;
     }

    private StatsUnit createStatsUnit() {
        StatsUnit statsUnit = new StatsUnit();
        statsUnit.setTimestamp(new DateTime());

        if (webServiceContext != null && webServiceContext.getMessageContext() != null) {
            MessageContext ctx = webServiceContext.getMessageContext();
            HttpServletRequest request = (HttpServletRequest) ctx.get(AbstractHTTPDestination.HTTP_REQUEST);

            statsUnit.setRemoteAddress(request.getRemoteAddr());

            if (request.getHeader("User-Agent") != null) {
                statsUnit.setUserAgent(request.getHeader("User-Agent"));
            }
        }
        return statsUnit;
    }

    private void populateStatsFromJoinpoint(StatsUnit statsUnit, ProceedingJoinPoint pjp) {
        statsUnit.setMethodName(pjp.getSignature().getName());

        // query
        Object[] methodArgs = pjp.getArgs();

        String query = createQueryFromObject(pjp.getArgs()[0]);
        statsUnit.setQuery(query);

        // request info
        if (methodArgs.length > 1 && methodArgs[1] instanceof RequestInfo) {
            statsUnit.setRequestInfo((RequestInfo)methodArgs[1]);
        } else {
            RequestInfo reqInfo = new RequestInfo();
            reqInfo.setFirstResult(0);
            reqInfo.setBlockSize(Integer.MAX_VALUE);
            reqInfo.setResultType("");
            statsUnit.setRequestInfo(reqInfo);
        }

        // operand
        if (methodArgs.length >= 3) {
            if (methodArgs[2] instanceof String) {
                statsUnit.setOperand((String)methodArgs[2]);
            }
        }
    }

    private String createQueryFromObject(Object o) {
        String query;

        if (o instanceof DbRef) {
            DbRef dbRef = (DbRef)o;
            query = dbRefToString(dbRef);
        } else if (o instanceof List) {
            List<DbRef> dbRefs = (List<DbRef>)o;

            StringBuilder sb = new StringBuilder(dbRefs.size()*16);

            for (Iterator<DbRef> dbRefIterator = dbRefs.iterator(); dbRefIterator.hasNext();) {
                DbRef dbRef = dbRefIterator.next();
                sb.append(dbRefToString(dbRef));

                if (dbRefIterator.hasNext()) {
                    sb.append("; ");
                }
            }

            query = sb.toString();

        } else {
            query = o.toString();
        }

        return query;
    }

    private String dbRefToString(DbRef dbRef) {
        String query;
        query = dbRef.getId();

        if (dbRef.getDbAc() != null) {
            query = query + "["+dbRef.getDbAc()+"]";
        }
        return query;
    }

    private String findOperand(ProceedingJoinPoint pjp) {
        Object[] methodArgs = pjp.getArgs();

        if (methodArgs.length >= 3) {
            if (methodArgs[2] instanceof String) {
                return (String) methodArgs[2];
            }
        }
        
        return null;
    }
}
