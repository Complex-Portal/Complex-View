#######################################################################
#                      Contents:                                      #
#######################################################################

1.) SQL build scripts for an IntAct Oracle Mart.
2.) XML configuration files of the web application (MartView)
3.) A modificated file of BioMart to fix a bug to display 
    correctly attributes with clob datatype.
4.) The IntAct Mart Schema.
5.) Some demo versions of Canned Queries.




#######################################################################
#  1.) SQL build scripts for an IntAct Oracle Mart.                   #
#######################################################################

These IntAct Mart build scripts are written for an IntAct Oracle 
database.

To rebuild the IntAct Mart run:
intact-mart/src/mart-build-sql/beta_1_0/rebuild_intact_mart.sql





#######################################################################
# 2.) XML configuration files of the web application (MartView)       #
#######################################################################

To upload the XML configuration files of IntAct Mart start MartEditor.

Select BOTH files for uploading:
intact-mart/src/mart-view/beta_1_0/INTACT.xml
intact-mart/src/mart-view/beta_1_0/INTACT_template.template.xml

ATTENTION:

-) Be not surprised that it seems that nothing had happend.
   To see the changes, import the Meta Dataset with MartEditor.


-) intact-mart/src/mart-view/grand_synonym_meta_dataset.sql

   This script have to run, if the user have uploaded the first
   time the Mart via MartEditor, to set all synonyms and grands
   for the meta dataset (hidden configuration dataset of BioMart).





#######################################################################
# 3.) A modificated file of BioMart to fix a bug to display           #
#     correctly attributes with clob datatype.                        #
#######################################################################

The bug was that the Oracle throws an exception if you not set two 
specific variables for returning values with clob datatype.

The sequence length of an interactor can be something like 40 000 chars.

The sequence will be also displayed in one row if you replace in BioMart:
	biomart-perl/lib/BioMart/Dataset/TableSet.pm
with this patched file:
	intact-mart/src/modification/TableSet.pm






#######################################################################
#           4.) The IntAct Mart Schema.                               #
#######################################################################

This schema is created with DBDesigner and can be downloaded at:
http://dev.mysql.com/downloads/workbench/5.0.html

The IntAct Mart Schema is stored as png:
intact-mart/src/schema/beta_1_0/intact_mart_schema.png






#######################################################################
#  5.) Some demo versions of Canned Queries.                          #
#######################################################################

These Demo Canned Queries are based on the URLs generated on the test server
evo-test.ebi.ac.uk and are not valid outside the EBI network.

To get a drop down menu for Canned Queries you have to replace:
	biomart-perl/conf/templates/default/header.tt
with the edited file:
	intact-mart/src/canned-queries-demo/default/header.tt

The demo files are written just in HTML an cotain some small Java Scripts.

To integrate Prototype create a folder called "js" in:
	biomart-perl/htdocs
and paste in it the file:
 	intact-mart/src/canned-queries-demo/htdocs/js/prototype.js

Copied in the folder:
	biomart-perl/htdocs
The two parameterized Canned Queries:
	intact-mart/src/canned-queries-demo/htdocs/cannedQueryPride.html
	intact-mart/src/canned-queries-demo/htdocs/cannedQueryDataset.html

	
	
