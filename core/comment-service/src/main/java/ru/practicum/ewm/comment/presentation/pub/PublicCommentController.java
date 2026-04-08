package ru.practicum.ewm.comment.presentation.pub;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.api.client.comment.dto.CommentDto;
import ru.practicum.ewm.comment.application.CommentService;
import ru.practicum.ewm.comment.application.params.PublicCommentParameters;

@RestController
@RequestMapping("/events/{eventId}/comments")
@RequiredArgsConstructor
@Validated
@Slf4j
public class PublicCommentController {

    private final CommentService commentService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<CommentDto> getCommentsForEventId(
            @PathVariable @Positive Long eventId,
            @RequestParam(name = "from", defaultValue = "0") @PositiveOrZero int from,
            @RequestParam(name = "size", defaultValue = "10") @Positive int size,
            @Pattern(regexp = "^(createdOn),(ASC|DESC)$",
                      message = "Параметр sort должен иметь формат createdOn,ASC|DESC")
            @RequestParam(defaultValue = "createdOn,DESC") String sort) {
        log.info("Public: Received request to get list comments for eventId:" +
                        " {}, parameters: from: {}, size: {}, sort: {}", eventId, from, size, sort);
        Sort sortingRule;
        if (sort != null && sort.equalsIgnoreCase("createdOn,ASC")) {
            sortingRule = Sort.by(Sort.Direction.ASC, "createdOn");
        } else {
            sortingRule = Sort.by(Sort.Direction.DESC, "createdOn");
        }
        PublicCommentParameters parameters = PublicCommentParameters.builder()
                .from(from)
                .size(size)
                .sort(sortingRule)
                .build();
        List<CommentDto> result = commentService.getCommentsForEvent(eventId, parameters);
        log.info("Public: Got list comments for eventId: {}, parameters: from: {}, size: {}, sort: {}",
                eventId, from, size, sort);
        return result;
    }

}
