package com.example.clivoapi;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.assertj.core.api.Assertions.assertThat;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import java.util.List;
import org.junit.jupiter.api.Test;

class ArchitectureTest {

    private static final String ROOT = "com.example.clivoapi";

    private static final String COMMON = ROOT + ".common..";

    private static final String CONFIGURATION = ROOT + ".configuration..";

    private static final String CORE = ROOT + ".core..";

    private static final String MODULES = ROOT + ".modules..";

    private static final String PATTERNS = ROOT + ".patterns..";

    private final JavaClasses production = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages(ROOT);

    @Test
    void theCoreDoesNotKnowTheModules() {
        noClasses()
                .that()
                .resideInAPackage(CORE)
                .should()
                .dependOnClassesThat()
                .resideInAPackage(MODULES)
                .because("a module the clinic did not contract must leave no trace in the core")
                .check(production);
    }

    @Test
    void theCoreReachesConfigurationAndPatternsThroughCommonOnly() {
        noClasses()
                .that()
                .resideInAPackage(CORE)
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(CONFIGURATION, PATTERNS)
                .because("the core declares ports in common/extension and lets Spring inject the implementations")
                .check(production);
    }

    @Test
    void noTwoViewsClaimTheSameSchemaName() {
        assertThat(viewNames())
                .as("springdoc names a schema after the simple class name and keeps one definition per "
                        + "name, so two views sharing one silently document each other's endpoint")
                .doesNotHaveDuplicates();
    }

    @Test
    void theSharedPortsDependOnNoOneElse() {
        noClasses()
                .that()
                .resideInAPackage(COMMON)
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(CONFIGURATION, CORE, MODULES, PATTERNS)
                .because("everyone depends on common, so common may depend on nobody")
                .check(production);
    }

    private List<String> viewNames() {
        return production.stream()
                .map(JavaClass::getSimpleName)
                .filter(name -> name.endsWith("View"))
                .sorted()
                .toList();
    }
}
