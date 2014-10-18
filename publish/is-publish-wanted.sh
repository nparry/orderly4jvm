#!/bin/bash

function do_not_publish {
  echo "Artifact publishing disabled: $1" >&2
  exit 1
}

[ "$TRAVIS_PULL_REQUEST" == "false" ] || do_not_publish "Pull request"
[ -z "$TRAVIS_TAG" ] && do_not_publish "Not a tagged release"
if sbt ++$TRAVIS_SCALA_VERSION isSnapshot | tail -1 | grep -q true; then do_not_publish "Not a release version"; fi

