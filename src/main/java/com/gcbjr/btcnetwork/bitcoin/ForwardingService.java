package com.gcbjr.btcnetwork.bitcoin;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import org.bitcoinj.core.*;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.listeners.WalletCoinsReceivedEventListener;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Component
public class ForwardingService {

    @Autowired
    private WalletAppKit walletAppKit;

    @Autowired
    private Address address;

    @Autowired
    @Qualifier("bitcoinExecutor")
    private Executor bitcoinExecutor;

    @Autowired
    private NetworkParameters networkParameters;

    @PostConstruct
    public void start() {
        walletAppKit.startAsync();
        walletAppKit.awaitRunning();

        walletAppKit.wallet().addCoinsReceivedEventListener((wallet, tx, prevBalance, newBalance) -> {
            Coin value = tx.getValueSentToMe(wallet);
            System.out.println("Received tx for " + value.toFriendlyString());
            System.out.println("Transaction will be forwarded after it confirms");

            Futures.addCallback(tx.getConfidence().getDepthFuture(1), new FutureCallback<TransactionConfidence>() {
                @Override
                public void onSuccess(@Nullable TransactionConfidence transactionConfidence) {
                    forwardCoins(tx);
                }

                @Override
                public void onFailure(Throwable throwable) {

                }
            }, bitcoinExecutor);
        });

        Address sendToAddress = LegacyAddress.fromKey(networkParameters, walletAppKit.wallet().currentReceiveKey());
        System.out.println("Send coins to: " + sendToAddress);


    }

    private void forwardCoins(Transaction tx) {
        try {
            Coin value = tx.getValueSentToMe(walletAppKit.wallet());
            System.out.println("Forwarding " + value.toFriendlyString() + " BTC");
            final Coin amountToSend = value.subtract(Transaction.REFERENCE_DEFAULT_MIN_TX_FEE);
            final Wallet.SendResult sendResult = walletAppKit.wallet().sendCoins(walletAppKit.peerGroup(), address, amountToSend);
            System.out.println("Sending...");
            sendResult.broadcastComplete.addListener(new Runnable() {
                @Override
                public void run() {
                    System.out.println("Sent coins onwards! Transaction hash is " + sendResult.tx.getTxId().toString());
                }
            },bitcoinExecutor);
        } catch (InsufficientMoneyException e) {
            throw new RuntimeException(e);
        }
    }

}
