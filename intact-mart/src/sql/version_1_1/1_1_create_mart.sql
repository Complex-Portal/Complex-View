-- #############################################################################
-- #           1. main and deminsion tables for experiment
-- #############################################################################

CREATE TABLE intact__experiment__main (
  experiment_key VARCHAR2(30) NOT NULL, 
  experiment_short VARCHAR2(50) NULL, 
  experiment_full VARCHAR2(400) NULL, 
  host_organism_taxid INTEGER NULL,
  host_organism_short VARCHAR2(50) NULL,
  host_organism_full VARCHAR2(200) NULL,
  participant_identmethod_mi VARCHAR2(30) NULL,
  participant_identmethod_short VARCHAR2(30) NULL,
  participant_identmethod_full VARCHAR2(400) NULL,
  interaction_detectmethod_mi VARCHAR2(30) NULL,
  interaction_detectmethod_short VARCHAR2(30) NULL,
  interaction_detectmethod_full VARCHAR2(400) NULL,
  interaction_count INTEGER NULL
);

CREATE INDEX tbl_experiment_index
ON intact__experiment__main (experiment_key, experiment_short, experiment_full, 
  host_organism_taxid, host_organism_short, host_organism_full, participant_identmethod_mi,
  participant_identmethod_short, participant_identmethod_full, interaction_detectmethod_mi,
  interaction_detectmethod_short, interaction_detectmethod_full, interaction_count);

CREATE TABLE intact__publication__dm (
  experiment_key VARCHAR2(30) NOT NULL,
  pmid VARCHAR2(30) NULL,
  title VARCHAR2(400) NULL,
  doi VARCHAR2(30) NULL,
  interaction_count INTEGER NULL,
  experiment_count INTEGER NULL
);

CREATE INDEX tbl_publication_index
ON intact__publication__dm (experiment_key,
  pmid, title, doi, interaction_count, experiment_count);

CREATE TABLE intact__hostOrg_celltype__dm (
  experiment_key VARCHAR2(30) NOT NULL,
  cabri_id VARCHAR2(30) NULL,
  celltype_short VARCHAR2(100) NULL,
  celltype_full VARCHAR2(400) NULL
);

CREATE INDEX tbl_hostOrg_celltype_index
ON intact__hostOrg_celltype__dm (experiment_key, cabri_id,
  celltype_short,celltype_full);

CREATE TABLE intact__hostOrg_tissue__dm (
  experiment_key VARCHAR2(30) NOT NULL,
  brenda_id VARCHAR2(30) NULL,
  tissue_short VARCHAR2(100) NULL,
  tissue_full VARCHAR2(100) NULL
);

CREATE INDEX tbl_hostOrg_tissue_index
ON intact__hostOrg_tissue__dm (experiment_key,
  brenda_id, tissue_short, tissue_full);

CREATE TABLE intact__experiment_alias__dm (
  experiment_key VARCHAR2(30) NOT NULL,
  name VARCHAR2(30) NULL,
  alias_type_mi VARCHAR2(30) NULL,
  alias_type_short VARCHAR2(30) NULL,
  alias_type_full VARCHAR2(30) NULL
);

CREATE INDEX tbl_experiment_alias_index
ON intact__experiment_alias__dm (experiment_key,
  name, alias_type_mi,alias_type_short, alias_type_full);

CREATE TABLE intact__experiment_anno__dm (
  experiment_key VARCHAR2(30) NOT NULL,
  description VARCHAR2(4000) NULL,
  topic_mi VARCHAR2(30) NULL,
  topic_short VARCHAR2(30) NULL,
  topic_full VARCHAR2(400) NULL
);

CREATE INDEX tbl_experiment_anno_index
ON intact__experiment_anno__dm (experiment_key,
  description, topic_mi, topic_short, topic_full);

CREATE TABLE intact__experiment_xref__dm (
  experiment_key VARCHAR2(30) NOT NULL,
  primary_id VARCHAR2(30) NULL,
  secondary_id VARCHAR2(30) NULL,
  database_mi VARCHAR2(30) NULL,
  database_short VARCHAR2(30) NULL,
  database_full VARCHAR2(400) NULL,
  qualifier_mi VARCHAR2(30) NULL,
  qualifier_short VARCHAR2(30) NULL,
  qualifier_full VARCHAR2(400) NULL
);

