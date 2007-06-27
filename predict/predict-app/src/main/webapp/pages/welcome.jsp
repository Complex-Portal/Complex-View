<%@ page import="uk.ac.ebi.intact.application.predict.business.PredictUser"%>
<!--
   - Copyright (c) 2002-2004 The European Bioinformatics Institute, and others.
   - All rights reserved. Please see the file LICENSE
   - in the root directory of this distribution.
   -
   - Hello World form page
   -
   - @author Samuel Kerrien (skerrien@ebi.ac.uk)
   - @version $Id$
-->

<%@ page language="java" %>

<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html"%>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c"%>

<%@ page errorPage="error.jsp" %>

<c:set var="species" value="${user.species}" scope="session"/>

<%-- The context path. --%>
<%
    PredictUser user = (PredictUser) session.getAttribute("user");

    String ctxpath = request.getContextPath();
%>

</br>
<font size="4">
<b>Introduction</b>
<br/>
</font>
<br/>
</p>
Uncovering the interactome is one of the primary challenges in the
post-genomic era. The topology information gained from the coverage of
interaction 		space can shed light on the likely function and
structure of proteins within the network (Vitkup et al, 2001). Here we present
a list of the 'best baits' 		which give the highest return on
experimental effort - i.e. those proteins which form 'hubs' in the interaction
network. These lists are 		generated using the Pay-As-You-Go
strategy (Lappe, M. Holm, L. 2004) which detects	and prioritises those
proteins which have the highest likelihood of 		being hubs based on the
current data within IntAct for various species. To illustrate the experimental
effort which could be saved by using 		the strategy recommended here,
take the Human interactome as an example. It is estimated that 50,000 experiments
would be required to cover the 		entire human interactome following a
purely random strategy for the selection of targets. The information gain here
is close to linear for 		each experiment. This could be drastically
reduced to less than 10,000 for 90% coverage of the entire human interactome by
using the proteins listed here as baits. This would save years of experimental
effort. This of course relies on the timely deposition of experimental data
into the IntAct database in order that the Pay-As-You-Go
algorithm should be fully uptodate and effective.
<br/>
<br/>
</p>
Presented below is a brief outline of the Pay-As-You-Go strategy, for further
details please see (Lappe, M. Holm, L. 2004). The link below displays the top 50
target proteins for use as bait in pull-down experiments for the species currently
contained within the IntAct database.
<br/>
<br/>
<br/>
<html:form action="/predict.do">
    <table>
        <tr>
            <td align="left">
               Display top 50 Pay-As-You-Go predicted protein targets for:
            </td>
            <td align="left">
                <html:select property="specie" value="<%=user.getDefaultChoice()%>">
                    <html:options name="species"/>
                </html:select>
            </td>
            <td><html:submit value="Go"/></td>
        </tr>
    </table>
</html:form>
<br/>
<br/>
<font size="4">
<b>The Pay-As-You-Go Algorithm</b>
<br/>
</font>
<br/></p>
The Pay-As-You-Go algorithm is a method to explore any scale-free network with
near-optimal efficiency. In the context of the interactome it is intuitive that
by focussing initial experimental efforts on proteins which form interaction
'hubs' the greatest number of protein interactions will be detected with the
least number of experiments. The essence of the algorithm is to determine
which proteins are likely to form such hubs, using only the data which is
currently available in the IntAct database and exploiting the scale-free
distribution of protein interaction networks. Essentially the algorithm explores
the network using the experimental interaction information that is currently
available to determine which proteins (that have not been used as bait as yet)
are seen most often as prey. These proteins are likely to form hubs because they
have the largest number of interactions and thus yield the most information when
used in experiments.
<br/>
<br/>

<br/>
<font size="3">
<b>Performance</b>
<br/>
</font>
<br/></p>


The efficiency of any given strategy to provide coverage for a scale-free network,
such as a protein interaction network, is dependent on the order in which baits
are utilized. By choosing those baits with the highest number of interactions
first, it is possible to uncover the network of interactions much more quickly.
The pay-as-you-go strategy dynamically explores this network, selecting those
proteins which have been pulled out as prey most often in previous experiments.
These are the baits which most likely have a large number of interactions and
these are the baits, which if selected first will allow faster and more efficient
coverage of the network.
<br/>
<br/>
Figure 1 shows how the pay-as-you-go strategy covers the network as compared to a
random strategy as tested on various <i>Saccharomyces cerevisiae</i> datasets - DIP
(Xenarios et al, 2000), CORE (Deane et al, 2002), CZ (Gavin et al, 2002) and MDS
(Ho et al, 2002) datasets. It demonstrates a large performance advantage over
current, essentially random strategies, for efficient and cost-effective coverage
of interactomes.
<br/>

