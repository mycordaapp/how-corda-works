################################################
## 1. The root certificate.
################################################
# CSR
openssl req -new \
      -config root.conf \
      -keyout root.key \
      -out root.csr \

# Create cert
openssl x509 \
      -signkey root.key \
      -in root.csr \
      -req -days 365 -out root.pem \
      -extfile root.conf -extensions req_ext

# View cert
openssl x509 -in root.pem -text

# Convert to DER format
openssl x509 -outform der -in root.pem -out root.der

################################################
## 2. The CA certificate.
################################################
openssl req -new \
      -config ca.conf \
      -keyout ca.key \
      -out ca.csr \
      -days 365

openssl x509 -req -days 365 -in ca.csr -CA root.pem -CAkey root.key -set_serial 01 -out ca.pem

# View cert
openssl x509 -in ca.pem -text

# Convert to DER format
openssl x509 -outform der -in ca.pem -out ca.der

################################################
## 3. The Node  certificate
################################################
openssl req -new \
      -config node.conf \
      -keyout node.key \
      -out node.csr \
      -days 365

openssl x509 -req -days 365 -in node.csr -CA ca.pem -CAkey ca.key -set_serial 01  -out node.pem

# View cert
openssl x509 -in node.pem -text

# Convert to DER format
openssl x509 -outform der -in node.pem -out node.der



#openssl x509 -req -days 365 -in ca.csr -CA root.pem -CAkey root.key -set_serial 01 -out ca.cer

#openssl ca -in ca.csr -out ca.cer

# Create and sign the certificate
#openssl ca -policy policy_anything -keyfile A.key -cert A.pem -out B.pem -infiles B.request

#openssl genrsa -out client.key 1024
#openssl req -new -key client.key -out client.csr
#openssl ca -in client.csr -out client.cer



#openssl req -new -config test.conf -keyout test.key -out test.csr