#!/bin/sh

# Decrypt travis-build-settings.sh.enc file script

PROJECT=professionalserviceslabs
KEYRING=ps-keyring
KEY=commercetools-paypal-plus-integration

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
CONFIG_DIR="${SCRIPT_DIR}/configuration"
PLAIN_FILE="${CONFIG_DIR}/travis-build-settings.sh"
CIPHER_FILE="${CONFIG_DIR}/travis-build-settings.sh.enc"

gcloud kms decrypt \
  --project="$PROJECT"\
  --location="global" \
  --keyring="$KEYRING" \
  --key="$KEY" \
  --plaintext-file="$PLAIN_FILE" \
  --ciphertext-file="$CIPHER_FILE" \
  && chmod u+x "$PLAIN_FILE" \
  && printf "\nDecrypted successfully:\n\n" \
  && ls -l "$PLAIN_FILE"


