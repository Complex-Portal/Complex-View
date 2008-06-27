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
   -- views for first main and dimension tables
   drop_m_view_if_exist('v_experiment');
   drop_m_view_if_exist('v_interaction_count');
   drop_m_view_if_exist('v_experiment_xref');
   drop_m_view_if_exist('v_experiment_alias_type');
   drop_m_view_if_exist('v_publication_counts');
   drop_m_view_if_exist('v_publication_pmid');
   drop_m_view_if_exist('v_publication_doi');
   drop_m_view_if_exist('v_publication_mi');
   -- views for second main and dimension tables
   drop_m_view_if_exist('v_interaction');
   drop_m_view_if_exist('v_interaction_type');
   drop_m_view_if_exist('v_interactor_count');
   drop_m_view_if_exist('v_interaction_owner_xref');
   drop_m_view_if_exist('v_interaction_owner');
   drop_m_view_if_exist('v_component_xref');
   drop_m_view_if_exist('v_component_roles');
   -- views for third main and dimension tables
   drop_m_view_if_exist('v_interactor_type');
   drop_m_view_if_exist('v_interactor_alias_type');
   drop_m_view_if_exist('v_interactor_xref');
END;

-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
--    Just for testing if there are any not deleted table of the mart


-- SELECT * 
-- FROM user_tables
-- WHERE table_name LIKE 'V__%';