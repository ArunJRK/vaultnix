#!/bin/bash

# Radical defaults, bro
BASE_DIR="./keys"
CA_DIR="${BASE_DIR}/ca"
CA_NAME="jcava-ca"
DAYS=365  # Default to 1 year for service certs
KEYSTORE_PASSWORD="changeit"  # Default Java keystore password
CA_PASSWORD="changeit"  # Default CA keystore password

# Function to print usage info
print_usage() {
    echo "Usage: $0 -s SERVICE_NAME [OPTIONS]"
    echo "Options:"
    echo "  -s, --service NAME     Set service name (required)"
    echo "  -d, --days DAYS        Set certificate validity in days (default: $DAYS)"
    echo "  -b, --base DIR         Set base directory (default: $BASE_DIR)"
    echo "  -c, --ca NAME          Set CA name (default: $CA_NAME)"
    echo "  -p, --password PASS    Set service keystore password (default: $KEYSTORE_PASSWORD)"
    echo "  -cp, --ca-password PASS Set CA keystore password (default: $CA_PASSWORD)"
    echo "  -h, --help             Show this gnarly help message"
}

# Parse those command-line args, dude
while [[ $# -gt 0 ]]; do
    key="$1"
    case $key in
        -s|--service)
            SERVICE_NAME="$2"
            shift 2
            ;;
        -d|--days)
            DAYS="$2"
            shift 2
            ;;
        -b|--base)
            BASE_DIR="$2"
            CA_DIR="${BASE_DIR}/ca"
            shift 2
            ;;
        -c|--ca)
            CA_NAME="$2"
            shift 2
            ;;
        -p|--password)
            KEYSTORE_PASSWORD="$2"
            shift 2
            ;;
        -cp|--ca-password)
            CA_PASSWORD="$2"
            shift 2
            ;;
        -h|--help)
            print_usage
            exit 0
            ;;
        *)
            echo "Whoa, unknown option: $1"
            print_usage
            exit 1
            ;;
    esac
done

# Make sure we've got a service name, man
if [ -z "$SERVICE_NAME" ]; then
    echo "Dude, you gotta provide a service name!"
    print_usage
    exit 1
fi

# Set up the service directory
SERVICE_DIR="${BASE_DIR}/${SERVICE_NAME}"
mkdir -p "$SERVICE_DIR"

# Generate service keystore and CSR
keytool -genkeypair \
    -alias $SERVICE_NAME \
    -keyalg RSA \
    -keysize 2048 \
    -keystore "${SERVICE_DIR}/${SERVICE_NAME}.jks" \
    -storepass $KEYSTORE_PASSWORD \
    -keypass $KEYSTORE_PASSWORD \
    -validity $DAYS \
    -dname "CN=${SERVICE_NAME}, OU=JCava Services, O=JCava Corp, L=Silicon Valley, ST=California, C=US"

keytool -certreq \
    -alias $SERVICE_NAME \
    -keystore "${SERVICE_DIR}/${SERVICE_NAME}.jks" \
    -storepass $KEYSTORE_PASSWORD \
    -file "${SERVICE_DIR}/${SERVICE_NAME}.csr"

# Sign the CSR with our gnarly CA
keytool -gencert \
    -alias $CA_NAME \
    -keystore "${CA_DIR}/${CA_NAME}.jks" \
    -storepass $CA_PASSWORD \
    -infile "${SERVICE_DIR}/${SERVICE_NAME}.csr" \
    -outfile "${SERVICE_DIR}/${SERVICE_NAME}.crt" \
    -validity $DAYS \
    -ext KeyUsage:critical="digitalSignature,keyEncipherment" \
    -ext ExtendedKeyUsage="serverAuth,clientAuth" \
    -ext SubjectAlternativeName="DNS:${SERVICE_NAME},DNS:${SERVICE_NAME}.local,DNS:localhost"

# Import the CA certificate and the signed certificate into the service keystore
keytool -importcert \
    -alias ca \
    -file "${CA_DIR}/${CA_NAME}.crt" \
    -keystore "${SERVICE_DIR}/${SERVICE_NAME}.jks" \
    -storepass $KEYSTORE_PASSWORD \
    -noprompt

keytool -importcert \
    -alias $SERVICE_NAME \
    -file "${SERVICE_DIR}/${SERVICE_NAME}.crt" \
    -keystore "${SERVICE_DIR}/${SERVICE_NAME}.jks" \
    -storepass $KEYSTORE_PASSWORD \
    -noprompt

# Copy files
cp "${SERVICE_DIR}/${SERVICE_NAME}.jks" "${SERVICE_NAME}/src/main/resources"
cp "${CA_DIR}/${CA_NAME}.crt" "${SERVICE_NAME}/src/main/resources"
cp "${CA_DIR}/client-truststore.jks" "${SERVICE_NAME}/src/main/resources"

echo "Booyah! Your service keystore is ready to rock, dude!"
echo "Service Keystore: ${SERVICE_DIR}/${SERVICE_NAME}.jks"
echo "Keystore password: $KEYSTORE_PASSWORD"
echo "Service alias: ${SERVICE_NAME}"
echo "CA alias: ca"