package com.gaoxing.jira.utils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections.CollectionUtils;

import java.util.Collection;

/**
 * Created by gaoxing on 2017/7/10.
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(value = {"valid"})
public class Response<T> {

    /**
     * status
     */
    private ResponseStatus status;

    /**
     * message
     */
    private String message;

    /**
     * data
     */
    private T data;

    /**
     * Constructor
     */
    public Response(ResponseStatus status, String message) {
        this(status, message, null);
    }

    /**
     * Constructor
     */
    public Response(ResponseStatus status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    /**
     * 此响应是否有效
     */
    public boolean isValid() {
        return isValid(false);
    }

    /**
     * 此响应是否有效
     */
    public boolean isValid(boolean allowEmpty) {
        if (status != ResponseStatus.SUCCESS) {
            return false;
        }

        if (allowEmpty) {
            return true;
        }

        if (data instanceof Collection) {
            return CollectionUtils.isNotEmpty((Collection) data);
        } else {
            return data != null;
        }
    }
}