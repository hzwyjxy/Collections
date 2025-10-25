package database.core;

/**
 * 存储异常类
 * 统一封装所有数据库操作相关的异常
 * 
 * @author Collections Team
 * @since 1.0
 */
public class StorageException extends Exception {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 错误代码
     */
    private final String errorCode;
    
    /**
     * 错误参数
     */
    private final Object[] params;
    
    /**
     * 默认错误代码
     */
    private static final String DEFAULT_ERROR_CODE = "STORAGE_ERROR";
    
    /**
     * 构造存储异常
     * 
     * @param message 错误信息
     */
    public StorageException(String message) {
        super(message);
        this.errorCode = DEFAULT_ERROR_CODE;
        this.params = new Object[0];
    }
    
    /**
     * 构造存储异常
     * 
     * @param message 错误信息
     * @param cause 原始异常
     */
    public StorageException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = DEFAULT_ERROR_CODE;
        this.params = new Object[0];
    }
    
    /**
     * 构造存储异常
     * 
     * @param errorCode 错误代码
     * @param message 错误信息
     * @param params 错误参数
     */
    public StorageException(String errorCode, String message, Object... params) {
        super(message);
        this.errorCode = errorCode != null ? errorCode : DEFAULT_ERROR_CODE;
        this.params = params != null ? params : new Object[0];
    }
    
    /**
     * 构造存储异常
     * 
     * @param errorCode 错误代码
     * @param message 错误信息
     * @param cause 原始异常
     * @param params 错误参数
     */
    public StorageException(String errorCode, String message, Throwable cause, Object... params) {
        super(message, cause);
        this.errorCode = errorCode != null ? errorCode : DEFAULT_ERROR_CODE;
        this.params = params != null ? params : new Object[0];
    }
    
    /**
     * 获取错误代码
     * 
     * @return 错误代码
     */
    public String getErrorCode() {
        return errorCode;
    }
    
    /**
     * 获取错误参数
     * 
     * @return 错误参数数组
     */
    public Object[] getParams() {
        return params.clone();
    }
    
    /**
     * 获取格式化的错误信息
     * 
     * @return 包含错误代码和参数的格式化信息
     */
    public String getFormattedMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(errorCode).append("] ");
        sb.append(getMessage());
        
        if (params.length > 0) {
            sb.append(" - Params: ");
            for (int i = 0; i < params.length; i++) {
                if (i > 0) sb.append(", ");
                sb.append(params[i]);
            }
        }
        
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return getFormattedMessage();
    }
}