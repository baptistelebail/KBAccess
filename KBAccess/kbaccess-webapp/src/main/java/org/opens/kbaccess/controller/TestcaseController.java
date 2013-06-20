/*
 * KBAccess - Collaborative database of accessibility examples
 * Copyright (C) 2012-2016  Open-S Company
 *
 * This file is part of KBAccess.
 *
 * KBAccess is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contact us by mail: open-s AT open-s DOT com
 */
package org.opens.kbaccess.controller;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.logging.LogFactory;
import org.opens.kbaccess.command.DeleteTestcaseCommand;
import org.opens.kbaccess.command.EditTestcaseCommand;
import org.opens.kbaccess.command.NewTestcaseCommand;
import org.opens.kbaccess.controller.utils.AMailerController;
import org.opens.kbaccess.entity.authorization.Account;
import org.opens.kbaccess.entity.reference.*;
import org.opens.kbaccess.entity.service.subject.WebarchiveDataService;
import org.opens.kbaccess.entity.subject.Testcase;
import org.opens.kbaccess.entity.subject.Webarchive;
import org.opens.kbaccess.keystore.FormKeyStore;
import org.opens.kbaccess.keystore.ModelAttributeKeyStore;
import org.opens.kbaccess.presentation.AccountPresentation;
import org.opens.kbaccess.presentation.TestcasePresentation;
import org.opens.kbaccess.utils.AccountUtils;
import org.opens.kbaccess.validator.EditTestcaseValidator;
import org.opens.kbaccess.validator.NewTestcaseValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

/**
 *
 * @author bcareil
 */
@Controller
@RequestMapping("/example/")
public class TestcaseController extends AMailerController {
    
    @Autowired
    private WebarchiveDataService webarchiveDataService;
    @Autowired
    private WebarchiveController webarchiveController;

    /*
     * Private methods
     */      
    private Map<String, String> buildMapFromSearchParameters(
            Reference reference,
            Criterion criterion,
            Theme theme,
            Test test,
            Level level,
            Result result
            ) {
        Map<String, String> parametersMap = new LinkedHashMap<String, String>();
        
        // building criteria list
        if (reference != null) {
            parametersMap.put("accessibility.reference", reference.getLabel());
        }
         if (theme != null) {
            parametersMap.put("accessibility.theme", theme.getLabel());
        }
        if (criterion != null) {
            parametersMap.put("accessibility.criterion", criterion.getLabel());
        }
         if (test != null) {
            parametersMap.put("accessibility.test", test.getLabel());
        }
        if (level != null) {
            parametersMap.put("accessibility.level", level.getCode());
        }
        if (result != null) {
            parametersMap.put("result", result.getCode());
        }
        
        // post process criteria list
        if (parametersMap.isEmpty()) {
            // no criteria
            parametersMap.put("testcase.searchAllTestcasesTitle", reference.getLabel());
        } 
        
        return parametersMap;
    }
    
    private String displayAddTestcaseForm(Model model, NewTestcaseCommand newTestcaseCommand) {
        // handle login and breadcrumb
        handleUserLoginForm(model);
        handleBreadcrumbTrail(model);
        // create the form command
        model.addAttribute("testByRef", getTestByRef());
        model.addAttribute("resultList", getResults());
        model.addAttribute("newTestcaseCommand", newTestcaseCommand);
        return "testcase/add";
    }
    
    private String displayAttachWebarchiveForm(Model model, NewTestcaseCommand newTestcaseCommand) {
        // handle login and breadcrumb
        handleUserLoginForm(model);
        handleBreadcrumbTrail(model);
        // create the form command
        model.addAttribute("webarchiveList", webarchiveDataService.findAll());
        model.addAttribute("newTestcaseCommand", newTestcaseCommand);
        return "testcase/add-webarchive";        
    }
    
