# Certificate Configurations Explained

This section details the configuration (CNF) files used for generating CA and Nginx certificates.

## CA Certificate Configuration

The CA certificate configuration (`${ca_name}.cnf`) defines the properties of the Certificate Authority:

```ini
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
```

Key points:
- `distinguished_name`: Defines the certificate's subject information.
- `x509_extensions`: Specifies extensions for the CA certificate.
- `v3_ca` section: Sets critical CA properties.
  - `basicConstraints`: Marks this as a CA certificate.
  - `keyUsage`: Allows this certificate to sign other certificates and CRLs.

## Nginx Certificate Configuration

The Nginx certificate configuration (`${nginx_name}.cnf`) defines properties for server certificates:

```ini
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
```

Key points:
- `distinguished_name`: Defines the certificate's subject information.
- `v3_req` section: Sets critical server certificate properties.
  - `basicConstraints`: Marks this as a non-CA certificate.
  - `keyUsage` and `extendedKeyUsage`: Specifies allowed uses for the certificate.
  - `subjectAltName`: Defines additional names the certificate is valid for.

These configurations ensure that the generated certificates have the correct properties and uses for their respective roles in the mTLS setup.