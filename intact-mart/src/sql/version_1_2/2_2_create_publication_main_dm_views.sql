-- #############################################################################
-- #                       intact__publication__main                       
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
