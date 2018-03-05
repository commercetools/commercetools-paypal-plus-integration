#!/bin/sh

# Decrypt travis-build-settings.sh.enc file script.
# Run the script without any arguments and commit "$CIPHER_FILE" after encryption

COMMON_SCRIPT="$(dirname "$0")/crypt-common.sh"

if [ ! -r "$COMMON_SCRIPT" ] ; then
    echo "Error: script [${COMMON_SCRIPT}] not found!"
    exit
fi

. "$COMMON_SCRIPT"

encryptDecrypt "encrypt" \
  && printf "\nEncrypted successfully:\n\n" \
  && ls -l "$CIPHER_FILE" \
  && printf "\nDon't forget to add/commit the encrypted file\n"