CREATE INDEX tbl_experiment_xref_index
ON intact__experiment_xref__dm (experiment_key, primary_id, secondary_id,
  database_mi,database_short, database_full,qualifier_mi, qualifier_short, qualifier_full);

-- #############################################################################
-- #           2. main and deminsion tables for interaction
-- #############################################################################

CREATE TABLE intact__interaction__main (
  experiment_key VARCHAR2(30) NULL,
  interaction_key VARCHAR2(30) NULL,
  experiment_short VARCHAR2(50) NULL,
  experiment_full VARCHAR2(400) NULL,
  host_organism_taxid INTEGER NULL,
  host_organism_short VARCHAR2(30) NULL,
  host_organism_full VARCHAR2(200) NULL,
  participant_identmethod_mi VARCHAR2(30) NULL,
  participant_identmethod_short VARCHAR2(30) NULL,
  participant_identmethod_full VARCHAR2(400) NULL,
  interaction_detectmethod_mi VARCHAR2(30) NULL,
  interaction_detectmethod_short VARCHAR2(30) NULL,
  interaction_detectmethod_full VARCHAR2(400) NULL,
  interaction_count INTEGER NULL,
  interaction_type_mi VARCHAR2(30) NULL,
  interaction_type_short VARCHAR2(30) NULL,
  interaction_type_full VARCHAR2(400) NULL,
  component_count INTEGER NULL,
  molecule_count INTEGER NULL
);

CREATE INDEX tbl_interaction_index
ON intact__interaction__main (experiment_key, interaction_key,
  experiment_short, experiment_full, host_organism_taxid, host_organism_short,
  host_organism_full, participant_identmethod_mi,
  participant_identmethod_short, participant_identmethod_full,
  interaction_detectmethod_mi, interaction_detectmethod_short,
  interaction_detectmethod_full, interaction_count,
  interaction_type_mi, interaction_type_short,
  interaction_type_full, component_count, molecule_count);

CREATE TABLE intact__interaction_alias__dm (
  interaction_key VARCHAR2(30) NULL,
  name VARCHAR2(30) NULL,
  alias_type_mi VARCHAR2(30) NULL,
  alias_type_short VARCHAR2(30) NULL,
  alias_type_full VARCHAR2(30) NULL
);

CREATE INDEX tbl_interaction_alias_index
ON intact__interaction_alias__dm (interaction_key,
  name, alias_type_mi, alias_type_short, alias_type_full);

CREATE TABLE intact__interaction_anno__dm (
  interaction_key VARCHAR2(30) NOT NULL,
  description VARCHAR2(4000) NULL,
  topic_mi VARCHAR2(30) NULL,
  topic_short VARCHAR2(30) NULL,
  topic_full VARCHAR2(400) NULL
);

CREATE INDEX tbl_interaction_anno_index
ON intact__interaction_anno__dm (interaction_key,
  description, topic_mi, topic_short, topic_full);
  
CREATE TABLE intact__interaction_xref__dm (
  interaction_key VARCHAR2(30) NOT NULL,
  primary_id VARCHAR2(30) NULL,
  secondary_id VARCHAR2(30) NULL,
  database_mi VARCHAR2(30) NULL,
  database_short VARCHAR2(30) NULL,
  database_full VARCHAR2(400) NULL,
  qualifier_mi VARCHAR2(30) NULL,
  qualifier_short VARCHAR2(30) NULL,
  qualifier_full VARCHAR2(400) NULL
);

CREATE INDEX tbl_interaction_xref_index
ON intact__interaction_xref__dm (interaction_key,
  primary_id, secondary_id, database_mi, database_short,
  database_full, qualifier_mi, qualifier_short, qualifier_full);

CREATE TABLE intact__interaction_owner__dm (
  interaction_key VARCHAR2(30) NOT NULL,
  interaction_owner_mi VARCHAR2(30) NULL,
  interaction_owner_short VARCHAR2(30) NULL,
  interaction_owner_full VARCHAR2(100) NULL
);

CREATE INDEX tbl_interaction_owner_index
ON intact__interaction_owner__dm (interaction_key,
  interaction_owner_mi, interaction_owner_short, interaction_owner_full);

