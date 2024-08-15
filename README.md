## Nginx mTLS Test

Generated 3 nginx configs with configuration to allow or deny based on **CN** in the certificate
#### The -l argument allows an CN with name given.
```log
./generate-conf.sh -n nginx1 -p 8081 -l nginx2 -l nginx3 -o nginx1.conf
./generate-conf.sh -n nginx2 -p 8082 -l nginx1 -o nginx2.conf
./generate-conf.sh -n nginx3 -p 8083 -o nginx3.conf
```

#### The that logic maps certificates CN to allow or deny. 
Below is config generated in nginx1.conf which allows 2 and 3
```nginx
    map $ssl_client_s_dn $is_allowed_client {
        default 0;
        ~CN=nginx2(,|$) 1;
        ~CN=nginx3(,|$) 1;
}
```

#### Test results of `./test-results.zsh`

```log
Starting mTLS cross-connection tests...
Using certificates from ./nginx-config/tls directory
Server configuration from servers.json

Parsed servers:
nginx1:8081
nginx2:8082
nginx3:8083

======================================
Testing: nginx1 -> nginx2 (Port 8082)
Sending request...
Connection successful! (HTTP 200 OK)

======================================
Testing: nginx1 -> nginx3 (Port 8083)
Sending request...
Authenticated but forbidden. (HTTP 403 Forbidden)

======================================
Testing: nginx2 -> nginx1 (Port 8081)
Sending request...
Connection successful! (HTTP 200 OK)

======================================
Testing: nginx2 -> nginx3 (Port 8083)
Sending request...
Authenticated but forbidden. (HTTP 403 Forbidden)

======================================
Testing: nginx3 -> nginx1 (Port 8081)
Sending request...
Connection successful! (HTTP 200 OK)

======================================
Testing: nginx3 -> nginx2 (Port 8082)
Sending request...
Authenticated but forbidden. (HTTP 403 Forbidden)
mTLS cross-connection tests complete!
Successful tests: 6 / 6
```