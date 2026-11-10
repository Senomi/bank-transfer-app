package com.example.bank_transfer_app.controller;

import java.math.BigDecimal;

import com.example.bank_transfer_app.dto.TransferRequest;
import com.example.bank_transfer_app.model.Customer;
import com.example.bank_transfer_app.model.TransactionStatus;
import com.example.bank_transfer_app.repository.BankTransactionRepository;
import com.example.bank_transfer_app.repository.CustomerRepository;
import com.example.bank_transfer_app.service.BankService;
import com.example.bank_transfer_app.service.TransactionAuditService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class BankController {

    private final BankService bankService;
    private final TransactionAuditService auditService;
    private final CustomerRepository customerRepository;
    private final BankTransactionRepository transactionRepository;

    // Dashboard data
    private void populateDashboard(Model model) {

        model.addAttribute("customers",
                customerRepository.findAllOrdered());

        model.addAttribute("allCustomers",
                customerRepository.findAllOrdered());

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
                transactionRepository.countByStatus(TransactionStatus.SUCCESS));

        model.addAttribute("failedCount",
                transactionRepository.countByStatus(TransactionStatus.FAILED));
    }

    // Dashboard
    @GetMapping({"", "/"})
    public String dashboard(
            @RequestParam(required = false) String keyword,
            Model model) {

        populateDashboard(model);

        if (keyword != null && !keyword.trim().isEmpty()) {

            model.addAttribute("customers",
                    customerRepository.searchByName(keyword.trim()));

            model.addAttribute("keyword", keyword.trim());
        }

        return "bank";
    }

    @GetMapping("/bank")
    public String bankAlias() {
        return "redirect:/";
    }

    // Add customer
    @PostMapping("/customers")
    public String addCustomer(
            @Valid @ModelAttribute("customer") Customer customer,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (result.hasErrors()) {

            populateDashboard(model);

            model.addAttribute("customer", customer);

            return "bank";
        }

        try {

            bankService.createCustomer(customer);

            redirectAttributes.addFlashAttribute(
                    "success", "Customer saved successfully!");

        } catch (RuntimeException e) {

            redirectAttributes.addFlashAttribute(
                    "error", messageFor(e));
        }

        return "redirect:/";
    }

    // Transfer money
    @PostMapping("/transfer")
    public String transfer(
            @Valid @ModelAttribute("transferRequest") TransferRequest transferRequest,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (result.hasErrors()) {

            populateDashboard(model);

            model.addAttribute("transferRequest", transferRequest);

            return "bank";
        }

        try {

            bankService.transfer(
                    transferRequest.getFromAccount(),
                    transferRequest.getToAccount(),
                    transferRequest.getAmount(),
                    transferRequest.isSimulateFailure()
            );

            redirectAttributes.addFlashAttribute(
                    "success", "Transfer completed successfully!");

        } catch (RuntimeException e) {

            // Record failure separately after the transfer transaction rolls back
            auditService.recordFailure(
                    transferRequest.getFromAccount(),
                    transferRequest.getToAccount(),
                    transferRequest.getAmount(),
                    messageFor(e)
            );

            redirectAttributes.addFlashAttribute(
                    "error", "Transfer failed: " + messageFor(e));
        }

        return "redirect:/";
    }

    // Deposit form
    @GetMapping("/deposit")
    public String showDepositForm(Model model) {

        model.addAttribute("customers",
                customerRepository.findAllOrdered());

        return "deposit";
    }

    // Deposit money
    @PostMapping("/deposit")
    public String deposit(
            @RequestParam String accountNumber,
            @RequestParam BigDecimal amount,
            RedirectAttributes redirectAttributes) {

        try {

            bankService.deposit(accountNumber, amount);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Successfully deposited LKR " + amount
                            + " to account " + accountNumber);

        } catch (RuntimeException e) {

            redirectAttributes.addFlashAttribute(
                    "error", messageFor(e));
        }

        return "redirect:/deposit";
    }

    // Withdrawal form
    @GetMapping("/withdraw")
    public String showWithdrawForm(Model model) {

        model.addAttribute("customers",
                customerRepository.findAllOrdered());

        return "withdraw";
    }

    // Withdraw money
    @PostMapping("/withdraw")
    public String withdraw(
            @RequestParam String accountNumber,
            @RequestParam BigDecimal amount,
            RedirectAttributes redirectAttributes) {

        try {

            bankService.withdraw(accountNumber, amount);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Successfully withdrew LKR " + amount
                            + " from account " + accountNumber);

        } catch (RuntimeException e) {

            auditService.recordFailure(
                    accountNumber,
                    null,
                    amount,
                    messageFor(e)
            );

            redirectAttributes.addFlashAttribute(
                    "error", messageFor(e));
        }

        return "redirect:/withdraw";
    }

    // Account transaction history
    @GetMapping("/accounts/{no}/history")
    public String accountHistory(
            @PathVariable("no") String accountNumber,
            Model model,
            RedirectAttributes redirectAttributes) {

        Customer customer = customerRepository
                .getByAccountNumber(accountNumber)
                .orElse(null);

        if (customer == null) {
            redirectAttributes.addFlashAttribute(
                    "error", "Account " + accountNumber + " was not found");
            return "redirect:/";
        }

        model.addAttribute("customer", customer);

        model.addAttribute("transactions",
                transactionRepository.findHistory(accountNumber));

        model.addAttribute("totalSent",
                transactionRepository.getTotalSentByAccount(
                        accountNumber, TransactionStatus.SUCCESS));

        return "history";
    }

    // Large transfers
    @GetMapping("/large-transfers")
    public String largeTransfers(
            @RequestParam(defaultValue = "10000.00") BigDecimal min,
            Model model) {

        BigDecimal minimum = min == null ? new BigDecimal("10000.00") : min;
        if (minimum.signum() < 0) {
            minimum = BigDecimal.ZERO;
        }

        model.addAttribute("min", minimum);

        model.addAttribute("transfers",
                        transactionRepository.findLargeTransfers(
                                minimum, TransactionStatus.SUCCESS));

        return "large-transfers";
    }

    private String messageFor(RuntimeException exception) {
        return exception.getMessage() == null
                ? "An unexpected error occurred"
                : exception.getMessage();
    }
}