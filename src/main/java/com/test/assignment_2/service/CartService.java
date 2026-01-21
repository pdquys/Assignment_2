package com.test.assignment_2.service;

import com.test.assignment_2.dto.cart.CartResponse;
import com.test.assignment_2.dto.cart.CreateCartResponse;
import com.test.assignment_2.entities.cart.Cart;

import java.util.UUID;

public interface CartService {

    CreateCartResponse createCart();
    CartResponse getCart(UUID cartToken);
    CartResponse addItem(UUID cartToken, UUID variantId, int quantity);
    CartResponse updateItem(UUID cartToken, UUID variantId, int quantity);
    CartResponse removeItem(UUID cartToken, UUID variantId);

}
