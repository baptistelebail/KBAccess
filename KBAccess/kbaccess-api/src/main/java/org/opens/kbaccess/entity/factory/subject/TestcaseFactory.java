package org.opens.kbaccess.entity.factory.subject;

import org.opens.kbaccess.entity.authorization.Account;
import org.opens.kbaccess.entity.reference.Result;
import org.opens.kbaccess.entity.reference.Test;
import org.opens.kbaccess.entity.subject.Testcase;
import org.opens.kbaccess.entity.subject.Webarchive;
import org.opens.tanaguru.sdk.entity.factory.GenericFactory;

/**
 * 
 * @author jkowalczyk
 * @version 1.0.0
 */
public interface TestcaseFactory extends GenericFactory<Testcase> {

    /**
     * Create a fully initialized testcase.
     * 
     * @param account
     * @param webarchive
     * @param result
     * @param test
     * @param description
     * @return 
     */
    Testcase create(
            Account account,
            Webarchive webarchive,
            Result result,
            Test test,
            String description
            );
}