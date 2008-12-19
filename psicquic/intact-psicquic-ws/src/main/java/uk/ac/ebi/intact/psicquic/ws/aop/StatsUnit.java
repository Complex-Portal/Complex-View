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

import org.hupo.psi.mi.psicquic.QueryResponse;
import org.hupo.psi.mi.psicquic.RequestInfo;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.io.Serializable;

/**
 * Contains stats information for one request
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class StatsUnit implements Serializable {

    private static final long serialVersionUID = 1448281813;

    private String methodName;
    private String query;
    private String operand;
    private DateTime timestamp;
    private Interval executionTime;
    private QueryResponse queryResponse;
    private RequestInfo requestInfo;
    private String remoteAddress;
    private String userAgent;

    public StatsUnit() {
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public void setRemoteAddress(String remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getOperand() {
        return operand;
    }

    public void setOperand(String operand) {
        this.operand = operand;
    }

    public DateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(DateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Interval getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(Interval executionTime) {
        this.executionTime = executionTime;
    }

    public QueryResponse getQueryResponse() {
        return queryResponse;
    }

    public void setQueryResponse(QueryResponse queryResponse) {
        this.queryResponse = queryResponse;
    }

    public RequestInfo getRequestInfo() {
        return requestInfo;
    }

    public void setRequestInfo(RequestInfo requestInfo) {
        this.requestInfo = requestInfo;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("StatsUnit");
        sb.append("{methodName='").append(methodName).append('\'');
        sb.append(", query='").append(query).append('\'');
        sb.append(", operand='").append(operand).append('\'');
        sb.append(", timestamp=").append(timestamp);
        sb.append(", executionTime=").append(executionTime);
        sb.append(", queryResponse=").append(queryResponse);
        sb.append(", requestInfo=").append(requestInfo);
        sb.append('}');
        return sb.toString();
    }
}
