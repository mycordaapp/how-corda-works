@file:Suppress("JAVA_MODULE_DOES_NOT_EXPORT_PACKAGE")
package net.corda.core.identity


import org.junit.Ignore
import org.junit.Test
import sun.security.provider.certpath.X509CertPath
import sun.security.x509.X509CertImpl
import java.io.File
import java.io.FileInputStream
import java.security.KeyStore

/**

- X509 certificate
https://www.decalage.info/security/cert4tests

https://www.golinuxcloud.com/openssl-create-certificate-chain-linux/

https://pkiwidgets.quovadisglobal.com/scriptgen/openssl.aspx

- PEM to DER
https://knowledge.digicert.com/solution/SO26449.html

-
https://stackoverflow.com/questions/30634658/how-to-create-a-certificate-chain-using-keytool
[ req ]
default_bits = 2048
prompt = no
encrypt_key = no
default_md = sha1
distinguished_name = dn

[ dn ]
CN = dfdfaa
emailAddress = ian@test.com
O = dsdsds
L = sadsdssad
ST = sddsds
C = AD
0.OU= dsdss


https://stackoverflow.com/questions/62195898/openssl-still-pointing-to-libressl-2-8-3

 */


class PartyAndCertificateTest {

    /**
     * Uses cert files extracted from the java JKS
     * files created by `./gradlew deployNode.
     *
     * Example data and extracted certs are at `src/test/resources/deployNodes'
     */
    @Test
    fun loadFromExtractedCertificate() {
        val location = "src/test/resources/deployNodes"

        val root = loadCert("$location/cordarootca.der")
        val ca = loadCert("$location/cordaclientca.der")
        val node = loadCert("$location/identity-private-key.der")

        val certPath = X509CertPath(listOf(node, ca, root))
        println(certPath)
        val pAndC = PartyAndCertificate(certPath)
        println(pAndC)
    }


    /**
     * In this example the certificates are built using just
     * openssl commands.
     *
     * Example commands and the generated certs are at `src/test/resources/openssl'
     */
    @Test
    fun loadFromOpenSSLGeneratedCerts() {
        val location = "src/test/cmds/openssl"

        val root = loadCert("$location/root.der")
        val ca = loadCert("$location/ca.der")
        val node = loadCert("$location/node.der")

        val certPath = X509CertPath(listOf(node, ca, root))

        println(certPath)
        val pAndC = PartyAndCertificate(certPath)

        println(pAndC)

        //node.getCertificate()
    }

    fun loadCert(path: String): X509CertImpl {
        val f = File(path)
        val cert = X509CertImpl(FileInputStream(f))
        return cert
    }

    fun loadCert2(name: String): X509CertImpl {
        val f = File("/Users/ian/corda/montis-spike/app/testing/src/test/cmds/openssl/$name")
        val cert = X509CertImpl(FileInputStream(f))
        return cert
    }

    fun loadCert3(name: String): X509CertImpl {
        val f = File("/Users/ian/corda/montis/app/deployment/corda/AliceSARL/certificates/$name")
        val cert = X509CertImpl(FileInputStream(f))
        return cert
    }

    fun loadJKS(name: String, password: String): KeyStore {
        val ks = KeyStore.getInstance("JKS")
        val f = File("/Users/ian/corda/montis-spike/app/testing/src/test/resources/deployNodes/$name")

        ks.load(FileInputStream(f), password.toCharArray())
        //val cert = X509CertImpl(FileInputStream(f))
        return ks
    }
}
