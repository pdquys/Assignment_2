package com.test.assignment_2.service;

import com.test.assignment_2.dto.checkout.ReserveResponse;
import com.test.assignment_2.entities.inventory.InventoryReservation;
import com.test.assignment_2.entities.inventory.ReservationStatus;

import java.util.UUID;

public interface InventoryReservationService {
    ReserveResponse reserveFromCart(UUID cartToken, Integer holdMinutes);
    void cancelReservation(UUID reservationToken);
    void releaseExpiredReservations();
    void releaseById(UUID reservationToken);

}
