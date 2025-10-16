package com.techmath.ecommerce.application.services;

import com.techmath.ecommerce.domain.entities.Order;
import com.techmath.ecommerce.domain.entities.Product;
import com.techmath.ecommerce.domain.exceptions.InsufficientStockException;
import com.techmath.ecommerce.domain.repositories.ProductRepository;
import com.techmath.ecommerce.infrastructure.search.services.ProductSearchSyncService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository repository;
    private final ProductSearchSyncService searchSyncService;

    @Transactional
    public Product createProduct(Product product) {
        return saveAndSync(product);
    }

    public Page<Product> getAllProducts(Product filters, Pageable pageable) {
        var example = Example.of(filters, ExampleMatcher.matchingAll().withIgnoreNullValues().withIgnoreCase()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING));
        return repository.findAll(example, pageable);
    }

    public Product getProductById(UUID id) {
        return repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));
    }

    @Transactional
    public Product updateProduct(UUID id, Product toUpdate) {
        var product = getProductById(id);
        BeanUtils.copyProperties(toUpdate, product, "id");
        return saveAndSync(product);
    }

    @Transactional
    public Product updateProductStock(Product product, int quantity) {
        if (product.hasStock(quantity)) {
            product.decreaseStock(quantity);
        } else {
            throw new InsufficientStockException("Insufficient stock for product: " + product.getName());
        }
        return saveAndSync(product);
    }

    @Transactional
    public void deleteProduct(UUID id) {
        var product = getProductById(id);
        repository.delete(product);
    }

    public void handleInsufficientStock(Order order) {
        var message = order.getItems().stream()
                .filter(item -> item.getQuantity() > item.getProduct().getStockQuantity())
                .map(item -> "[Product:" + item.getProduct().getName()
                        + ", Available: " + item.getProduct().getStockQuantity()
                        + ", Requested: " + item.getQuantity() + "]")
                .collect(Collectors.joining("\n"));
        throw new InsufficientStockException(message);
    }

    private Product saveAndSync(Product product) {
        product = repository.save(product);
        searchSyncService.syncProduct(product);
        return product;
    }

}
