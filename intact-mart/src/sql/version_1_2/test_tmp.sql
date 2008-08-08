-- there is only a one to one relationship between component interactor
SELECT *
FROM

(SELECT interaction.ac AS ac,
       COUNT(*)       AS component_count
FROM ia_interactor interaction,
     ia_component com
WHERE interaction.ac = com.interaction_ac
GROUP BY interaction.ac)  com,

(SELECT interaction.ac AS ac,
       COUNT(*)       AS interactor_count
FROM v_interaction_type interaction,
     ia_component com,
     v_interactor_type interactor
WHERE interaction.ac = com.interaction_ac
      and com.interactor_ac = interactor.interactor_ac
GROUP BY interaction.ac)   inter

WHERE com.ac = inter.ac 
      and com.component_count <> inter.interactor_count;
      
      

SELECT * 
FROM user_tables 
WHERE table_name LIKE 'META%';





SELECT interaction.ac AS ac,
       COUNT(*)       AS component_count
FROM ia_interactor interaction,
     ia_component com
WHERE interaction.ac = com.interaction_ac
GROUP BY interaction.ac
ORDER BY interaction.ac





DESC intact__interaction__main

SELECT interaction_key, molecule_count
FROM intact__interaction__main
WHERE molecule_count = 1

SELECT COUNT(*) 
FROM intact__interaction__main
WHERE molecule_count IS NOT NULL

-- moleculcount equals 1 ??????
-- EBI-1381500
-- EBI-1380837
-- EBI-986047
-- EBI-985998

-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

DECLARE
  CURSOR get_molecule_count IS
    SELECT interaction.ac,
           com.stoichiometry
    FROM ia_interactor interaction,
         ia_component com
    WHERE interaction.ac = com.interaction_ac
    ORDER BY interaction.ac;
    
  -- results from the cursor
  ac ia_interactor.ac%TYPE;
  stoichiometry ia_component.stoichiometry%TYPE;
  -- stores the last interactor_ac
  last_ac ia_interactor.ac%TYPE;
  -- result  
  molecule_count intact__interaction__main.component_count%TYPE := 0; 
  -- stoichiometry available
  sm INTEGER := 1;
BEGIN
  
  OPEN get_molecule_count;
  FETCH get_molecule_count INTO last_ac, stoichiometry;
  
  IF stoichiometry = 0 THEN
    sm := 0;
  ELSE
    molecule_count := molecule_count + stoichiometry;
  END IF;
  
  IF get_molecule_count%FOUND THEN
    LOOP 
    
      FETCH get_molecule_count INTO ac, stoichiometry;
      EXIT WHEN get_molecule_count%NOTFOUND;
      
      IF last_ac = ac THEN
        IF stoichiometry = 0 THEN
          sm := 0;
        ELSE
          molecule_count := molecule_count + stoichiometry;
        END IF;
      ELSE 
        IF sm = 1 THEN
          INSERT INTO tbl_molecule_count_tmp
               VALUES ( last_ac, molecule_count);
        END IF;
        IF stoichiometry = 0 THEN
          sm := 0;
        ELSE
          molecule_count := stoichiometry;
          sm := 1;
        END IF;
      END IF;
      last_ac := ac;
    END LOOP;
    
    INSERT INTO tbl_molecule_count_tmp 
           VALUES ( last_ac, molecule_count);   
  END IF;
  CLOSE get_molecule_count;
  COMMIT;
END;

--~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    SELECT interaction.ac,
           com.stoichiometry
    FROM ia_interactor interaction,
         ia_component com
    WHERE interaction.ac = com.interaction_ac
          and interaction.ac = 'EBI-1380837'
    ORDER BY interaction.ac;

SELECT * FROM intact__publication__dm

SELECT * FROM intact__interactor__main WHERE involved_interaction_count is not null

SELECT * FROM v_interactor_type

SELECT * FROM  v_involved_interaction_count ic, ia_interactor inter
WHERE inter.ac = ic.interactor_ac

SELECT * FROM  v_involved_interaction_count ic, ia_component com
WHERE com.interaction_ac = ic.interactor_ac

