#! /bin/bash

# this script must be called after successful gradle build/test commands

set -e

# build/push images only for non pull request
if [[ "$TRAVIS_PULL_REQUEST" == "false" ]] ; then
  ./gradlew pushDockerImage
else
  echo "Pull request (\$TRAVIS_PULL_REQUEST != \"false\") identified, docker image build is skipped"
  exit 0
fi

