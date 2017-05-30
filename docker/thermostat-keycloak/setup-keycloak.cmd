@echo off
rem
rem Copyright 2012-2017 Red Hat, Inc.
rem
rem This file is part of Thermostat.
rem
rem Thermostat is free software; you can redistribute it and/or modify
rem it under the terms of the GNU General Public License as published
rem by the Free Software Foundation; either version 2, or (at your
rem option) any later version.
rem
rem Thermostat is distributed in the hope that it will be useful, but
rem WITHOUT ANY WARRANTY; without even the implied warranty of
rem MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
rem General Public License for more details.
rem
rem You should have received a copy of the GNU General Public License
rem along with Thermostat; see the file COPYING.  If not see
rem <http://www.gnu.org/licenses/>.
rem
rem Linking this code with other modules is making a combined work
rem based on this code.  Thus, the terms and conditions of the GNU
rem General Public License cover the whole combination.
rem
rem As a special exception, the copyright holders of this code give
rem you permission to link this code with independent modules to
rem produce an executable, regardless of the license terms of these
rem independent modules, and to copy and distribute the resulting
rem executable under terms of your choice, provided that you also
rem meet, for each linked independent module, the terms and conditions
rem of the license of that module.  An independent module is a module
rem which is not derived from or based on this code.  If you modify
rem this code, you may extend this exception to your version of the
rem library, but you are not obligated to do so.  If you do not wish
rem to do so, delete this exception statement from your version.
rem

rem Be polite, tell the user everything that's missing before exitting.
if not defined KEYCLOAK_HOME (
	echo KEYCLOAK_HOME is not set
)
if not defined JAVA_HOME (
	echo JAVA_HOME is not set
)

rem now, exit if something is not set
if not defined KEYCLOAK_HOME (
	exit /b 1
)
if not defined JAVA_HOME (
	exit /b 1
)

set KEYCLOAK_ADMIN=tms-admin
set KEYCLOAK_PASSWD=tms-admin
set THERMOSTAT_USER=tms-user
set THERMOSTAT_PASSWORD=tms-pass
set REALM=thermostat
set SERVER=http://localhost:8080/auth
set CLI=%KEYCLOAK_HOME%\bin\kcadm.bat

call %KEYCLOAK_HOME%\bin\add-user.bat  --user %KEYCLOAK_ADMIN% --password %KEYCLOAK_PASSWD%
call %KEYCLOAK_HOME%\bin\add-user-keycloak.bat  --user %KEYCLOAK_ADMIN% --password %KEYCLOAK_PASSWD%

start %KEYCLOAK_HOME%\bin\standalone.bat
rem Wait for keycloak to startup
rem (use ping because there's no 'sleep' command)
ping 127.0.0.1 -n 30 >nul

call %CLI% config credentials --server %SERVER% --realm master --user %KEYCLOAK_ADMIN% --password %KEYCLOAK_PASSWD%
call %CLI% create realms -s realm=%REALM% -s enabled=true
call %CLI% create roles -r %REALM% -s name=thermostat
call %CLI% create clients -r %REALM% -s clientId=thermostat-bearer -s enabled=true -s bearerOnly=true
call %CLI% create clients -r %REALM% -s clientId=thermostat-web-client -s enabled=true -s publicClient=true -s 'redirectUris=["http://localhost:8080/*"]' -s 'webOrigins=["+"]' -s directAccessGrantsEnabled=true
call %CLI% create users -r %REALM% -s enabled=true -s username=%THERMOSTAT_USER%
call %CLI% add-roles -r %REALM% --uusername %THERMOSTAT_USER% --rolename thermostat
call %CLI% set-password -r %REALM% --username %THERMOSTAT_USER% --new-password %THERMOSTAT_PASSWORD%

%KEYCLOAK_HOME%\bin\jboss-cli.bat --connect command=:shutdown
