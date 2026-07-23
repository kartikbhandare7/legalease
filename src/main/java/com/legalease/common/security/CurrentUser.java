package com.legalease.common.security;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import java.lang.annotation.*;

// Extracts the UUID from the authenticated principal
// Usage: @CurrentUser UUID lawyerId in any controller method
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@AuthenticationPrincipal(expression = "#this == 'anonymousUser' " +
        "? null : @userPrincipalResolver.resolve(#this)")
public @interface CurrentUser {}
