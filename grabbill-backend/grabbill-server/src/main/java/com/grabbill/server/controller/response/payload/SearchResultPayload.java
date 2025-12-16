package com.grabbill.server.controller.response.payload;

import com.grabbill.server.controller.response.ApiPayload;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author michaellow
 */
@Data
@Builder
public class SearchResultPayload<T> implements ApiPayload {

    private List<T> items;

    private Long totalItems;

    private Integer totalPages;

}
