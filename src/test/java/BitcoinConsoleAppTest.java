import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.wallet.DeterministicKeyChain;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.KeyChainGroup;
import org.bitcoinj.wallet.Wallet;
import org.example.BitcoinConsoleApp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BitcoinConsoleAppTest {

    private NetworkParameters params;
    private Wallet wallet;

    @BeforeEach
    public void setup() {
        params = TestNet3Params.get();

        List<String> mnemonic = BitcoinConsoleApp.generateMnemonic();

        DeterministicSeed seed = new DeterministicSeed(mnemonic, null, "", System.currentTimeMillis());

        KeyChainGroup keyChainGroup = KeyChainGroup.builder(params)
                .addChain(DeterministicKeyChain.builder().seed(seed).build())
                .build();
        wallet = Wallet.fromSeed(params, seed);
    }

    @Test
    public void testWalletCreation() {
        assertNotNull(wallet);
    }

    @Test
    public void testFreshReceiveAddress() {
        assertNotNull(wallet.freshReceiveAddress());
    }

    @Test
    public void testMnemonicGeneration() {
        List<String> mnemonic = BitcoinConsoleApp.generateMnemonic();
        assertNotNull(mnemonic);
        assertEquals(12, mnemonic.size());
    }
}