-- involved molecule count per interaction
CREATE TABLE tbl_molecule_count_tmp (
  interaction_key VARCHAR2(30) NULL,
  molecule_count INTEGER NULL,
  PRIMARY KEY (interaction_key)
);

-- #############################################################################
-- #           3. main and deminsion tables for interactor
-- #############################################################################

CREATE TABLE intact__interactor__main (
  experiment_key VARCHAR2(30) NULL,
  interaction_key VARCHAR2(30) NULL,
  interactor_key VARCHAR2(30) NULL,
  experiment_short VARCHAR2(50) NULL,
  experiment_full VARCHAR2(400) NULL,
  host_organism_taxid INTEGER NULL,
  host_organism_short VARCHAR2(30) NULL,
  host_organism_full VARCHAR2(200) NULL,
  participant_identmethod_mi VARCHAR2(30) NULL,
  participant_identmethod_short VARCHAR2(30) NULL,
  participant_identmethod_full VARCHAR2(400) NULL,
  interaction_detectmethod_mi VARCHAR2(30) NULL,
  interaction_detectmethod_short VARCHAR2(30) NULL,
  interaction_detectmethod_full VARCHAR2(400) NULL,
  interaction_count INTEGER NULL,
  interaction_type_mi VARCHAR2(30) NULL,
  interaction_type_short VARCHAR2(30) NULL,
  interaction_type_full VARCHAR2(400) NULL,
  component_count INTEGER NULL,
  molecule_count INTEGER NULL,
  interactor_short VARCHAR2(30) NULL,
  interactor_full VARCHAR2(400) NULL,
  interactor_type_mi VARCHAR2(30) NULL,
  interactor_type_short VARCHAR2(30) NULL,
  interactor_type_full VARCHAR2(400) NULL,
  experimental_role_mi VARCHAR2(30) NULL,
  experimental_role_short VARCHAR2(30) NULL,
  experimental_role_full VARCHAR2(400) NULL,
  biological_role_mi VARCHAR2(30) NULL,
  biological_role_short VARCHAR2(30) NULL,
  biological_role_full VARCHAR2(400) NULL,
  interactor_biosource_taxid VARCHAR2(30) NULL,
  interactor_biosource_short VARCHAR2(30) NULL,
  interactor_biosource_full VARCHAR2(400) NULL,
  component_expressed_in_taxid VARCHAR2(30) NULL,
  component_expressed_in_short VARCHAR2(30) NULL,
  component_expressed_in_full VARCHAR2(400) NULL,
  stoichiometry INTEGER NULL,
  interactor_sequence CLOB NULL,
  interactor_sequence_length INTEGER NULL,
  crc64 VARCHAR2(30) NULL,
  involved_interaction_count INTEGER NULL
);

CREATE INDEX tbl_interactor1_index
ON intact__interactor__main (experiment_key, interaction_key, interactor_key,
  experiment_short, experiment_full, host_organism_taxid, host_organism_short,
  host_organism_full, participant_identmethod_mi, participant_identmethod_short,
  participant_identmethod_full, interaction_detectmethod_mi, interaction_detectmethod_short,
  interaction_detectmethod_full, interaction_count, interaction_type_mi, interaction_type_short);
CREATE INDEX tbl_interactor2_index
ON intact__interactor__main ( interaction_type_full, component_count, molecule_count, interactor_short, interactor_full,
  interactor_type_mi, interactor_type_short, interactor_type_full, experimental_role_mi,
  experimental_role_short, experimental_role_full, biological_role_mi, biological_role_short,
  biological_role_full, interactor_biosource_taxid, interactor_biosource_short, interactor_biosource_full,
  component_expressed_in_taxid, component_expressed_in_short, component_expressed_in_full,
  stoichiometry, interactor_sequence_length, crc64, involved_interaction_count);

CREATE TABLE intact__interactor_alias__dm (
  interactor_key VARCHAR2(30) NOT NULL,
  name VARCHAR2(30) NULL,
  alias_type_mi VARCHAR2(30) NULL,
  alias_type_short VARCHAR2(30) NULL,
  alias_type_full VARCHAR2(400) NULL
);

CREATE INDEX tbl_interactor_alias_index
ON intact__interactor_alias__dm (interactor_key,
  name, alias_type_mi, alias_type_short, alias_type_full);

