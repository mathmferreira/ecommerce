package com.techmath.ecommerce.infrastructure.search.services;

import com.techmath.ecommerce.domain.entities.Product;
import com.techmath.ecommerce.infrastructure.search.documents.ProductDocument;
import com.techmath.ecommerce.infrastructure.search.repositories.ProductSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
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
        List<Criteria> criteriaList = new ArrayList<>();
        criteriaList.add(Criteria.where("stockQuantity").greaterThan(0));

        if (StringUtils.isNotBlank(name)) {
            criteriaList.add(Criteria.where("name").fuzzy(name));
        }

        if (StringUtils.isNotBlank(category)) {
            criteriaList.add(Criteria.where("category").is(category));
        }

        priceFilter(minPrice, maxPrice, criteriaList);

        Criteria criteria = new Criteria();
        if (!criteriaList.isEmpty()) {
            criteriaList.forEach(criteria::and);
        }

        Query query = new CriteriaQuery(criteria).setPageable(pageable);

        SearchHits<ProductDocument> searchHits = elasticsearchOperations.search(query, ProductDocument.class);
        List<ProductDocument> products = searchHits.getSearchHits()
                .stream()
                .map(SearchHit::getContent)
                .toList();

        return PageableExecutionUtils.getPage(products, pageable, searchHits::getTotalHits);
    }

    public Page<ProductDocument> searchByName(String name, Pageable pageable) {
        return searchProducts(name, null, null, null, pageable);
    }

    public Page<ProductDocument> searchByCategory(String category, Pageable pageable) {
        return searchProducts(null, category, null, null, pageable);
    }

    public Page<ProductDocument> searchByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        return searchProducts(null, null, minPrice, maxPrice, pageable);
    }

    private void priceFilter(BigDecimal minPrice, BigDecimal maxPrice, List<Criteria> criteriaList) {
        if (Objects.nonNull(minPrice) && Objects.nonNull(maxPrice)) {
            criteriaList.add(Criteria.where("price").between(minPrice, maxPrice));
        } else if (Objects.nonNull(minPrice) ) {
            criteriaList.add(Criteria.where("price").greaterThanEqual(minPrice));
        } else if (Objects.nonNull(maxPrice)) {
            criteriaList.add(Criteria.where("price").lessThanEqual(maxPrice));
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
