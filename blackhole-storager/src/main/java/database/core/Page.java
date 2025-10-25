package database.core;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 分页查询结果类
 * 封装分页查询的结果数据和相关分页信息
 * 
 * @param <T> 数据类型
 * @author Collections Team
 * @since 1.0
 */
public class Page<T> {
    
    private final List<T> content;
    private final int pageNum;
    private final int pageSize;
    private final long totalElements;
    private final int totalPages;
    private final boolean hasNext;
    private final boolean hasPrevious;
    private final boolean isFirst;
    private final boolean isLast;
    
    /**
     * 构造分页结果
     * 
     * @param content 当前页的数据内容
     * @param pageNum 当前页码（从1开始）
     * @param pageSize 每页大小
     * @param totalElements 总记录数
     */
    public Page(List<T> content, int pageNum, int pageSize, long totalElements) {
        this.content = Collections.unmodifiableList(Objects.requireNonNull(content, "Content must not be null"));
        this.pageNum = validatePageNum(pageNum);
        this.pageSize = validatePageSize(pageSize);
        this.totalElements = validateTotalElements(totalElements);
        this.totalPages = calculateTotalPages();
        this.hasNext = calculateHasNext();
        this.hasPrevious = calculateHasPrevious();
        this.isFirst = calculateIsFirst();
        this.isLast = calculateIsLast();
    }
    
    private int validatePageNum(int pageNum) {
        if (pageNum < 1) {
            throw new IllegalArgumentException("Page number must be greater than 0");
        }
        return pageNum;
    }
    
    private int validatePageSize(int pageSize) {
        if (pageSize < 1) {
            throw new IllegalArgumentException("Page size must be greater than 0");
        }
        return pageSize;
    }
    
    private long validateTotalElements(long totalElements) {
        if (totalElements < 0) {
            throw new IllegalArgumentException("Total elements must be non-negative");
        }
        return totalElements;
    }
    
    private int calculateTotalPages() {
        return (int) Math.ceil((double) totalElements / pageSize);
    }
    
    private boolean calculateHasNext() {
        return pageNum < totalPages;
    }
    
    private boolean calculateHasPrevious() {
        return pageNum > 1;
    }
    
    private boolean calculateIsFirst() {
        return pageNum == 1;
    }
    
    private boolean calculateIsLast() {
        return pageNum >= totalPages;
    }
    
    /**
     * 获取当前页的数据内容
     * 
     * @return 不可修改的数据列表
     */
    public List<T> getContent() {
        return content;
    }
    
    /**
     * 获取当前页码
     * 
     * @return 页码（从1开始）
     */
    public int getPageNum() {
        return pageNum;
    }
    
    /**
     * 获取每页大小
     * 
     * @return 每页记录数
     */
    public int getPageSize() {
        return pageSize;
    }
    
    /**
     * 获取总记录数
     * 
     * @return 总记录数
     */
    public long getTotalElements() {
        return totalElements;
    }
    
    /**
     * 获取总页数
     * 
     * @return 总页数
     */
    public int getTotalPages() {
        return totalPages;
    }
    
    /**
     * 是否有下一页
     * 
     * @return true表示有下一页
     */
    public boolean hasNext() {
        return hasNext;
    }
    
    /**
     * 是否有上一页
     * 
     * @return true表示有上一页
     */
    public boolean hasPrevious() {
        return hasPrevious;
    }
    
    /**
     * 是否为第一页
     * 
     * @return true表示是第一页
     */
    public boolean isFirst() {
        return isFirst;
    }
    
    /**
     * 是否为最后一页
     * 
     * @return true表示是最后一页
     */
    public boolean isLast() {
        return isLast;
    }
    
    /**
     * 获取当前页的实际记录数
     * 
     * @return 当前页记录数
     */
    public int getNumberOfElements() {
        return content.size();
    }
    
    /**
     * 当前页是否有内容
     * 
     * @return true表示有内容
     */
    public boolean hasContent() {
        return !content.isEmpty();
    }
    
    /**
     * 当前页是否为空
     * 
     * @return true表示为空
     */
    public boolean isEmpty() {
        return content.isEmpty();
    }
    
    @Override
    public String toString() {
        return String.format("Page {content size: %d, pageNum: %d, pageSize: %d, totalElements: %d, totalPages: %d}",
                content.size(), pageNum, pageSize, totalElements, totalPages);
    }
    
    /**
     * 创建空分页结果
     * 
     * @param <T> 数据类型
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 空分页结果
     */
    public static <T> Page<T> empty(int pageNum, int pageSize) {
        return new Page<>(Collections.emptyList(), pageNum, pageSize, 0);
    }
}