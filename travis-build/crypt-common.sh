#!/bin/sh

# Common script to encrypt/decrypt travis-build-settings.sh

PROJECT=professionalserviceslabs
KEYRING=ps-keyring
KEY=commercetools-paypal-plus-integration

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
CONFIG_DIR="${SCRIPT_DIR}/configuration"
PLAIN_FILE="${CONFIG_DIR}/travis-build-settings.sh"
CIPHER_FILE="${CONFIG_DIR}/travis-build-settings.sh.enc"

function encryptDecrypt() {
    gcloud kms "$1" \
      --project="$PROJECT"\
      --location="global" \
      --keyring="$KEYRING" \
      --key="$KEY" \
      --plaintext-file="$PLAIN_FILE" \
      --ciphertext-file="$CIPHER_FILE"
}
