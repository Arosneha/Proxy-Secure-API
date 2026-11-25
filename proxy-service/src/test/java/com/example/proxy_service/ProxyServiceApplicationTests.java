package com.example.proxy_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"weather.api.key=test-api-key",
		"jwt.public-key-path=secrets/public.pem"
})
class ProxyServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
//@SpringBootTest
//class ProxyServiceApplicationTests {
//
//	@Test
//	void contextLoads() {
//	}
//
//}
