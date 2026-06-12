package tech.doshikawa.carerecord.common.config;

import java.util.Set;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.relational.core.mapping.event.BeforeSaveCallback;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;

/**
 * Spring Data JDBC用 バリデーション設定
 * Repositoryのsave()メソッドが呼ばれた際、DB保存前に自動的にバリデーションを実行する。
 */
@Configuration
public class ValidationConfig {

    @Bean
    public BeforeSaveCallback<Object> validateBeforeSave(Validator validator) {
        return (entity, aggregateChange) -> {
            // エンティティのアノテーション検証を実行
            Set<ConstraintViolation<Object>> violations = validator.validate(entity);
            
            if (!violations.isEmpty()) {
                // 違反がある場合は例外をスローしてトランザクションをロールバックさせる
                throw new ConstraintViolationException(violations);
            }
            
            return entity;
        };
    }
}