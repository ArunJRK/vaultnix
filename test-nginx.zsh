#!/bin/zsh
# File: test-mtls-cross.zsh

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Set the correct base path for the nginx configuration
NGINX_CONFIG_PATH="./nginx-config"

# Function to test a server
test_server() {
    local client=$1
    local server=$2
    local port=$3
    
    echo -e "\n${BLUE}======================================${NC}"
    echo -e "${BLUE}Testing: $client -> $server (Port $port)${NC}"
    echo -e "${BLUE}======================================${NC}"
    
    local client_cert="${NGINX_CONFIG_PATH}/tls/${client}.crt"
    local client_key="${NGINX_CONFIG_PATH}/tls/${client}.key"
    local ca_path="${NGINX_CONFIG_PATH}/tls/nginx_ca.crt"

    # Check if certificate files exist
    for file in "$client_cert" "$client_key" "$ca_path"; do
        if [[ ! -f $file ]]; then
            echo -e "${RED}Error: File not found - $file${NC}"
            return 1
        fi
    done

    # Test with client certificate
    echo -e "${YELLOW}Sending request...${NC}"
    curl_output=$(curl -s -o /dev/null -w "%{http_code}" --cacert $ca_path --cert $client_cert --key $client_key -k https://localhost:$port 2>/dev/null)
    
    # Check for successful connection
    if [[ $curl_output == "200" ]]; then
        echo -e "${GREEN}Connection successful! (HTTP 200 OK)${NC}"
    else
        case $curl_output in
            401)
                echo -e "${RED}Authenticated but unauthorized. (HTTP 401 Unauthorized)${NC}"
                ;;
            403)
                echo -e "${RED}Authenticated but forbidden. (HTTP 403 Forbidden)${NC}"
                ;;
            *)
                echo -e "${RED}Connection failed. (HTTP $curl_output)${NC}"
                ;;
        esac
    fi
}

# Function to parse JSON
parse_json() {
    local json=$1
    if ! command -v jq &> /dev/null; then
        echo -e "${RED}Error: jq is not installed. Please install jq to parse JSON.${NC}"
        exit 1
    fi
    echo $json | jq -r 'to_entries | map("\(.key):\(.value)") | .[]'
}

# Main script
echo -e "${YELLOW}Starting mTLS cross-connection tests...${NC}"
echo -e "Using certificates from ${BLUE}$NGINX_CONFIG_PATH/tls${NC} directory"
echo -e "Server configuration from ${BLUE}servers.json${NC}\n"

# Check if servers.json exists
if [[ ! -f servers.json ]]; then
    echo -e "${RED}Error: servers.json not found${NC}"
    exit 1
fi

# Read server configuration
servers_json=$(cat servers.json)
servers=$(parse_json "$servers_json")

# Check if servers were parsed correctly
if [[ -z "$servers" ]]; then
    echo -e "${RED}Error: Failed to parse servers from servers.json${NC}"
    exit 1
fi

echo -e "${YELLOW}Parsed servers:${NC}"
echo "$servers" | sed 's/^/  /'
echo ""

# Perform cross-testing
total_tests=0
successful_tests=0

while IFS= read -r client_entry; do
    client_name=${client_entry%%:*}
    client_port=${client_entry#*:}
    
    while IFS= read -r server_entry; do
        server_name=${server_entry%%:*}
        server_port=${server_entry#*:}
        
        if [[ $client_name != $server_name ]]; then
            ((total_tests++))
            if test_server $client_name $server_name $server_port; then
                ((successful_tests++))
            fi
        fi
    done <<< "$servers"
done <<< "$servers"

echo -e "\n${YELLOW}mTLS cross-connection tests complete!${NC}"
echo -e "${GREEN}Successful tests: $successful_tests / $total_tests${NC}"