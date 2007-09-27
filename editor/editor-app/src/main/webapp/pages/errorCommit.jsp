<!--
- Author: Sugath Mudali (smudali@ebi.ac.uk)
- Version: $Id: error.jsp,v 1.5 2003/03/27 17:34:08 skerrien Exp $
- Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
- All rights reserved. Please see the file LICENSE in the root directory of
- this distribution.
-->

<%--
  - Displays an error message stored in the struts framework.
  --%>

<%@ page language="java" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld"  prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld"  prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>

<h1><font color="red">Error when commiting</font></h1>

<script type="text/javascript" language="JavaScript">
    var message = getMessage();
    document.write('<a href="mailto:intact-help@ebi.ac.uk?&body='
            + getMessage() + '">' + 'Send an already formated email to the intact-help?' + '</a>');

    function getClockTime()
    {
        var now    = new Date();
        var hour   = now.getHours();
        var minute = now.getMinutes();
        var second = now.getSeconds();
        var ap = "AM";
        if (hour   > 11) { ap = "PM";             }
        if (hour   > 12) { hour = hour - 12;      }
        if (hour   == 0) { hour = 12;             }
        if (hour   < 10) { hour   = "0" + hour;   }
        if (minute < 10) { minute = "0" + minute; }
        if (second < 10) { second = "0" + second; }
        var timeString = hour +
                         ':' +
                         minute +
                         ':' +
                         second +
                         " " +
                         ap;
        return timeString;
    }

    function getCalendarDate()
    {
        var months = new Array(13);
        months[0]  = "January";
        months[1]  = "February";
        months[2]  = "March";
        months[3]  = "April";
        months[4]  = "May";
        months[5]  = "June";
        months[6]  = "July";
        months[7]  = "August";
        months[8]  = "September";
        months[9]  = "October";
        months[10] = "November";
        months[11] = "December";
        var now         = new Date();
        var monthnumber = now.getMonth();
        var monthname   = months[monthnumber];
        var monthday    = now.getDate();
        var year        = now.getYear();
        if(year < 2000) { year = year + 1900; }
        var dateString = monthname +
                         ' ' +
                         monthday +
                         ', ' +
                         year;
        return dateString;
    }

    function getMessage() {
        var calendarDate = getCalendarDate();
        var clockTime = getClockTime();
        var message ='Hi,%0A%0AI got a commit Exception on the ' +
                      calendarDate + ' at ' + clockTime + '%0A%0AWhen it occured I was : ...';
        return message;
    }

</script>
</hr>