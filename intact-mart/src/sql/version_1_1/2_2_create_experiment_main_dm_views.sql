-- #############################################################################
-- #                      intact__experiment__main                            
-- #############################################################################

-- how many interaction in an experiment
CREATE MATERIALIZED VIEW v_interaction_count AS
SELECT int_exp.experiment_ac AS exp_ac,
       COUNT(*)              AS interaction_count
FROM ia_int2exp int_exp
GROUP BY int_exp.experiment_ac;


-- #############################################################################
-- #                       intact__publication__dm                             
-- #############################################################################

-- how many experiments belong to a spezific publication and how many interaction
CREATE MATERIALIZED VIEW v_publication_counts AS
SELECT exp.publication_ac               AS pub_ac,
       SUM(int_count.interaction_count) AS interaction_count,  -- counts all interaction in a publication
       COUNT(*)                         AS experiment_count    -- counts all experiments in a publication
FROM ia_experiment exp LEFT OUTER JOIN  v_interaction_count int_count
                                    ON ( int_count.exp_ac = exp.ac )
GROUP BY exp.publication_ac
HAVING exp.publication_ac IS NOT NULL;

-- all pubmedids
CREATE MATERIALIZED VIEW v_publication_pmid AS
SELECT xref.parent_ac AS pub_ac,
       xref.primaryid AS pmid
FROM ia_publication_xref xref LEFT OUTER JOIN v_cv_mi cv
                                           ON ( xref.database_ac = cv.ac)
-- 'MI:0446' is equals pubmed
WHERE cv.mi = 'MI:0446'; 

-- all dois
CREATE MATERIALIZED VIEW v_publication_doi AS
SELECT xref.parent_ac AS pub_ac,
       xref.primaryid AS doi
FROM ia_publication_xref xref LEFT OUTER JOIN v_cv_mi cv
                                           ON ( xref.database_ac = cv.ac)
-- 'MI:0574' is equals doi
WHERE cv.mi = 'MI:0574'; 


-- the rest of possible mi identifiers
CREATE MATERIALIZED VIEW v_publication_mi AS
SELECT xref.parent_ac AS pub_ac,
       xref.primaryid       AS mi
FROM ia_publication_xref xref LEFT OUTER JOIN v_cv_mi cv
                                           ON ( xref.database_ac = cv.ac)
-- 'MI:0574' is equals doi
WHERE cv.mi <> 'MI:0574'
-- 'MI:0446' is equals pubmed
     and cv.mi <> 'MI:0446';