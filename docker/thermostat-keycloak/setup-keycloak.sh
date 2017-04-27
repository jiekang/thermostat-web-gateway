#!/bin/bash
#
# Copyright 2012-2017 Red Hat, Inc.
#
# This file is part of Thermostat.
#
# Thermostat is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published
# by the Free Software Foundation; either version 2, or (at your
# option) any later version.
#
# Thermostat is distributed in the hope that it will be useful, but
# WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
# General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with Thermostat; see the file COPYING.  If not see
# <http://www.gnu.org/licenses/>.
#
# Linking this code with other modules is making a combined work
# based on this code.  Thus, the terms and conditions of the GNU
# General Public License cover the whole combination.
#
# As a special exception, the copyright holders of this code give
# you permission to link this code with independent modules to
# produce an executable, regardless of the license terms of these
# independent modules, and to copy and distribute the resulting
# executable under terms of your choice, provided that you also
# meet, for each linked independent module, the terms and conditions
# of the license of that module.  An independent module is a module
# which is not derived from or based on this code.  If you modify
# this code, you may extend this exception to your version of the
# library, but you are not obligated to do so.  If you do not wish
# to do so, delete this exception statement from your version.
#


KEYCLOAK_ADMIN=tms-admin
THERMOSTAT_USER=tms-user
THERMOSTAT_PASSWORD=tms-pass
REALM=thermostat

SERVER=http://localhost:8080/auth
CLI=keycloak/bin/kcadm.sh

keycloak/bin/add-user-keycloak.sh --user ${KEYCLOAK_ADMIN} --password ${KEYCLOAK_ADMIN}

keycloak/bin/standalone.sh & >/dev/null 2&>1

# Wait for keycloak to startup
sleep 10

${CLI} config credentials --server ${SERVER} --realm master --user ${KEYCLOAK_ADMIN} --password ${KEYCLOAK_ADMIN}

${CLI} create realms -s realm=${REALM} -s enabled=true

${CLI} create roles -r ${REALM} -s name=thermostat

${CLI} create clients -r ${REALM} -s clientId=thermostat-bearer -s enabled=true -s bearerOnly=true

${CLI} create clients -r ${REALM} -s clientId=thermostat-web-client -s enabled=true -s publicClient=true -s 'redirectUris=["http://localhost:8080/*"]' -s 'webOrigins=["+"]' -s directAccessGrantsEnabled=true

${CLI} create users -r ${REALM} -s enabled=true -s username=${THERMOSTAT_USER}
${CLI} add-roles -r ${REALM} --uusername ${THERMOSTAT_USER} --rolename thermostat
${CLI} set-password -r ${REALM} --username ${THERMOSTAT_USER} --new-password ${THERMOSTAT_PASSWORD}

keycloak/bin/jboss-cli.sh --connect command=:shutdown
