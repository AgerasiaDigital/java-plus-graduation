package ru.practicum.event.util;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class OffsetBasedPageRequest implements Pageable {
    private final int limit;
    private final int offset;
    private final Sort sort;

    public OffsetBasedPageRequest(int offset, int limit, Sort sort) {
        if (offset < 0) throw new IllegalArgumentException("The offset must be greater 0");
        if (limit < 1) throw new IllegalArgumentException("The number of elements in the set must be greater 1");
        this.limit = limit;
        this.offset = offset;
        this.sort = sort == null ? Sort.unsorted() : sort;
    }

    @Override public int getPageNumber() { return offset / limit; }
    @Override public int getPageSize() { return limit; }
    @Override public long getOffset() { return offset; }
    @Override public Sort getSort() { return sort; }
    @Override public Pageable next() { return new OffsetBasedPageRequest((int) getOffset() + getPageSize(), getPageSize(), getSort()); }
    @Override public Pageable previousOrFirst() { int o = offset - limit; return new OffsetBasedPageRequest(Math.max(o, 0), limit, sort); }
    @Override public Pageable first() { return new OffsetBasedPageRequest(0, limit, sort); }
    @Override public boolean hasPrevious() { return offset > limit; }
    @Override public Pageable withPage(int pageNumber) { return new OffsetBasedPageRequest(pageNumber * limit, limit, sort); }
}
