# Travis Build Configuration

To execute Travis CI build we require for now:
  - `SPRING_APPLICATION_JSON` environment variable + (for integration tests)
  - `DOCKER_USERNAME` and `DOCKER_PASSWORD` for built container deployment to 
  [Docker Hub](https://hub.docker.com/r/commercetoolsps/commercetools-paypal-plus-integration/)

To protect the secret values [Google Cloud KMS](https://cloud.google.com/kms/) is used. 
Pls, contact Professional Services team to get an access to the encrypted data.

## How to encrypt/decrypt files

1. [Install/update `gcloud` client (SDK)](https://cloud.google.com/sdk/gcloud/)

1. Run the following commands to:
    - Encrypt `travis-build-settings.sh` file (must be ignored):

        ```bash
        gcloud kms encrypt \
            --project=professionalserviceslabs \
            --location=global  \
            --keyring=ps-keyring \
            --key=commercetools-paypal-plus-integration \
            --plaintext-file=travis-build-settings.sh \
            --ciphertext-file=travis-build-settings.sh.enc
        ```
    
        Add to  git updated encrypted value.

    - Decrypt `travis-build-settings.sh.enc`:
    
      ```bash
      gcloud kms decrypt \
        --project=professionalserviceslabs \
        --location=global  \
        --keyring=ps-keyring \
        --key=commercetools-paypal-plus-integration \
        --plaintext-file=travis-build-settings.sh \
        --ciphertext-file=travis-build-settings.sh.enc

1. For convenience [`encrypt.sh`](encrypt.sh) and [`decrypt.sh`](decrypt.sh) script could be used to 
encrypt/decrypt `travis-build-settings.sh` file

### Known issues

Because encryption uses salt every time, 
it's not possible to recognize from indexed encrypted file `travis-build-settings.sh.enc` 
whether actual content of `travis-build-settings.sh` changed or not.
So, it is **strongly recommended** to make these kind of changes atomic (e.g. commit only encrypted file)
with explicit message what exactly changed (of course, without any sensitive data in the commit message, 
just what changed), like:
    
    update PayPal sandox API token for integration tests