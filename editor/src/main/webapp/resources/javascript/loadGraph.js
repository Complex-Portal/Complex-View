/**
 * Created by IntelliJ IDEA.
 * User: cjandras
 * Date: 14/07/11
 * Time: 14:33
 * To change this template use File | Settings | File Templates.
 */
var vis;
function loadGraph(xmlUrl){

    // wait for the data to be loaded prior, handy for larger network.
    var xml;
    // declare variable for interaction with cytoscapeWeb (will be inialized in drawGraph)

    jQuery.ajax({
        url: xmlUrl,
        success: function(gotXml) {
            xml = gotXml;
            drawGraph();
        },
        error: function() {
            var element = document.getElementById("cytoscapeweb");
            $(element).empty();
            $(element).append("An error occurred while loading the data.");
        }
    });

    // Enables the shapes present in the GraphML input to be displayed in the Flash panel.

    function drawGraph() {

        var options = {
            swfPath: "/editor/resources/swf/CytoscapeWeb",
            flashInstallerPath: "/editor/resources/swf/playerProductInstall"
        };

        // whether the edges should be merged or not
        var merged = true;

        // loadData and draw
        vis = new org.cytoscapeweb.Visualization("cytoscapeweb", options);

        // callback when Cytoscape Web has finished drawing
        vis.ready(function() {

            vis.edgesMerged(merged);

            vis.addContextMenuItem("Select first neighbours", "nodes",
                    function (evt) {
                        // Get the right-clicked node:
                        var rootNode = evt.target;

                        // Get the first neighbors of that node:
                        var fNeighbors = vis.firstNeighbors([rootNode]);
                        var neighborNodes = fNeighbors.neighbors;

                        // Select the root node and its neighbors:
                        vis.select([rootNode]).select(neighborNodes);
                    }
                    );

            // TODO in the node right click, link out to the service database using that molecule id
            // TODO edge > right click, link out to the service database using that interaction AC.

//            vis.addContextMenuItem("Link out to source web site", "nodes",
//                                  function (evt) {
//                                      // Get the right-clicked node:
//                                      var target = evt.target;
//
//                                      var identifier = target.data[ 'identifier' ];
//
//                                      // get url template
//                                      var url='http://www.google.com/q=${ac}}';
//
//                                      // open URL for that molecule
//                                      window.open(url.replace('${ac}}',identifier),'_blank',null);
//                                  }
//                    );

            vis.addContextMenuItem("Expand network around this molecule", "nodes",
                    function (evt) {
                        // Get the right-clicked node:
                        var target = evt.target;

                        var crossRef = target.data['identifier'].split('#');
                        var identifier = crossRef[1];

                        // update the search field
                        var searchQuery = document.getElementById('queryTxt').value;
                        var newQuery = '(' + searchQuery + ') OR id:' + identifier;
                        document.getElementById('queryTxt').value = newQuery;

                        // submit search
                        document.mainForm.quickSearchBtn.click();
                    }
                    );

            // add a listener for when nodes and edges are clicked
            vis.addListener("click", "nodes", function(event) {
                handle_node_click(event);
            });

            vis.addListener("click", "edges", function(event) {
                clear();
                handle_edge_click(event);
            });

            function handle_edge_click(event) {
                var target = event.target;

                clear();
                // print stuff here (eg. method, type, pmid, author, confidence value...).
            }

            function handle_node_click(event) {
                var target = event.target;

                clear();
                print('<p><b><u>Node Properties</u></b></p>');

                print('<b>Molecule type</b>: ' + target.data['type'] + '<br/>');
                print('<b>Interactor</b>: ' + target.data['label'] + '<br/>');
                var species = target.data['specie'];
                if (species != null) {
                    print('<b>Species</b>: ' + species + '<br/>');
                }

                var crossRef = target.data['identifier'].split('#');
                var link = null;
                var db = crossRef[0];
                if (db == 'uniprotkb') {
                    link = 'http://www.uniprot.org/uniprot/' + crossRef[1];
                } else if (db == 'chebi') {
                    link = 'http://www.ebi.ac.uk/chebi/searchId.do?chebiId=' + crossRef[1];
                }
                print('<b>Identifier</b>: ');
                if (link != null) {
                    print('<a href="' + link + '" target="_blank">' + crossRef[1] + '</a>');
                } else {
                    print(crossRef[1] + ' (' + crossRef[0] + ')' + '<br/>')
                }
            }

            function clear() {
                document.getElementById("note").innerHTML = "";
            }

            function print(msg) {
                document.getElementById("note").innerHTML += msg;
            }
        });

        var style = {
            nodes: {
                shape: { passthroughMapper: { attrName: "shape" } }
            }
        };

        var draw_options = {
            // set visual style
            visualStyle: style,

            // your data goes here
            network: xml
        };

        vis.draw(draw_options);

        graphResize();
    }
    // showhide('cytoscapeweb');
    //showhide('graphController');
}


    ///////////////////////////////
// Cytoscape Web interactions

function selectMerged() {
    document.getElementById('mergeOn').style.fontWeight='bold';
    document.getElementById('mergeOff').style.fontWeight='normal';

    merged = true;
    vis.edgesMerged( merged );
}

function unselectMerged() {
    document.getElementById('mergeOn').style.fontWeight='normal';
    document.getElementById('mergeOff').style.fontWeight='bold';

    merged = false;
    vis.edgesMerged( merged );
}

function selectForceDirectedLayout() {
    document.getElementById('forceDirectedLayout').style.fontWeight='bold';
    document.getElementById('radialLayout').style.fontWeight='normal';
    document.getElementById('circleLayout').style.fontWeight='normal';
    vis.layout('ForceDirected');
}

function selectRadialLayout() {
    document.getElementById('forceDirectedLayout').style.fontWeight='normal';
    document.getElementById('radialLayout').style.fontWeight='bold';
    document.getElementById('circleLayout').style.fontWeight='normal';
    vis.layout('Radial');
}

function selectCircleLayout() {
    document.getElementById('forceDirectedLayout').style.fontWeight='normal';
    document.getElementById('radialLayout').style.fontWeight='normal';
    document.getElementById('circleLayout').style.fontWeight='bold';
    vis.layout('Circle');
}

function graphResize() {
    var heigth = $(window).height() - 290;
    var x = document.getElementById('cytoscapeweb');
    x.style.height = heigth + 'px';
}