package com.techmath.ecommerce.infrastructure.search.repositories;

import com.techmath.ecommerce.infrastructure.search.documents.ProductDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface ProductSearchRepository extends ElasticsearchRepository<ProductDocument, String> {

    Page<ProductDocument> findByNameContaining(String name, Pageable pageable);

    Page<ProductDocument> findByCategory(String category, Pageable pageable);

    Page<ProductDocument> findByPriceBetween(Double minPrice, Double maxPrice, Pageable pageable);

}