    private String displayEditTestcaseForm(Model model, EditTestcaseCommand editTestcaseCommand, String errorMessage) {
        // handle login form and breadcrumb
        handleUserLoginForm(model);
        handleBreadcrumbTrail(model);
        // create form
        if (errorMessage != null) {
            model.addAttribute("errorMessage", errorMessage);
        } else {
            model.addAttribute("testByRef", getTestByRef());
            model.addAttribute("resultList", getResults());
            model.addAttribute("editTestcaseCommand", editTestcaseCommand);
        }
        return "testcase/edit-details";
    }
    
    private String displayTestcaseDetails(Model model, Testcase testcase) {
       TestcasePresentation testcasePresentation = new TestcasePresentation(testcase, true);
       
        // handle form login
        handleUserLoginForm(model);
        // handle breadcrumb
        handleBreadcrumbTrail(model);
        model.addAttribute("testcase", testcasePresentation);
        return "testcase/details";
    }

    /*
     * Endpoints
     */
    
    @RequestMapping(value="search-by-url")
    public String searchByUrlHandler(Model model) {
        // handle login form and breadcrumb trail
        handleUserLoginForm(model);
        handleBreadcrumbTrail(model);
        return "testcase/search-by-url";
    }
    
    @RequestMapping(value={"search", "result"}, method=RequestMethod.GET)
    public String searchHandler(Model model) {
        // handle login form, breadcrumb and search form
        handleUserLoginForm(model);
        handleBreadcrumbTrail(model);
        handleTestcaseSearchForm(model);
        return "testcase/search";
    }
       
    @RequestMapping(value="list")
    public String listHandler(
            Model model,
            @RequestParam(value="account", required=false) Long idAccount,
            @RequestParam(value="reference", required=false) Long idReference,
            @RequestParam(value="theme", required=false) Long idTheme,
            @RequestParam(value="level", required=false) Long idLevel,
            @RequestParam(value="criterion", required=false) Long idCriterion,
            @RequestParam(value="test", required=false) Long idTest,
            @RequestParam(value="result", required=false) Long idResult
            ) {
        Collection<TestcasePresentation> testcases;
        String contextOfRequest = null;
        boolean joker;
        Reference reference;
        Theme theme;
        Level level;
        Criterion criterion;
        Test test;
        Result result;
        
        // fetch all testcases ?
        joker = (idAccount == null
                && idReference == null
                && idTheme == null
                && idLevel == null
                && idCriterion == null
                && idTest == null
                && idResult == null
                );
        // fetch entities and set title and H1
        if (joker) {
            testcases = TestcasePresentation.fromCollection(
                    (Collection) testcaseDataService.findAll(),
                    true
                    );
        contextOfRequest = "allTestcases";
        handleBreadcrumbTrail(model);
        // fetch the testcases of a precise user
        } else if (idAccount != null) {
            String authorDisplayedName;
            
            Account account = accountDataService.read(idAccount);
            authorDisplayedName = AccountPresentation.generateDisplayedName(account);
            
            testcases = TestcasePresentation.fromCollection(
                testcaseDataService.getAllFromAccount(account),
                true
                );
            
            contextOfRequest = "userTestcases";
            handleBreadcrumbTrail(model);
            model.addAttribute("account", new AccountPresentation(account, accountDataService));
        // All other requests combinations
        } else {
            reference = (idReference == null ? null : referenceDataService.read(idReference));
            theme = (idTheme == null ? null : themeDataService.read(idTheme));
            level = (idLevel == null ? null : levelDataService.read(idLevel));
            criterion = (idCriterion == null ? null : criterionDataService.read(idCriterion));
            test = (idTest == null ? null : testDataService.read(idTest));
            result = (idResult == null ? null : resultDataService.read(idResult));
            testcases = TestcasePresentation.fromCollection(
                    testcaseDataService.getAllFromUserSelection(reference, criterion, theme, test, level, result),
                    true
                    );
            
            handleBreadcrumbTrail(model);
            model.addAttribute("parameterMap", buildMapFromSearchParameters(reference, criterion, theme, test, level, result));
        }
        // handle login and breadcrumb
        handleUserLoginForm(model); 
        model.addAttribute("contextOfRequest", contextOfRequest);
        
        // result list
        model.addAttribute(ModelAttributeKeyStore.TESTCASE_LIST_KEY, testcases);
        model.addAttribute("showTestcaseSearchForm", false);
        return "testcase/list";
    }
    
