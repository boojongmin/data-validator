package com.datavalidator

data class CollectResult( val condition: Boolean, val rule: String, val ruleStep: String, val message: String, val result: Any?)

fun collectDataByrule(rule: String, data: Any): List<CollectResult> {
    return collectData(rule.split("."), data)
}

fun collectData(splitedRule: List<String>, data: Any): List<CollectResult> {
    if(splitedRule.size == 0 || splitedRule[0] == "") {
        return TC.createResult(false, splitedRule, 0, "rules is empty", null)
    }

    fun collect(rules: List<String>, index: Int, data: Any?): List<CollectResult> {
        fun collectIf(rules: List<String>, index: Int, data: Any?): List<CollectResult> {
            val isLastIndex = rules.size == (index +1)
            if(isLastIndex) {
                return TC.createResult(true, rules, index, "", data)
            } else {
                val nextIndex = index + 1
                when(data) {
                    //TODO @array 여기서 한번 더 체크하는게 맘에 안드는데.. 일단 보류..
                    is ArrayList<*> -> {
                        if("@array" == rules[nextIndex]) {
                            if(rules.size == (nextIndex + 1)) {
                                return TC.createResult(true, rules, nextIndex, "", data)
                            }
                            return data.map { collect(rules, (nextIndex + 1), it) }.flatten()
                        } else {
                            return TC.createResult(false, rules, nextIndex, "array type이 아닙니다.", data)
                        }
                    }
                    else -> {
                        return collect(rules, index + 1, data)
                    }
                }
            }
        }

        val r = rules[index]
        data ?: return TC.createResult(false, rules, index, "데이터 탐색중 NULL이 발생했습니다.", data)

        if(TC.isType(r)) {
            val typeName = r.split("@")[1]
            if(TC.isContainerType(typeName)) {
                when(typeName) {
                    "object" -> {
                        val r = data as? Map<*, *>
                        r ?: return TC.createResult(false, rules, index, "object 타입이 아닙니다", data)
                        return collectIf(rules, index, data)
                    }
                    "array" -> {
                        val r = data as? ArrayList<*>
                        r ?: return TC.createResult(false, rules, index, "array 타입이 아닙니다", data)
                        if(r.isEmpty()) return collectIf(rules, index, data)
                        if(r.isEmpty()) return TC.createResult(true, rules, index, "array is empty", data)
                        return collectIf(rules, index, data)
                    }
                    else -> {
                        return TC.createResult(false, rules, index, "invalid rule type", data)
                    }
                }
            } else if(TC.isValueType(typeName)) {
                when(typeName) {
                    "string" -> {
                        data as? String ?: return TC.createResult(false, rules, index, "string이 아닙니다.", data)
                        return TC.createResult(true, rules, index, "", data)
                    }
                    "number" -> {
                        data as? Number ?: return TC.createResult(false, rules, index, "number가 아닙니다.", data)
                    }
                    "boolean" -> {
                        data as? Boolean ?: return TC.createResult(false, rules, index, "boolean이 아닙니다.", data)
                    }
                    else -> {
                        return TC.createResult(false, rules, index, "invalid rule type", null)
                    }
                }
                return TC.createResult(true, rules, index, "", data)
            } else {
                return TC.createResult(false, rules, index, "타입정보에 문제가 있습니다.", null)
            }
        } else if(TC.isKey(r)) {
            when(data) {
                is Map<*, *> -> {
                    return collectIf(rules, index, data.get(r))
                }
                else -> {
                    return TC.createResult(false, rules, index, "object가 아닙니다.", null)
                }
            }
        } else {
            return TC.createResult(false, rules, index, "등록하신 rule이 올바르지 않습니다.", null)
        }
    }

    return collect(splitedRule, 0, data)
}

object TC {
    fun isKey(rule: String): Boolean {
        return !rule.startsWith("@")
    }

    fun isType(typeInfo: String): Boolean {
        if(!typeInfo.startsWith("@")) {
            return false
        }
        val typeName = typeInfo.split("@")[1]
        if(TC.isContainerType(typeName) || TC.isValueType(typeName)) {
            return true
        }
        return false
    }

    fun  isContainerType(typeName: String): Boolean {
        when(typeName) {
            "object", "array" -> {return true}
            else -> {return false}
        }
    }

    fun  isValueType(typeName: String): Boolean {
        when(typeName) {
            "number", "string", "boolean" -> {return true}
            else -> {return false}
        }
    }

    fun  createResult(condition: Boolean, rules: List<String>, index: Int, message: String, result: Any?): List<CollectResult> {
        var reduceFunc: (String, String) -> String = { x, y -> x + "." + y }

        val ruleStr = rules.reduce(reduceFunc)
        val ruleStepStr = rules.take(index + 1).reduce(reduceFunc)

        return listOf(CollectResult(condition, ruleStr, ruleStepStr, message, result))

    }

}