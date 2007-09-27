<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/intact.tld" prefix="intact"%>

<script language="JavaScript" type="text/javascript">

    // Skip client-side javascript validation
    function skipValidation() {
        bCancel = true;
    }
</script>

<%-- The anchor name for this page --%>
<a name="autoCompletion"/>

<table border="0" cellspacing="1" cellpadding="2">
    <tr class="tableRowHeader">
        <th class="tableCellHeader">
            <bean:message key="exp.label.pubmedid"/>
        </th>
        <th class="tableCellHeader">

        </th>
        <th>
            <intact:documentation section="editor.short.labels"/>
        </th>
    </tr>
    <tr class="tableRowEven">
        <td class="tableCell">
            <html:text property="pubmedId" size="20" maxlength="20" name="expForm"
                styleClass="inputRequired" onkeypress="return handleEnter(this, event)"/>
        </td>

        <td class="tableCell">
            <html:submit property="dispatch" onclick="skipValidation();"
                titleKey="exp.button.autocompletion.titleKey">
                <bean:message key="exp.button.autocompletion"/>
            </html:submit>
        </td>
    </tr>
</table>
<html:messages id="msg" property="autocomp">
    <font color="red"><li><bean:write name="msg" /></li></font>
</html:messages>
<%--<html:messages id="msg" id="" property="autocomp"/>--%>