    /*
     * Handlers to add a testcase
     */
    
    @RequestMapping(value={"add", "add-finalize"}, method=RequestMethod.GET)
    public String addHandler(Model model) {
        return displayAddTestcaseForm(model, new NewTestcaseCommand());
    }
    
    @RequestMapping(value="add", method=RequestMethod.POST)
    public String addHandler(
            @ModelAttribute("newTestcaseCommand") NewTestcaseCommand testcaseCommand,
            BindingResult result,
            Model model
            ) {
        NewTestcaseValidator newTestcaseValidator = new NewTestcaseValidator(
                criterionDataService,
                testDataService,
                resultDataService,
                webarchiveDataService,
                NewTestcaseValidator.Step.STEP_TESTCASE
                );

        // validate command
        newTestcaseValidator.validate(testcaseCommand, result);
        if (result.hasErrors()) {
            // return to the first step
            return displayAddTestcaseForm(model, testcaseCommand);
        }
        // display the second step
        return displayAttachWebarchiveForm(model, testcaseCommand);
    }
    
    @RequestMapping(value="add-finalize", method=RequestMethod.POST)
    public String finalizeAddHandler(
            @ModelAttribute("newTestcaseCommand") NewTestcaseCommand testcaseCommand,
            BindingResult result,
            Model model
            ) {
        Webarchive webarchive;
        Account currentUser;
        Testcase newTestcase;
        TestcasePresentation testcasePresentation;
        NewTestcaseValidator newTestcaseValidator = new NewTestcaseValidator(
                criterionDataService,
                testDataService,
                resultDataService,
                webarchiveDataService,
                NewTestcaseValidator.Step.STEP_WEBARCHIVE
                );
        
        // validate command
        newTestcaseValidator.validate(testcaseCommand, result);
        if (result.hasErrors()) {
            if (result.hasFieldErrors(FormKeyStore.ID_TEST_KEY) || result.hasFieldErrors(FormKeyStore.ID_RESULT_KEY)) {
                // return to the first step if we have an error on the test or result id
                return displayAddTestcaseForm(model, testcaseCommand);
            } else {
                // return to the second step
                return displayAttachWebarchiveForm(model, testcaseCommand);
            }
        }
        // handle login and breadcrumb
        handleUserLoginForm(model);
        handleBreadcrumbTrail(model);
        // sanity check
        currentUser = AccountUtils.getInstance().getCurrentUser();
        if (currentUser == null) {
            LogFactory.getLog(TestcaseController.class).error("An unauthentified user reached testcase/add-finalize. Check spring security configuration.");
            return "guest/login";
        }
        // get webarchive
        if (!testcaseCommand.getCreateWebarchive()) {
            webarchive = webarchiveDataService.read(testcaseCommand.getIdWebarchive());
        } else {
            webarchive = webarchiveController.createWebarchive(
                    currentUser,
                    testcaseCommand.getUrlNewWebarchive(),
                    testcaseCommand.getDescriptionNewWebarchive()
                    );
            if (webarchive != null) {
                // persist the webarchive
                webarchiveDataService.saveOrUpdate(webarchive);
            } // a null webarchive is handled below
        }
        // sanity check
        if (webarchive == null) {
            // most of the time, if the webarchive is null, it means its creation
            // fails.
            testcaseCommand.setGeneralErrorMessage("Unable to create the webarchive.");
            // return to the second step
            return displayAttachWebarchiveForm(model, testcaseCommand);
        }
        // create testcase
        if (testcaseCommand.getIdTest() != null) {
            newTestcase = testcaseDataService.createFromTest(
                    currentUser,
                    webarchive,
                    resultDataService.read(testcaseCommand.getIdResult()),
                    testDataService.read(testcaseCommand.getIdTest()),
                    testcaseCommand.getDescription()
                    );
        } else {
            newTestcase = testcaseDataService.createFromCriterion(
                    currentUser,
                    webarchive,
                    resultDataService.read(testcaseCommand.getIdResult()),
                    criterionDataService.read(testcaseCommand.getIdCriterion()),
                    testcaseCommand.getDescription()
                    );            
        }
        // persist testcase
        newTestcase.setTitle("title"); // FIXME: title is not used anymore
        testcaseDataService.saveOrUpdate(newTestcase);
        // email notification
        sendTestcaseCreationNotification(newTestcase);
        // display testcase
        
        testcasePresentation = new TestcasePresentation(newTestcase, true);
        model.addAttribute("testcase", testcasePresentation);
        return "testcase/add-summary";
    }
    
