<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://ebi.ac.uk/intact/commons" prefix="intact" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%--
  Created by IntelliJ IDEA.
  User: iarmean
  Date: 28-Feb-2008
  Time: 17:10:36
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html:form
        action="/filter">

    <table width="100%" border="1" border-collapse="collapse" cellpadding="10">
        <tr>
            <td width="10%" align="center">
                <html:radio property="method" value="FIRST_METHOD"/>
            </td>
            <td width="60%">
                <html:text property="confidenceValue" size="2" maxlength="4"/>

                <html:select property="relation">
                    <html:option value=">=">>=</html:option>
                    <html:option value=">">></html:option>
                    <html:option value="<="><=</html:option>
                    <html:option value="<"><</html:option>
                    <html:option value="=">=</html:option>
                </html:select>
            </td>
        </tr>
        <tr>
            <td width="10%" align="center">
                <html:radio property="method" value="SECOND_METHOD"/>
            </td>
            <td width="60%">
                <table cellspacing="3">
                    <tr>
                        <td> between
                            <html:text property="minConfidenceValue" size="2" maxlength="4"/>
                            and
                            <html:text property="maxConfidenceValue" size="2" maxlength="4"/></td>
                    </tr>
                    <tr>
                        <td>
                            <html:radio property="clusivity" value="INCLUSIVE"/>inclusive<br/>                           
                            <html:radio property="clusivity" value="EXCLUSIVE"/>exclusive
                        </td>
                    </tr>
                </table>

            </td>
        </tr>
        <tr>
            <td></td>
            <td>
                <html:submit property="action">
                    <bean:message key="result.highlight.source.submit.button"/>
                </html:submit>
            </td>
        </tr>

    </table>

</html:form>