-- #############################################################################
-- #   This is a rebuild script for an IntAct Oracle Mart                      #
-- #############################################################################



-- Drop and Create new IntAct Mart tables
@ intact-mart/src/mart-build-sql/beta_1_0/1_0_drop_mart.sql
@ intact-mart/src/mart-build-sql/beta_1_0/1_1_create_mart.sql
--------------------------------------------------------------------------------



-- Drop if existing all Materialized Views of IntAct Mart.
-- Rebuild all necessary Views for the IntAct Mart.
@ intact-mart/src/mart-build-sql/beta_1_0/2_0_drop_views.sql
@ intact-mart/src/mart-build-sql/beta_1_0/2_1_create_common_views.sql
@ intact-mart/src/mart-build-sql/beta_1_0/2_2_create_publication_main_dm_views.sql
@ intact-mart/src/mart-build-sql/beta_1_0/2_3_create_experiment_main_dm_views.sql
@ intact-mart/src/mart-build-sql/beta_1_0/2_4_create_interaction_main_dm_views.sql
@ intact-mart/src/mart-build-sql/beta_1_0/2_5_create_component_main_dm_views.sql
@ intact-mart/src/mart-build-sql/beta_1_0/2_6_create_interactor_main_dm_views.sql
@ intact-mart/src/mart-build-sql/beta_1_0/2_7_create_feature_main_dm_views.sql
--------------------------------------------------------------------------------



-- Insert all values from the IntAct Model into the IntAct Mart
@ intact-mart/src/mart-build-sql/beta_1_0/3_0_insert_publication_main_dm.sql
@ intact-mart/src/mart-build-sql/beta_1_0/3_1_insert_experiment_main_dm.sql
-- This is a specific cursor which calculates in a preprocessing
-- step the molecule count of every interaction and store it in a
-- temporary table.
@ intact-mart/src/mart-build-sql/beta_1_0/3_2_0_insert_tmp_molecule_count.sql 
@ intact-mart/src/mart-build-sql/beta_1_0/3_2_1_insert_interaction_main_dm.sql
@ intact-mart/src/mart-build-sql/beta_1_0/3_3_insert_component_main_dm.sql
-- This is again a specific cursor which concate in a preprocessing
-- step the interactor sequence and store it also in a temporary 
-- table.
@ intact-mart/src/mart-build-sql/beta_1_0/3_4_0_insert_tmp_sequence.sql
@ intact-mart/src/mart-build-sql/beta_1_0/3_4_1_insert_interactor_main_dm.sql
@ intact-mart/src/mart-build-sql/beta_1_0/3_5_insert_feature_main_dm.sql
--------------------------------------------------------------------------------



-- Grand the reading permission to the IntAct Mart
@ intact-mart/src/mart-build-sql/beta_1_0/4_0_grand_synonym_intact_dataset.sql
