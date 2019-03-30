package com.gcbjr.btcnetwork.bitcoin;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.LegacyAddress;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.utils.BriefLogFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import sun.nio.ch.Net;

import java.io.File;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
public class Config {

    @Value("${bitcoin.network}")
    private String network;

    @Value("${bitcoin.address-to-send-back-to}")
    private String btcAddress;

    @Value("${bitcoin.file-prefix}")
    private String filePrefix;

    @Value("${bitcoin.file-location}")
    private String btcFileLocation;

    private NetworkParameters networkParameters;

    public Config() {
        BriefLogFormatter.init();
    }

    @Bean
    public NetworkParameters networkParameters() {
        if(network.equals("testnet")) {
            return TestNet3Params.get();
        } else if(network.equals("regtest")) {
            return RegTestParams.get();
        } else {
            return MainNetParams.get();
        }
    }

    @Bean
    public Address address(@Autowired NetworkParameters networkParameters) {
        return LegacyAddress.fromBase58(networkParameters, this.btcAddress);
    }

    @Bean
    public WalletAppKit walletAppKit(@Autowired NetworkParameters networkParameters) {
        return new WalletAppKit(networkParameters, new File(btcFileLocation), filePrefix) {
            @Override
            protected void onSetupCompleted() {
                if (wallet().getKeyChainGroupSize() < 1) {
                    wallet().importKey(new ECKey());
                }
            }
        };
    }

    @Bean("bitcoinExecutor")
    public Executor BitcoinExecutor() {
        return Executors.newFixedThreadPool(10);
    }

}
