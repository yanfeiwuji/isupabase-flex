package io.github.yanfeiwuji.isupabase.request.ex;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.StrUtil;
import io.github.yanfeiwuji.isupabase.request.utils.CacheTableInfoUtils;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.metadata.ConstraintDescriptor;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.internal.util.annotation.AnnotationDescriptor;

import java.util.List;
import java.util.Optional;

/**
 * @author yanfeiwuji
 * @date 2024/6/9 09:59
 */
public record ViolationInfo(List<String> groups, String name, String message, String path, Object violationValue) {
    public static ViolationInfo of(ConstraintViolation<Object> violation) {
        final ConstraintDescriptor<?> constraintDescriptor = violation.getConstraintDescriptor();
        final List<String> groups = constraintDescriptor.getGroups().stream()
                .map(Class::getSimpleName).toList();

        final String name = Optional.of(constraintDescriptor)
                .filter(ConstraintDescriptorImpl.class::isInstance)
                .map(ConstraintDescriptorImpl.class::cast)
                .map(ConstraintDescriptorImpl::getAnnotationDescriptor)
                .map(AnnotationDescriptor::getType)
                .map(Class::getSimpleName)
                .orElse(CharSequenceUtil.EMPTY);

        final String path = CacheTableInfoUtils.propertyToParamKey(violation.getPropertyPath().toString());
        return new ViolationInfo(groups, name, violation.getMessage(), path, violation.getInvalidValue());
    }
}
