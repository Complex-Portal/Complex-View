<%--
  Created by IntelliJ IDEA.
  User: mkoch
  Date: 22/05/15
  Time: 16:30
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<html>
<table style="border: hidden;">
    <tr>
        <td>
            <div style="float:right">
                <img src="<c:url value="/resources/svg/smallMol.svg"/>">
            </div>
        <td>Small molecule</td>
    </tr>
    <tr>
        <td>
            <div style="float:right">
                <img src="<c:url value="/resources/svg/proteinBlob.svg"/>">
            </div>
        </td>
        <td>Protein</td>
    </tr>
    <tr>
        <td>
            <div style="float:right">
                <img src="<c:url value="/resources/svg/proteinBar.svg"/>">
            </div>
        </td>
        <td> - click or tap to toggle between circle and bar (bar shows binding sites, if
            known)
        </td>
    </tr>
    <tr>
        <td>
            <div style="float:right">
                <img src="<c:url value="/resources/svg/gene.svg"/>">
            </div>
        </td>
        <td>Gene</td>
    </tr>
    <tr>
        <td>
            <div style="float:right">
                <img src="<c:url value="/resources/svg/DNA.svg"/>">
            </div>
        </td>
        <td>
            DNA
        </td>
    </tr>
    <tr>
        <td>
            <div style="float:right">
                <img src="<c:url value="/resources/svg/RNA.svg"/>">
            </div>
        </td>
        <td>RNA</td>
    </tr>
</table>
</html>
