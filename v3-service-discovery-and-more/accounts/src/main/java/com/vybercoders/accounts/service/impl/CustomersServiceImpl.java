package com.vybercoders.accounts.service.impl;


import com.vybercoders.accounts.dto.AccountsDto;
import com.vybercoders.accounts.dto.CardsDto;
import com.vybercoders.accounts.dto.CustomerDetailsDto;
import com.vybercoders.accounts.dto.LoansDto;
import com.vybercoders.accounts.entity.Accounts;
import com.vybercoders.accounts.entity.Customer;
import com.vybercoders.accounts.exception.ResourceNotFoundException;
import com.vybercoders.accounts.mapper.AccountsMapper;
import com.vybercoders.accounts.mapper.CustomerMapper;
import com.vybercoders.accounts.repository.AccountsRepository;
import com.vybercoders.accounts.repository.CustomerRepository;
import com.vybercoders.accounts.service.ICustomersService;
import com.vybercoders.accounts.service.client.CardsFeignClient;
import com.vybercoders.accounts.service.client.LoansFeignClient;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CustomersServiceImpl implements ICustomersService {

    private AccountsRepository accountsRepository;
    private CustomerRepository customerRepository;
    private CardsFeignClient cardsFeignClient;
    private LoansFeignClient loansFeignClient;

    /**
     * @param mobileNumber - Input Mobile Number
     * @return Customer Details based on a given mobileNumber
     */
    @Override
    public CustomerDetailsDto fetchCustomerDetails(String mobileNumber) {
        Customer customer = customerRepository.findByMobileNumber(mobileNumber).orElseThrow(
                () -> new ResourceNotFoundException("Customer", "mobileNumber", mobileNumber)
        );
        Accounts accounts = accountsRepository.findByCustomerId(customer.getCustomerId()).orElseThrow(
                () -> new ResourceNotFoundException("Account", "customerId", customer.getCustomerId().toString())
        );

        CustomerDetailsDto customerDetailsDto = CustomerMapper.mapToCustomerDetailsDto(customer, new CustomerDetailsDto());
        customerDetailsDto.setAccountsDto(AccountsMapper.mapToAccountsDto(accounts, new AccountsDto()));

        ResponseEntity<LoansDto> loansDtoResponseEntity = loansFeignClient.fetchLoanDetails(mobileNumber);
        customerDetailsDto.setLoansDto(loansDtoResponseEntity.getBody());

        ResponseEntity<CardsDto> cardsDtoResponseEntity = cardsFeignClient.fetchCardDetails(mobileNumber);
        customerDetailsDto.setCardsDto(cardsDtoResponseEntity.getBody());

        return customerDetailsDto;

    }
}
