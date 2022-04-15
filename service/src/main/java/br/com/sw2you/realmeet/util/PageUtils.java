package br.com.sw2you.realmeet.util;

import static java.util.Objects.isNull;

import br.com.sw2you.realmeet.exception.InvalidOrderByFieldException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.apache.commons.lang3.StringUtils;

public final class PageUtils {

    private PageUtils() {}

    public static Pageable newPageable(
        Integer page,
        Integer limit,
        int maxLimit,
        String orderBy,
        List<String> validSortableFields
    ) {
        int definedPage = isNull(page) ? 0 : page;
        int definedLimit = isNull(limit) ? maxLimit : Math.min(limit, maxLimit);
        Sort definedSort = parseOrderByField(orderBy, validSortableFields);
        return PageRequest.of(definedPage, definedLimit, definedSort);
    }

    private static Sort parseOrderByField(String orderby, List<String> validSortableFieds) {
        if(isNull(validSortableFieds) || validSortableFieds.isEmpty()) {
            throw new IllegalArgumentException("No valid sortable fields were defined");
        }

        if(StringUtils.isBlank(orderby)) {
            return Sort.unsorted();
        }

        return Sort.by(
            Stream
            .of(orderby.split(","))
            .map(
                f -> {
                    String fieldName;
                    Sort.Order order;

                    if(f.startsWith("-")){
                        fieldName = f.substring(1);
                        order = Sort.Order.desc(fieldName);
                    } else {
                        fieldName = f;
                        order = Sort.Order.asc(fieldName);
                    }

                    if(!validSortableFieds.contains(fieldName)) {
                        throw new InvalidOrderByFieldException();
                    }

                    return order;
                }
            )
            .collect(Collectors.toList())
        );
    }
}
