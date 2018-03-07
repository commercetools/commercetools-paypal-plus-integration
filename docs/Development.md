<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->
**Table of Contents**  *generated with [DocToc](https://github.com/thlorenz/doctoc)*

- [Local debug](#local-debug)
- [Tests](#tests)
  - [Integration tests](#integration-tests)
- [Docker build](#docker-build)
  - [Configure environment](#configure-environment)
  - [Run build/push from gradle task](#run-buildpush-from-gradle-task)

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

After successful project build/test - assemble and push docker image. 
This could be done using configured gradle tasks, described below.

For now we build/publish only following docker tags:
  
  - `[dev-version]-[git-commit-hash]` for WIP builds, when git tag is not specified. 
  The _dev-version_ prefix is defined in [`build.gradle`](/build.gradle) file.  
      
      Example: `0.3.0-DEV-3b0dfea`
      
  - `vX.Y.Z` - stable build tag, performed automatically when new git tag `vX.Y.Z` is pushed. 
    
      Example: `v0.3.0`
      
The version resolution workflow is defined in [`version-resolver.gradle`](/gradle/version-resolver.gradle) file.
The docker tagging is applied in [`docker-build.gradle`](/gradle/docker-build.gradle) file

Tags like `latest`, `master`, `WIP-*`, `[branch-name]` and similar are not published any more 
since they are confusing and often used wrong.

### Configure environment

Docker hub user/password for image push are required. Set either:
   - `docker.username` and `docker.password` in gradle properties (using one of `-D`, `-P` running arguments, 
   or set them in user/project `gradle.properties` file)
   - `DOCKER_USERNAME` and `DOCKER_PASSWORD` environment variables.
   
For TravisCI build these values could be found in decrypted 
[`travis-build-settings.sh.enc`](/travis-build/configuration/travis-build-settings.sh.enc)
file. See [`TravisBuildConfiguration`](/travis-build/configuration/TravisBuildConfiguration.md) for more details.
 
### Run build/push from gradle task

Build local docker image:

```bash 
./gradlew buildDockerImage
```

Build and push docker image to [Docker hub](https://hub.docker.com/r/commercetoolsps/commercetools-paypal-plus-integration/):

```bash 
./gradlew pushDockerImage 
```

See [docker-build.gradle](/gradle/docker-build.gradle) script for more details
