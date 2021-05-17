package net.corda.core.identity;

import org.junit.Ignore
import org.junit.Test;
import sun.security.provider.certpath.X509CertPath
import sun.security.x509.X509CertImpl
import java.io.File
import java.io.FileInputStream
import java.security.KeyStore
import javax.swing.KeyStroke

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

@Ignore
class PartyAndCertificateTest {

    @Test
    fun wibble() {
        //v//al cert = X509CertImpl()

        //val f = File("/Users/ian/corda/montis-spike/app/testing/src/test/resources/certchain/serverchain.der")
        //val cert = X509CertImpl(FileInputStream(f))

        val root = loadCert("root.der")
        val ca = loadCert("ca.der")
        val server = loadCert("server.der")

        val certPath = X509CertPath(listOf(root,ca,server))

        println(certPath)
        val pAndC = PartyAndCertificate(certPath)

        println(pAndC)
    }

    @Test
    fun blibble() {
        //val node = loadJKS("nodekeystore.jks", "cordacadevpass")
        //println(node)

        val root = loadCert2("root.der")
        val ca = loadCert2("ca.der")
        val node = loadCert2("node.der")

        val certPath = X509CertPath(listOf(root,ca,node))

        println(certPath)
        val pAndC = PartyAndCertificate(certPath)

        println(pAndC)

        //node.getCertificate()
    }

    @Test
    fun twibble() {
        //val node = loadJKS("nodekeystore.jks", "cordacadevpass")
        //println(node)

        //..val root = loadCert2("y.cert")
        val ca = loadCert3("y.cert")
        val node = loadCert3("x.cert")

        val certPath = X509CertPath(listOf(ca,node))

        println(certPath)
        val pAndC = PartyAndCertificate(certPath)

        println(pAndC)

        //node.getCertificate()
    }

    fun loadCert (name: String) : X509CertImpl {
        val f = File("/Users/ian/corda/montis-spike/app/testing/src/test/resources/certchain/$name")
        val cert = X509CertImpl(FileInputStream(f))
        return  cert
    }

    fun loadCert2 (name: String) : X509CertImpl {
        val f = File("/Users/ian/corda/montis-spike/app/testing/src/test/cmds/openssl/$name")
        val cert = X509CertImpl(FileInputStream(f))
        return  cert
    }

    fun loadCert3 (name: String) : X509CertImpl {
        val f = File("/Users/ian/corda/montis/app/deployment/corda/AliceSARL/certificates/$name")
        val cert = X509CertImpl(FileInputStream(f))
        return  cert
    }

    fun loadJKS (name: String, password: String) : KeyStore {
        val ks = KeyStore.getInstance("JKS")
        val f = File("/Users/ian/corda/montis-spike/app/testing/src/test/resources/deployNodes/$name")

        ks.load(FileInputStream(f),password.toCharArray())
        //val cert = X509CertImpl(FileInputStream(f))
        return  ks
    }
}
