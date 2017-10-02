#! /bin/bash

set -e

export SCRIPT_DIR=$(dirname "$0")
export COMMIT="${TRAVIS_COMMIT::8}"

if [[ "$TRAVIS_PULL_REQUEST" == "false" ]] ; then
   # skip images build for pull requests
   echo "Pull request $TRAVIS_PULL_REQUEST identified, docker image build is skipped" ;
   exit 0
fi

export REPO="commercetoolsps/commercetools-paypal-plus-integration"
export DOCKER_TAG=`if [[ "$TRAVIS_BRANCH" == "master" ]] ; then echo "latest"; else echo "wip-${TRAVIS_BRANCH//\//-}" ; fi`

echo "Docker build script variables:"
echo "    COMMIT=$COMMIT"
echo "    TRAVIS_BUILD_NUMBER=$TRAVIS_BUILD_NUMBER"
echo "    TRAVIS_BRANCH=$TRAVIS_BRANCH"
echo "    TRAVIS_TAG=$TRAVIS_TAG"
echo "    REPO=$REPO"
echo "    DOCKER_TAG=$DOCKER_TAG"

echo "Building Docker image using tag '${REPO}:${COMMIT}'."
docker build --file "$SCRIPT_DIR/Dockerfile"  --tag "${REPO}:${COMMIT}" .

docker login -u="${DOCKER_USERNAME}" -p="${DOCKER_PASSWORD}"

if [ "$TRAVIS_TAG" ] ; then
  echo "Adding additional tag '${REPO}:${TRAVIS_TAG}' to already built Docker image '${REPO}:${COMMIT}'."
  docker tag $REPO:$COMMIT $REPO:${TRAVIS_TAG}
else
  echo "Adding additional tag '${REPO}:${DOCKER_TAG}' to already built Docker image '${REPO}:${COMMIT}'."
  docker tag $REPO:$COMMIT $REPO:$DOCKER_TAG
fi

echo "Pushing Docker images to repository '${REPO}' (all local tags are pushed)."
docker push $REPO

docker logout
