# Deploy Nodes 

This folder contains the  Java key / trustores generated for a single node 
using the `./gradlew deployNodes` command. 

To list the contents:

```bash 
keytool -list -keystore truststore.jks -storepass trustpass
keytool -list -keystore nodekeystore.jks -storepass cordacadevpass
keytool -list -keystore sslkeystore.jks -storepass cordacadevpass
```

The output will be similar to the following 
```bash
$ keytool -list -keystore truststore.jks -storepass trustpass

Keystore type: jks
Keystore provider: SUN

Your keystore contains 1 entry

cordarootca, 13-May-2021, trustedCertEntry, 
Certificate fingerprint (SHA-256): 20:34:B7:A3:FF:76:D1:2C:15:F5:21:AF:17:41:1D:B5:B2:6C:5A:75:C8:6D:CD:E1:B0:93:7E:11:03:E3:F3:F4


$ keytool -list -keystore nodekeystore.jks -storepass cordacadevpass

Keystore type: jks
Keystore provider: SUN

Your keystore contains 2 entries

cordaclientca, 13-May-2021, PrivateKeyEntry, 
Certificate fingerprint (SHA-256): 35:AD:E5:5A:E6:4F:2A:FE:86:0C:42:EE:EE:81:DA:E6:9A:CE:71:AB:41:60:F2:67:42:E1:E8:97:5E:6C:5F:05
identity-private-key, 13-May-2021, PrivateKeyEntry, 
Certificate fingerprint (SHA-256): 5C:A3:9A:CC:60:BB:6A:CE:22:18:95:76:D3:D9:87:07:AC:FA:06:CD:15:3F:D7:C9:90:FC:B6:92:EA:47:3C:39


$ keytool -list -keystore sslkeystore.jks -storepass cordacadevpass 

Keystore type: jks
Keystore provider: SUN

Your keystore contains 1 entry

cordaclienttls, 13-May-2021, PrivateKeyEntry, 
Certificate fingerprint (SHA-256): 7F:76:E5:A9:B7:92:0B:7F:51:C9:EA:75:81:69:76:5F:0E:08:E1:15:25:43:EF:33:48:45:AC:A5:A0:85:6F:AF
```

To extract the certificates:

```bash
keytool -exportcert -keystore truststore.jks -storepass trustpass -alias cordarootca -rfc -file cordarootca.der 
keytool -exportcert -keystore nodekeystore.jks -storepass cordacadevpass -alias cordaclientca -rfc -file cordaclientca.der 
keytool -exportcert -keystore nodekeystore.jks -storepass cordacadevpass -alias identity-private-key -rfc -file identity-private-key.der 
```



We can now inspect these with `openssl` 

```bash
openssl x509 -in cordarootca.der -text
openssl x509 -in cordaclientca.der -text
openssl x509 -in identity-private-key.der -text
```

If you look at the output for the `cordaclientca` and `identity-private-key` certificate you will see that there is a custom 
extension defined (a snippet of the output is below)

```
X509v3 extensions:
            X509v3 Subject Key Identifier: 
                C0:56:66:12:9B:47:DE:17:CA:F9:00:B3:DE:1A:5D:03:49:96:66:1E
            ...
            1.3.6.1.4.1.50530.1.1: 
                ...
```

This is a custom extension registered with R3 that is used to define the purpose of the certificate. It 
is essential that it is set properly 

https://docs.corda.net/docs/corda-os/4.8/permissioning.html#certificate-role-extension

The `identity-private-key` certificate has the X500 name of the node (see snippet below)

```
Certificate:
    Data:
        Version: 3 (0x2)
        Serial Number:
            4d:d1:e3:03:75:d8:e1:69:03:10:7d:3a:aa:0a:29:9e
    Signature Algorithm: ecdsa-with-SHA256
        Issuer: C=FR, L=Paris, O=Alice SARL

```