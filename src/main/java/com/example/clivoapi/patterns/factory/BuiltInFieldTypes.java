package com.example.clivoapi.patterns.factory;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class BuiltInFieldTypes {

    @Bean("LONG_TEXT")
    FieldFactory longText(@Qualifier(TextFieldFactory.SHORT_TEXT) FieldFactory text) {
        return text;
    }

    @Bean("DECIMAL")
    FieldFactory decimal(@Qualifier(NumberFieldFactory.INTEGER) FieldFactory number) {
        return number;
    }

    @Bean("SCALE")
    FieldFactory scale(@Qualifier(NumberFieldFactory.INTEGER) FieldFactory number) {
        return number;
    }
}
