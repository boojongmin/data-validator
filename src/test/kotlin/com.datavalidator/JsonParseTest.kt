package com.datavalidator

import com.datavalidator.TestUtil.readJson
import com.datavalidator.TestUtil.readRule
import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths

object TestUtil {
    val mapper = ObjectMapper()
    fun readJson(): String {
//        val chaset = Charset.forName("UTF-8")
        val path: URL = javaClass.classLoader.getResource("target.json")
        val result = Files.newBufferedReader(Paths.get(path.toURI())).use {
            it.lines().reduce("", { a, b -> a + "\n" + b })
        }
        return result.trim()
    }

    fun readRule(isWrong: Boolean = false): String {
//        val charset = Charset.forName("UTF-8")
        val fileName = if (isWrong) "_not" else ""
        val path: URL = javaClass.classLoader.getResource("validate${ fileName }.rule")
        val result = Files.newBufferedReader(Paths.get(path.toURI())).use {
            it.lines().reduce("", { a, b -> a + "\n" + b })
        }
        return result.trim()
    }

    fun parseJsonString(jsonStr: String): Any {
        return mapper.readValue(jsonStr, Any::class.java)
    }

}

class JsonParseTest {
    @Test
    fun testCreateParseJson() {
        val ruleValidator = RuleValidator(readRule())
        assertThat(ruleValidator.parseJson(readJson()) is Map<*, *>).isEqualTo(true)
        assertThat(ruleValidator.parseJson("[]") is ArrayList<*>).isEqualTo(true)
        assertThat(ruleValidator.parseJson("{}") is Map<*, *>).isEqualTo(true)
        assertThat(ruleValidator.parseJson("1234") is Number).isEqualTo(true)
        assertThat(ruleValidator.parseJson(""" "Hello World" """) is String).isEqualTo(true)
        assertThat(ruleValidator.parseJson("true") is Boolean).isEqualTo(true)
        // null은..................
//        assertThat(ruleValidator.parseJson("null")).isEqualTo(null)
    }

    @Test
    fun testValidateJson() {
        val ruleValidator = RuleValidator(readRule())
        val json = ruleValidator.parseJson(readJson())
        val allRules = readRule().split('\n')
        var i: Int = 0
        allRules.forEach {
            val rules = it.split('.')
            var result = ruleValidator.validateJson(rules, json)
            i++
            println("${i}: ${result}")
            assertThat(result.condition).isTrue()
        }
    }

    @Test
    fun testValdation() {
        val ruleValidator = RuleValidator(readRule(false))
        ruleValidator.validate(readJson()).forEach {
            println(it)
            assertThat(it.condition).isTrue()
        }
    }

    @Test
    fun testWrongValdation() {
        val ruleValidator = RuleValidator(readRule(true))
        ruleValidator.validate(readJson()).forEach {
            println(it)
            assertThat(it.condition).isFalse()
        }
    }
}

