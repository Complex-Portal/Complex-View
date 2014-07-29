/**
 * Copyright 2011 The European Bioinformatics Institute, and others.
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
package uk.ac.ebi.intact.editor.controller.admin.report;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import org.joda.time.DateTime;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.intact.editor.controller.JpaAwareController;
import uk.ac.ebi.intact.model.CvLifecycleEventType;
import uk.ac.ebi.intact.model.LifecycleEvent;

import javax.faces.event.ActionEvent;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
@Scope("session")
public class AssignmentReportController extends JpaAwareController {

    private List<AssignmentInfo> assignmentInfos;
    private Date fromDate;
    private Date toDate;

    public AssignmentReportController() {
        fromDate = new DateTime().minusMonths(1).toDate();
        toDate = new DateTime().toDate();
    }

    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRED, readOnly = true)
    public void calculate(ActionEvent evt) {
        assignmentInfos = new ArrayList<AssignmentInfo>();

        Query query = getDaoFactory().getEntityManager().createQuery("select e from LifecycleEvent e where " +
                "e.event.identifier = :cvEventId and e.when >= :dateFrom and e.when <= :dateTo and e.note is null order by e.when");
        query.setParameter("cvEventId", CvLifecycleEventType.READY_FOR_CHECKING.identifier());
        query.setParameter("dateFrom", fromDate);
        query.setParameter("dateTo", new DateTime(toDate).plusDays(1).minusSeconds(1).toDate());

        List<LifecycleEvent> events = query.getResultList();

        Multiset<String> multiset = HashMultiset.create();

        for (LifecycleEvent event : events) {
            multiset.add(event.getPublication().getCurrentReviewer().getLogin());
        }

        int total = multiset.size();

        for (String reviewer : multiset.elementSet()) {
            int count = multiset.count(reviewer);
            int percentage = count * 100 / total;
            assignmentInfos.add(new AssignmentInfo(reviewer, count, percentage));
        }
    }

    public List<AssignmentInfo> getAssignmentInfos() {
        return assignmentInfos;
    }

    public void setAssignmentInfos(List<AssignmentInfo> assignmentInfos) {
        this.assignmentInfos = assignmentInfos;
    }

    public Date getFromDate() {
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getToDate() {
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public class AssignmentInfo {

        private String reviewer;
        private int count;
        private int percentage;

        private AssignmentInfo(String reviewer, int count, int percentage) {
            this.reviewer = reviewer;
            this.count = count;
            this.percentage = percentage;
        }

        public String getReviewer() {
            return reviewer;
        }

        public int getCount() {
            return count;
        }

        public int getPercentage() {
            return percentage;
        }
    }
}
