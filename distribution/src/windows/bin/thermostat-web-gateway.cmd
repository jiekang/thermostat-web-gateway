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

if not defined THERMOSTAT_GATEWAY_HOME (
  set THERMOSTAT_GATEWAY_HOME=%~dp0..
)

set THERMOSTAT_GATEWAY_LIBS=%THERMOSTAT_GATEWAY_HOME%\libs

set THERMOSTAT_GATEWAY_ETC=%THERMOSTAT_GATEWAY_HOME%\etc

set THERMOSTAT_GATEWAY_CONFIG=%THERMOSTAT_GATEWAY_ETC%\services.properties

set THERMOSTAT_GATEWAY_SERVICES=%THERMOSTAT_GATEWAY_HOME%\services

powershell -command "(get-content %THERMOSTAT_GATEWAY_CONFIG%).replace('__SERVICES__','%THERMOSTAT_GATEWAY_SERVICES%').replace('\','/').replace('//','/') | set-content %THERMOSTAT_GATEWAY_CONFIG%"

java -cp "%THERMOSTAT_GATEWAY_LIBS%/*" com.redhat.thermostat.gateway.server.Start
