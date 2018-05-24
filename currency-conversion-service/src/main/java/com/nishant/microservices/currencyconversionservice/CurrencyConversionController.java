package com.nishant.microservices.currencyconversionservice;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;


@RestController
public class CurrencyConversionController {
	Logger logger =LoggerFactory.getLogger(CurrencyConversionController.class);
	@Autowired
	private CurrencyExchangeServiceProxy proxy;

	@GetMapping("currency-convertor/from/{from}/to/{to}/quantity/{quantity}")
	public CurrencyConversionBean convertCurrency(@PathVariable String from, @PathVariable String to,
			@PathVariable BigDecimal quantity) {

		//Feign - Problem 1
		Map<String, String> uriVariables = new HashMap<>();
		uriVariables.put("from", from);
		uriVariables.put("to", to);
		ResponseEntity<CurrencyConversionBean> responseEntity = new RestTemplate().getForEntity(
				"http://localhost:8000/currency-exchange/from/{from}/to/{to}",
				CurrencyConversionBean.class, uriVariables);
		CurrencyConversionBean response = responseEntity.getBody();
		return new CurrencyConversionBean(response.getId(), response.getFrom(), response.getTo(),
				response.getConversionMultiple(), quantity, quantity.multiply(response.getConversionMultiple()),
				response.getPort());
	}
	
	/*
	 * This does the same thing as above. Only difference is reduces to code.
	 * It requires to create a proxy
	 */
	@GetMapping("currency-convertor-feign/from/{from}/to/{to}/quantity/{quantity}")
	public CurrencyConversionBean convertCurrencyFeign(@PathVariable String from, @PathVariable String to,
			@PathVariable BigDecimal quantity) {

		CurrencyConversionBean response = proxy.retrieveExchangeValue(from, to);
		logger.info("{}", response);
		return new CurrencyConversionBean(response.getId(), response.getFrom(), response.getTo(),
				response.getConversionMultiple(), quantity, quantity.multiply(response.getConversionMultiple()),
				response.getPort());
	}

}
