package br.com.condominio.servico.usuario.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(
    packages = "br.com.condominio",
    importOptions = ImportOption.DoNotIncludeTests.class
)
class CleanArchitectureTest {

  @ArchTest
  static final ArchRule domain_deve_ser_puro =
      noClasses()
          .that().resideInAPackage("..domain..")
          .should().dependOnClassesThat().resideInAnyPackage(
              "org.springframework..",
              "org.hibernate..",
              "jakarta..",
              "javax..",
              "com.fasterxml.jackson..",
              "..application..",
              "..adapter..",
              "..infrastructure..",
              "..config.."
          )
          .allowEmptyShould(true);

  @ArchTest
  static final ArchRule application_nao_pode_depender_de_framework =
      noClasses()
          .that().resideInAPackage("..application..")
          .should().dependOnClassesThat().resideInAnyPackage(
              "org.springframework..",
              "org.hibernate..",
              "jakarta..",
              "javax..",
              "com.fasterxml.jackson..",
              "..adapter..",
              "..infrastructure..",
              "..config.."
          )
          .allowEmptyShould(true);
}
