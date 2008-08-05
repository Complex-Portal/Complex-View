-- #############################################################################
-- #                      intact__experiment__main                            
-- #############################################################################

-- how many interaction in an experiment
CREATE MATERIALIZED VIEW v_interaction_count AS
SELECT int_exp.experiment_ac AS exp_ac,
       COUNT(*)              AS interaction_count
FROM ia_int2exp int_exp
GROUP BY int_exp.experiment_ac;

