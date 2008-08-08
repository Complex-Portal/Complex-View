Overview for special configuration:
(Just for Oracle IntAct BioMart)

-------------------------------------------------------------

biomart-perl/lib/BioMart/Dataset/TableSet.pm

This file have to be replaced by 

bioMartModification/TableSet.pm

(Bug fix for the display of interactor sequence)

-------------------------------------------------------------

grand_synonym_meta_dataset.sql

This script have to run if the user have exported the first 
time the mart via MartEditor to set all synonyms and grands 
for the meta dataset (hidden configuration dataset of BioMart).



