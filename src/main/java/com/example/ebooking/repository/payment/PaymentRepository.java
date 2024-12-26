package com.example.ebooking.repository.payment;

import com.example.ebooking.model.Payment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findBySessionId(String sessionId);

    Optional<List<Payment>> findAllByStatus(Payment.PaymentStatus status);

    @Transactional
    @Modifying
    @Query("UPDATE Payment p SET p.status = :status WHERE p.id IN :paymentId")
    void updateStatus(@Param("paymentId") Long paymentId,
                              @Param("status") Payment.PaymentStatus status);

    Optional<List<Payment>> findByBookingUserId(Long userId);

    boolean existsByBookingUserIdAndStatus(Long userId,
                                           Payment.PaymentStatus status);
}
