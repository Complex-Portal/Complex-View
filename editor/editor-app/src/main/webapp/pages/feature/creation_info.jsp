<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>


<table  width="100%" border="0" cellspacing="1" cellpadding="2">
<tr class="tableRowEven">
        <td class="tableCell">
            <bean:message key="label.creation"/> <bean:write property="created" name="featureForm" /> by <bean:write property="creator" name="featureForm" />.
        </td>
        <td class="tableCell">
            <bean:message key="label.update"/> <bean:write property="updated" name="featureForm" /> by <bean:write property="updator" name="featureForm" />.
        </td>
    </tr>
</table>