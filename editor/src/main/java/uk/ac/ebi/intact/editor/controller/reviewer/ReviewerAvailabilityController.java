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
package uk.ac.ebi.intact.editor.controller.reviewer;

import org.apache.myfaces.orchestra.conversation.annotations.ConversationName;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.intact.editor.controller.JpaAwareController;
import uk.ac.ebi.intact.model.user.Preference;
import uk.ac.ebi.intact.model.user.User;
import uk.ac.ebi.intact.model.util.UserUtils;

import javax.faces.event.ActionEvent;
import javax.faces.event.ComponentSystemEvent;
import java.util.List;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
@Scope( "conversation.access" )
@ConversationName( "reviewer" )
public class ReviewerAvailabilityController extends JpaAwareController {

    private List<User> reviewers;

    public ReviewerAvailabilityController() {
    }

    @Transactional(value = "transactionManager")
    public void loadData(ComponentSystemEvent evt) {
        reviewers = getDaoFactory().getUserDao().getReviewers();
    }

    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRES_NEW)
    public void save(ActionEvent evt) {
        for (User reviewer : reviewers) {

            Preference pref = reviewer.getPreference(Preference.KEY_REVIEWER_AVAILABILITY);

            if (pref != null) {
                getDaoFactory().getPreferenceDao().merge(pref);
            }

        }

        addInfoMessage("Saved", "The reviewers' availability has been updated");
    }

    public ReviewerWrapper wrapReviewer(User user) {
        return new ReviewerWrapper(user);
    }

    public List<User> getReviewers() {
        return reviewers;
    }

    public void setReviewers(List<User> reviewers) {
        this.reviewers = reviewers;
    }


    public class ReviewerWrapper {
        private User reviewer;

        private ReviewerWrapper(User reviewer) {
            this.reviewer = reviewer;
        }

        public int getAvailability() {
            return UserUtils.getReviewerAvailability(reviewer);
        }

        public void setAvailability(int value) {
            UserUtils.setReviewerAvailability(reviewer, value);
        }
    }
}