<br/>
<center>
<img border="0" align="center"
    src="<%=ctxpath%>/images/PAYG_random_relative_performance.gif" width="350" height="400">
<img border="0" align="center"
    src="<%=ctxpath%>/images/PAYG_random_relative_performance_2.gif" width="350" height="400">
<font size="2">
<br/>
<br/>


<b>Figure 1: Relative Performance of Pay-As-You-Go vs Random Strategy for the detection
of new interactions as tested on MDS, DIP, CZ and CORE datasets</b>
<br/>

</font>
</center>
The relative performance advantage of pay-as-you-go vs. random plotted over the
fraction of proteins used as bait (right) demonstrates the huge initial coverage
provided by the pay-as-you-go strategy. Plotting the ratio of baits required in
both strategies in order to reach a certain fraction coverage shows a speed up
of upto a factor of 9 in the MDS data. Upto 80% coverage is reached about 1.5-2
times faster on the CORE and CZ datasets using pay-as-you-go while there is a
speed up of about 3 in the DIP and 4 in the MDS networks at this point. Overall,
the performance of the pay-as-you-go strategy seems to improve with increasing
density of the interaction networks. It should also be noted that the pay-as-you-go
strategy performs even better than this at confirming interactions, the results
are similar to those shown above.
<br/>
<br/>
<br/>
<font size="3">
<b>Algorithmic Principles</b><br>
</font>
    <br/>
    </p>
    Figure 2 illustrates the basic principles of the pay-as-you-go strategy.
    For an in depth description and discussion of the algorithm please see
    (Lappe, M, Holm, L. 2004).
    </p>
    The bait proteins form the set <i>P</i>, the prey which have not been used
    as bait form the set <i>Q</i> whilst the rest of the proteome for which no
    information is available form the set <i>R</i>. The objective is to reveal
    as much of R (i.e. move as much of R to Q) as possible whilst minimizing
    the number of new baits to be selected from set Q (i.e. to minimize the
    number of experiments needed). Prior to any experimental data on protein
    interaction data <i>P</i> and <i>Q</i> are empty and all proteins are
    contained within <i>R</i>. As more and more interactions are revealed in
    successive experiments <i>P</i> and <i>Q</i> contain everlarger fractions
    of the network. Finally after every protein has been used as bait <i>P</i>
    contains all proteins whilst <i>Q</i> and <i>R</i> are empty.<br/>
    </p>
    The bait-prey relationships resulting from experiments can be modelled as
    directed edges leading from <i>P</i> into <i>Q</i>.
    Thus the information obtained at any point is stored as a directed graph.
    While there are no edges leading into <i>R</i>, for all the prey-proteins
    contained in <i>Q</i>, the number of times they have been detected is
    known (Figure 3).
    </p>
    The next bait is determined by selecting the node with the maximum
    <i>indegree</i> (the number of times this protein was detected as prey) and
    the maximum deltaK (see below).
    </p>
    At any stage, the average <i>indegree</i> of all proteins contained in
    <i>Q</i> can be calculated. If there is no protein in <i>Q</i> with an
    above average indegree, the strategy resorts to choosing the next bait at
    random. This typically occurs during the initial and final stages of network
    coverage. Since at the start <i>Q</i> is empty and the average indegree of
    <i>Q</i>, the whole process is kickstarted this way by a few randomly chosen
    baits. The number of times a protein from <i>Q</i> has been seen as prey is an
    integer number. Hence at any given stage there can be several proteins in
    <i>Q</i> with the same maximum <i>indegree</i>. In order to break such a tie,
    the node with the highest deltaK is chosen. Both <i>indegree</i> and deltaK
    are initially set to zero for all proteins.
    <br/>
    <br/>
    deltaK is a 'distributed' weight factor that is defined as follows. Every time
    a pull-down is performed the number of neighbours (or degrees) k of the bait
    protein is being determined as well. This degree is distributed over all its
    prey proteins by adding 1/k to the respective deltaK of every prey, whilst the
    indegree of every prey protein detected is increased by one. The rationale
    behind this comes from the observation that hubs are less likely to be linked
    to other hubs. As an additional
    local measure deltaK was developed such that prey proteins interacting with
    less connected proteins get higher deltaK values than prey proteins which are
    less connected to hubs. So deltaK is an empirical measure which guides the
    strategy towards choosing baits which are linked to lowly connected proteins
    and hence these baits are more likely to be hubs.
<br/>
<br/>
<center>
<br/>
    <img border="0" align="center"
    src="<%=ctxpath%>/images/PAYG_Outline.jpg" width="700" height="500">
