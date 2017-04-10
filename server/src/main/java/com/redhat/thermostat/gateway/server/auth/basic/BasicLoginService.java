/*
 * Copyright 2012-2017 Red Hat, Inc.
 *
 * This file is part of Thermostat.
 *
 * Thermostat is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2, or (at your
 * option) any later version.
 *
 * Thermostat is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Thermostat; see the file COPYING.  If not see
 * <http://www.gnu.org/licenses/>.
 *
 * Linking this code with other modules is making a combined work
 * based on this code.  Thus, the terms and conditions of the GNU
 * General Public License cover the whole combination.
 *
 * As a special exception, the copyright holders of this code give
 * you permission to link this code with independent modules to
 * produce an executable, regardless of the license terms of these
 * independent modules, and to copy and distribute the resulting
 * executable under terms of your choice, provided that you also
 * meet, for each linked independent module, the terms and conditions
 * of the license of that module.  An independent module is a module
 * which is not derived from or based on this code.  If you modify
 * this code, you may extend this exception to your version of the
 * library, but you are not obligated to do so.  If you do not wish
 * to do so, delete this exception statement from your version.
 */

package com.redhat.thermostat.gateway.server.auth.basic;

import java.util.ArrayList;

import javax.security.auth.Subject;

import org.eclipse.jetty.security.DefaultIdentityService;
import org.eclipse.jetty.security.DefaultUserIdentity;
import org.eclipse.jetty.security.IdentityService;
import org.eclipse.jetty.security.LoginService;
import org.eclipse.jetty.server.UserIdentity;

public class BasicLoginService implements LoginService {

    private final BasicUserStore store;
    private final String realmName;
    private IdentityService identityService = new DefaultIdentityService();

    public BasicLoginService(BasicUserStore store, String realmName) {
        this.store = store;
        this.realmName = realmName;
    }

    @Override
    public String getName() {
        return realmName;
    }

    @Override
    public UserIdentity login(final String username, Object credentials) {
        BasicWebUser user = (BasicWebUser)store.getUser(username);
        if (user == null) {
            return null;
        }
        if (credentials instanceof String) {
            String pw = (String) credentials;
            if (user.getPassword().equals(pw)) {
                String[] roles = new ArrayList<>(user.getRoles())
                        .toArray(new String[] {});
                return new DefaultUserIdentity(new Subject(), user, roles);
            }
        }
        return null;
    }

    @Override
    public boolean validate(UserIdentity user) {
        return store.getUser(user.getUserPrincipal().getName()) != null;
    }

    @Override
    public IdentityService getIdentityService() {
        return identityService;
    }

    @Override
    public void setIdentityService(IdentityService service) {
        this.identityService = service;
    }

    @Override
    public void logout(UserIdentity user) {
        // nothing
    }

}
