# JCava mTLS Setup

This directory contains the following:

- `ca/`: Certificate Authority files
  - `jcava-ca.key`: CA private key
  - `jcava-ca.crt`: CA certificate
  - `jcava-ca.cnf`: CA configuration

To create a certificate for a new service:

1. Generate a private key:
   openssl genpkey -algorithm RSA -out "./keys/service-name.key" -pkeyopt rsa_keygen_bits:2048

2. Create a certificate signing request (CSR):
   openssl req -new -key "./keys/service-name.key" -out "./keys/service-name.csr"

3. Sign the CSR with the CA:
   openssl x509 -req -in "./keys/service-name.csr" \
       -CA "./keys/ca/jcava-ca.crt" -CAkey "./keys/ca/jcava-ca.key" \
       -CAcreateserial -out "./keys/service-name.crt" -days 365 -sha256

4. Clean up the CSR (optional):
   rm "./keys/service-name.csr"

Rock on with your mTLS setup!
