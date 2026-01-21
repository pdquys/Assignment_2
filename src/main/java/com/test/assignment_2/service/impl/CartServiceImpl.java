package com.test.assignment_2.service.impl;


import java.math.BigDecimal;
import java.util.UUID;

import com.test.assignment_2.dto.cart.CartItemResponse;
import com.test.assignment_2.dto.cart.CartResponse;
import com.test.assignment_2.dto.cart.CreateCartResponse;
import com.test.assignment_2.entities.cart.Cart;
import com.test.assignment_2.entities.cart.CartItem;
import com.test.assignment_2.exception.BadRequestException;
import com.test.assignment_2.exception.NotFoundException;
import com.test.assignment_2.repository.CartRepository;
import com.test.assignment_2.repository.ProductVariantRepository;
import com.test.assignment_2.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

  private final CartRepository cartRepository;
  private final ProductVariantRepository variantRepository;

  @Override
  @Transactional
  public CreateCartResponse createCart() {
    var token = UUID.randomUUID();
    var cart = Cart.builder().token(token).build();
    cartRepository.save(cart);
    return new CreateCartResponse(token);
  }

  @Override
  @Transactional(readOnly = true)
  public CartResponse getCart(UUID cartToken) {
    var cart = cartRepository.findByTokenWithItems(cartToken)
        .orElseThrow(() -> new NotFoundException("Cart not found"));
    return mapCart(cart);
  }

  @Override
  @Transactional
  public CartResponse addItem(UUID cartToken, UUID variantId, int quantity) {
    var cart = cartRepository.findByTokenWithItems(cartToken)
        .orElseThrow(() -> new NotFoundException("Cart not found"));

    var variant = variantRepository.findById(variantId)
        .orElseThrow(() -> new NotFoundException("Variant not found"));

    if (!variant.isActive()) throw new BadRequestException("VARIANT_INACTIVE", "Variant is not available");

    int desiredQty = quantity;
    var existing = cart.getItems().stream().filter(i -> i.getVariant().getId().equals(variantId)).findFirst();
    if (existing.isPresent()) {
      desiredQty = existing.get().getQuantity() + quantity;
      existing.get().setQuantity(desiredQty);
    } else {
      cart.addItem(CartItem.builder().variant(variant).quantity(quantity).build());
    }

    if (desiredQty > variant.availableStock()) {
      throw new BadRequestException("INSUFFICIENT_STOCK", "Not enough stock to add to cart");
    }

    cartRepository.save(cart);
    return mapCart(cart);
  }

  @Override
  @Transactional
  public CartResponse updateItem(UUID cartToken, UUID variantId, int quantity) {
    var cart = cartRepository.findByTokenWithItems(cartToken)
        .orElseThrow(() -> new NotFoundException("Cart not found"));

    var item = cart.getItems().stream()
        .filter(i -> i.getVariant().getId().equals(variantId))
        .findFirst()
        .orElseThrow(() -> new NotFoundException("Cart item not found"));

    var variant = variantRepository.findById(variantId).orElseThrow(() -> new NotFoundException("Variant not found"));

    if (quantity > variant.availableStock()) {
      throw new BadRequestException("INSUFFICIENT_STOCK", "Not enough stock to set quantity");
    }

    item.setQuantity(quantity);
    cartRepository.save(cart);
    return mapCart(cart);
  }

  @Override
  @Transactional
  public CartResponse removeItem(UUID cartToken, UUID variantId) {
    var cart = cartRepository.findByTokenWithItems(cartToken)
        .orElseThrow(() -> new NotFoundException("Cart not found"));

    cart.getItems().removeIf(i -> i.getVariant().getId().equals(variantId));
    cartRepository.save(cart);
    return mapCart(cart);
  }

  private CartResponse mapCart(Cart cart) {
    var items = cart.getItems().stream().map(i -> {
      var v = i.getVariant();
      var unit = v.getPrice();
      var line = unit.multiply(BigDecimal.valueOf(i.getQuantity()));
      return new CartItemResponse(
          v.getId(),
          v.getSkuCode(),
          v.getProduct().getName(),
          v.getColor() + " / " + v.getSize(),
          unit,
          i.getQuantity(),
          line,
          v.availableStock()
      );
    }).toList();

    BigDecimal total = items.stream().map(CartItemResponse::lineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    return new CartResponse(cart.getToken(), items, total);
  }
}