CREATE TABLE intact__interactor_anno__dm (
  interactor_key VARCHAR2(30) NOT NULL,
  description VARCHAR2(4000) NULL,
  topic_mi VARCHAR2(30) NULL,
  topic_short VARCHAR2(30) NULL,
  topic_full VARCHAR2(400) NULL
);

CREATE INDEX tbl_interactor_anno_index
ON intact__interactor_anno__dm (interactor_key,
  description, topic_mi, topic_short, topic_full);

CREATE TABLE intact__interactor_xref__dm (
  interactor_key VARCHAR2(30) NOT NULL,
  primary_id VARCHAR2(30) NULL,
  secondary_id VARCHAR2(30) NULL,
  database_mi VARCHAR2(30) NULL,
  database_short VARCHAR2(30) NULL,
  database_full VARCHAR2(400) NULL,
  qualifier_mi VARCHAR2(30) NULL,
  qualifier_short VARCHAR2(30) NULL,
  qualifier_full VARCHAR2(400) NULL
);

CREATE INDEX tbl_interactor_xref_index
ON intact__interactor_xref__dm (interactor_key,
  primary_id, secondary_id, database_mi, database_short, database_full,
  qualifier_mi, qualifier_short, qualifier_full);

-- stores temoprary the interactor_sequence
CREATE TABLE tbl_sequence_tmp (
  intactor_key varchar2(30),
  interactor_sequence CLOB NULL,
  interactor_sequence_length INTEGER NULL,
  PRIMARY KEY (intactor_key)
);

-- #############################################################################
-- #           4. main and deminsion tables for feature
-- #############################################################################

CREATE TABLE intact__feature__main (
  experiment_key VARCHAR2(30) NULL,
  interaction_key VARCHAR2(30) NULL,
  interactor_key VARCHAR2(30) NULL,
  feature_key VARCHAR2(30) NULL,
  experiment_short VARCHAR2(50) NULL,
  experiment_full VARCHAR2(400) NULL,
  host_organism_taxid INTEGER NULL,
  host_organism_short VARCHAR2(30) NULL,
  host_organism_full VARCHAR2(200) NULL,
  participant_identmethod_mi VARCHAR2(30) NULL,
  participant_identmethod_short VARCHAR2(30) NULL,
  participant_identmethod_full VARCHAR2(400) NULL,
  interaction_detectmethod_mi VARCHAR2(30) NULL,
  interaction_detectmethod_short VARCHAR2(30) NULL,
  interaction_detectmethod_full VARCHAR2(400) NULL,
  interaction_count INTEGER NULL,
  interaction_type_mi VARCHAR2(30) NULL,
  interaction_type_short VARCHAR2(30) NULL,
  interaction_type_full VARCHAR2(400) NULL,
  component_count INTEGER NULL,
  molecule_count INTEGER NULL,
  interactor_short VARCHAR2(30) NULL,
  interactor_full VARCHAR2(400) NULL,
  interactor_type_mi VARCHAR2(30) NULL,
  interactor_type_short VARCHAR2(30) NULL,
  interactor_type_full VARCHAR2(400) NULL,
  experimental_role_mi VARCHAR2(30) NULL,
  experimental_role_short VARCHAR2(30) NULL,
  experimental_role_full VARCHAR2(400) NULL,
  biological_role_mi VARCHAR2(30) NULL,
  biological_role_short VARCHAR2(30) NULL,
  biological_role_full VARCHAR2(400) NULL,
  interactor_biosource_taxid VARCHAR2(30) NULL,
  interactor_biosource_short VARCHAR2(30) NULL,
  interactor_biosource_full VARCHAR2(400) NULL,
  component_expressed_in_taxid VARCHAR2(30) NULL,
  component_expressed_in_short VARCHAR2(30) NULL,
  component_expressed_in_full VARCHAR2(400) NULL,
  stoichiometry INTEGER NULL,
  interactor_sequence CLOB NULL, 
  interactor_sequence_length INTEGER NULL,
  crc64 VARCHAR2(30) NULL,
  involved_interaction_count INTEGER NULL,
  feature_shortlabel VARCHAR2(30) NULL,
  feature_fullname VARCHAR2(400) NULL,
  feature_type_mi VARCHAR2(30) NULL,
  feature_type_short VARCHAR2(30) NULL,
  feature_type_full VARCHAR2(400) NULL,
  feature_identmethod_mi VARCHAR2(30) NULL,
  feature_identmethod_short VARCHAR2(30) NULL,
  feature_identmethod_full VARCHAR2(400) NULL
);

