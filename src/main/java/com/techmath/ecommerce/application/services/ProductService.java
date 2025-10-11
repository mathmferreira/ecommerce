package com.techmath.ecommerce.application.services;

import com.techmath.ecommerce.domain.entities.Product;
import com.techmath.ecommerce.domain.repositories.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor(onConstructor_ = { @Autowired })
public class ProductService {

    private final ProductRepository repository;

    @Transactional
    public Product createProduct(Product product) {
        return repository.save(product);
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
        return repository.save(product);
    }

    @Transactional
    public void deleteProduct(UUID id) {
        var product = getProductById(id);
        repository.delete(product);
    }

}
