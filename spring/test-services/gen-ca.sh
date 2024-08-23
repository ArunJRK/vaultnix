#!/bin/bash

# Rad default values, my dude
CA_NAME="jcava-ca"
CA_DAYS=3650  # Keepin' that 10-year vibe
BASE_DIR="./keys"
CA_DIR="${BASE_DIR}/ca"
CA_PASSWORD="changeit"  # Default Java keystore password

# Update that usage info
print_usage() {
    echo "Usage: $0 [OPTIONS]"
    echo "Options:"
    echo "  -n, --name NAME      Set CA name (default: $CA_NAME)"
    echo "  -d, --days DAYS      Set CA validity in days (default: $CA_DAYS)"
    echo "  -b, --base DIR       Set base directory (default: $BASE_DIR)"
    echo "  -p, --password PASS  Set CA keystore password (default: $CA_PASSWORD)"
    echo "  -h, --help           Show this gnarly help message"
}

# Parse those args, keeping it chill
while [[ $# -gt 0 ]]; do
    key="$1"
    case $key in
        -n|--name)
            CA_NAME="$2"
            shift 2
            ;;
        -d|--days)
            CA_DAYS="$2"
            shift 2
            ;;
        -b|--base)
            BASE_DIR="$2"
            CA_DIR="${BASE_DIR}/ca"
            shift 2
            ;;
        -p|--password)
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

# Create that sweet CA directory
mkdir -p "$CA_DIR"

# Generate that killer CA private key and certificate
keytool -genkeypair \
    -alias $CA_NAME \
    -keyalg RSA \
    -keysize 4096 \
    -keystore "${CA_DIR}/${CA_NAME}.jks" \
    -storepass $CA_PASSWORD \
    -keypass $CA_PASSWORD \
    -validity $CA_DAYS \
    -dname "CN=${CA_NAME}, OU=JCava CA, O=JCava Corp, L=Silicon Valley, ST=California, C=US" \
    -ext bc:c \
    -ext ku:c=digitalSignature,keyCertSign \
    -ext KeyUsage:critical="keyCertSign" \
    -ext BasicConstraints:critical="ca:true"

# Export the CA certificate
keytool -exportcert \
    -alias $CA_NAME \
    -keystore "${CA_DIR}/${CA_NAME}.jks" \
    -storepass $CA_PASSWORD \
    -file "${CA_DIR}/${CA_NAME}.crt" \
    -rfc

# Export client truststore
keytool -import -alias $CA_NAME \
    -file "${CA_DIR}/${CA_NAME}.crt" \
    -storepass $CA_PASSWORD \
    -keystore "${CA_DIR}/client-truststore.jks"


echo "Booyah! Your JCava CA is ready to shred, dude!"
echo "CA Keystore: ${CA_DIR}/${CA_NAME}.jks"
echo "CA Certificate: ${CA_DIR}/${CA_NAME}.crt"
echo "CA Keystore password: $CA_PASSWORD"
echo "CA alias: $CA_NAME"