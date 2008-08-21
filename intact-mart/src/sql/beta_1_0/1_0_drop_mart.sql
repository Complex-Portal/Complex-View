DECLARE 

PROCEDURE drop_table_if_exist (tbl_name IN VARCHAR2) IS
   v_count NUMBER(5) := 0;
BEGIN

   SELECT COUNT(*)
   INTO v_count
   FROM user_tables
   WHERE upper(table_name) = upper(tbl_name); 

   IF ( v_count = 1 ) THEN
       EXECUTE IMMEDIATE 'DROP TABLE ' || tbl_name;
   else
       dbms_output.put_line( tbl_name || ' doesn''t exist');
   END IF;
END;

BEGIN
   -- first main and dimension tables
   drop_table_if_exist ('intact__publication__main');
   drop_table_if_exist ('intact__publication_alias__dm');
   drop_table_if_exist ('intact__publication_anno__dm');
   drop_table_if_exist ('intact__publication_xref__dm');
   -- second main and dimension tables
   drop_table_if_exist ('intact__experiment__main');
   drop_table_if_exist ('intact__experiment_xref__dm');
   drop_table_if_exist ('intact__experiment_anno__dm');
   drop_table_if_exist ('intact__experiment_alias__dm');
   drop_table_if_exist ('intact__publication__dm');
   drop_table_if_exist ('intact__hostOrg_tissue__dm');
   drop_table_if_exist ('intact__hostOrg_celltype__dm');
   -- third main and dimension tables
   drop_table_if_exist ('intact__interaction__main');
   drop_table_if_exist ('intact__interaction_xref__dm');
   drop_table_if_exist ('intact__interaction_anno__dm');
   drop_table_if_exist ('intact__interaction_alias__dm');
   drop_table_if_exist ('intact__interaction_owner__dm');
   drop_table_if_exist ('tbl_molecule_count_tmp');
   -- fourth main and dimension tables
   drop_table_if_exist ('intact__component__main');
   drop_table_if_exist ('intact__component_alias__dm');
   drop_table_if_exist ('intact__component_anno__dm');
   drop_table_if_exist ('intact__component_xref__dm');
   -- fith main and dimension tables
   drop_table_if_exist ('intact__interactor__main');
   drop_table_if_exist ('intact__interactor_xref__dm');
   drop_table_if_exist ('intact__interactor_anno__dm');
   drop_table_if_exist ('intact__interactor_alias__dm');
   drop_table_if_exist ('tbl_sequence_tmp');
   -- sixth main and dimension tables
   drop_table_if_exist ('intact__feature__main');
   drop_table_if_exist ('intact__feature_xref__dm');
   drop_table_if_exist ('intact__feature_anno__dm');
   drop_table_if_exist ('intact__feature_alias__dm');
   drop_table_if_exist ('intact__range__dm');
END;



-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
--    Just for testing if there are any not deleted table of the mart


-- SELECT * 
-- FROM user_tables
-- WHERE table_name LIKE 'INTACT__%';