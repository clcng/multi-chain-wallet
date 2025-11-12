package com.wallet.service.blockchain;

import com.wallet.config.TonConfig;
import com.wallet.model.Transaction;
import com.wallet.model.enums.BlockchainNetwork;
import com.wallet.model.enums.TransactionStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.ton.ton4j.address.Address;
import org.ton.ton4j.mnemonic.Mnemonic;
import org.ton.ton4j.mnemonic.Pair;
import org.ton.ton4j.smartcontract.wallet.v3.WalletV3R2;
import org.ton.ton4j.tlb.StateInit;
import org.ton.ton4j.tonlib.Tonlib;
import org.ton.ton4j.tonlib.types.*;
import org.ton.ton4j.utils.Utils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class TonService implements BlockchainService {

    private final TonConfig tonConfig;
    private final Tonlib tonlib;
    private final String rpcUrl;
    private final RestTemplate restTemplate;

    private BlockIdExt lastBlock;
//
    public TonService(TonConfig tonConfig, @Value("${app.blockchain.ton.rpc-url}") String rpcUrl) {
        this.rpcUrl = rpcUrl;
        this.restTemplate = new RestTemplate();
        this.tonConfig = tonConfig;
        this.tonlib = tonConfig.tonlib();
    }

    @Override
    public BlockchainNetwork getNetwork() {
        return BlockchainNetwork.TON;
    }

    @Override
    public WalletKeyPair generateWallet() {
        try {
            // Generate mnemonic (24 words)
            List<String> mnemonic = Mnemonic.generate(24);

            // Create wallet from mnemonic
            byte[] seed = Mnemonic.toSeed(mnemonic);
            Pair keyPair = Mnemonic.toKeyPair(mnemonic);

//            Options options = Options.builder()
//                    .publicKey(keyPair.getPublicKey())
//                    .walletId(698983191 + tonConfig.getWallet().getWorkchain())
//                    .build();

            WalletV3R2 tonWallet = WalletV3R2.builder()
                    .publicKey(keyPair.getPublicKey())
                    .walletId(698983191 + tonConfig.getWallet().getWorkchain())
//                    .options(options)
                    .build();

            StateInit stateInit = tonWallet.getStateInit();
            Address address = tonWallet.getAddress();
            log.info("Generated TON wallet: {}", address);
            return new WalletKeyPair(address.toString(), new String(keyPair.getSecretKey()), new String(keyPair.getPublicKey()));
        } catch (Exception e) {
//            log.error("Error generating wallet for user {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Failed to generate wallet", e);
        }
    }

    @Override
    public BigDecimal getBalance(String address) {
        try {
            log.info("Getting balance for TON address: {}", address);
            Address addr = Address.of(address);
            RawAccountState accountState = tonlib.getRawAccountState(addr);
            return Utils.fromNano(new BigInteger(accountState.getBalance()));
        } catch (Exception e) {
            log.error("Error getting balance for TON address: {}", address, e);
            return BigDecimal.ZERO;
        }
    }

    @Override
    public String sendTransaction(String privateKey, String toAddress, BigDecimal amount) {
        try {
            //TODO:
            log.info("Sending {} TON transaction to: {}", amount, toAddress);

            // Destination address
            Address destination = Address.of(toAddress);

            // Amount in nanoTON (1 TON = 1,000,000,000 nanoTON)
            BigInteger amountInNanoTon = Utils.toNano(amount); // 1.5 TON


//            WalletV3R2.builder().publicKey()
//            // Send transaction
//            ExtMessageInfo result = tonlib.sendTonCoins(
//                    mnemonic,
//                    destination,
//                    amountInNanoTon
//            );



            return "ton_tx_" + System.currentTimeMillis();
        } catch (Exception e) {
            log.error("Error sending TON transaction", e);
            throw new RuntimeException("Failed to send TON transaction", e);
        }
    }

    @Override
    public List<Transaction> getWalletTransactions(String address, Long fromLt, String fromHash, int limit) {
        try {
            Address addr = Address.of(address);
            RawTransactions transactions = tonlib.getRawTransactions(
                    addr.toString(false),
                    BigInteger.valueOf(fromLt),
                    fromHash
            );

            List<RawTransaction> txList = new ArrayList<>();
            if (transactions != null && transactions.getTransactions() != null) {
                txList.addAll(transactions.getTransactions());
            }

            txList = txList.stream().limit(limit).toList();
            List<Transaction> transactionList = new ArrayList<>();
            for (RawTransaction rawTx: txList) {
                transactionList.add(mapToTransaction(rawTx));
            }
            return transactionList;
        } catch (Exception e) {
            log.error("Error getting transactions for address {}: {}", address, e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public TransactionDetails getTransactionDetails(String transactionHash) {
        try {
            log.info("Getting TON transaction details: {}", transactionHash);
            //TODO:
            return new TransactionDetails(
                    transactionHash,
                    "",
                    "",
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    0L,
                    0,
                    false
            );
        } catch (Exception e) {
            log.error("Error getting TON transaction details: {}", transactionHash, e);
            throw new RuntimeException("Failed to get transaction details", e);
        }
    }

    @Override
    public boolean isValidAddress(String address) {
        try {
            Address tonAddress = Address.of(address);
            return tonAddress != null;
        } catch (Exception e) {
            return false;
        }
    }

    private Transaction mapToTransaction(RawTransaction tx) {
        Transaction transaction = new Transaction();

        // Transaction ID
        transaction.setStatus(TransactionStatus.CONFIRMED);
        transaction.setTransactionHash(tx.getTransaction_id().getHash());
        transaction.setBlockNumber(tx.getTransaction_id().getLt().longValue());
//        details.setAccount(tx.getTransactionId().getAccount());
        if (lastBlock != null) {
            transaction.setConfirmations((int) (lastBlock.getSeqno() - transaction.getBlockNumber()));
        }

        // Incoming message
        if (tx.getIn_msg() != null) {
            transaction.setFromAddress(tx.getIn_msg().getSource().getAccount_address());
            if (tx.getIn_msg().getSource() != null) {
                transaction.setToAddress(tx.getIn_msg().getDestination().getAccount_address());
            }
            if (tx.getIn_msg().getValue() != null) {
                transaction.setStatus(TransactionStatus.SUCCESS);
                transaction.setAmount(Utils.fromNano(new BigInteger(tx.getIn_msg().getValue())));
            } else {
                transaction.setStatus(TransactionStatus.FAILED);
            }
            transaction.setMemo(tx.getIn_msg().getMessage());
        }

        transaction.setFee(Utils.fromNano(new BigInteger(tx.getFee())));
        return transaction;
    }

    /**
     * Convert nanoTON to TON
     */
    private BigDecimal nanoTonToTon(BigInteger nanoTon) {
        if (nanoTon == null) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(nanoTon).divide(
                new BigDecimal("1000000000"),
                9,
                BigDecimal.ROUND_DOWN
        );
    }

    public void getCurrentBlock() {
        try {
            lastBlock = tonlib.getLast().getLast();
        } catch (Exception e) {
            log.error("Error getting block height: {}", e.getMessage());
        }
    }
}
