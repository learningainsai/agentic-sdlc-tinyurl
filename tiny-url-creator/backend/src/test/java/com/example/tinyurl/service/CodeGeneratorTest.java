package com.example.tinyurl.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** Unit tests for {@link CodeGenerator} (TEST-001/005 support). */
class CodeGeneratorTest {

    private final CodeGenerator generator = new CodeGenerator();

    @Test
    void generatesSevenCharacterBase62Codes() {
        for (int i = 0; i < 1000; i++) {
            String code = generator.generate();
            assertThat(code).hasSize(CodeGenerator.CODE_LENGTH);
            assertThat(code.chars())
                    .allMatch(c -> CodeGenerator.ALPHABET.indexOf(c) >= 0);
        }
    }

    @Test
    void generatesVaryingCodes() {
        String first = generator.generate();
        String second = generator.generate();
        // Not a strict guarantee, but collisions across two draws are astronomically unlikely.
        assertThat(first).isNotEqualTo(second);
    }
}