SELECT 'DROP TABLE ' || table_name || ';' FROM user_tables where table_name like 'META_%'


CREATE TABLE tbl_test (
  teste varchar2(30),
  blub integer
);

CREATE INDEX tbl_test_index
ON tbl_test (teste,blub);

INSERT INTO tbl_test VALUES ('asdf',12);

GRANT SELECT ON TBL_TEST_INDEX TO INTACT_SELECT;


SELECT * FROM intact__interactor__main


SELECT 'DROP TABLE '|| table_name ||';' FROM user_tables WHERE table_name LIKE 'META_%'

-- -----------------------------------------------------------------------------


select * from intact__publication__dm


select * 
from intact__publication__dm pub,
     intact__experiment__main exp
where exp.experiment_key = pub.experiment_key



SELECT int_exp.interaction_ac AS experiment_key,
       pub.pmid               AS pmid,         
       exp.fullname           AS title,
       null                   AS doi,
       interaction_count,
       experiment_count
FROM ia_experiment exp JOIN v_publication_pmid pub
                         ON ( pub.pub_ac = exp.publication_ac)
                       JOIN ia_int2exp int_exp
                         ON ( int_exp.experiment_ac = exp.ac )
            LEFT OUTER JOIN v_publication_counts pub_counts
                         ON ( pub_counts.pub_ac = exp.publication_ac )
                         

TRUNCATE TABLE intact__publication__dm;


--- ---------------------------------------------------------------------------

SELECT DISTINCT link FROM ia_range;


SELECT  pmid from intact__publication__dm
                         
SELECT DISTINCT to_fuzzytype_short from intact__range__dm             
                      
                      
                      -------------------
                      
                      
                      
SELECT * 
FROM ia_publication_xref 
WHERE primaryid like '999999%';



SELECT inter.ac,          -- interaction_key
       cv.mi,             -- interaction_type_mi
       cv.shortlabel,     -- interaction_type_short
       cv.fullname        -- interaction_type_full
FROM ia_interactor inter LEFT OUTER JOIN v_cv_mi cv
                                      ON ( inter.interactortype_ac = cv.ac )
WHERE cv.mi = 'MI:0317';  -- 'interaction'
DROP  TABLE intact__interactor__main


SELECT 'DROP TABLE ' || table_name || ';'
FROM user_tables 
WHERE table_name LIKE 'META_%'

DROP TABLE META_CONF__DATASET__MAIN;
DROP TABLE META_CONF__INTERFACE__DM;
DROP TABLE META_CONF__USER__DM;
DROP TABLE META_CONF__XML__DM;
DROP TABLE META_TEMPLATE__TEMPLATE__MAIN;
DROP TABLE META_TEMPLATE__XML__DM;
DROP TABLE META_VERSION__VERSION__MAIN;

SELECT * FROM intact__experiment_alias__dm 


-- -----------------------------------------------------------------------------
-- dummy inserts
INSERT INTO intact__publication_alias__dm 
VALUES ('test','test','test','test','test' );

INSERT INTO intact__experiment_alias__dm 
VALUES ('test','test','test','test','test' );

INSERT INTO intact__interaction_alias__dm 
VALUES ('test','test','test','test','test' );

INSERT INTO intact__component_alias__dm 
VALUES ('test','test','test','test','test' );

INSERT INTO intact__feature_alias__dm 
VALUES ('test','test','test','test','test' );

-- ------------
INSERT INTO intact__publication_anno__dm 
VALUES ('test','test','test','test','test' );

INSERT INTO intact__component_anno__dm 
VALUES ('test','test','test','test','test' );

INSERT INTO intact__feature_anno__dm 
VALUES ('test','test','test','test','test' );
-- ------------
INSERT INTO intact__publication_xref__dm 
VALUES ('test','test','test','test','test','test','test','test','test' );

INSERT INTO intact__component_xref__dm 
VALUES ('test','test','test','test','test','test','test','test','test' );

commit;








-- -----------------------------------------------------------------------------
