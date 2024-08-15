# File: vault-config/init-vault.sh

#!/bin/bash

# Wait for Vault to start
until vault status > /dev/null 2>&1; do
    echo "Waiting for Vault to start..."
    sleep 1
done

# Initialize Vault
vault operator init > /vault/config/init.txt

# Unseal Vault
for i in {1..3}; do
    key=$(grep "Unseal Key $i:" /vault/config/init.txt | cut -d' ' -f4)
    vault operator unseal $key
done

# Log in to Vault
root_token=$(grep "Initial Root Token:" /vault/config/init.txt | cut -d' ' -f4)
vault login $root_token

# Enable the PKI secrets engine
vault secrets enable pki

# Set the max lease TTL to 87600 hours (10 years)
vault secrets tune -max-lease-ttl=87600h pki

# Generate the root certificate
vault write pki/root/generate/internal \
    common_name=example.com \
    ttl=87600h

# Configure the CA and CRL URLs
vault write pki/config/urls \
    issuing_certificates="http://vault:8200/v1/pki/ca" \
    crl_distribution_points="http://vault:8200/v1/pki/crl"

# Create a role for issuing certificates
vault write pki/roles/nginx-server \
    allowed_domains=example.com \
    allow_subdomains=true \
    max_ttl=72h

# Create a policy for Nginx servers to request certificates
cat << EOF > /vault/config/nginx-policy.hcl
path "pki/issue/nginx-server" {
  capabilities = ["create", "update"]
}
EOF

vault policy write nginx-server /vault/config/nginx-policy.hcl

# Enable AppRole auth method
vault auth enable approle

# Create an AppRole for Nginx servers
vault write auth/approle/role/nginx-role \
    secret_id_ttl=10m \
    token_num_uses=10 \
    token_ttl=20m \
    token_max_ttl=30m \
    secret_id_num_uses=40 \
    policies=nginx-server

echo "Vault initialization and configuration complete!"