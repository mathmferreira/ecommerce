package com.techmath.ecommerce.infrastructure.search.services;

import com.techmath.ecommerce.domain.entities.Product;
import com.techmath.ecommerce.infrastructure.search.documents.ProductDocument;
import com.techmath.ecommerce.infrastructure.search.repositories.ProductSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductSearchService {

    private final ProductSearchRepository repository;
    private final ElasticsearchOperations elasticsearchOperations;

    public void syncProduct(Product product) {
        try {
            ProductDocument document = toDocument(product);
            repository.save(document);
            ensureElasticsearchIndexExists();
            log.info("Product {} synced to Elasticsearch", product.getId());
        } catch (Exception e) {
            log.error("Failed to sync product with id {}: {}", product.getId(), e.getMessage());
        }
    }

    public Page<ProductDocument> searchProducts(
            String name,
            String category,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Pageable pageable
    ) {
        try {
            if (StringUtils.isNotBlank(name)) {
                return repository.findByNameContaining(name, pageable);
            }

            if (StringUtils.isNotBlank(category)) {
                return repository.findByCategory(category, pageable);
            }

            if (Objects.nonNull(minPrice) && Objects.nonNull(maxPrice)) {
                return repository.findByPriceBetween(minPrice.doubleValue(), maxPrice.doubleValue(), pageable);
            }

            return repository.findAll(pageable);

        } catch (Exception e) {
            log.error("Error searching products: {}", e.getMessage());
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
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

    private void ensureElasticsearchIndexExists() {
        log.info("Checking Elasticsearch 'products' index...");
        var indexOps = elasticsearchOperations.indexOps(ProductDocument.class);

        if (!indexOps.exists()) {
            log.info("Creating 'products' index in Elasticsearch...");

            var settings = Document.create();
            settings.put("index.number_of_replicas", 0);
            settings.put("index.number_of_shards", 1);

            indexOps.create(settings);
            indexOps.putMapping(indexOps.createMapping());
            log.info("Index 'products' created successfully (0 replicas for single-node)");
        }
    }

}
