package com.cr.cmr.service;

import com.cr.cmr.entity.MergeRequest;
import com.cr.cmr.entity.Product;
import com.cr.cmr.mapper.ProductMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ProductService {

    @Autowired
    private ProductMapper productMapper;
    private final Integer poolSize = 1;
    private LinkedBlockingQueue<MergeRequest<Integer, Product>> queue = new LinkedBlockingQueue<>(10000);

    @PostConstruct
    public void init() {
        ScheduledThreadPoolExecutor stpe = new ScheduledThreadPoolExecutor(1);
        stpe.scheduleAtFixedRate(() -> {
            int size = queue.size();
            if (size <= 0) return;
            List<MergeRequest<Integer, Product>> requests = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                requests.add(queue.poll());
            }
            log.info("request size - {}", requests.size());

            List<Integer> ids = requests.stream().map(MergeRequest::getId).collect(Collectors.toList());
            Map<Integer, Product> map = productMapper.batchQuery(ids);

            for (MergeRequest<Integer, Product> request : requests) {
                if (request != null) {
                    Product product = map.get(request.getId());
                    request.getFuture().complete(product);
                }
            }
        }, 0, 10, TimeUnit.MILLISECONDS);
    }

    /**
     * 查询商品
     *
     * @param id
     * @return
     */
    public Product queryById(Integer id) throws InterruptedException, ExecutionException, TimeoutException {
        MergeRequest<Integer, Product> request = new MergeRequest<>();
        request.setId(id);
        request.setFuture(new CompletableFuture());
        queue.add(request);

        Product product = request.getFuture().get(20, TimeUnit.MILLISECONDS);
        return product;
    }

}
