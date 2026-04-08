package ru.practicum.ewm.comment.application.params;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.data.domain.Sort;

@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode
@AllArgsConstructor
public class PublicCommentParameters {
    private final int from;
    private final int size;
    private final Sort sort;
}
