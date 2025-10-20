package com.techmath.ecommerce.presentation.controllers;

import com.techmath.ecommerce.application.converters.ProductConverter;
import com.techmath.ecommerce.application.services.ProductService;
import com.techmath.ecommerce.infrastructure.search.documents.ProductDocument;
import com.techmath.ecommerce.infrastructure.search.services.ProductSearchService;
import com.techmath.ecommerce.presentation.dto.ProductDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService service;
    private final ProductConverter converter;
    private final ProductSearchService searchService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public ProductDTO createProduct(@RequestBody @Valid ProductDTO productDTO) {
        var entity = converter.toEntity(productDTO);
        var product = service.createProduct(entity);
        return converter.toDTO(product);
    }

    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAllProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @PageableDefault(sort = "name") Pageable pageable
    ) {
        var modifiedPageable = createModifiedPageable(pageable);
        var page = searchService.searchProducts(name, category, minPrice, maxPrice, modifiedPageable);
        var content = page.map(doc -> new ProductDTO(
                UUID.fromString(doc.getId()),
                doc.getName(),
                doc.getDescription(),
                doc.getPrice(),
                doc.getCategory(),
                doc.getStockQuantity(),
                doc.getCreatedAt(),
                doc.getUpdatedAt()
            )
        ).getContent();

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

    private Pageable createModifiedPageable(Pageable pageable) {
        if (pageable.getSort().isSorted()) {
            var modifiedSort = Sort.unsorted();
            for (Sort.Order order : pageable.getSort()) {
                var property = order.getProperty().equals("name") ? "name.keyword" : order.getProperty();
                modifiedSort = modifiedSort.and(Sort.by(order.getDirection(), property));
            }
            return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), modifiedSort);
        }
        return pageable;
    }

    private HttpHeaders mountPageableHttpHeaders(Pageable pageable, Page<ProductDocument> result) {
        var headers = new HttpHeaders();
        headers.add("X-Current-Page", String.valueOf(pageable.getPageNumber()));
        headers.add("X-Current-Elements", String.valueOf(result.getNumberOfElements()));
        headers.add("X-Total-Count", String.valueOf(result.getTotalElements()));
        headers.add("X-Total-Pages", String.valueOf(result.getTotalPages()));
        return headers;
    }

}