    /*
     * Handlers to modify a testcase
     */
    @RequestMapping(value="edit-details/{id}/*", method=RequestMethod.GET)
    public String editDetailsHandler(
            @PathVariable("id") Long id,
            Model model
            ) {
        EditTestcaseCommand editTestcaseCommand;
        Testcase testcase = null;
        Account account;
        TestcasePresentation testcasePresentation;
        
        // fetch test case
        try {
            testcase = testcaseDataService.read(id, true);
        } catch (NullPointerException e) {
            LogFactory.getLog(TestcaseController.class.getName()).debug("testcase doesn't exist");
        }
        
        if (testcase == null) {
            model.addAttribute("errorMessage", "Cet exemple n'existe pas");
            return "testcase/edit-details";
        }
        // check permissions
        account = AccountUtils.getInstance().getCurrentUser();
        if (account == null) {
            LogFactory.getLog(TestcaseController.class).error("An unauthentified user reached testcase/edit-details. Check spring security configuration.");
            return "guest/login";
        } else if (!AccountUtils.getInstance().currentUserhasPermissionToEditTestcase(testcase)) {
            model.addAttribute("errorMessage", "Vous n'êtes pas autorisé à modifier cet exemple.");
            return "testcase/edit-details";
        }
        
        testcasePresentation = new TestcasePresentation(testcase, true);
        model.addAttribute("testcase", testcasePresentation);
        // create form
        editTestcaseCommand = new EditTestcaseCommand(testcase);

        return displayEditTestcaseForm(model, editTestcaseCommand, null);
    }
    
    @RequestMapping(value="edit-details/{id}/*", method=RequestMethod.POST)
    public String editDetailsHandler(
            @ModelAttribute("editTestcaseCommand") EditTestcaseCommand editTestcaseCommand,
            BindingResult result,
            Model model
            ) {
        Testcase testcase;
        Account account;
        EditTestcaseValidator testcaseValidator = new EditTestcaseValidator(
                testcaseDataService,
                resultDataService,
                testDataService
                );
        
        // fetch account
        account = AccountUtils.getInstance().getCurrentUser();
        if (account == null) {
            LogFactory.getLog(TestcaseController.class).error("An unauthentified user reached edit-details. Check spring security configuration.");
            return "guest/login";
        }
        
        // fetch testcase
        testcase = testcaseDataService.read(editTestcaseCommand.getId(), true);
        
        if (testcase == null) {
            return displayEditTestcaseForm(model, null, "Exemple invalide.");
        }
        
        // check permisions
        if (!AccountUtils.getInstance().currentUserhasPermissionToEditTestcase(testcase)) {
            return displayEditTestcaseForm(model, null, "Vous n'êtes pas autorisé à modifier cet exemple.");
        }
        
        // validate form
        testcaseValidator.validate(editTestcaseCommand, result);
        
        if (result.hasErrors()) {    
            return displayEditTestcaseForm(model, editTestcaseCommand, null);
        }
        
        // update testcase
        editTestcaseCommand.update(testcase, criterionDataService, testDataService, testresultDataservice, resultDataService);
        testcaseDataService.saveOrUpdate(testcase);
        
        // confirmation message
        model.addAttribute("successMessage", "L'exemple a bien été modifié.");
        return displayTestcaseDetails(model, testcase);
    }
    
