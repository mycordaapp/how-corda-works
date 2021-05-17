package mycorda.app


import net.corda.core.identity.CordaX500Name
import net.corda.testing.core.TestIdentity


// Parties
object Parties {
    val NOTARY = TestIdentity(CordaX500Name.parse("O=Notary, L=London, C=GB"))

    val PARTICIPANT_1 = TestIdentity(CordaX500Name.parse("O=Alice SARL, L=Paris, C=FR"))
    val PARTICIPANT_2 = TestIdentity(CordaX500Name.parse("O=Bob Ltd, L=Milton Keynes, C=GB"))
    val PARTICIPANT_3 = TestIdentity(CordaX500Name.parse("O=Charlie Pty, L=Melbourne, S=Victoria, C=AU"))
}

