package com.datavalidator

import com.datavalidator.TestUtil.parseJsonString
import com.datavalidator.TestUtil.readJson
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths

/**
 * Created by james.boo on 2017. 6. 9..
 */

class DataCollectorTest2 {
    var data: Any = ""

    fun readJson(): String {
//        val chaset = Charset.forName("UTF-8")
        val path: URL = javaClass.classLoader.getResource("slot/data.json")
        val result = Files.newBufferedReader(Paths.get(path.toURI())).use {
            it.lines().reduce("", { a, b -> a + "\n" + b })
        }
        return result.trim()
    }

    fun readRule(isWrong: Boolean = false): String {
        val path: URL = javaClass.classLoader.getResource("slot/validate")
        val result = Files.newBufferedReader(Paths.get(path.toURI())).use {
            it.lines().reduce("", { a, b -> a + "\n" + b })
        }
        return result.trim()
    }

    fun parseJsonString(jsonStr: String): Any {
        return TestUtil.mapper.readValue(jsonStr, Any::class.java)
    }

    @Before
    fun before() {
        var json = readJson()
        data =  parseJsonString(json)
    }


    @Test
    fun testArrayAndKeys() {
        var result = collectDataByrule("@object.top.@object.title.@string", data)
        println(result)
        result.forEach { assertThat(it.condition).isTrue() }
    }

    @Test
    fun testArray() {
        readRule().split("\n")
            .filter { !it.equals("") }
            .map { x ->
               collectDataByrule(x, data)
            }.map {
                println(it)
                it
            }.forEach {
                it.forEach {
                    assertThat(it.condition).isTrue()
                }
            }
//        var result = collectDataByrule("@object.top.@object.title.@string", data)
//        println(result)
//        result.forEach { assertThat(it.condition).isTrue() }
    }
}
