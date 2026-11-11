
package com.example.bank_transfer_app.controller;

import java.math.BigDecimal;

import com.example.bank_transfer_app.dto.TransferRequest;
import com.example.bank_transfer_app.model.Customer;
import com.example.bank_transfer_app.model.TransactionStatus;
import com.example.bank_transfer_app.repository.BankTransactionRepository;
import com.example.bank_transfer_app.repository.CustomerRepository;
import com.example.bank_transfer_app.service.BankService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class BankController {

    private final BankService bankService;

    private final CustomerRepository customerRepository;

    private final BankTransactionRepository transactionRepository;

    // Display dashboard
    @GetMapping({"", "/"})
    public String dashboard(Model model) {

        model.addAttribute("customers",
                customerRepository.findAll());

        model.addAttribute("allCustomers",
                customerRepository.findAll());

        model.addAttribute("transferRequest",
                new TransferRequest());

        model.addAttribute("customer",
                new Customer());

        model.addAttribute("transactions",
                transactionRepository.findAllLatest());

        model.addAttribute("totalCustomers",
                customerRepository.countCustomersNative());

        model.addAttribute("totalBalance",
                customerRepository.getTotalBalance());

        model.addAttribute("successCount",
                transactionRepository.countByStatus(
                        TransactionStatus.SUCCESS));

        model.addAttribute("failedCount",
                transactionRepository.countByStatus(
                        TransactionStatus.FAILED));

        return "bank";
    }

    // Create a new customer
    @PostMapping("/customers")
    public String createCustomer(
            @ModelAttribute Customer customer,
            RedirectAttributes redirectAttributes) {

        try {
            bankService.createCustomer(customer);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Customer created successfully!");

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage());
        }

        return "redirect:/";
    }

    // Transfer money between accounts
    @PostMapping("/transfer")
    public String transfer(
            @ModelAttribute TransferRequest request,
            RedirectAttributes redirectAttributes) {

        try {
            bankService.transfer(
                    request.getFromAccount(),
                    request.getToAccount(),
                    request.getAmount(),
                    request.isSimulateFailure()
            );

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Transfer completed successfully!");

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage());
        }

        return "redirect:/";
    }

    // View account transaction history
    @GetMapping("/accounts/{accountNumber}/history")
    public String accountHistory(
            @PathVariable String accountNumber,
            Model model) {

        model.addAttribute("accountNumber", accountNumber);

        model.addAttribute("transactions",
                transactionRepository.findHistory(accountNumber));

        return "history";
    }
}
