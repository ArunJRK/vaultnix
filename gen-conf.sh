#!/bin/bash

# Default values
SERVER_NAME="example.com"
PORT=443
SSL_CERT_PATH="/etc/nginx/ssl/server.crt"
SSL_KEY_PATH="/etc/nginx/ssl/server.key"
CA_CERT_PATH="/etc/nginx/ssl/trusted_clients.crt"
OUTPUT_FILE="nginx.conf"
ALLOWED_CNS=()

# Function to print usage
print_usage() {
    echo "Usage: $0 [OPTIONS]"
    echo "Generate Nginx configuration for mTLS authentication"
    echo
    echo "Options:"
    echo "  -n, --server-name NAME     Set server name (default: $SERVER_NAME)"
    echo "  -p, --port PORT            Set listen port (default: $PORT)"
    echo "  -c, --ssl-cert PATH        Set SSL certificate path (default: $SSL_CERT_PATH)"
    echo "  -k, --ssl-key PATH         Set SSL key path (default: $SSL_KEY_PATH)"
    echo "  -a, --ca-cert PATH         Set CA certificate path (default: $CA_CERT_PATH)"
    echo "  -o, --output FILE          Set output file name (default: $OUTPUT_FILE)"
    echo "  -l, --allowed-cn CN        Add an allowed Common Name (can be used multiple times)"
    echo "  -h, --help                 Print this help message"
}

# Parse command-line arguments
while [[ $# -gt 0 ]]; do
    key="$1"
    case $key in
        -n|--server-name)
            SERVER_NAME="$2"
            shift 2
            ;;
        -p|--port)
            PORT="$2"
            shift 2
            ;;
        -c|--ssl-cert)
            SSL_CERT_PATH="$2"
            shift 2
            ;;
        -k|--ssl-key)
            SSL_KEY_PATH="$2"
            shift 2
            ;;
        -a|--ca-cert)
            CA_CERT_PATH="$2"
            shift 2
            ;;
        -o|--output)
            OUTPUT_FILE="$2"
            shift 2
            ;;
        -l|--allowed-cn)
            ALLOWED_CNS+=("$2")
            shift 2
            ;;
        -h|--help)
            print_usage
            exit 0
            ;;
        *)
            echo "Unknown option: $1"
            print_usage
            exit 1
            ;;
    esac
done

# Function to print configuration summary
print_summary() {
    echo "Configuration Summary:"
    echo "  Server Name: $SERVER_NAME"
    echo "  Port: $PORT"
    echo "  SSL Certificate: $SSL_CERT_PATH"
    echo "  SSL Key: $SSL_KEY_PATH"
    echo "  CA Certificate: $CA_CERT_PATH"
    echo "  Output File: $OUTPUT_FILE"
    echo "  Allowed CNs: ${ALLOWED_CNS[*]}"
    echo
}

# Function to get user confirmation
get_confirmation() {
    while true; do
        read -p "Do you want to proceed with this configuration? (y/n): " yn
        case $yn in
            [Yy]* ) return 0;;
            [Nn]* ) return 1;;
            * ) echo "Please answer yes or no.";;
        esac
    done
}

# Function to generate the map of allowed CNs
generate_allowed_cns_map() {
    local map="map \$ssl_client_s_dn \$is_allowed_client {\n"
    map+="    default 0;\n"
    for cn in "${ALLOWED_CNS[@]}"; do
        # Use regex matching for more flexible DN matching
        map+="    ~CN=$cn(,|$) 1;\n"
    done
    map+="}"
    echo -e "$map"
}

# Nginx configuration template
read -r -d '' NGINX_TEMPLATE << EOL
user nginx;
worker_processes auto;
error_log /var/log/nginx/error.log warn;
pid /var/run/nginx.pid;

events {
    worker_connections 1024;
}

http {
    include /etc/nginx/mime.types;
    default_type application/octet-stream;

    log_format mTLS '\$remote_addr - \$remote_user [\$time_local] "\$request" '
                '\$status \$body_bytes_sent "\$http_referer" '
                '"\$http_user_agent" "\$http_x_forwarded_for" '
                '\$ssl_client_s_dn "\$ssl_client_verify"';
    access_log /var/log/nginx/mtls_access.log mTLS;

    sendfile on;
    keepalive_timeout 65;

    # Allowed clients map
    {{ALLOWED_CNS_MAP}}

    server {
        listen {{PORT}} ssl;
        server_name {{SERVER_NAME}};

        ssl_certificate {{SSL_CERT_PATH}};
        ssl_certificate_key {{SSL_KEY_PATH}};
        ssl_client_certificate {{CA_CERT_PATH}};
        ssl_verify_client on;

        ssl_protocols TLSv1.2 TLSv1.3;
        ssl_prefer_server_ciphers on;
        ssl_ciphers ECDHE-RSA-AES256-GCM-SHA384:ECDHE-RSA-AES128-GCM-SHA256:DHE-RSA-AES256-GCM-SHA384:DHE-RSA-AES128-GCM-SHA256;

        ssl_stapling on;
        ssl_stapling_verify on;

        location / {
            if (\$ssl_client_verify != SUCCESS) {
                add_header X-Error-Message "Client certificate verification failed";
                return 401;
            }
            if (\$is_allowed_client = 0) {
                add_header X-Error-Message "Client not allowed";
                return 403;
            }

            root /usr/share/nginx/html;
            index index.html index.htm;
            
            add_header X-Client-DN \$ssl_client_s_dn;
        }

        location /healthz {
            access_log off;
            add_header Content-Type text/plain;
            return 200 'OK';
        }
    }
}
EOL

# Print configuration summary
print_summary

# Get user confirmation
if get_confirmation; then
    # Generate the final configuration
    ALLOWED_CNS_MAP=$(generate_allowed_cns_map)
    FINAL_CONFIG="${NGINX_TEMPLATE/\{\{ALLOWED_CNS_MAP\}\}/$ALLOWED_CNS_MAP}"
    FINAL_CONFIG="${FINAL_CONFIG/\{\{PORT\}\}/$PORT}"
    FINAL_CONFIG="${FINAL_CONFIG/\{\{SERVER_NAME\}\}/$SERVER_NAME}"
    FINAL_CONFIG="${FINAL_CONFIG/\{\{SSL_CERT_PATH\}\}/$SSL_CERT_PATH}"
    FINAL_CONFIG="${FINAL_CONFIG/\{\{SSL_KEY_PATH\}\}/$SSL_KEY_PATH}"
    FINAL_CONFIG="${FINAL_CONFIG/\{\{CA_CERT_PATH\}\}/$CA_CERT_PATH}"
    # Write the configuration to the output file
    echo "$FINAL_CONFIG" > "$OUTPUT_FILE"

    echo "Nginx configuration has been generated in $OUTPUT_FILE"
else
    echo "Configuration generation cancelled."
    exit 1
fi