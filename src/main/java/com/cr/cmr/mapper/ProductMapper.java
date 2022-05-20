package com.cr.cmr.mapper;

import com.cr.cmr.entity.Product;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface ProductMapper {

    Map<Integer, Product> batchQuery(List<Integer> productIds);

}
