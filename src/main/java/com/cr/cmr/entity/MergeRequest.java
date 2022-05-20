package com.cr.cmr.entity;

import lombok.Data;

import java.util.concurrent.CompletableFuture;

@Data
public class MergeRequest<T, R> {

    private T id;
    private CompletableFuture<R> future;

}
