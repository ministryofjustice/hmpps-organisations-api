#!/bin/bash
#
# NAME:  call-api-as-system-client.sh
# Script to test APIs in the DEV environment or locally which grabs the system token from kubectl, gets an auth token and then
# invokes your URL.
# 
# Parameters:
# 1. URL - the absolute URL you want to call, you may need to quote the URL
#
# Example:
# 
# $ ./call-api-as-system-client.sh <URL>
#

DIR=$(dirname "$0")
TOKEN=$(source "$DIR/get-auth-token.sh")
curl -s -X GET --location "$1" -H "Authorization: Bearer $TOKEN" -H "Accept: application/json" | jq .

# End
