<!--
   - Copyright (c) 2002 The European Bioinformatics Institute, and others.
   - All rights reserved. Please see the file LICENSE
   - in the root directory of this distribution.
   -
   - Start page of MiNe
   -
   - @author Andreas Groscurth (groscurt@ebi.ac.uk)
-->

<%@ page language="java"%>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>

<style type="text/css">
.headline {font-weight: bold; font-size:20px; text-decoration:underline;}
</style>

<!-- Introduction -->
<p class="headline">Introduction</p>
<p>To understand the connection of proteins to each other it is possible to consider
the interactions in which one can find them. Though there is an existing problem that those protein –
interaction networks are mostly huge and therefore difficult to keep track of.
This leads to the fact that it is not easy to detect a subgraph which contains a small number
of proteins one is interested in and actually to detect the minimal subgraph of these proteins.<br><br>
The MiNe project undertakes the task of computing the minimal connecting networks for a given set of proteins.
It forwards them to the application HierachView which visualise them. This provides an easy and obvious way to gain information about the connection of proteins.
<br>
<form action="<%=request.getContextPath()%>/do/search" focus="AC">
You can enter a search phrase here:&nbsp;
<input type="text" name="AC" size="35">
&nbsp;<input type="submit" value="Find network"></form></p>
<p>As search phrases can be used:
<ol><li>IntAct Ac: EBI-44444</li>
<li>Gene names: lsm7</li>
<li>SPTR Ac: Q08162</li>
<li>SPTR Id: rr44_yeast</li>
<li>InterPro Ac: IPR001900</li>
<li>GO Id: GO:0000176</li></ol></p>
<p>&nbsp;</p>
<!-- Application flow -->
<p class="headline">Application flow</p>
<p>The application split itself into a pre – processing procedure and the actual application.
<ul><div style="font-weight:bold; font-size:12px; text-decoration:underline;">1. Pre – Processing:</div>
All interactions of the IntAct database are fetched and inserted into the application database table.
Based on these interactions the different connecting graphs foreach biosource are computed.
This prevents that the algorithm starts although the given proteins are not in a connecting graph.
So every interaction is assigned to a biosource and a ID for the connecting graph which the interaction is part of.
</ul>
<ul><div style="font-weight:bold; font-size:12px; text-decoration:underline;">2. Application:</div>
The application is easy to use. One can use the search panel on the left side to search for the several proteins
(search phrases are seperated via comma). If the search resturns an ambiguous result for a search,
all found proteins are displayed, grouped by the search phrases. One can now select the proteins someone
wants to calculate the minimal connecting network. This selection page is shown as long as the
search returns an ambiguous result.</ul>
If the search does not returns an ambiguous result, the algorithm computes the minimal connecting
networks and forward the result to HierachView to visualise it.</p>
<p>&nbsp;</p>
<!-- algorithm -->
<p class="headline">Algorithm</p>
<p>The algorithm which computes the minimal connecting network is a modified Shortest – Path – Algorithm
created by the Dutch computer scientist Edsger Dijkstra. In general the algorithm finds the shortest paths from one node to all other nodes in a directed graph
with nonnegative edge weights. </p>
The application is using an implementation of the the algorithm provided by
the <a href="http://www.jdsl.org" target="new">JDSL</a> library.
The JDSL library is the Java Data Structure Library and was developed at
the <a href="http://www.cs.brown.edu/cgc/cgc-brown.html" target="new">Center for Geometric Computing</a>,
<a href="http://www.cs.brown.edu/" target="new">Department of Computer Science</a>,
<a href="http://www.brown.edu/" target="new">Brown University</a>,
in cooperation with <a href="http://www.cs.jhu.edu/labs/cgc/" target="new">Center for Algorithm Engineering</a>,
<a href="http://www.cs.jhu.edu/" target="new">Department of Computer Science</a>,
<a href="http://www.jhu.edu/" target="new">Johns Hopkins University</a>.</p>
<p>Among other algorithms and data structures the library provides several classes to build
up a graph und running the Dijkstra algorithm on it. The implementation of the Dijkstra algorithm
was modified due to the fact that it is not computing the distance from the source node to all 
other nodes, but to compute the distance from the source node to a given end node.<br><br>
How the implementation of the JDSL library works can be understood in the following image (its searching
for the shortest path from node <i>a</i> to node <i>e</i>):<br>
<div align="center"><img src="<%=request.getContextPath()%>/images/djikstra.gif"></div>
<ol><li>the algorithm starts at node <i>a</i>. All outgoing edges are inspected.</li>
<li>the shortest path from <i>a</i> is the edge to node <i>d</i>, which then is marked as visited, because
there is no shorter path to node <i>d</i>. All outgoing edges from <i>d</i> are inspected.</li>
<li>the shortest path then from <i>a</i> is to node <i>b</i>. Again the node is marked and all outgoing edges
are inspected.</li>
<li>the same happens then with the path to node <i>c</i></li>
<li>and finally with node <i>e</i> where the algorithm stops because the end node is found</li></ol><br></p>
<p>The application uses three additional points to improve the performance of the algorithm:
<ol><li>If the end node is not reached after a specified number of leaps, the search is stopped</li>
<li>If the minimal networt for the nodes A, B and C is computed and while searching for the shortest
path from A to C B was found, no search from A to B is needed.</li>
<li>Same situation as 2), but additional: If in the shortest path from A to C one can
find node B, no search from B to C is needed, because all subpaths of the shortest 
path from A to C are also minimal.</li></ol></p>




