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

_find_thermostat_gateway_home() {
  # Compute THERMOSTAT_GATEWAY_HOME by finding the (symlink-resolved) location of the
  # currently executing code's parent dir. See
  # http://stackoverflow.com/a/246128/3561275 for implementation details.
  SOURCE="${BASH_SOURCE[0]}"
  while [ -h "$SOURCE" ]; do
    DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"
    SOURCE="$(readlink "$SOURCE")"
    [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE"
  done
  DIR="$(cd -P "$(dirname "$SOURCE")" && cd .. && pwd)"
  echo "$DIR"
}

if [ "$(uname -s | cut -b1-6)" == "CYGWIN" ]; then
  ##echo "Running under Cygwin"
  export CYGWIN_MODE=1
else
  ##echo "Running under Linux"
  export CYGWIN_MODE=0
fi

if [[ "${THERMOSTAT_GATEWAY_HOME}" = "" ]]; then
  THERMOSTAT_GATEWAY_HOME="$(_find_thermostat_gateway_home)"
fi

# on cygwin, convert to Windows format
if [ $CYGWIN_MODE -eq 1 ]; then
  THERMOSTAT_GATEWAY_HOME="`cygpath -w $THERMOSTAT_GATEWAY_HOME`"
fi

LOGGING_ARGS=()
LOG_CONFIG_FILE="${THERMOSTAT_GATEWAY_HOME}/etc/logging.properties"
if [ -f "${LOG_CONFIG_FILE}" ] ; then
  if [ $CYGWIN_MODE -eq 1 ]; then
    LOGGING_ARGS=( "-Djava.util.logging.config.file=`cygpath -w ${LOG_CONFIG_FILE}`" )
  else
    LOGGING_ARGS=( "-Djava.util.logging.config.file=${LOG_CONFIG_FILE}" )
  fi
fi

THERMOSTAT_GATEWAY_LIBS=${THERMOSTAT_GATEWAY_HOME}/libs

export THERMOSTAT_GATEWAY_HOME
exec java ${THERMOSTAT_GATEWAY_EXT_JAVA_OPTS} -cp "${THERMOSTAT_GATEWAY_LIBS}/*" \
        "${LOGGING_ARGS[@]}" com.redhat.thermostat.gateway.server.Start
