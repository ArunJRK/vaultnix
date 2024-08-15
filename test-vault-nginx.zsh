#!/bin/zsh
# File: test-mtls.zsh

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to test a server
test_server() {
    local server=$1
    local port=$2
    echo "${YELLOW}Testing $server on port $port...${NC}"
    
    # Test with client certificate
    result=$(curl --cacert ca.crt --cert client.crt --key client.key -k https://localhost:$port 2>/dev/null)
    if [[ $result == *"Hello from $server"* ]]; then
        echo "${GREEN}✓ Successfully connected to $server with client certificate${NC}"
    else
        echo "${RED}✗ Failed to connect to $server with client certificate${NC}"
    fi
    
    # Test without client certificate
    result=$(curl -k https://localhost:$port 2>/dev/null)
    if [[ $result == *"Hello from $server"* ]]; then
        echo "${RED}✗ Connected to $server without client certificate (should fail)${NC}"
    else
        echo "${GREEN}✓ Correctly rejected connection to $server without client certificate${NC}"
    fi
    
    echo ""
}

# Main script
echo "${YELLOW}Starting mTLS connection tests...${NC}"
echo "Make sure you have the client certificate (client.crt), key (client.key), and CA certificate (ca.crt) in the current directory."
echo ""

# Test each server
test_server "nginx1" "8081"
test_server "nginx2" "8082"
test_server "nginx3" "8083"

echo "${YELLOW}Testing Vault...${NC}"
vault_result=$(curl -k https://localhost:8200/v1/sys/health 2>/dev/null)
if [[ $vault_result == *"initialized"* ]]; then
    echo "${GREEN}✓ Successfully connected to Vault${NC}"
else
    echo "${RED}✗ Failed to connect to Vault${NC}"
fi

echo ""
echo "${YELLOW}mTLS connection tests complete!${NC}"