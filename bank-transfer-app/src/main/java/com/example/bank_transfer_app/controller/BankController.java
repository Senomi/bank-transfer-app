
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class BankController {

    private final BankService bankService;
    private final CustomerRepository customerRepository;
    private final BankTransactionRepository transactionRepository;

    // ==========================================
    // DASHBOARD
    // ==========================================

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

    // ==========================================
    // CREATE CUSTOMER
    // ==========================================

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

    // ==========================================
    // TRANSFER MONEY
    // ==========================================

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

    // ==========================================
    // ACCOUNT TRANSACTION HISTORY
    // ==========================================

    @GetMapping("/accounts/{accountNumber}/history")
    public String accountHistory(
            @PathVariable String accountNumber,
            Model model) {

        Customer customer = customerRepository
                .getByAccountNumber(accountNumber)
                .orElse(null);

        if (customer == null) {
            return "redirect:/";
        }

        var transactions =
                transactionRepository.findHistory(accountNumber);

        BigDecimal totalSent = transactions.stream()
                .filter(t -> accountNumber.equals(t.getFromAccount()))
                .filter(t -> t.getStatus() == TransactionStatus.SUCCESS)
                .map(t -> t.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("customer", customer);
        model.addAttribute("transactions", transactions);
        model.addAttribute("totalSent", totalSent);
        model.addAttribute("accountNumber", accountNumber);

        return "history";
    }

    // ==========================================
    // LARGE TRANSFERS
    // ==========================================

    @GetMapping("/large-transfers")
    public String largeTransfers(
            @RequestParam(defaultValue = "10000") BigDecimal min,
            Model model) {

        model.addAttribute("min", min);

        model.addAttribute("transfers",
                transactionRepository.findLargeTransfers(
                        min,
                        TransactionStatus.SUCCESS));

        return "large-transfers";
    }

    // ==========================================
    // DISPLAY DEPOSIT FORM
    // ==========================================

    @GetMapping("/deposit")
    public String showDepositForm(Model model) {

        model.addAttribute("customers",
                customerRepository.findAll());

        return "deposit";
    }

    // ==========================================
    // PROCESS DEPOSIT FORM
    // ==========================================

    @PostMapping("/deposit")
    public String deposit(
            @RequestParam String accountNumber,
            @RequestParam BigDecimal amount,
            RedirectAttributes redirectAttributes) {

        try {

            bankService.deposit(accountNumber, amount);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Money deposited successfully!");

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage());
        }

        return "redirect:/deposit";
    }

    // ==========================================
    // DISPLAY WITHDRAW FORM
    // ==========================================

    @GetMapping("/withdraw")
    public String showWithdrawForm(Model model) {

        model.addAttribute("customers",
                customerRepository.findAll());

        return "withdraw";
    }

    // ==========================================
    // PROCESS WITHDRAW FORM
    // ==========================================

    @PostMapping("/withdraw")
    public String withdraw(
            @RequestParam String accountNumber,
            @RequestParam BigDecimal amount,
            RedirectAttributes redirectAttributes) {

        try {

            bankService.withdraw(accountNumber, amount);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Money withdrawn successfully!");

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage());
        }

        return "redirect:/withdraw";
    }
}
