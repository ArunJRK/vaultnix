#!/bin/bash
# File: nginx-config/get-cert.sh

# Function to log messages
log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1"
}

# Check if jq is installed
if ! command -v jq &> /dev/null; then
    log "Error: jq is not installed. Please install jq to run this script."
    exit 1
fi

# Vault address
VAULT_ADDR=${VAULT_ADDR:-"http://vault:8200"}

# AppRole credentials (these should be securely provided to the Nginx container)
ROLE_ID=${ROLE_ID:-""}
SECRET_ID=${SECRET_ID:-""}

# Certificate details
COMMON_NAME=${COMMON_NAME:-"nginx.example.com"}
TTL=${TTL:-"24h"}

# Authenticate with Vault using AppRole
log "Authenticating with Vault..."
VAULT_TOKEN=$(curl -s -X POST ${VAULT_ADDR}/v1/auth/approle/login \
    -d "{\"role_id\":\"${ROLE_ID}\",\"secret_id\":\"${SECRET_ID}\"}" | jq -r '.auth.client_token')

if [ -z "$VAULT_TOKEN" ] || [ "$VAULT_TOKEN" == "null" ]; then
    log "Error: Failed to authenticate with Vault"
    exit 1
fi

log "Successfully authenticated with Vault"

# Request a new certificate
log "Requesting new certificate for ${COMMON_NAME}..."
CERT_DATA=$(curl -s -X POST -H "X-Vault-Token: ${VAULT_TOKEN}" \
    ${VAULT_ADDR}/v1/pki/issue/nginx-server \
    -d "{\"common_name\":\"${COMMON_NAME}\",\"ttl\":\"${TTL}\"}")

# Extract certificate, private key, and CA certificate
CERT=$(echo $CERT_DATA | jq -r '.data.certificate')
PRIVATE_KEY=$(echo $CERT_DATA | jq -r '.data.private_key')
CA_CERT=$(echo $CERT_DATA | jq -r '.data.issuing_ca')

# Save the certificates and key
echo "$CERT" > /etc/nginx/ssl/nginx.crt
echo "$PRIVATE_KEY" > /etc/nginx/ssl/nginx.key
echo "$CA_CERT" > /etc/nginx/ssl/ca.crt

# Set correct permissions
chmod 644 /etc/nginx/ssl/nginx.crt /etc/nginx/ssl/ca.crt
chmod 600 /etc/nginx/ssl/nginx.key

log "Certificate retrieved and saved successfully"

# Reload Nginx to use the new certificate
nginx -s reload

log "Nginx reloaded with new certificate"