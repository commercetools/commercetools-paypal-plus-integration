<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->
**Table of Contents**  *generated with [DocToc](https://github.com/thlorenz/doctoc)*

- [Local debug](#local-debug)
- [Tests](#tests)
  - [Integration tests](#integration-tests)
- [Docker build](#docker-build)
  - [Configure environment](#configure-environment)
  - [Run build/push](#run-buildpush)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

## Local debug

```bash
./gradlew bootRun
```

Local _Run/Debug_ setting may be configured in [`/src/main/resources/config/application.yml`](/src/main/resources/config/application.yml)
file. If the file exists - it overrides default [`/src/main/resources/application.yml`](/src/main/resources/application.yml).


## Tests

### Integration tests

Local integration test setting may be configured in [`/src/test/resources/config/application.yml`](/src/test/resources/config/application.yml)
file. If the file exists - it overrides main context config and config from  [`/src/test/resources/application.yml`](/src/test/resources/application.yml).

**Note: keep this settings different from `/src/main/resources/config/application.yml`,
which is used for local run/debug, because the integration tests will remove all data form the CTP project**

## Docker build

<!-- update when https://github.com/commercetools/commercetools-paypal-plus-integration/pull/124 
is finished -->

### Configure environment

Docker hub user/password for image push are required. Set either:
   - `docker.username` and `docker.password` in gradle properties (using one of `-D`, `-P` running arguments, 
   or set them in user/project `gradle.properties` file)
   - `DOCKER_USERNAME` and `DOCKER_PASSWORD` environment variables.
   
For TravisCI build these values could be found in decrypted 
[`travis-build-settings.sh.enc`](/travis-build/configuration/travis-build-settings.sh.enc)
file. See [`TravisBuildConfiguration`](/travis-build/configuration/TravisBuildConfiguration.md) for more details.
 
### Run build/push

Build local docker image:
```bash 
./gradlew buildDockerImage
```

Build and push docker image to [Docker hub](https://hub.docker.com/r/commercetoolsps/commercetools-paypal-plus-integration/):
```bash 
./gradlew pushDockerImage 
```

See [docker-build.gradle](/gradle/docker-build.gradle) script for more details
