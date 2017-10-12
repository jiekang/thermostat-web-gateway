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

if [ $CYGWIN_MODE -eq 1 ]; then
  MONGO_FORK_ARG=
else
  MONGO_FORK_ARG=--fork
fi

if [[ "${THERMOSTAT_GATEWAY_HOME}" = "" ]]; then
  THERMOSTAT_GATEWAY_HOME="$(_find_thermostat_gateway_home)"
fi

IP=127.0.0.1
PORT=27519

DB_PATH=${THERMOSTAT_GATEWAY_HOME}/db
DATA_PATH=${DB_PATH}/data
JS_PATH=${THERMOSTAT_GATEWAY_HOME}/etc/mongo-dev-setup.js

LOG_PATH=${DB_PATH}/db.log
PID_PATH=${DB_PATH}/db.pid

SETUP_PATH=${DB_PATH}/setup.stamp

# on cygwin, convert to Windows format
if [ $CYGWIN_MODE -eq 1 ]; then
  LOG_PATH="`cygpath -w $LOG_PATH`"
  PID_PATH="`cygpath -w $PID_PATH`"
  DATA_PATH="`cygpath -w $DATA_PATH`"
fi

if [ -z "$1" ]; then
    echo "Usage: thermostat-mongodb.sh [start] [stop]"
    exit 1
fi

if [ $1 == "start" ]; then
    mkdir -p ${DB_PATH}
    mkdir -p ${DATA_PATH}

    if [ $CYGWIN_MODE -eq 1 ]; then
        mongod --quiet --nohttpinterface --bind_ip ${IP} --nojournal --dbpath ${DATA_PATH} --logpath ${LOG_PATH} --pidfilepath ${PID_PATH} --port ${PORT} --auth&
        if [ ! -f ${SETUP_PATH} ]; then
            sleep 3
            touch ${SETUP_PATH}
            mongo ${IP}:${PORT} `cygpath -w ${JS_PATH}`
        fi
    else
        mongod --quiet ${MONGO_FORK_ARG} --nohttpinterface --bind_ip ${IP} --nojournal --dbpath ${DATA_PATH} --logpath ${LOG_PATH} --pidfilepath ${PID_PATH} --port ${PORT} --auth
        if [ ! -f ${SETUP_PATH} ]; then
            sleep 3
            touch ${SETUP_PATH}
            mongo ${IP}:${PORT} ${JS_PATH}
        fi
    fi
fi

if [ $1 == "stop" ]; then
    if [ -f ${PID_PATH} ]; then
        mongo ${IP}:${PORT}/admin --eval "db.auth('thermostat-admin','mongodevpassword') && db.shutdownServer()"
    fi
fi
