<%
response.setHeader("Cache-Control","no-cache"); //HTTP 1.1
response.setHeader("Pragma","no-cache");        //HTTP 1.0
response.setDateHeader ("Expires", 0);          //prevents caching at the proxy server
%>

<%@ page import="uk.ac.ebi.intact.model.CvIdentification,
                 uk.ac.ebi.intact.model.CvInteraction,
                 uk.ac.ebi.intact.model.CvInteractionType"%>
<%@ page import="uk.ac.ebi.intact.webapp.search.struts.util.SearchConstants"%>
<%@ page language="java" %>

<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://ebi.ac.uk/intact/commons"      prefix="intact"%>

<%--
    Search view which contains testfields to specify the query.
    Term to specify are so far:

    * searchObject  -> the object to be searched for
    * acNumber      -> the AC number to search for
    * shortlabel    -> the shortlabel to search for
    * description   -> the description to search for

    It also contains radiobuttons to specify if all search conditions should be found or any
    At the end there is a button to submit the data

    @author: anja, Catherine Leroy, Samuel Kerrien
--%>

<html:html>
<head>
<title>IntAct Database Search</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<link href="/layouts/styles/intact.css" rel="stylesheet" type="text/css">
<script language="javascript" type="text/javascript">
<!--

    var idArrayAll         = new Array("Interaction1", "Interaction2", "Interaction3", "Interaction4",
                                       "Experiment1", "Experiment2", "Experiment3", "Experiment4", "Experiment5",
                                       "Protein1", "Protein2", "Protein3", "Any1", "Any2", "Any3", "Cv1", "Cv2", "Cv3");
    var idArrayAny         = new Array("Any1", "Any2", "Any3");
    <%--var idArrayAny         = new Array("Interaction1","Experiment1", "Experiment2", "Any1", "Any2", "Any3");--%>
    var idArrayInteraction = new Array("Interaction1", "Interaction2", "Interaction3", "Interaction4");
    var idArrayExperiment  = new Array("Experiment1", "Experiment2", "Experiment3", "Experiment4", "Experiment5");
    var idArrayProtein     = new Array("Protein1", "Protein2", "Protein3");
    var idArrayCv          = new Array("Cv1", "Cv2", "Cv3");


    function openrow(rowsIDArray){
        for(var i=0; i< rowsIDArray.length; i++){
            var tag = document.getElementById(rowsIDArray[i]);
              if (navigator.userAgent.indexOf("Netscape6") != -1) {
                //if(tag.style.visibility=="hidden";){
                    tag.style.visibility="visible";
                    //alert("showing row: " + rowsIDArray[i]);
                //}
              }
              else{
                //if(tag.style.display=="none";){
                    tag.style.display="";
                    //alert("showing row: " + rowsIDArray[i]);
                //}
            }
        }
    }

    function closerow(rowsIDArray){
        for(var i=0; i< rowsIDArray.length; i++){
            var tag = document.getElementById(rowsIDArray[i]);
              if (navigator.userAgent.indexOf("Netscape6") != -1) {
              //if(tag.style.visibility=="visible";){
                    tag.style.visibility="hidden";
                    //alert("hiding row: " + rowsIDArray[i]);
                }
              //}
              else{
              //	if(tag.style.display=="";){
                    tag.style.display="none";
                    //alert("hiding row: " + rowsIDArray[i]);
                //}
            }
        }
    }


    // this function resets all form elements to its default value
    // it is used with the resetbutton and with the change of one radiobutton
    function resetForm(theValue){
        // first set the radiobutton to the the given value
        var btn = document.advancedForm["searchObject"];
        for (var x = 0;x < btn.length; x++){
            if(btn[x].checked==true){
                btn[x].checked=false;
                break;
            }
        }
        for (var x = 0;x < btn.length; x++){
            if(btn[x].value==theValue){
                btn[x].checked=true;
                // hide and show the correspomding rows
                closerow(idArrayAll);
                if(btn[x].value == "any"){
                   openrow(idArrayAny);
                }
                if(btn[x].value == "uk.ac.ebi.intact.model.Experiment"){
                   openrow(idArrayExperiment);
                }
                if(btn[x].value == "uk.ac.ebi.intact.model.InteractionImpl"){
                   openrow(idArrayInteraction);
                }
                if(btn[x].value == "uk.ac.ebi.intact.model.CvObject"){
                   openrow(idArrayCv);
                }
                if(btn[x].value == "uk.ac.ebi.intact.model.PolymerImpl"){
                   openrow(idArrayProtein);
                }
                break;
            }
        }

        // clear the ac number
        document.advancedForm["acNumber"].value = "";
        // clear the shortlabel
        document.advancedForm["shortlabel"].value = "";
        // clear the description
        document.advancedForm["description"].value = "";
        // clear the fulltext search
        //document.advancedForm["fulltext"].value = "";
        // clear the cvTopic textfield
        document.advancedForm["annotation"].value = "";
        // clear the cvDatabase textfield
        document.advancedForm["xRef"].value = "";

        // reset the drop down list of cvTopics
        var topic = document.advancedForm["cvTopic"];
        topic[0].selected = true;

        // reset the drop down list of cvDatabase
        var xref = document.advancedForm["cvDB"];
        xref[0].selected = true;

        // reset the drop down list of cvInteraction
        var interaction = document.advancedForm["cvInteraction"];
        interaction[0].selected = true;

        // reset the drop down list of cvInteractionType
        var interactionType = document.advancedForm["cvInteractionType"];
        interactionType[0].selected = true;

        // reset the drop down list of cvIdentification
        var identification = document.advancedForm["cvIdentification"];
        identification[0].selected = true;

        //alert("reset done!");
    }

    function hideLists(){
        closerow(idArrayAll);
        // check if 'any' is checked
        if(getRadioButton("document.advancedForm.searchObject[0]", "document.advancedForm.searchObject[0]")){
              //alert("any checked");
              openrow(idArrayAny);
        }
        // check if 'cv' is checked
        if(getRadioButton("document.advancedForm.searchObject[1]", "document.advancedForm.searchObject[1]")){
              //alert("cv checked");
              openrow(idArrayCv);
        }
        // check if 'experiment' is checked
        if(getRadioButton("document.advancedForm.searchObject[2]", "document.advancedForm.searchObject[2]")){
              //alert("experiment checked");
              openrow(idArrayExperiment);
        }
        // check if 'interaction' is checked
        if(getRadioButton("document.advancedForm.searchObject[3]", "document.advancedForm.searchObject[3]")){
              //alert("interaction checked");
              openrow(idArrayInteraction);
        }
        // check if 'protein' is checked
        if(getRadioButton("document.advancedForm.searchObject[4]", "document.advancedForm.searchObject[4]")){
              //alert("protein checked");
              openrow(idArrayProtein);
        }

    }

    // got the following function from 'http://ufia.hku.hk/ufiaLibrary/javascript/javascript.htm#getRadioButton'

    function getRadioButton(radioButtonNS,radioButtonIE) {
      var NS=(navigator.appName=='Netscape');
      return(NS?eval(radioButtonNS).checked:eval(radioButtonIE).checked);
    }

    <%-- script copied from 'http://www.jguru.com/faq/view.jsp?EID=1074755' and modified--%>

    // Function that open the CvBrowser and gives it the requires parameters.
    //
    // param: aCvName     the CV to be displayed.
    // param: contextPath the context of the current application, will be used to access CvBrowser.
    // param: aFieldForm  the HTML form name that should be updated when the user clicks on a term.
    //
    function openCvBrowser(aCvName, contextPath, aFieldForm) {
      aBase = contextPath + '/do/showGraph';
      doOpenRemote(aBase + '?cvName=' + aCvName + '&field=' + aFieldForm,'preview','500','600','scrollbars','form');
    }

    function doOpenRemote(aURL, newName, aHEIGHT, aWIDTH, aFeatures, orgName){
      if (aHEIGHT == "*"){ aHEIGHT = (screen.availHeight - 80) };
      if (aWIDTH == "*"){ aWIDTH = (screen.availWidth - 30) };
      var newFeatures = "height=" + aHEIGHT + ",innerHeight=" + aHEIGHT;
      newFeatures += ",width=" + aWIDTH + ",innerWidth=" + aWIDTH;
      if (window.screen){
        var ah = (screen.availHeight - 30);
        var aw = (screen.availWidth - 10);
        var xc = (( aw - aWIDTH ) / 2);
        var yc = (( ah - aHEIGHT ) /  2);
        newFeatures += ",left=" + xc + ",screenX=" + xc;
        newFeatures += ",top=" + yc + ",screenY=" + yc;
        newFeatures += ",resizable=yes";
        newFeatures += "," + aFeatures
      };
      var newWin = openWin(aURL, newName, newFeatures, orgName);
      newWin.focus();
      return newWin
    }

    function openWin(newURL, newName, newFeatures, orgName) {
      var newWin = open(newURL, newName, newFeatures);
      if (newWin.opener == null)
        newWin.opener = window;
      newWin.opener.name = orgName;
      return newWin
    }

