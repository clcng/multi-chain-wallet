package com.wallet.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.ton.ton4j.tonlib.Tonlib;
import org.ton.ton4j.utils.Utils;

import java.io.File;

@Slf4j
@Configuration
@ConfigurationProperties(prefix = "ton")
@Data
public class TonConfig {
    private String network;
    private String apiEndpoint;
    private String apiKey;
    private ScannerConfig scanner;
    private WalletConfig wallet;
    private Boolean isTestnet;

    @Data
    public static class ScannerConfig {
        private boolean enabled;
        private long interval;
        private int confirmationBlocks;
    }

    @Data
    public static class WalletConfig {
        private int workchain;
        private String version;
    }

    @Bean(destroyMethod = "destroy")
    public Tonlib tonlib() {
        try {
            log.info("Creating Tonlib bean...");

//            String path = detectNativeLibraryPath();
//            log.info("Using native library at: {}", libPath);
            String path = "/Users/cindy/IdeaProjects/multi-chain-wallet/tonlibjson-mac-x86-64.dylib";
            log.info("path " + path);

            Tonlib tonlib = Tonlib.builder()
                    .pathToTonlibSharedLib(path)
//                    .pathToTonlibSharedLib("https://github.com/ton-blockchain/ton4j")
                    .testnet(true)
                    .ignoreCache(false)
                    .build();

            log.info("Tonlib bean created successfully");
            return tonlib;

        } catch (Exception e) {
            log.error("Failed to create Tonlib bean: {}", e.getMessage());
            throw new RuntimeException("Failed to initialize TON library", e);
        }
    }

    private String detectNativeLibraryPath() {
        String userHome = System.getProperty("user.home");
        String osName = System.getProperty("os.name").toLowerCase();
        String osArch = System.getProperty("os.arch").toLowerCase();

        String libName;
        String libPath;

            libName = "tonlibjson-mac-x86-64.dylib";

            // Try common locations
            String[] possiblePaths = {
                    userHome + "/lib/" + libName,
                    "/usr/local/lib/" + libName,
                    "/opt/homebrew/lib/" + libName,
                    // Try to find in classpath
                    getClasspathLibraryPath(libName)
            };

            for (String path : possiblePaths) {
                if (path != null && new File(path).exists()) {
                    return path;
                }
            }

        throw new RuntimeException("Native library not found. Please extract it manually.");
    }

    private String getClasspathLibraryPath(String libName) {
        try {
            var url = getClass().getClassLoader().getResource(libName);
            if (url != null) {
                return url.getPath();
            }
        } catch (Exception e) {
            // Ignore
        }
        return null;
    }
}