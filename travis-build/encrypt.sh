#!/bin/sh

# Run it without any arguments to

PROJECT=professionalserviceslabs
KEYRING=ps-keyring
KEY=commercetools-paypal-plus-integration

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
CONFIG_DIR="${SCRIPT_DIR}/configuration"
PLAIN_FILE="${CONFIG_DIR}/travis-build-settings.sh"
CIPHER_FILE="${CONFIG_DIR}/travis-build-settings.sh.enc"

gcloud kms encrypt \
  --project="$PROJECT"\
  --location="global" \
  --keyring="$KEYRING" \
  --key="$KEY" \
  --plaintext-file="$PLAIN_FILE" \
  --ciphertext-file="$CIPHER_FILE" \
  && printf "\nEncrypted successfully:\n\n" \
  && ls -l "$CIPHER_FILE"

