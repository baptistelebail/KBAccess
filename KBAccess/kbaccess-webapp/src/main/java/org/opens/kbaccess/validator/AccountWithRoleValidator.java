/*
 * Copyright (C) 2008-2012  Open-S Company
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
package org.opens.kbaccess.validator;

import org.opens.kbaccess.command.AccountCommand;
import org.opens.kbaccess.command.AccountWithRoleCommand;
import org.opens.kbaccess.entity.service.authorization.AccessLevelDataService;
import org.opens.kbaccess.entity.service.authorization.AccountDataService;
import org.opens.kbaccess.keystore.FormKeyStore;
import org.opens.kbaccess.keystore.MessageKeyStore;
import org.opens.kbaccess.validator.utils.PasswordValidator;
import org.springframework.validation.Errors;

/**
 * Validator used for user modification by admins only
 * 
 * @author blebail
 */
public class AccountWithRoleValidator extends AccountDetailsValidator { 

    AccessLevelDataService accessLevelDataService;
    boolean passwordChanged;
    
    public AccountWithRoleValidator(
            AccountDataService accountDataService, 
            AccessLevelDataService accessLevelDataService, 
            String originalEmail, 
            boolean passwordChanged) {
        super(accountDataService, originalEmail);
        this.accessLevelDataService = accessLevelDataService;
        this.passwordChanged = passwordChanged;
    }  

    @Override
    // We override this method because as an admin we don't need to enter a password to edit an account
    // 
    protected boolean validatePassword(AccountCommand cmd, Errors errors) { 
        if (passwordChanged) {
            if (cmd.getPassword().equals(cmd.getPasswordConfirmation()) == false) {
                errors.rejectValue(FormKeyStore.PASSWORD_KEY, MessageKeyStore.PASSWORD_MISMATCH_KEY);
                return false;
            } else if (PasswordValidator.validate(cmd.getPassword()) == false) {
                errors.rejectValue(FormKeyStore.PASSWORD_KEY, MessageKeyStore.INVALID_PASSWORD_KEY);
                return false;
            }
        }
        
        return true;
    }

    @Override
    protected boolean validateAccessLevel(AccountCommand cmd, Errors errors) {
        AccountWithRoleCommand command = (AccountWithRoleCommand)cmd;
        Long accessLevelId = command.getAccessLevelId();
        
        if (accessLevelId == null)
            return false;
        
        if (accessLevelDataService.read(accessLevelId) == null)
            return false;
        
        return true;
    } 
}