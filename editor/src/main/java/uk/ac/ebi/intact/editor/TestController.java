/**
 * Copyright 2010 The European Bioinformatics Institute, and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.intact.editor;

import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.TransactionStatus;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.core.unit.IntactMockBuilder;
import uk.ac.ebi.intact.model.*;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.model.SelectItem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
@Scope("session")
public class TestController {

    private CvInteraction cvInteraction;
    private CvAliasType cvAliasType;
    private CvIdentification cvIdentification;


    private List<SelectItem> selectItems;

    private TreeNode cvRoot;

    private BioSource bioSource3;
    private String lala;

    private List<Lala> lalas;

    public TestController() {
        final IntactMockBuilder mockBuilder = new IntactMockBuilder(IntactContext.getCurrentInstance().getInstitution());
        cvInteraction = mockBuilder.createCvObject(CvInteraction.class, CvInteraction.COSEDIMENTATION_MI_REF, CvInteraction.COSEDIMENTATION);
        cvAliasType = mockBuilder.createCvObject(CvAliasType.class, CvAliasType.GENE_NAME_MI_REF, CvAliasType.GENE_NAME);
        cvIdentification = mockBuilder.createCvObject(CvIdentification.class, CvIdentification.PREDETERMINED_MI_REF, CvIdentification.PREDETERMINED);

        this.selectItems = new ArrayList<SelectItem>();

        try {
            final TransactionStatus transactionStatus = IntactContext.getCurrentInstance().getDataContext().beginTransaction();
            CvDagObject cv = (CvDagObject) IntactContext.getCurrentInstance().getDaoFactory().getCvObjectDao().getByPsiMiRef("MI:0001");
            cvRoot = buildTreeNode(cv, null);
            IntactContext.getCurrentInstance().getDataContext().commitTransaction(transactionStatus);
        } catch (Exception e) {
            e.printStackTrace();
        }


        bioSource3 = mockBuilder.createBioSource(9606, "human");

        lala = "Hey lala!";

        lalas = Arrays.asList(new Lala(mockBuilder.createBioSource(9606, "human")),
                new Lala(mockBuilder.createBioSource(7, "seven")));
    }

     private TreeNode buildTreeNode( CvDagObject cv, TreeNode node ) {
       TreeNode childNode = new DefaultTreeNode(cv, node);

        for ( CvDagObject child : cv.getChildren() ) {
            buildTreeNode( child, childNode );
        }

        return childNode;
    }

    public void print(Object obj) {
        System.out.println("OBJ: "+obj);
    }

    public void lalaEvent(AjaxBehaviorEvent evt) {
        System.out.println("LALALALALA!!! EVENT!!!");
    }

    public void addMessage(ActionEvent evt) {
        FacesContext.getCurrentInstance().addMessage("testForm", new FacesMessage("Test message"));
    }

    public CvInteraction getCvInteraction() {
        return cvInteraction;
    }

    public void setCvInteraction(CvInteraction cvInteraction) {
        this.cvInteraction = cvInteraction;
    }

    public CvAliasType getCvAliasType() {
        return cvAliasType;
    }

    public void setCvAliasType(CvAliasType cvAliasType) {
        this.cvAliasType = cvAliasType;
    }

    public CvIdentification getCvIdentification() {
        return cvIdentification;
    }

    public void setCvIdentification(CvIdentification cvIdentification) {
        this.cvIdentification = cvIdentification;
    }

    public BioSource getBioSource3() {
        return bioSource3;
    }

    public void setBioSource3(BioSource bioSource3) {
        this.bioSource3 = bioSource3;
    }

    public String getLala() {
        return lala;
    }

    public void setLala(String lala) {
        this.lala = lala;
    }

    public List<Lala> getLalas() {
        return lalas;
    }

    public TreeNode getCvRoot() {
        return cvRoot;
    }

    public void setCvRoot(TreeNode cvRoot) {
        this.cvRoot = cvRoot;
    }

    public List<SelectItem> getSelectItems() {
        return selectItems;
    }

    public void setSelectItems(List<SelectItem> selectItems) {
        this.selectItems = selectItems;
    }

    public class Lala {
        private BioSource bioSource;

        private Lala(BioSource bioSource) {
            this.bioSource = bioSource;
        }

        public BioSource getBioSource() {
            return bioSource;
        }

        public void setBioSource(BioSource bioSource) {
            this.bioSource = bioSource;
        }
    }
}