CREATE INDEX tbl_feature1_index
ON intact__feature__main (experiment_key, interaction_key, interactor_key,
  feature_key, experiment_short, experiment_full, host_organism_taxid,
  host_organism_short, host_organism_full, participant_identmethod_mi,
  participant_identmethod_short, participant_identmethod_full,
  interaction_detectmethod_mi, interaction_detectmethod_short,
  interaction_detectmethod_full, interaction_count, interaction_type_mi,
  interaction_type_short, interaction_type_full, component_count);
CREATE INDEX tbl_feature2_index
ON intact__feature__main ( molecule_count, interactor_short, interactor_full, interactor_type_mi,
  interactor_type_short, interactor_type_full, experimental_role_mi, experimental_role_short,
  experimental_role_full, biological_role_mi, biological_role_short, biological_role_full,
  interactor_biosource_taxid, interactor_biosource_short, interactor_biosource_full,
  component_expressed_in_taxid, component_expressed_in_short, component_expressed_in_full,
  stoichiometry, interactor_sequence_length, crc64, involved_interaction_count,
  feature_shortlabel, feature_fullname, feature_type_mi, feature_type_short, feature_type_full,
  feature_identmethod_mi, feature_identmethod_short, feature_identmethod_full);

CREATE TABLE intact__feature_alias__dm (
  feature_key VARCHAR2(30) NOT NULL,
  name VARCHAR2(30) NULL,
  alias_type_mi VARCHAR2(30) NULL,
  alias_type_short VARCHAR2(30) NULL,
  alias_type_full VARCHAR2(400) NULL
);

CREATE INDEX tbl_feature_alias_index
ON intact__feature_alias__dm (feature_key,
  name, alias_type_mi, alias_type_short, alias_type_full);

CREATE TABLE intact__feature_anno__dm (
  feature_key VARCHAR2(30) NOT NULL,
  description VARCHAR2(4000) NULL,
  topic_mi VARCHAR2(30) NULL,
  topic_short VARCHAR2(30) NULL,
  topic_full VARCHAR2(400) NULL
);

CREATE INDEX tbl_feature_anno_index
ON intact__feature_anno__dm (feature_key,
  description, topic_mi, topic_short, topic_full);

CREATE TABLE intact__feature_xref__dm (
  feature_key VARCHAR2(30) NOT NULL,
  primary_id VARCHAR2(30) NULL,
  secondary_id VARCHAR2(30) NULL,
  database_mi VARCHAR2(30) NULL,
  database_short VARCHAR2(30) NULL,
  database_full VARCHAR2(400) NULL,
  qualifier_mi VARCHAR2(30) NULL,
  qualifier_short VARCHAR2(30) NULL,
  qualifier_full VARCHAR2(400) NULL
);

CREATE INDEX tbl_feature_xref_index
ON intact__feature_xref__dm (feature_key, primary_id, secondary_id, database_mi, 
  database_short, database_full, qualifier_mi, qualifier_short, qualifier_full);

CREATE TABLE intact__range__dm (
  feature_key VARCHAR2(30) NOT NULL,
  undetermined CHAR NULL,
  link CHAR NULL,
  from_interval_start INTEGER NULL,
  from_interval_end INTEGER NULL,
  from_fuzzytype_mi VARCHAR2(30) NULL,
  from_fuzzytype_short VARCHAR2(30) NULL,
  from_fuzzytype_full VARCHAR2(400) NULL,
  to_interval_start INTEGER NULL,
  to_interval_end INTEGER NULL,
  to_fuzzytype_mi VARCHAR2(30) NULL,
  to_fuzzytype_short VARCHAR2(30) NULL,
  to_fuzzytype_full VARCHAR2(400) NULL
);

CREATE INDEX tbl_range_index
ON intact__range__dm (feature_key, undetermined, link, from_interval_start,
  from_interval_end, from_fuzzytype_mi, from_fuzzytype_short, from_fuzzytype_full,
  to_interval_start, to_interval_end, to_fuzzytype_mi, to_fuzzytype_short,
  to_fuzzytype_full);