package com.techmath.ecommerce.infrastructure.search.services;

import com.techmath.ecommerce.domain.entities.Product;
import com.techmath.ecommerce.infrastructure.search.documents.ProductDocument;
import com.techmath.ecommerce.infrastructure.search.repositories.ProductSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductSearchSyncService {

    private final ProductSearchRepository repository;

    public void syncProduct(Product product) {
        try {
            ProductDocument document = toDocument(product);
            repository.save(document);
            log.info("Product {} synced to Elasticsearch", product.getId());
        } catch (Exception e) {
            log.error("Failed to sync product with id {}: {}", product.getId(), e.getMessage());
        }
    }

    public void deleteProduct(UUID productId) {
        try {
            repository.deleteById(productId.toString());
            log.info("Product {} deleted from Elasticsearch", productId);
        } catch (Exception e) {
            log.error("Error deleting product {} from Elasticsearch: {}", productId, e.getMessage());
        }
    }

    private ProductDocument toDocument(Product product) {
        return new ProductDocument(
                product.getId().toString(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getCategory(),
                product.getStockQuantity(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }

}