<font size="2">
<br/>
<br/>

<b>Figure 2: Illustration of Pay-As-You-Go principles</b><br/>
</font>
</center>
At the top are two representations (C1 & C2) of the same Graph. The graph G = (V,E)
consists of 9 nodes (V={<i>a,b,c,d,e,f,g,h,i</i>}) linked 	by 10 undirected edges
(E). Edge-covering sets are indicated by the cone-shaded nodes. Both set C1 (left)
with 6 nodes ({<i>a,b,d,e,f,h</i>}) and set C2 (right) with 3 nodes ({<i>c,f,h</i>})
satisfy the condition that every edge is adjacent to an edge in
C1 or C2 respectively. In other words both sets of cone-shaded nodes in C1 & C2
reveal the entire interaction network, but the set shown in C2 reveal it in fewer
steps. In reality this means that fewer experiments would be required to reveal
the same interaction network. <br/>
In the adjacency matrix	representation of G an entry is marked in red to denote
an interaction between two proteins. The matrix M<sub>C2</sub> shows that
C2 = ({<i>c,f,h</i>})
is a covering set, since the respective rows and columns (grey) of this set cover
all interactions. The matrix M<sub>ord</sub> illustrates how the weight of a
graph is calculated from any given ordering of nodes. In this example the
experiments are
conducted such that 1st <i>c</i>, 2nd <i>f</i>, 3rd<i>h</i>, 4th<i>a</i> etc,
are used as bait.  The ordering of the nodes is marked on the diagonal of
M<sub>ord</sub>.
The upper right triangle or M<sub>ord</sub> denotes at what time an interaction
was first
seen, whilst the lower half of the matrix denotes at what time an interaction was
confirmed according to their ordering. <br/>
The lower left corner shows a performance plot, generated by plotting the number
of edges <i>seen and confirmed</i> at time step <i>t</i>. The earlier edges are
<i>seen</i> and <i>confirmed</i>, the lower the sum of the weight of all entries
in M<sub>ord</sub> becomes. Hence this time-weight (the sum over all entries
in M<sub>ord</sub>) can be
used as an indicator for the performance of an ordering in revealing the topology
of a network.
<br/>
<br/>
<br/>
<br/>

</p>
<center>
     <img border="0" align="center"
        src="<%=ctxpath%>/images/PQRprincipiA.gif" width="300" height="250">
     <img border="0" align="center"
        src="<%=ctxpath%>/images/PQRprincipiB.gif" width="300" height="250">
<br/>
<br/>
<br/>
<font size="2">
<b>Figure 3: Illustration of the subsets P,Q and R during the Pay-As-You-Go process</b><br>
</font>
</center>
   The proteins used as bait (set <i>P</i>) are denoted as <i>p</i>. The proteins detected
   as prey at his point are in the set <i>Q</i> and denoted as <i>q</i>. In this example (left)
   the highlighted protein from <i>Q</i> is selected for use as the next bait as it has the
   highest <i>indegree</i>. The situation resulting from using this protein as bait in the next
   experiment is depicted on the right. Now the protein becomes a member of <i>P</i>, and two new
   prey proteins are detected and inserted into <i>Q</i>. The situation now shows two nodes from
   <i>Q</i> which have the same <i>indegree</i>. In this case the additional measure of deltaK is
   employed to break the tie.
<br/>
<br/>

</p>
<font size="3">
<b>References</b><br>
</font>
<ol>
    <li>Vitkup et al. Completeness in Structural Genomics <i>Nat Struct Biol</i> <b>8</b>, 559-66 (2001).</li>
    <li>Lappe,M. Holm, L. Unravelling unknown interaction networks with near optimal efficiency <i>Nature Biotech</i> doi:10.1038/nbt921, Published online: 7 December 2003. </li>
    <li>Xenarios,I et al DIP: The database of interacting proteins. <i>Nucleic Acids Res</i> <b>28</b>, 289-91 (2000). </li>
    <li>Deane, C.M. et al. Protein Interactions: Two methods for the assessment of the reliability of High-Throughput Observations. <i>Mol Cell Proteomics</i> <b>1</b>, 349-356. (2002). </li>
    <li>Gavin, A.C. et al. Functional Organization of the yeast proteome by systematic analysis of protein complexes. <i>Nature</i> <b>415</b>, 141-7. (2002). </li>
    <li>Ho, Y. et al. Systematic identification of protein complexes in Saccharomyces cerevisiae by mass spectrometry. <i>Nature</i> <b>415</b>, 180-3 (2002). </li>
</ol>
