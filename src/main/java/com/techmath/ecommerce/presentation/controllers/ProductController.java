package com.techmath.ecommerce.presentation.controllers;

import com.techmath.ecommerce.application.converters.ProductConverter;
import com.techmath.ecommerce.application.dtos.ProductDTO;
import com.techmath.ecommerce.application.services.ProductService;
import com.techmath.ecommerce.domain.entities.Product;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor(onConstructor_ = { @Autowired })
public class ProductController {

    private final ProductService service;
    private final ProductConverter converter;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public ProductDTO createProduct(@RequestBody @Valid ProductDTO productDTO) {
        var entity = converter.toEntity(productDTO);
        var product = service.createProduct(entity);
        return converter.toDTO(product);
    }

    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAllProducts(ProductDTO filters, @PageableDefault(sort = "name") Pageable pageable) {
        var entityFilters = converter.toEntity(filters);
        var page = service.getAllProducts(entityFilters, pageable);
        var content = page.map(converter::toDTO).getContent();
        var headers = mountPageableHttpHeaders(pageable, page);
        var status = page.getTotalElements() == 0 ? HttpStatus.OK : HttpStatus.PARTIAL_CONTENT;
        return new ResponseEntity<>(content, headers, status);
    }

    @GetMapping("/{id}")
    public ProductDTO getProductById(@PathVariable UUID id) {
        var product = service.getProductById(id);
        return converter.toDTO(product);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ProductDTO updateProduct(@PathVariable UUID id, @RequestBody @Valid ProductDTO productDTO) {
        var entity = converter.toEntity(productDTO);
        var updatedProduct = service.updateProduct(id, entity);
        return converter.toDTO(updatedProduct);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProduct(@PathVariable UUID id) {
        service.deleteProduct(id);
    }

    private HttpHeaders mountPageableHttpHeaders(Pageable pageable, Page<Product> result) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Current-Page", String.valueOf(pageable.getPageNumber()));
        headers.add("X-Current-Elements", String.valueOf(result.getNumberOfElements()));
        headers.add("X-Total-Count", String.valueOf(result.getTotalElements()));
        headers.add("X-Total-Pages", String.valueOf(result.getTotalPages()));
        return headers;
    }

}
