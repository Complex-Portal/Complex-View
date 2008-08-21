-- #############################################################################
-- #   This is a rebuild script for an IntAct Oracle Mart                      #
-- #############################################################################



-- Drop and Create new IntAct Mart tables
@ intact-mart/src/sql/version_1_2/1_0_drop_mart.sql
@ intact-mart/src/sql/version_1_2/1_1_create_mart.sql
--------------------------------------------------------------------------------



-- Drop if existing all Materialized Views of IntAct Mart.
-- Rebuild all necessary Views for the IntAct Mart.
@ intact-mart/src/sql/version_1_2/2_0_drop_views.sql
@ intact-mart/src/sql/version_1_2/2_1_create_common_views.sql
@ intact-mart/src/sql/version_1_2/2_2_create_publication_main_dm_views.sql
@ intact-mart/src/sql/version_1_2/2_3_create_experiment_main_dm_views.sql
@ intact-mart/src/sql/version_1_2/2_4_create_interaction_main_dm_views.sql
@ intact-mart/src/sql/version_1_2/2_5_create_component_main_dm_views.sql
@ intact-mart/src/sql/version_1_2/2_6_create_interactor_main_dm_views.sql
@ intact-mart/src/sql/version_1_2/2_7_create_feature_main_dm_views.sql
--------------------------------------------------------------------------------



-- Insert all values from the IntAct Model into the IntAct Mart
@ intact-mart/src/sql/version_1_2/3_0_insert_publication_main_dm.sql
@ intact-mart/src/sql/version_1_2/3_1_insert_experiment_main_dm.sql
-- This is a specific cursor which calculates in a preprocessing
-- step the molecule count of every interaction and store it in a
-- temporary table.
@ intact-mart/src/sql/version_1_2/3_2_0_insert_tmp_molecule_count.sql 
@ intact-mart/src/sql/version_1_2/3_2_1_insert_interaction_main_dm.sql
@ intact-mart/src/sql/version_1_2/3_3_insert_component_main_dm.sql
-- This is again a specific cursor which concate in a preprocessing
-- step the interactor sequence and store it also in a temporary 
-- table.
@ intact-mart/src/sql/version_1_2/3_4_0_insert_tmp_sequence.sql
@ intact-mart/src/sql/version_1_2/3_4_1_insert_interactor_main_dm.sql
@ intact-mart/src/sql/version_1_2/3_5_insert_feature_main_dm.sql
