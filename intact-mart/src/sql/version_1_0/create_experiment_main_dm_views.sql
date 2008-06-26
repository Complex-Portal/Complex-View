
-- #############################################################################
-- #                      intact__experiment__main                            
-- #############################################################################

CREATE MATERIALIZED VIEW v_experiment AS
SELECT exp.ac            AS ac,
       exp.shortlabel    AS experiment_short,
       exp.fullname      AS experiment_full,
       bio.taxid         AS host_organism_taxid,
       bio.shortlabel    AS host_organism_short,
       bio.fullname      AS host_organism_full,
       cv1.mi            AS interaction_detectMethod_mi,
       cv1.shortlabel    AS interaction_detectMethod_short,            
       cv1.fullname      AS interaction_detectMethod_full,
       cv2.mi            AS feature_identMethod_mi,
       cv2.shortlabel    AS feature_identMethod_short,            
       cv2.fullname      AS feature_identMethod_full
FROM ia_experiment exp LEFT OUTER JOIN  ia_biosource bio
                                    ON (bio.ac = exp.biosource_ac)                        
                       LEFT OUTER JOIN v_cv_mi cv1
                                    ON (cv1.ac = exp.detectmethod_ac)
                       LEFT OUTER JOIN v_cv_mi cv2
                                    ON (cv2.ac = exp.identmethod_ac);

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



-- #############################################################################
-- #                  intact__experiment_alias__dm                            
-- #############################################################################

-- there are no alias type for experiment
CREATE MATERIALIZED VIEW v_experiment_alias_type AS
SELECT ali.parent_ac    AS alias_ac,
       ali.name         AS name,
       cv.mi            AS alias_type_mi,
       cv.shortlabel    AS alias_type_short,
       cv.fullname      AS alias_type_full
FROM ia_experiment_alias ali LEFT OUTER JOIN v_cv_mi cv
                                          ON (cv.ac = ali.aliastype_ac);


-- #############################################################################
-- #                  intact__experiment_xref__dm                             
-- #############################################################################

CREATE MATERIALIZED VIEW v_experiment_xref AS
SELECT xref.parent_ac    AS xref_ac,
       xref.primaryid    AS primary_id,
       xref.secondaryid  AS secondary_id,
       cv1.mi            AS database_mi,
       cv1.shortlabel    AS database_short,
       cv1.fullname      AS database_full,
       cv2.mi            AS qualifier_mi,
       cv2.shortlabel    AS qualifier_short,
       cv2.fullname      AS qualifier_full
FROM ia_experiment_xref xref LEFT OUTER JOIN v_cv_mi cv1
                                          ON (cv1.ac = xref.database_ac)
                             LEFT OUTER JOIN v_cv_mi cv2
                                          ON (cv2.ac = xref.qualifier_ac);

