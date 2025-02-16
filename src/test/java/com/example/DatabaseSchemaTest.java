package com.example;

import jakarta.persistence.EntityManagerFactory;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.tool.schema.internal.ExceptionHandlerHaltImpl;
import org.hibernate.tool.schema.spi.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest
class DatabaseSchemaTest {

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Test
    void validateDatabaseSchema() {
        try {
            // ✅ FIX: Unwrap the correct Hibernate implementation
            SessionFactoryImplementor sessionFactoryImplementor = entityManagerFactory
                    .unwrap(SessionFactoryImplementor.class);

            // ✅ Build Hibernate service registry
            StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                    .applySettings(sessionFactoryImplementor.getProperties())
                    .build();

            // ✅ Extract metadata from Hibernate
            Metadata metadata = new MetadataSources(serviceRegistry).buildMetadata();

            // ✅ Get Schema Management Tool
            SchemaManagementTool schemaManagementTool = serviceRegistry.getService(SchemaManagementTool.class);
            SchemaValidator schemaValidator = schemaManagementTool.getSchemaValidator(Collections.emptyMap());

            // ✅ Define Execution Options
            ExecutionOptions executionOptions = new ExecutionOptions() {
                @Override
                public boolean shouldManageNamespaces() {
                    return true;
                }

                @Override
                public Map<String, Object> getConfigurationValues() {
                    return Collections.emptyMap();
                }

                @Override
                public ExceptionHandler getExceptionHandler() {
                    return ExceptionHandlerHaltImpl.INSTANCE;
                }
            };

            // ✅ Perform Validation
            schemaValidator.doValidation(metadata, executionOptions, ContributableMatcher.ALL);

        } catch (Exception e) {
            fail("Database schema does not match entity models: " + e.getMessage());
        }
    }
}
