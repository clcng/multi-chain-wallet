package com.wallet.config;

import com.wallet.model.enums.BlockchainNetwork;
import com.wallet.service.blockchain.BlockchainService;
import com.wallet.service.blockchain.EthereumService;
import com.wallet.service.blockchain.TonService;
//import com.wallet.service.blockchain.TronService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class Web3Config {

    @Bean
    public Map<BlockchainNetwork, BlockchainService> blockchainServices(
            EthereumService ethereumService,
//            TronService tronService,
            TonService tonService) {

        Map<BlockchainNetwork, BlockchainService> services = new HashMap<>();
        services.put(BlockchainNetwork.ETHEREUM_SEPOLIA, ethereumService);
//        services.put(BlockchainNetwork.TRON, tronService);
        services.put(BlockchainNetwork.TON, tonService);

        return services;
    }
}
