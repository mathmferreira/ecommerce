package com.techmath.ecommerce.infrastructure.search.repositories;

import com.techmath.ecommerce.infrastructure.search.documents.ProductDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductSearchRepository extends ElasticsearchRepository<ProductDocument, String> {
}
