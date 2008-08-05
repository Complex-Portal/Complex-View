-- #############################################################################
-- #                    fills a temporary table with 
-- #             the molecule count of every interaction.
-- #         If for one component the stoichiometry ( equals 0)  
-- #            is unknown, the whole molecule count is null.
-- #############################################################################


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