// -->

</script>


</head>
<body leftmargin="0" topmargin="0" style="background-color: rgb(255, 255, 255);" marginheight="0"
      marginwidth="0" onload="hideLists()">

<html:form action="/advsearch">



<%
    String errorMessage = (String) session.getAttribute(SearchConstants.ERROR_MESSAGE);
    if(errorMessage != null && !(errorMessage.equals(""))){
%>
         <h3><font color="red"><%= errorMessage %></font></h3>
<%
    }
%>
<table style="width: 100%;">
<tbody>
    <tr>
    <td></td>
    <td align="right" valign="top">
         <intact:documentation section="advancedSearch" title="Help Topics"/>
     </td>
    </tr>
    <tr>
     <td></td>
     <td></td>
    </tr>
</tbody>
</table>

<table style="width: 100%; background-color: rgb(241, 245, 248);" border="1"
       bordercolor="#4b9996" cellpadding="5" cellspacing="0">
    <tbody>
        <tr>
            <td colspan="5" rowspan="1" class="headerdark" style="vertical-align: top;">
                <span class="whitelargerheadertext">Search</span>
            </td>
        </tr>

        <tr>
              <td class="headerlight" style="vertical-align: top;"><br>
                  <html:submit>Search</html:submit>
                  <html:button property="doReset" value="Reset" onclick="resetForm('any')"/>
              </td>
              <td colspan="2" rowspan="1" class="headerdarkmid" style="vertical-align: middle;">Perform the search according to the criteria selected<br>
              </td>
              <td colspan="1" rowspan="7" class="headerdark" style="vertical-align: top;">
              </td>
              <td id="Interaction2" colspan="1" rowspan="1" class="headerlight" style="vertical-align: middle;">Interaction Examples<br>
              </td>
              <td id="Experiment3" colspan="1" rowspan="1" class="headerlight" style="vertical-align: middle;">Experiment Examples<br>
              </td>
              <td id="Protein1" colspan="1" rowspan="1" class="headerlight" style="vertical-align: middle;">Protein Examples<br>
              </td>
              <td id="Cv1" colspan="1" rowspan="1" class="headerlight" style="vertical-align: middle;">CV Examples<br>
              </td>
              <td id="Any1" colspan="1" rowspan="1" class="headerlight" style="vertical-align: middle;">Examples<br>
              </td>

        </tr>
        <tr>
            <td class="headerlight" style="vertical-align: top;"><br>
                <!-- radiobuttons to specify the search object-->
                <table>
                <tr>
                    <td class="headerlight" style="vertical-align: middle;">
                    <html:radio property="searchObject" value="any" onclick="resetForm(this.value); " />
                       Any object
                     </td>
                     <td>
                     </td>
                </tr>
                </table>
                <table>
                <tr>
                    <td  class="headerlight" style="vertical-align: middle;">
                        <html:radio property="searchObject" value="uk.ac.ebi.intact.model.CvObject" onclick="resetForm(this.value); " />
                            Controlled Vocabulary Term
                    </td>
                    <td style="vertical-align: middle;">
                        <intact:documentation section="CVS" />
                    </td>
                </tr>
                </table>
                <table>
                <tr>
                    <td class="headerlight" style="vertical-align: middle;">
                        <html:radio property="searchObject" value="uk.ac.ebi.intact.model.Experiment" onclick="resetForm(this.value); " />
                          Experiment
                    </td>
                    <td  style="vertical-align: middle;">
                        <intact:documentation section="Experiment" />
                    </td>
                </tr>
                </table>
                <table>
                <tr>
                    <td class="headerlight" style="vertical-align: middle;">
                        <html:radio property="searchObject" value="uk.ac.ebi.intact.model.InteractionImpl" onclick="resetForm(this.value); " />
                          Interaction
                    </td>
                    <td style="vertical-align: middle;">
                        <intact:documentation section="Interaction" />
                    </td>
                </tr>
                </table>
                <table>
                <tr>
                    <td class="headerlight" style="vertical-align: middle;">
                        <html:radio property="searchObject" value="uk.ac.ebi.intact.model.PolymerImpl" onclick="resetForm(this.value);" />
                          Interactor
                     </td>
                     <td style="vertical-align: middle;">
                       <intact:documentation section="Interactor" />
                     </td>
                </tr>
                </table>
            </td>

            <td colspan="1" rowspan="1" class="headerdarkmid" style="vertical-align: middle;">which has<br>
            </td>

             <td id="h1" colspan="1" rowspan="4" style="vertical-align: top; color: #003063; font-family: Arial, Helvetica, sans-serif; font-size: 10pt;">

                        <ul>
                          <span align="center"><h4>Usage Hints</h4><br> </span>
                          <li>'*' or '%' will match any number of characters
                              when used at the beginning or end of a term<br />
                          </li>
                          <li>a list of terms separated by ',' (comma) or<br>
                              by ' ' (space) will be interpreted as "match<br>
                              any of these (OR)"<br />
                          </li>
                          <li>prefixing a term with '+' will be <br>
                              interpreted as "match all of these (AND)"<br />
                          </li>
                            <li>prefixing a term with '-' will exclude <br>
                              the results containing that term<br />
                          </li>
                          <li>word phrases have to be put in double quotes<br />
                          </li>
                          <li>word phrases without quotes will be <br>
                              interpreted as "match any of these (OR)"<br />
                          </li>

                        </ul>
            </td>



            <td id="Any2" colspan="1" rowspan="7" class="headerlight" style="vertical-align: middle;">
                  <img src="<%=request.getContextPath()%>/images/anyExample.png" align="left" border="0">
            </td>

            <td id="Cv2" colspan="1" rowspan="7" class="headerlight" style="vertical-align: middle;">
                  <img src="<%=request.getContextPath()%>/images/cvExample.png" align="left" border="0">
            </td>

            <td id="Interaction3" colspan="1" rowspan="8" class="headerlight" style="vertical-align: middle;">
                  <img src="<%=request.getContextPath()%>/images/interExample.png" align="left" border="0">
            </td>

            <td id="Experiment4" colspan="1" rowspan="9" class="headerlight" style="vertical-align: middle;">
                  <img src="<%=request.getContextPath()%>/images/expExample.png" align="left" border="0">
            </td>

            <td id="Protein2" colspan="1" rowspan="7" class="headerlight" style="vertical-align: middle;">
                  <img src="<%=request.getContextPath()%>/images/protExample.png" align="left" border="0">
            </td>

        </tr>
        <tr>
            <!-- textfield to specify the AC number -->
            <td class="headerlight" style="vertical-align: top;">
               <html:text property="acNumber" size="40"/>
            </td>
            <td class="headerdarkmid" style="vertical-align: middle; white-space: nowrap;">accession number
                                                                     <intact:documentation section="BasicObject.ac" /><br>
            </td>

        </tr>
        <tr>
            <!--textfield to specify the shortlabel -->
            <td class="headerlight" style="vertical-align: top;"><small><small>
               <html:text property="shortlabel" size="40"/>
            </td>
            <td class="headerdarkmid" style="vertical-align: middle;">short label
                                                                      <intact:documentation section="AnnotatedObject.shortLabel" /><br>
            </td>
        </tr>
        <tr>
            <!--textfield to specify the description -->
            <td class="headerlight" style="vertical-align: top;"><small><small><small>
               <html:text property="description" size="40"/>
            </td>
            <td class="headerdarkmid" style="vertical-align: middle;">description
                                                                      <intact:documentation section="AnnotatedObject.fullName" /><br>
            </td>
        </tr>

        <%--
        <tr>
           <td class="headerlight" style="vertical-align: top;"><small><small>
               <html:text property="fulltext" size="40" /></small></small>
           </td>
           <td  colspan="2" rowspan="1" class="headerdarkmid" style="vertical-align: top;">anywhere in the object annotation (fulltext search)<br />
           </td>
        </tr>
        --%>

            <tr>
                <td class="headerlight" style="vertical-align: top;">

                    Database<br>

                   <html:select property="cvDB" style="width: 200">
                        <html:options collection="cvDatabases" property="shortlabel" labelProperty="shortlabel" />
                   </html:select>
                   <br>
                   <img src="<%=request.getContextPath()%>/images/spacer.gif" width="100" height="5" border="0"><br>
                   <html:text property="xRef" size="40" />

                   </td>

                  <td colspan="2" rowspan="1" class="headerdarkmid" style="vertical-align: middle;"> as a crossreference referring to this database
                                                                                                    <intact:documentation section="AnnotatedObject.Xref" /></td>
              </tr>


              <tr>
                 <td class="headerlight" style="vertical-align: top;">

                      Topic<br>
                      <html:select property="cvTopic" style="width: 200">
                          <html:options collection="cvTopics" property="shortlabel" labelProperty="shortlabel" />
                      </html:select><br>
                      <img src="<%=request.getContextPath()%>/images/spacer.gif" width="100" height="5" border="0"><br>
                      <html:text property="annotation" size="40" />
                </td>
                <td colspan="2" rowspan="1" class="headerdarkmid" style="vertical-align: middle;"> as an annotation with this annotation topic
                                                                                                   <intact:documentation section="AnnotatedObject.Annotation" /></td>
             </tr>


        <tr  id="Experiment1">
            <td class="headerlight" style="vertical-align: top;">
                      Interaction Detection<br>
                      <html:select property="cvInteraction" style="width: 200">
                          <html:options collection="cvInteractions" property="shortlabel" labelProperty="shortlabel" />
                      </html:select><br>
                       <img src="<%=request.getContextPath()%>/images/spacer.gif" width="100" height="5" border="0"><br>
                       <html:checkbox property="cvInteractionIncludeChildren">Include children in search</html:checkbox>
                       <input type="hidden" name="cvInteractionIncludeChildren" value="false">
                       <br>
                       <input type="button" name="button1" value="Interaction Detection Browser" style="width: 200" onclick="openCvBrowser('<%= CvInteraction.class.getName() %>', '<%=request.getContextPath()%>', 'cvInteraction')">
                </td>

            <td colspan="2" rowspan="1" class="headerdarkmid" style="vertical-align: middle;"> where this method has been used to determine the interaction
                                                                                               <intact:documentation section="editor.experiment" /> </td>
            <td colspan="1" rowspan="1" class="headerdark" style="vertical-align: top;"></td>
       </tr>

       <tr  id="Experiment2">
            <td class="headerlight" style="vertical-align: middle;">
            Participant Detection<br>
                      <html:select property="cvIdentification" style="width: 200">
                          <html:options collection="cvIdentifications" property="shortlabel" labelProperty="shortlabel" />
                      </html:select><br>
                       <img src="<%=request.getContextPath()%>/images/spacer.gif" width="100" height="5" border="0"><br>
                      <html:checkbox property="cvIdentificationIncludeChildren">Include children in search</html:checkbox>
                      <input type="hidden" name="cvIdentificationIncludeChildren" value="false">
                      <br>
                      <input type="button" name="button2" value="Participant Detection Browser" style="width: 200" onclick="openCvBrowser('<%= CvIdentification.class.getName() %>', '<%=request.getContextPath()%>', 'cvIdentification')">
            </td>

            <td colspan="2" rowspan="1" class="headerdarkmid" style="vertical-align: middle;">where this method has been used to determine the participants in the interaction
                                                                                              <intact:documentation section="editor.experiment" /> </td>
            <td colspan="1" rowspan="1" class="headerdark" style="vertical-align: top;"></td>
          </tr>

            <tr  id="Interaction1">
            <td class="headerlight" style="vertical-align: top;">
                      Interaction Type<br>
                      <html:select property="cvInteractionType" style="width: 200">
                          <html:options collection="cvInteractionTypes" property="shortlabel" labelProperty="shortlabel" />
                      </html:select><br>
                       <img src="<%=request.getContextPath()%>/images/spacer.gif" width="100" height="5" border="0"><br>
                      <html:checkbox property="cvInteractionTypeIncludeChildren">Include children in search</html:checkbox>
                      <input type="hidden" name="cvInteractionTypeIncludeChildren" value="false">
                      <br>
                      <input type="button" name="button3" value="CvInteractionType Browser" style="width: 200" onclick="openCvBrowser('<%= CvInteractionType.class.getName() %>', '<%=request.getContextPath()%>', 'cvInteractionType')">
             </td>

            <td colspan="2" rowspan="1" class="headerdarkmid" style="vertical-align: middle;"> having this interaction type
                                                                                               <intact:documentation section="Interaction" /></td>
            <td colspan="1" rowspan="1" class="headerdark" style="vertical-align: top;"></td>
       </tr>


         <tr>
            <!--radiobuttons to specify the type of connecting the search terms -->
            <td class="headerlight">
                <table>
                    <tr>
                     <td  class="headerlight" style="vertical-align: top;"><html:radio property="connection" value="and" /> all of the above (and)</td>
                     <td class="headerlight" valign="top" align="right"></td>
                    </tr>
                    <tr>
                     <td  class="headerlight" style="vertical-align: top;"><html:radio property="connection" value="or" /> any of the above (or)</td>
                     <td></td>
                    </tr>
                </table>
            </td>
            <td colspan="2" rowspan="1" class="headerdarkmid" style="vertical-align: top;">criteria combination<intact:documentation section="advancedSearch.basicOverview.criteriaCombination" /><br> </td>
            <td colspan="1" rowspan="1" class="headerdark" style="vertical-align: top;"></td>
        </tr>


        <tr>
            <td class="headerlight" style="vertical-align: top;"><br>
                <html:submit>Search</html:submit>
                <html:button property="doReset" value="Reset" onclick="resetForm('any')"/>
            </td>
            <td colspan="2" rowspan="1" class="headerdarkmid" style="vertical-align: middle;">Perform the search according to the criteria selected<br>
            </td>
            <td colspan="1" rowspan="1" class="headerdark" style="vertical-align: top;"></td>
            <td id="Interaction4" colspan="1" rowspan="1" class="headerlight" style="vertical-align: middle;">Interaction Examples<br>
            </td>
            <td id="Experiment5" colspan="1" rowspan="1" class="headerlight" style="vertical-align: middle;">Experiment Examples<br>
            </td>
            <td id="Protein3" colspan="1" rowspan="1" class="headerlight" style="vertical-align: middle;">Protein Examples<br>
            </td>
            <td id="Cv3" colspan="1" rowspan="1" class="headerlight" style="vertical-align: middle;">CV Examples<br>
            </td>
            <td id="Any3" colspan="1" rowspan="1" class="headerlight" style="vertical-align: middle;">Examples<br>
            </td>


        </tr>

    </tbody>
</table>

</html:form>
</body>

</html:html>