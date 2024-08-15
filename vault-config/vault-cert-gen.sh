#!/bin/bash

# Create a directory for the certificates if it doesn't exist
mkdir -p vault-config/tls

# Generate a private key
openssl genrsa -out vault-config/tls/vault.key 2048

# Generate a self-signed certificate
openssl req -x509 -new -nodes -key vault-config/tls/vault.key -sha256 -days 1024 -out vault-config/tls/vault.crt -subj "/CN=vault.example.com"

echo "Vault certificate and key generated successfully!"