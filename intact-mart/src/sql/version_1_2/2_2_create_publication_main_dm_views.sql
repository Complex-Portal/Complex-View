-- #############################################################################
-- #                       intact__publication__main                       
-- #############################################################################

-- how many interaction are involved in an experiment
CREATE MATERIALIZED VIEW v_interaction_count AS
SELECT int_exp.experiment_ac AS exp_ac,
       COUNT(*)              AS interaction_count
FROM ia_int2exp int_exp
GROUP BY int_exp.experiment_ac;

-- how many experiments and interactions belong to a spezific publication 
CREATE MATERIALIZED VIEW v_publication_counts AS
SELECT exp.publication_ac               AS pub_ac,
       SUM(int_count.interaction_count) AS interaction_count,  -- counts all interaction in a publication
       COUNT(*)                         AS experiment_count    -- counts all experiments in a publication
FROM ia_experiment exp LEFT OUTER JOIN  v_interaction_count int_count
                                    ON ( int_count.exp_ac = exp.ac )
GROUP BY exp.publication_ac
HAVING exp.publication_ac IS NOT NULL;
