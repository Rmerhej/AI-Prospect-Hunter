package fr.prospecthunter.aiprospecthunter.enrichment;

import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Protection SSRF : l'application ne doit jamais aller lire une adresse interne.
 * Uniquement des adresses IP écrites en chiffres : aucune résolution DNS, donc aucun accès réseau.
 */
class HttpPageFetcherTest {

    @Test
    void refuseLesAdressesInternes() throws Exception {
        for (String ip : new String[]{"127.0.0.1", "10.0.0.5", "172.17.0.2", "192.168.1.10", "169.254.169.254",
                "0.0.0.0", "100.64.0.1", "::1", "fd00::1"}) {
            assertFalse(HttpPageFetcher.isPublic(InetAddress.getByName(ip)), ip + " doit être refusée");
        }
        assertTrue(HttpPageFetcher.isPublic(InetAddress.getByName("8.8.8.8")));
    }

    @Test
    void refuseLesSchemasEtPortsInhabituels() {
        assertFalse(HttpPageFetcher.isAllowed(URI.create("file:///etc/passwd")));
        assertFalse(HttpPageFetcher.isAllowed(URI.create("ftp://8.8.8.8/")));
        assertFalse(HttpPageFetcher.isAllowed(URI.create("http://8.8.8.8:3306/")));
        assertFalse(HttpPageFetcher.isAllowed(URI.create("http://user@8.8.8.8/")));
        assertFalse(HttpPageFetcher.isAllowed(URI.create("http://127.0.0.1/")));
        assertTrue(HttpPageFetcher.isAllowed(URI.create("https://8.8.8.8/")));
    }
}
