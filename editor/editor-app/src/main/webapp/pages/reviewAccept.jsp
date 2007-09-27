<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>

<div class="tableBorder">

<a name="Reject of accept the experiment"/>

<table class="table" width="100%" cellspacing="1" cellpadding="2">
    <tr class="tableRowOdd">
        <td width="50%" align="center" bgcolor="blue">
            <html:submit property="dispatch"
                titleKey="exp.button.accept.titleKey">
                <bean:message key="exp.button.accept"/>
            </html:submit>
        </td>
        <td width="50%" align="center" bgcolor="lightblue">
            <html:submit property="dispatch"
                titleKey="exp.button.review.titleKey">
                <bean:message key="exp.button.review"/>
            </html:submit>
        </td>
    </tr>
</table>

</div>