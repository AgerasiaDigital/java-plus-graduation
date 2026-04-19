package ru.practicum.ewm.stats.client.aop;

import com.google.protobuf.Timestamp;
import java.lang.reflect.Method;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import ru.practicum.ewm.stats.client.CollectorClient;
import ru.practicum.ewm.stats.grpc.ActionTypeProto;
import ru.practicum.ewm.stats.grpc.UserActionProto;

@Aspect
@RequiredArgsConstructor
@Slf4j
public class UserActionAspect {

    private final CollectorClient collectorClient;
    private final ExpressionParser expressionParser = new SpelExpressionParser();
    private final ParameterNameDiscoverer parameterNameDiscoverer;

    @AfterReturning(pointcut = "@annotation(logUserAction)", argNames = "joinPoint,logUserAction")
    public void logAction(JoinPoint joinPoint, LogUserAction logUserAction) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            log.warn("Cannot log user action: not in an HTTP request context.");
            return;
        }

        String userIdStr = attributes.getRequest().getHeader("X-EWM-USER-ID");
        if (userIdStr == null || userIdStr.isBlank()) {
            log.warn("Cannot log user action: X-EWM-USER-ID header is missing.");
            return;
        }
        long userId = Long.parseLong(userIdStr);

        Long eventId = evaluateSpelExpression(joinPoint, logUserAction.eventId());
        if (eventId == null) {
            log.error("Could not evaluate SpEL expression '{}' to find eventId.", logUserAction.eventId());
            return;
        }

        ActionTypeProto protoType = mapToActionTypeProto(logUserAction.value());

        Instant now = Instant.now();
        Timestamp timestamp = Timestamp.newBuilder().setSeconds(now.getEpochSecond()).setNanos(now.getNano()).build();

        UserActionProto userAction = UserActionProto.newBuilder()
            .setUserId(userId)
            .setEventId(eventId)
            .setActionType(protoType)
            .setTimestamp(timestamp)
            .build();

        collectorClient.collectUserAction(userAction);
        log.debug("Successfully logged user action: {}", userAction);
    }

    private Long evaluateSpelExpression(JoinPoint joinPoint, String spelExpression) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String[] parameterNames = parameterNameDiscoverer.getParameterNames(method);
        Object[] args = joinPoint.getArgs();

        EvaluationContext context = new StandardEvaluationContext();
        for (int i = 0; i < args.length; i++) {
            if (parameterNames != null) {
                context.setVariable(parameterNames[i], args[i]);
            }
        }

        try {
            Expression expression = expressionParser.parseExpression(spelExpression);
            return expression.getValue(context, Long.class);
        } catch (Exception e) {
            log.error("Failed to evaluate SpEL expression '{}'", spelExpression, e);
            return null;
        }
    }

    private ActionTypeProto mapToActionTypeProto(ActionType nativeType) {
        try {
            return ActionTypeProto.valueOf("ACTION_" + nativeType.name());
        } catch (IllegalArgumentException e) {
            log.error("Unknown ActionType '{}'. Mapping to UNRECOGNIZED.", nativeType);
            return ActionTypeProto.UNRECOGNIZED;
        }
    }
}