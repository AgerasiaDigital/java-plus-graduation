package ru.practicum.ewm.user.application.params;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@Builder
@EqualsAndHashCode
@AllArgsConstructor
public class GetListUsersParameters {
    private final List<Long> ids;
    private final int from;
    private final int size;
}
