package br.com.itaipu.grfe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class GrfeApplication {

	public static void main(String[] args) {
		SpringApplication.run(GrfeApplication.class, args);
	}

}