    @RequestMapping(value="details/{id}/*", method={RequestMethod.GET, RequestMethod.POST})
    public String detailsHandler(
            @PathVariable("id") Long id,
            Model model
            ) {       
        Testcase testcase;
        
        // fetch testcase
        try {
            testcase = testcaseDataService.read(id, true);
        } catch (NullPointerException e) {
            return "home";
        }
        
        return displayTestcaseDetails(model, testcase);
    }
    
    /*
     * Handlers to delete a testcase
     */
    @RequestMapping(value="delete/{id}/*", method=RequestMethod.GET)
    public String deleteHandler(
            @PathVariable("id") Long id,
            Model model
            ) {
        DeleteTestcaseCommand deleteTestcaseCommand;
        Testcase testcase;
        Account account;
        TestcasePresentation testcasePresentation;
        
        // handle login and breadcrumb
        handleUserLoginForm(model);
        handleBreadcrumbTrail(model);
        
        // fetch test case
        try {
            testcase = testcaseDataService.read(id, true);
        } catch (NullPointerException e) {
            model.addAttribute("errorMessage", "Cet exemple n'existe pas");  
            return "testcase/delete";
        }

        // check permissions
        account = AccountUtils.getInstance().getCurrentUser();
        if (account == null) {
            LogFactory.getLog(TestcaseController.class).error("An unauthentified user reached testcase/delete. Check spring security configuration.");
            return "guest/login";
        } else if (!AccountUtils.getInstance().currentUserhasPermissionToEditTestcase(testcase)) {
            model.addAttribute("errorMessage", "Vous n'êtes pas autorisé à supprimer cet exemple.");
            return "testcase/delete";
        }
        
        deleteTestcaseCommand = new DeleteTestcaseCommand(testcase);
        testcasePresentation = new TestcasePresentation(testcase, true);
        model.addAttribute("deleteTestcaseCommand", deleteTestcaseCommand);
        model.addAttribute("testcase", testcasePresentation);
        
        return "testcase/delete";
    }
    
    @RequestMapping(value="delete/{id}/*", method=RequestMethod.POST)
    public String confirmDeleteHandler(
            @ModelAttribute("deleteTestcaseCommand") DeleteTestcaseCommand deleteTestcaseCommand,
            BindingResult result,
            Model model
            ) {
        Testcase testcase;
        Account account;
        TestcasePresentation testcasePresentation;
        
        // handle login and breadcrumb
        handleUserLoginForm(model);
        handleBreadcrumbTrail(model);
        
        // fetch test case
        try {
            testcase = testcaseDataService.read(deleteTestcaseCommand.getId(), true);
        } catch (NullPointerException e) {
            model.addAttribute("errorMessage", "testcase doesn't exist");  
            return "testcase/delete";
        }
        
        if (testcase == null) {
            model.addAttribute("errorMessage", "Ce testcase n'existe pas");
            return "testcase/delete";
        }
        // check permissions
        account = AccountUtils.getInstance().getCurrentUser();
        if (account == null) {
            LogFactory.getLog(TestcaseController.class).error("An unauthentified user reached testcase/delete. Check spring security configuration.");
            return "guest/login";
        } else if (!AccountUtils.getInstance().currentUserhasPermissionToEditTestcase(testcase)) {
            model.addAttribute("errorMessage", "Vous n'êtes pas autorisé à supprimer ce testcase.");
            return "testcase/delete";
        }
        
        // delete the testcase
        testcaseDataService.delete(testcase.getId());
        
        testcasePresentation = new TestcasePresentation(testcase, true);
        model.addAttribute("testcase", testcasePresentation);
        // confirmation message
        model.addAttribute("successMessage", "L'exemple " + testcase.getId() + " a bien été supprimé.");
        
        return "testcase/delete";
    }
    
    /*
     * Accessors
     */

    public WebarchiveDataService getWebarchiveDataService() {
        return webarchiveDataService;
    }

    public void setWebarchiveDataService(WebarchiveDataService webarchiveDataService) {
        this.webarchiveDataService = webarchiveDataService;
    }

}
