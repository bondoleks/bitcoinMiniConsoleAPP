package org.example;
import org.bitcoinj.core.*;
import org.bitcoinj.crypto.*;
import org.bitcoinj.params.*;
import org.bitcoinj.wallet.*;

import java.io.*;
import java.net.*;
import java.security.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.Scanner;

public class BitcoinConsoleApp {
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        // Generate mnemonic phrase
        List<String> mnemonic = generateMnemonic();

        // Create seed from mnemonic phrase
        DeterministicSeed seed = new DeterministicSeed(mnemonic, null, "", System.currentTimeMillis());

        // Create wallet from seed
        NetworkParameters params = TestNet3Params.get();
        KeyChainGroup keyChainGroup = KeyChainGroup.builder(params).addChain(DeterministicKeyChain.builder().seed(seed).build()).build();
        Wallet wallet = Wallet.fromSeed(params, seed);

        // Get the first address from the wallet
        Address address = wallet.freshReceiveAddress();

        // Print mnemonic phrase and address to screen
        System.out.println("Mnemonic phrase: " + mnemonic);
        System.out.println("Address: " + address);

        // Get the balance of the address and print to screen
        try {
            double balance = getBalance(address.toString());
            System.out.println("Balance: " + balance);
        } catch (IOException e) {
            System.err.println("Failed to get balance: " + e.getMessage());
        }

        // Update balance every 10 seconds
        scheduler.scheduleAtFixedRate(() -> {
            try {
                double balance = getBalance(address.toString());
                System.out.println("Balance: " + balance);
            } catch (IOException e) {
                System.err.println("Failed to get balance: " + e.getMessage());
            }
        }, 0, 10, TimeUnit.SECONDS);

        // Get recipient address from user
        System.out.println("Enter recipient address: ");
        String recipientAddressStr = scanner.nextLine();
        NetworkParameters networkParameters = TestNet3Params.get();
        Address recipientAddress = Address.fromString(networkParameters, recipientAddressStr);


        // Get transaction amount from user
        System.out.println("Enter transaction amount (BTC): ");
        double amountBTC = scanner.nextDouble();
        Coin amount = Coin.parseCoin(String.valueOf(amountBTC));

        // Send transaction
        try {
            sendTransaction(wallet, recipientAddress, amount);
        } catch (Exception e) {
            System.err.println("Failed to send transaction: " + e.getMessage());
        }
    }

    // Generate mnemonic phrase
    public static List<String> generateMnemonic() {
        try {
            MnemonicCode mnemonicCode = new MnemonicCode();
            SecureRandom secureRandom = new SecureRandom();
            return mnemonicCode.toMnemonic(secureRandom.generateSeed(16));
        } catch (Exception e) {
            System.err.println("Failed to generate mnemonic: " + e.getMessage());
            return null;
        }
    }

    // Get balance of the address
    private static double getBalance(String address) throws IOException {
        URL url = new URL("https://blockstream.info/testnet/api/address/" + address + "/utxo");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        // Parse JSON response and calculate balance
        // In this example, I'm just showing how much spent bitcoin we have at the address.
        // In a real application, this method should be configured to properly calculate the balance.
        // This can be done by analyzing transactions on the blockchain.
        // This example is just a demonstration.
        return response.toString().split(":").length - 1;
    }

    // Send transaction
    private static void sendTransaction(Wallet wallet, Address recipientAddress, Coin amount) throws Exception {
        // Get network parameter
        NetworkParameters params = TestNet3Params.get();

        // Create new transaction
        Transaction transaction = new Transaction(params);

        // Add output address
        transaction.addOutput(amount, recipientAddress);

        // Prepare request to send transaction
        SendRequest request = SendRequest.forTx(transaction);

        // Sign transaction
        wallet.signTransaction(request);

        // Send transaction
        wallet.commitTx(request.tx);

        // Print message about successful transaction sending
        System.out.println("Transaction sent! Transaction hash: " + request.tx.getHashAsString());
    }
}
