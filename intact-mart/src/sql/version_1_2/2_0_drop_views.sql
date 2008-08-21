-- if views exist, then drop these.
DECLARE

PROCEDURE drop_m_view_if_exist (tbl_name IN VARCHAR2) IS
   v_count NUMBER(5) := 0;
BEGIN

   SELECT COUNT(*)
   INTO v_count
   FROM user_tables
   WHERE upper(table_name) = upper(tbl_name); 

   IF ( v_count = 1 ) THEN
       EXECUTE IMMEDIATE 'DROP MATERIALIZED VIEW ' || tbl_name;
   else
       dbms_output.put_line( tbl_name || ' doesn''t exist');
   END IF;
END;

BEGIN
   -- common views
   drop_m_view_if_exist('v_cv_primaryid');
   drop_m_view_if_exist('v_cv_mi');
   drop_m_view_if_exist('v_annotation');
   -- views for the first main and dimension tables
   drop_m_view_if_exist('v_interaction_count');
   drop_m_view_if_exist('v_publication_counts');
   -- views for the third main and dimension tables
   drop_m_view_if_exist('v_interactor_count');
   drop_m_view_if_exist('v_component_count');
   drop_m_view_if_exist('v_interaction');
   drop_m_view_if_exist('v_interaction_owner_xref');
   drop_m_view_if_exist('v_interaction_owner');
   -- views for the fourth main and dimension tables
   drop_m_view_if_exist('v_com_roles');
   -- views for the fifth main and dimension tables
   drop_m_view_if_exist('v_involved_interaction_count');
   drop_m_view_if_exist('v_interactor');
   drop_m_view_if_exist('v_uniprotkb');
   -- view for the sixth main table
   drop_m_view_if_exist('v_feature');
END;

-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
--    Just for testing if there are any not deleted table of the mart


-- SELECT * 
-- FROM user_tables
-- WHERE table_name LIKE 'V__%';