package com.datavalidator

import com.fasterxml.jackson.databind.ObjectMapper
import javax.activation.UnsupportedDataTypeException

data class ValidateResult(val condition: Boolean, val path: String, val message: String)

class RuleValidator(rule: String) {
    val mapper = ObjectMapper()
    var rules: List<String> = rule.trim().split("\n")

    fun parseJson(jsonStr: String): Any {
        val json: Any = mapper.readValue(jsonStr, Any::class.java)
        return json
    }

    fun validateJson(rules: List<String>, json: Any, ruleSteps: MutableList<String>): ValidateResult {

        //@로 시작하면 type, 아니면 key
//        if(rules.size == 0) {
//            return ValidateResult(true, ruleStepsString, "")
//        }

        val rule: String = rules.get(0)
        ruleSteps.add(rule)
        var ruleStepsString = if(ruleSteps.isEmpty()) { "" } else { ruleSteps.reduce { x, y -> x + '.' + y } }

        //@가 있으면 type 없으면 key
        //시작과 끝은 @ type으로 되어야함.
        if(rule.contains("@")) {
            var typeInfo = rule.split("@")[1]
            when(typeInfo) {
                "object" -> {
                    val r = json as? Map<*, *>
                    return validateJson(rules.drop(1), r as Any, ruleSteps)
                }
                "array" -> {
                    val r = json as? ArrayList<*>
                    // TODO check keyword !!
                    // TODO array가 null, empty인 경우 하위 child validate 안함
                    if(r?.isEmpty()!!) return ValidateResult(true, ruleStepsString, "array is empty")
                    return validateJson(rules.drop(1), r.first(), ruleSteps)
                }
                "string" -> {
                    json as? String ?: return ValidateResult(false, ruleStepsString, "string type의 value가 없습니다.")
                    return ValidateResult(true, ruleStepsString, "")
                }
                "number" -> {
                    json as? Number ?: return ValidateResult(false, ruleStepsString, "number type의 value가 없습니다.")
                    return ValidateResult(true, ruleStepsString, "")
                }
                "boolean" -> {
                    json as? Boolean ?: return ValidateResult(false, ruleStepsString, "boolean type의 value가 없습니다.")
                    return ValidateResult(true, ruleStepsString, "")
                }
                else -> {
                    return ValidateResult(false, ruleStepsString, "지원하지 않는 type을 입력하셨습니다.")
                }
            }
        } else {
            when(json) {
                is Map<*, *> ->  {
                    val result = json[rule] ?: return ValidateResult(false, ruleStepsString, "키에 해당하는 값이 없습니다")
                    return validateJson(rules.drop(1), result, ruleSteps)
                }
                else -> {
                    return ValidateResult(false, ruleStepsString, "유효하지 않은 json type입니다.(key를 조회하려면 object 타입 하위에 있어야합니다.)")
                }
            }
        }
        return ValidateResult(false, ruleStepsString, "validate중 오류가 발생했습니다.")
    }

    fun validateJson(rules: List<String>, json: Any): ValidateResult {
        return validateJson(rules, json, mutableListOf())
    }

    fun validate(jsonStr: String): List<ValidateResult> {
        val json = parseJson(jsonStr)
        return rules.map { validateJson(it.split('.'), json) }
    }
}

