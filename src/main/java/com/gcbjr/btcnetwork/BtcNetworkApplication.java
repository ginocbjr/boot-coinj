package com.gcbjr.btcnetwork;

import com.gcbjr.btcnetwork.bitcoin.ForwardingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BtcNetworkApplication {

//	@Autowired
//	private ForwardingService forwardingService;

	public static void main(String[] args) {
		SpringApplication.run(BtcNetworkApplication.class, args);
	}

}
