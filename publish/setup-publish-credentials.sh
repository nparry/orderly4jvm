#!/bin/bash

function do_not_publish {
  echo "Artifact publishing disabled: $1" >&2
  exit 1
}

[ -z "$BINTRAY_USER" ] && do_not_publish "BINTRAY_USER is not set"
[ -z "$BINTRAY_KEY" ] && do_not_publish "BINTRAY_KEY is not set"
[ -z "$BINTRAY_PASSPHRASE" ] && do_not_publish "BINTRAY_PASSPHRASE is not set"

mkdir ~/.bintray

cat << CREDENTIAL_CONFIG > ~/.bintray/.credentials
realm = Bintray API Realm
host = api.bintray.com
user = $BINTRAY_USER
password = $BINTRAY_KEY
CREDENTIAL_CONFIG

openssl des3 -d -salt -in ./publish/bintray.acs.encoded -out ~/.bintray/bintray.asc -k "$BINTRAY_PASSPHRASE"

