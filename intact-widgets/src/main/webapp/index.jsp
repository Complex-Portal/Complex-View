<html>
    <head>
        <title>Intact Widgets</title>
        <script type="text/javascript" src="resources/javascript/functions.js"></script>
        <script type="text/javascript" src="resources/javascript/InteractionRepresentation/resources/raphael.js"></script>
        <script type="text/javascript" src="resources/javascript/InteractionRepresentation/resources/jquery-1.4.3.min.js"></script>
        <script type="text/javascript" src="resources/javascript/InteractionRepresentation/resources/jquery-ui-1.8.10.custom.min.js"></script>

        <!-- Interaction Representation -->
        <script type="text/javascript" src="resources/javascript/InteractionRepresentation/resources/annotOverlaping.js"></script>
        <script type="text/javascript" src="resources/javascript/InteractionRepresentation/InteractionRepresentation/ui.Interaction.js"></script>
        <script type="text/javascript" src="resources/javascript/InteractionRepresentation/InteractionRepresentation/resources/js/featureDrawer.js"></script>
        <script type="text/javascript" src="resources/javascript/InteractionRepresentation/InteractionRepresentation/resources/js/utils.js"></script>
        <script type="text/javascript" src="resources/javascript/InteractionRepresentation/InteractionRepresentation/resources/js/participantDrawer.js"></script>
        <script type="text/javascript" src="resources/javascript/InteractionRepresentation/InteractionRepresentation/resources/js/rangeStatusFunctionCollection.js"></script>
        <script type="text/javascript" src="resources/javascript/InteractionRepresentation/InteractionRepresentation/resources/js/shapeDrawer.js"></script>

        <!-- Jmol -->
        <script type="text/javascript" src="resources/javascript/Jmol/ui.Jmol.js"></script>
        <script type="text/javascript" src="resources/javascript/Jmol/resources/jmol-12.0.48/Jmol.js"></script>


    </head>
    <body style="font-family:Arial;font-size:12">


        <h3>Intact Widgets Demo</h3>
        <br/>
        <hr/>
        Examples:

        <!-- Functions will be assigned in the <script> part in this file-->
        <a id="EBI-1554232" href="">EBI-1554232 </a> |
        <a id="EBI-477390" href="">EBI-477390 </a> |
        <a id="EBI-1102040" href="">EBI-1102040 </a> |
        <a id="EBI-1168819" href="">EBI-1168819 </a> |
        <a id="EBI-625235" href="">EBI-625235 </a> |
        <a id="EBI-696373" href="">EBI-696373 </a> |
        <a id="EBI-1178028" href="">EBI-1178028 </a> |
        <a id="EBI-2462697" href="">EBI-2462697 </a> |
        <a id="EBI-2627646" href="">EBI-2627646 </a> |
        <a id="EBI-2607906" href="">EBI-2607906 </a>

        <br/>
        <hr/>
        <br/>
        <br/>

        <table id="dataTable">
            <tr><th id="interactionAc">EBI-1554232</th></tr>
			<tr><td id="featureDiv"></td></tr>
			<tr><td>Display 3D information
			<input id="displayJmol" type="checkbox" name="displayJmol" value="jmol" onclick="displayJmol()"/>
			<br/>(by checking this box a Jmol window will be opened and features and interactors clicked in the widget will be displayed in this window)</td></tr>
			<tr id="jmolRow"></tr>
		</table>

        <%
            String contextPath = request.getContextPath();
        %>

        <script type="text/javascript">
            // all functions can be found in the file 'functions.js'
            // the variables interactionPlugin and jmolPlugin are used in these functions

            var contextPath = "<%=request.getContextPath()%>";
            var interactionPlugin = null;
            var jmolPlugin = null;

            $(document).bind('interactor_selected', interactorSelected);

            $(document).bind('pdb_selected', pdbSelected);

            $(document).bind('feature_selected', featureSelected);

            $('a').click(function(e){
                e.preventDefault();
                // get interactionAc 'EBI-xxxxx'
                $('#interactionAc').html(e.currentTarget.attributes.id.textContent);
                initiateInteractionPlugin(e.currentTarget.attributes.id.textContent);
            });

            initiateInteractionPlugin('EBI-1554232');

            var displayJmol = function(){
                if($('#displayJmol:checked').val() === undefined){
                    if(jmolPlugin != null){
                        jmolPlugin.Jmol('reset');
                    }
                    $('#jmolRow').hide();
                    interactionPlugin.Interaction('unhighlightRegion');
                    interactionPlugin.Interaction('unhighlightFeature');
                }else{
                    if(jmolPlugin == null){
                        initiateGraph();
                    }
                    jQuery('#jmolRow').show();
                }
            }
        </script>
    </body>
</html>
