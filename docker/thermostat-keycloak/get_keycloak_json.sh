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

#
# Utility script for getting the Keycloak JSON
# config from the Thermostat dev container programmatically.
#
#set -x
set -e

URL="http://127.0.0.1:8900/auth"
REALM="thermostat"
OPENID_CONNECT_JSON_PROVIDER="keycloak-oidc-keycloak-json"
CLIENT_NAME="thermostat-bearer"
TIMEOUT_KEYCLOAK=60

find_gw_home() {
  this_script="${BASH_SOURCE[0]}"
  parent=$(dirname ${this_script})
  echo "${parent}/../../distribution/target/image"
}

config_path() {
  gw_home=$(find_gw_home)
  echo "${gw_home}/etc/keycloak.json"
}

try_connect() {
  curl -o /dev/null -w %{http_code} -s -d "client_id=admin-cli" \
         -d "username=tms-admin"  \
         -d "password=tms-admin"  \
         -d "grant_type=password"  \
          "${URL}/realms/master/protocol/openid-connect/token"
}

wait_for_ready() {
  echo -n "Waiting for Keycloak container to become ready ..."
  attempts=0
  while [ "$(try_connect)_" != "200_" ]; do
    echo -n "."
    sleep 1
    if [ ${attempts} -eq ${TIMEOUT_KEYCLOAK} ]; then
      echo "failed."
      echo "Keycloak container not becoming available."
      exit 1
    fi
    attempts=$(( $attempts + 1 ))
  done
  echo "done."
}

# Get the access token json (including the bearer access token)
access_token_json() {
  curl -s -d "client_id=admin-cli" \
         -d "username=tms-admin"  \
         -d "password=tms-admin"  \
         -d "grant_type=password"  \
          "${URL}/realms/master/protocol/openid-connect/token"
}

# Retrieve the access_token property from the access token JSON
access_token() {
  echo $(access_token_json | sed 's|.*"access_token":"\([^"]\+\)".*|\1|g')
}

# Get the client ID for a given client name
#
# Note: needs to remove "protocolMappers" array, since "id" is not
#       unique otherwise.
client_id() {
  local clientId="$1"
  curl -s \
    -H "Authorization: bearer $t" \
    "${URL}/admin/realms/${REALM}/clients?clientId=${clientId}" | \
    sed 's|\(.*\)"protocolMappers":\[[^]]\+\]\(.*\)|\1\2|g' | \
    sed 's|.*"id":"\([^"]\+\)".*|\1|g'
}

# Get the Keycloak JSON config for a given client
json_config() {
  local id="$1"
  curl -o ${JSON_FILE} -s \
    -H "Authorization: bearer $t" \
    "${URL}/admin/realms/${REALM}/clients/${id}/installation/providers/${OPENID_CONNECT_JSON_PROVIDER}"
}
 
wait_for_ready

t=$(access_token)
cid=$(client_id ${CLIENT_NAME})

JSON_FILE=$(config_path)
json_config ${cid}
echo "${JSON_FILE} generated and installed."
