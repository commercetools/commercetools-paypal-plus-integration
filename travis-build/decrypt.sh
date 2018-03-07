#!/bin/sh

# Decrypt travis-build-settings.sh.enc file script.
# Run the script without any arguments

COMMON_SCRIPT="$(dirname "$0")/crypt-common.sh"

if [ ! -r "$COMMON_SCRIPT" ] ; then
    echo "Error: script [${COMMON_SCRIPT}] not found!"
    exit
fi

. "$COMMON_SCRIPT"

encryptDecrypt "decrypt" \
  && chmod u+x "$PLAIN_FILE" \
  && printf "\nDecrypted successfully:\n\n" \
  && ls -l "$PLAIN_FILE"
