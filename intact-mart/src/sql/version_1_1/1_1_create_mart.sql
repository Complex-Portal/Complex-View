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

CREATE TABLE intact__publication__dm (
  experiment_key VARCHAR2(30) NOT NULL,
  pmid VARCHAR2(30) NULL,
  title VARCHAR2(400) NULL,
  doi VARCHAR2(30) NULL,
  interaction_count INTEGER NULL,
  experiment_count INTEGER NULL
);

CREATE TABLE intact__hostOrg_celltype__dm (
  experiment_key VARCHAR2(30) NOT NULL,
  cabri_id VARCHAR2(30) NULL,
  celltype_short VARCHAR2(100) NULL,
  celltype_full VARCHAR2(400) NULL
);

CREATE TABLE intact__hostOrg_tissue__dm (
  experiment_key VARCHAR2(30) NOT NULL,
  brenda_id VARCHAR2(30) NULL,
  tissue_short VARCHAR2(100) NULL,
  tissue_full VARCHAR2(100) NULL
);

CREATE TABLE intact__experiment_alias__dm (
  experiment_key VARCHAR2(30) NOT NULL,
  name VARCHAR2(30) NULL,
  alias_type_mi VARCHAR2(30) NULL,
  alias_type_short VARCHAR2(30) NULL,
  alias_type_full VARCHAR2(30) NULL
);

CREATE TABLE intact__experiment_anno__dm (
  experiment_key VARCHAR2(30) NOT NULL,
  description LONG NULL,
  topic_mi VARCHAR2(30) NULL,
  topic_short VARCHAR2(30) NULL,
  topic_full VARCHAR2(400) NULL
);

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
  interactor_count INTEGER NULL
);

CREATE TABLE intact__interaction_alias__dm (
  interaction_key VARCHAR2(30) NULL,
  name VARCHAR2(30) NULL,
  alias_type_mi VARCHAR2(30) NULL,
  alias_type_short VARCHAR2(30) NULL,
  alias_type_full VARCHAR2(30) NULL
);

CREATE TABLE intact__interaction_anno__dm (
  interaction_key VARCHAR2(30) NOT NULL,
  description LONG NULL,
  topic_mi VARCHAR2(30) NULL,
  topic_short VARCHAR2(30) NULL,
  topic_full VARCHAR2(400) NULL
);

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

CREATE TABLE intact__interaction_owner__dm (
  interaction_key VARCHAR2(30) NOT NULL,
  interaction_owner_mi VARCHAR2(30) NULL,
  interaction_owner_short VARCHAR2(30) NULL,
  interaction_owner_full VARCHAR2(100) NULL
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
  interactor_count INTEGER NULL,
  interactor_short VARCHAR2(30) NULL,
  interactor_full VARCHAR2(400) NULL,
  interactor_type_mi VARCHAR2(30) NULL,
  interactor_type_short VARCHAR2(30) NULL,
  interactor_type_full VARCHAR2(400) NULL,
  experimental_role_mi VARCHAR2(30) NULL,
  experimental_role_short VARCHAR2(30) NULL,
  experimental_role_full VARCHAR2(30) NULL,
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
  molecule_count INTEGER NULL,
  interactor_sequence LONG NULL,
  interactor_sequence_length INTEGER NULL,
  crc64 VARCHAR2(30) NULL
);

CREATE TABLE intact__interactor_alias__dm (
  interactor_key VARCHAR2(30) NOT NULL,
  name VARCHAR2(30) NULL,
  alias_type_mi VARCHAR2(30) NULL,
  alias_type_short VARCHAR2(30) NULL,
  alias_type_full VARCHAR2(400) NULL
);

CREATE TABLE intact__interactor_anno__dm (
  interactor_key VARCHAR2(30) NOT NULL,
  description LONG NULL,
  topic_mi VARCHAR2(30) NULL,
  topic_short VARCHAR2(30) NULL,
  topic_full VARCHAR2(400) NULL
);

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
  interaction_type_full VARCHAR2(30) NULL,
  interactor_count INTEGER NULL,
  interactor_short VARCHAR2(30) NULL,
  interactor_full VARCHAR2(30) NULL,
  interactor_type_mi VARCHAR2(30) NULL,
  interactor_type_short VARCHAR2(30) NULL,
  interactor_type_full VARCHAR2(30) NULL,
  experimental_role_mi VARCHAR2(30) NULL,
  experimental_role_short VARCHAR2(30) NULL,
  experimental_role_full VARCHAR2(30) NULL,
  biological_role_mi VARCHAR2(30) NULL,
  biological_role_short VARCHAR2(30) NULL,
  biological_role_full VARCHAR2(30) NULL,
  interactor_biosource_taxid VARCHAR2(30) NULL,
  interactor_biosource_short VARCHAR2(30) NULL,
  interactor_biosource_full VARCHAR2(30) NULL,
  component_expressed_in_taxid VARCHAR2(30) NULL,
  component_expressed_in_short VARCHAR2(30) NULL,
  component_expressed_in_full VARCHAR2(30) NULL,
  stoichiometry INTEGER NULL,
  molecule_count INTEGER NULL,
  interactor_sequence LONG NULL,
  interactor_sequence_length INTEGER NULL,
  crc64 VARCHAR2(30) NULL,
  feature_shortlabel VARCHAR2(30) NULL,
  feature_fullname VARCHAR2(30) NULL,
  feature_type_mi VARCHAR2(30) NULL,
  feature_type_short VARCHAR2(30) NULL,
  feature_type_full VARCHAR2(30) NULL,
  feature_identmethod_mi VARCHAR2(30) NULL,
  feature_identmethod_short VARCHAR2(30) NULL,
  feature_identmethod_full VARCHAR2(30) NULL
);

CREATE TABLE intact__feature_alias__dm (
  feature_key VARCHAR2(30) NOT NULL,
  name VARCHAR2(30) NULL,
  alias_type_mi VARCHAR2(30) NULL,
  alias_type_short VARCHAR2(30) NULL,
  alias_type_full VARCHAR2(400) NULL
);

CREATE TABLE intact__feature_anno__dm (
  feature_key VARCHAR2(30) NOT NULL,
  description LONG NULL,
  topic_mi VARCHAR2(30) NULL,
  topic_short VARCHAR2(30) NULL,
  topic_full VARCHAR2(400) NULL
);

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

CREATE TABLE intact__range__dm (
  feature_key VARCHAR2(30) NOT NULL,
  undetermined CHAR NULL,
  link CHAR NULL,
  from_interval_start INTEGER NULL,
  from_interval_end INTEGER NULL,
  from_fuzzytype_mi VARCHAR2(30) NULL,
  from_fuzzytype_short VARCHAR2(30) NULL,
  from_fuzzytype_full VARCHAR2(30) NULL,
  to_interval_start INTEGER NULL,
  to_interval_end INTEGER NULL,
  to_fuzzytype_mi VARCHAR2(30) NULL,
  to_fuzzytype_short VARCHAR2(30) NULL,
  to_fuzzytype_full VARCHAR2(30) NULL
);