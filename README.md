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


## Test 2 with `ssl_trusted_certificate` directive

`curl -v --cert nginx-config/tls/nginx3.crt --key nginx-config/tls/nginx3.key --cacert nginx-config/tls/nginx_ca.crt https://localhost:8081`

```log
* Host localhost:8081 was resolved.
* IPv6: ::1
* IPv4: 127.0.0.1
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
  0     0    0     0    0     0      0      0 --:--:-- --:--:-- --:--:--     0*   Trying [::1]:8081...
* Connected to localhost (::1) port 8081
* ALPN: curl offers h2,http/1.1
* (304) (OUT), TLS handshake, Client hello (1):
} [314 bytes data]
*  CAfile: nginx-config/tls/nginx_ca.crt
*  CApath: none
* (304) (IN), TLS handshake, Server hello (2):
{ [122 bytes data]
* (304) (IN), TLS handshake, Unknown (8):
{ [25 bytes data]
* (304) (IN), TLS handshake, Request CERT (13):
{ [154 bytes data]
* (304) (IN), TLS handshake, Certificate (11):
{ [1355 bytes data]
* (304) (IN), TLS handshake, CERT verify (15):
{ [264 bytes data]
* (304) (IN), TLS handshake, Finished (20):
{ [52 bytes data]
* (304) (OUT), TLS handshake, Certificate (11):
} [1355 bytes data]
* (304) (OUT), TLS handshake, CERT verify (15):
} [264 bytes data]
* (304) (OUT), TLS handshake, Finished (20):
} [52 bytes data]
* SSL connection using TLSv1.3 / AEAD-AES256-GCM-SHA384 / [blank] / UNDEF
* ALPN: server accepted http/1.1
* Server certificate:
*  subject: C=US; ST=California; L=San Francisco; O=My Organization; OU=My Organizational Unit; CN=nginx1
*  start date: Aug 15 10:23:28 2024 GMT
*  expire date: Aug 15 10:23:28 2025 GMT
*  subjectAltName: host "localhost" matched cert's "localhost"
*  issuer: C=US; ST=State; L=City; O=Organization; OU=CA; CN=nginx_ca
*  SSL certificate verify ok.
* using HTTP/1.x
> GET / HTTP/1.1
> Host: localhost:8081
> User-Agent: curl/8.6.0
> Accept: */*
> 
< HTTP/1.1 200 OK
< Server: nginx/1.27.1
< Date: Fri, 16 Aug 2024 17:50:50 GMT
< Content-Type: text/html
< Content-Length: 375
< Last-Modified: Wed, 14 Aug 2024 06:45:50 GMT
< Connection: keep-alive
< ETag: "66bc529e-177"
< X-Client-DN: CN=nginx3,OU=My Organizational Unit,O=My Organization,L=San Francisco,ST=California,C=US
< Accept-Ranges: bytes
< 
{ [375 bytes data]
100   375  100   375    0     0  32111      0 --:--:-- --:--:-- --:--:-- 34090
* Connection #0 to host localhost left intact
```
Hyperfine test results with connection close header

```log
  Time (mean ± σ):      16.2 ms ±   2.6 ms    [User: 5.4 ms, System: 3.0 ms]
  Range (min … max):    11.3 ms …  50.8 ms    1100 runs
```

## Test 3 without `ssl_trusted_certificate`

`curl -v --cert nginx-config/tls/nginx3.crt --key nginx-config/tls/nginx3.key --cacert nginx-config/tls/nginx_ca.crt https://localhost:8081`

```log
* Host localhost:8081 was resolved.
* IPv6: ::1
* IPv4: 127.0.0.1
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
  0     0    0     0    0     0      0      0 --:--:-- --:--:-- --:--:--     0*   Trying [::1]:8081...
* Connected to localhost (::1) port 8081
* ALPN: curl offers h2,http/1.1
* (304) (OUT), TLS handshake, Client hello (1):
} [314 bytes data]
*  CAfile: nginx-config/tls/nginx_ca.crt
*  CApath: none
* (304) (IN), TLS handshake, Server hello (2):
{ [122 bytes data]
* (304) (IN), TLS handshake, Unknown (8):
{ [25 bytes data]
* (304) (IN), TLS handshake, Request CERT (13):
{ [154 bytes data]
* (304) (IN), TLS handshake, Certificate (11):
{ [1355 bytes data]
* (304) (IN), TLS handshake, CERT verify (15):
{ [264 bytes data]
* (304) (IN), TLS handshake, Finished (20):
{ [52 bytes data]
* (304) (OUT), TLS handshake, Certificate (11):
} [1355 bytes data]
* (304) (OUT), TLS handshake, CERT verify (15):
} [264 bytes data]
* (304) (OUT), TLS handshake, Finished (20):
} [52 bytes data]
* SSL connection using TLSv1.3 / AEAD-AES256-GCM-SHA384 / [blank] / UNDEF
* ALPN: server accepted http/1.1
* Server certificate:
*  subject: C=US; ST=California; L=San Francisco; O=My Organization; OU=My Organizational Unit; CN=nginx1
*  start date: Aug 15 10:23:28 2024 GMT
*  expire date: Aug 15 10:23:28 2025 GMT
*  subjectAltName: host "localhost" matched cert's "localhost"
*  issuer: C=US; ST=State; L=City; O=Organization; OU=CA; CN=nginx_ca
*  SSL certificate verify ok.
* using HTTP/1.x
> GET / HTTP/1.1
> Host: localhost:8081
> User-Agent: curl/8.6.0
> Accept: */*
> 
< HTTP/1.1 200 OK
< Server: nginx/1.27.1
< Date: Fri, 16 Aug 2024 17:53:49 GMT
< Content-Type: text/html
< Content-Length: 375
< Last-Modified: Wed, 14 Aug 2024 06:45:50 GMT
< Connection: keep-alive
< ETag: "66bc529e-177"
< X-Client-DN: CN=nginx3,OU=My Organizational Unit,O=My Organization,L=San Francisco,ST=California,C=US
< Accept-Ranges: bytes
< 
{ [375 bytes data]
100   375  100   375    0     0  31472      0 --:--:-- --:--:-- --:--:-- 34090
* Connection #0 to host localhost left intact
```

Hyperfine test results with connection close header

```log
  Time (mean ± σ):      15.9 ms ±   3.0 ms    [User: 5.3 ms, System: 3.0 ms]
  Range (min … max):    10.8 ms …  41.1 ms    1100 runs
```