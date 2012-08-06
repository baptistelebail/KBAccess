/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.opens.kbaccess.entity.service.subject;

import java.util.List;
import org.opens.kbaccess.entity.authorization.Account;
import org.opens.kbaccess.entity.reference.*;
import org.opens.kbaccess.entity.subject.Testcase;
import org.opens.tanaguru.sdk.entity.service.GenericDataService;

/**
 *
 * @author jkowalczyk
 */
public interface TestcaseDataService
        extends GenericDataService<Testcase, Long> {

    /**
     *
     * @return
     */
    int getMaxPriorityFromTable();

    /**
     *
     * @param account
     * @return All the testcases created by a user or null if there are none.
     */
    List<Testcase> getAllFromAccount(Account account);

    /**
     *
     * @param account
     * @return the last five testcases created by a user or an empty list if
     *         there are no results
     */
    List<Testcase> getLastTestcasesFromAccount(Account account, int nbOfTestcases);

    /**
     *
     * @param account
     * @return the last five created testcases or an empty list if
     *         there a no results.
     */
    List<Testcase> getLastTestcases(int nbOfTestcases);

    /**
     *
     * @param reference
     * @param criterion
     * @param theme
     * @param test
     * @param level
     * @param result
     * @return The list of testcases corresponding to the search, an empty list
     *         if there are no results.
     */
    List<Testcase> getAllFromUserSelection (
            Reference reference,
            Criterion criterion,
            Theme theme,
            Test test,
            Level level,
            Result result);

}
