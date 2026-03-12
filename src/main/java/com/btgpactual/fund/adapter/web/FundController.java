package com.btgpactual.fund.adapter.web;

import com.btgpactual.fund.adapter.web.dto.FundResponse;
import com.btgpactual.fund.adapter.web.dto.SubscribeRequest;
import com.btgpactual.fund.adapter.web.dto.SubscriptionResponse;
import com.btgpactual.fund.adapter.web.dto.TransactionResponse;
import com.btgpactual.fund.application.service.SubscriptionService;
import com.btgpactual.fund.domain.model.Fund;
import com.btgpactual.fund.domain.model.Subscription;
import com.btgpactual.fund.domain.model.Transaction;
import com.btgpactual.fund.domain.repository.FundRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FundController {

    private final SubscriptionService subscriptionService;
    private final FundRepository fundRepository;

    @GetMapping("/funds/subscriptions")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<SubscriptionResponse>> listSubscriptions(
            @AuthenticationPrincipal UserDetails userDetails) {
        String userId = userDetails.getUsername();
        List<Subscription> subscriptions = subscriptionService.getSubscriptions(userId);
        return ResponseEntity.ok(subscriptions.stream().map(SubscriptionResponse::from).toList());
    }

    @GetMapping("/funds")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<FundResponse>> listFunds() {
        List<Fund> funds = fundRepository.findAll();
        return ResponseEntity.ok(funds.stream().map(FundResponse::from).toList());
    }

    @PostMapping("/funds/subscribe")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<SubscriptionResponse> subscribe(
            @Valid @RequestBody SubscribeRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String userId = userDetails.getUsername(); // we'll store userId in username for simplicity
        Subscription subscription = subscriptionService.subscribe(userId, request.fundCode());
        return ResponseEntity.status(HttpStatus.CREATED).body(SubscriptionResponse.from(subscription));
    }

    @DeleteMapping("/funds/unsubscribe/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Void> unsubscribe(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        String userId = userDetails.getUsername();
        subscriptionService.unsubscribe(userId, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/transactions/history")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<TransactionResponse>> getHistory(
            @RequestParam(defaultValue = "50") int limit,
            @AuthenticationPrincipal UserDetails userDetails) {
        String userId = userDetails.getUsername();
        List<Transaction> transactions = subscriptionService.getTransactionHistory(userId, Math.min(limit, 100));
        return ResponseEntity.ok(transactions.stream().map(TransactionResponse::from).toList());
    }
}
