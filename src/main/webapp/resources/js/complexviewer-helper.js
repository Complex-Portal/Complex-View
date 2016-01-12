/**
    * Created by Maximilian Koch (mkoch@ebi.ac.uk).
    */
var xlv;

function loadComplexViewer(complexAc, data){
    var targetDiv = document.getElementById('networkContainer');
    xlv = new xiNET(targetDiv);
    xlv.legendCallbacks.push(function (colourAssignment) {
        var coloursKeyDiv = document.getElementById('colours');

        var table = "<table><tr style='height:10px;'></tr><tr><td style='width:80px;margin:10px;"
            + "background:#70BDBD;opacity:0.3;border:none;'>"
            + "</td><td>" + complexAc + "</td></tr>";

        if (colourAssignment) {
            var domain = colourAssignment.domain();
            //~ console.log("Domain:"+domain);
            var range = colourAssignment.range();
            //~ console.log("Range:"+range);
            table += "<tr style='height:10px;'></tr>";
            for (var i = 0; i < domain.length; i++) {
                //make transparent version of colour
                var temp = new RGBColor(range[i % 20]);
                var trans = "rgba(" + temp.r + "," + temp.g + "," + temp.b + ", 0.6)";
                table += "<tr><td style='width:75px;margin:10px;background:"
                    + trans + ";border:1px solid "
                    + range[i % 20] + ";'></td><td>"
                    + domain[i] + "</td></tr>";
            }
        }
        table += "</table>";
        coloursKeyDiv.innerHTML = table;
    });
    xlv.readMIJSON(data, true);
    xlv.autoLayout();
}
function exportSVG() {
    var xml = xlv.getSVG();
    var xmlAsUrl = 'data:image/svg;filename=xiNET-output.svg,';
    xmlAsUrl += encodeURIComponent(xml);
    window.open(xmlAsUrl, 'xiNET-output.svg');
}