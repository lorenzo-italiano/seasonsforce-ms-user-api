package fr.polytech.annotation;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasAnyRole('client_candidate', 'client_recruiter') and @userService.checkUser(#id, #token)")
public @interface IsCandidateOrRecruiterAndSender {
}