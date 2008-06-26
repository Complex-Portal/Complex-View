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
   drop_table_if_exist ('intact__range__dm');
   drop_table_if_exist ('intact__publication__dm');
   drop_table_if_exist ('intact__interactor__main');
   drop_table_if_exist ('intact__interactor_xref__dm');
   drop_table_if_exist ('intact__interactor_anno__dm');
   drop_table_if_exist ('intact__interactor_alias__dm');
   drop_table_if_exist ('intact__interaction__main');
   drop_table_if_exist ('intact__interaction_xref__dm');
   drop_table_if_exist ('intact__interaction_anno__dm');
   drop_table_if_exist ('intact__interaction_alias__dm');
   drop_table_if_exist ('intact__interaction_owner__dm');
   drop_table_if_exist ('intact__hostOrg_tissue__dm');
   drop_table_if_exist ('intact__hostOrg_celltype__dm');
   drop_table_if_exist ('intact__feature__main');
   drop_table_if_exist ('intact__feature_xref__dm');
   drop_table_if_exist ('intact__feature_anno__dm');
   drop_table_if_exist ('intact__feature_alias__dm');
   drop_table_if_exist ('intact__experiment__main');
   drop_table_if_exist ('intact__experiment_xref__dm');
   drop_table_if_exist ('intact__experiment_anno__dm');
   drop_table_if_exist ('intact__experiment_alias__dm');
END;