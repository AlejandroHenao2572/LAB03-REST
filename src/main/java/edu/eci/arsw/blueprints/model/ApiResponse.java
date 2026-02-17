package edu.eci.arsw.blueprints.model;

public record ApiResponse<T>(int code,String message, T data) {   
    //éxito
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(200, message, data);
    }

    //creación
    public static <T> ApiResponse<T> created(String message, T data) {
        return new ApiResponse<>(201, message, data);
    }

    //actulizacion
    public static <T> ApiResponse<T> updated(String message, T data) {
        return new ApiResponse<>(202, message, data);
    }
    //error 
    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }
}