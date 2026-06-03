package com.taobao.dto;

import lombok.Data;
import java.util.List;

@Data
public class PageResult<T> {
    private List<T> records;
    private long total;
    private long page;
    private long pages;

    public static <T> PageResult<T> of(List<T> all, int page, int size) {
        PageResult<T> r = new PageResult<>();
        long total = all.size();
        int offset = (page - 1) * size;
        int to = Math.min(offset + size, all.size());
        r.records = offset < all.size() ? all.subList(offset, to) : List.of();
        r.total = total;
        r.page = page;
        r.pages = size > 0 ? (long) Math.ceil((double) total / size) : 1;
        return r;
    }
}
