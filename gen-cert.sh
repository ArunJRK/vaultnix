# NGINX CERT GEN
generate_nginx_cert() {
    local nginx_name=$1
    local ca_name=$2
    local days=${3:-365}  # Default to 1 year

    echo "Generating certificates for Nginx server: $nginx_name"

    # Generate Nginx private key
    openssl genpkey -algorithm RSA -out "${nginx_name}.key" -pkeyopt rsa_keygen_bits:2048

    # Create Nginx config file
    cat > "${nginx_name}.cnf" <<EOF
[req]
distinguished_name = req_distinguished_name
req_extensions = v3_req
x509_extensions = v3_req
prompt = no

[req_distinguished_name]
C = US
ST = California
L = San Francisco
O = My Organization
OU = My Organizational Unit
CN = ${nginx_name}

[v3_req]
basicConstraints = CA:FALSE
keyUsage = critical, digitalSignature, keyEncipherment
extendedKeyUsage = serverAuth, clientAuth
subjectAltName = @alt_names

[alt_names]
DNS.1 = ${nginx_name}
DNS.2 = ${nginx_name}.local
DNS.3 = localhost
IP.1 = 127.0.0.1
IP.2 = ::1
EOF

    # Generate CSR
    openssl req -new -key "${nginx_name}.key" -out "${nginx_name}.csr" -config "${nginx_name}.cnf"

    # Generate certificate
    openssl x509 -req \
        -in "${nginx_name}.csr" \
        -CA "${ca_name}.crt" \
        -CAkey "${ca_name}.key" \
        -CAcreateserial \
        -out "${nginx_name}.crt" \
        -extfile "${nginx_name}.cnf" \
        -extensions v3_req \
        -days "$days"

    # Verify certificate
    openssl verify -CAfile "${ca_name}.crt" "${nginx_name}.crt"

    # Create PEM file (certificate + key)
    cat "${nginx_name}.crt" "${nginx_name}.key" > "${nginx_name}.pem"

    echo "Nginx certificate generation completed."
    echo "Files created: ${nginx_name}.key, ${nginx_name}.csr, ${nginx_name}.crt, ${nginx_name}.pem"
}

# Make CA
generate_ca() {
    local ca_name=$1
    local days=${2:-1826}  # Default to 5 years

    echo "Generating CA certificate and key: $ca_name"
    
    # Generate CA private key
    openssl genpkey -algorithm RSA -out "${ca_name}.key" -pkeyopt rsa_keygen_bits:4096

    # Create CA config file
    cat > "${ca_name}.cnf" <<EOF
[req]
distinguished_name = req_distinguished_name
x509_extensions = v3_ca
prompt = no

[req_distinguished_name]
C = US
ST = State
L = City
O = Organization
OU = CA
CN = ${ca_name}

[v3_ca]
subjectKeyIdentifier = hash
authorityKeyIdentifier = keyid:always,issuer
basicConstraints = critical, CA:true
keyUsage = critical, digitalSignature, cRLSign, keyCertSign
EOF

    # Generate CA certificate
    openssl req -x509 -new -nodes -key "${ca_name}.key" -sha256 -days "$days" -out "${ca_name}.crt" -config "${ca_name}.cnf"

    echo "CA certificate and key generated: ${ca_name}.crt, ${ca_name}.key"
}