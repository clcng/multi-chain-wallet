package com.wallet.service.blockchain;

import com.wallet.model.Transaction;
import com.wallet.model.enums.BlockchainNetwork;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Transfer;
import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

@Slf4j
@Service
public class EthereumService implements BlockchainService {

    private final Web3j web3j;
    private final long chainId;

    public EthereumService(
            @Value("${app.blockchain.ethereum.rpc-url}") String rpcUrl,
            @Value("${app.blockchain.ethereum.chain-id}") long chainId) {
        this.web3j = Web3j.build(new HttpService(rpcUrl));
        this.chainId = chainId;
    }

    @Override
    public BlockchainNetwork getNetwork() {
        return BlockchainNetwork.ETHEREUM_SEPOLIA;
    }

    @Override
    public WalletKeyPair generateWallet() {
        try {
            ECKeyPair keyPair = Keys.createEcKeyPair();
            String address = "0x" + Keys.getAddress(keyPair);
            String privateKey = keyPair.getPrivateKey().toString(16);
            String publicKey = keyPair.getPublicKey().toString(16);

            log.info("Generated Ethereum wallet: {}", address);
            return new WalletKeyPair(address, privateKey, publicKey);
        } catch (Exception e) {
            log.error("Error generating Ethereum wallet", e);
            throw new RuntimeException("Failed to generate Ethereum wallet", e);
        }
    }

    @Override
    public BigDecimal getBalance(String address) {
        try {
            BigInteger balanceWei = web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST)
                    .send()
                    .getBalance();

            return Convert.fromWei(balanceWei.toString(), Convert.Unit.ETHER);
        } catch (Exception e) {
            log.error("Error getting balance for address: {}", address, e);
            throw new RuntimeException("Failed to get balance", e);
        }
    }

    @Override
    public String sendTransaction(String privateKey, String toAddress, BigDecimal amount) {
        try {
            Credentials credentials = Credentials.create(privateKey);

            TransactionReceipt receipt = Transfer.sendFunds(
                    web3j,
                    credentials,
                    toAddress,
                    amount,
                    Convert.Unit.ETHER
            ).send();

            log.info("Ethereum transaction sent: {}", receipt.getTransactionHash());
            return receipt.getTransactionHash();
        } catch (Exception e) {
            log.error("Error sending Ethereum transaction", e);
            throw new RuntimeException("Failed to send transaction", e);
        }
    }

    @Override
    public List<Transaction> getWalletTransactions(String address, Long fromLt, String fromHash, int limit) {
        return null;
    }

    @Override
    public TransactionDetails getTransactionDetails(String transactionHash) {
        try {
            var transaction = web3j.ethGetTransactionByHash(transactionHash)
                    .send()
                    .getTransaction()
                    .orElseThrow(() -> new RuntimeException("Transaction not found"));

            EthGetTransactionReceipt receiptResponse = web3j.ethGetTransactionReceipt(transactionHash).send();
            TransactionReceipt receipt = receiptResponse.getTransactionReceipt().orElse(null);

            BigInteger currentBlock = web3j.ethBlockNumber().send().getBlockNumber();
            BigInteger txBlock = transaction.getBlockNumber();
            Integer confirmations = txBlock != null ?
                    currentBlock.subtract(txBlock).intValue() + 1 : 0;

            BigDecimal amount = Convert.fromWei(transaction.getValue().toString(), Convert.Unit.ETHER);
            BigDecimal fee = receipt != null ?
                    Convert.fromWei(receipt.getGasUsed().multiply(transaction.getGasPrice()).toString(),
                            Convert.Unit.ETHER) : BigDecimal.ZERO;

            return new TransactionDetails(
                    transactionHash,
                    transaction.getFrom(),
                    transaction.getTo(),
                    amount,
                    fee,
                    txBlock != null ? txBlock.longValue() : null,
                    confirmations,
                    confirmations >= 12
            );
        } catch (Exception e) {
            log.error("Error getting transaction details: {}", transactionHash, e);
            throw new RuntimeException("Failed to get transaction details", e);
        }
    }

    @Override
    public boolean isValidAddress(String address) {
        return address != null && address.matches("^0x[0-9a-fA-F]{40}$");
    }

    @Override
    public void getCurrentBlock() {

    }
}
