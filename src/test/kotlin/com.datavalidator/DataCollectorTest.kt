package com.datavalidator

import com.datavalidator.TestUtil.parseJsonString
import com.datavalidator.TestUtil.readJson
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

/**
 * Created by james.boo on 2017. 6. 9..
 */

class DataCollectorTest {
    var data: Any = "";

    @Before
    fun before() {
        var json = readJson()
        data =  parseJsonString(json)
    }

    @Test
    fun testEmptyRule() {
        var result = collectDataByrule("", data)
        assertThat(result[0].condition).isFalse()
    }

    @Test
    fun testStrKey() {
        var result = collectDataByrule("@object.str.@string", data)
        println(result.first())
        assertThat(result[0].condition).isTrue()
    }

    @Test
    fun testArray() {
        var result = collectDataByrule("@object.arr.@array.@string", data)
        println(result)
        result.forEach { assertThat(it.condition).isTrue() }
    }

    @Test
    fun testArrayAndKeys() {
        var result = collectDataByrule("@object.obj.@object.arr.@array.@object.key.@string", data)
        println(result)
        result.forEach { assertThat(it.condition).isTrue() }
    }
}
