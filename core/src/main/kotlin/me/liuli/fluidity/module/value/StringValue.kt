package me.liuli.fluidity.module.value

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive

/**
 * Text value represents a value with a string
 */
open class StringValue(name: String, value: String) : Value<String>(name, value) {
    override fun toJson() = JsonPrimitive(value)

    override fun fromJson(element: JsonElement) {
        if (element.isJsonPrimitive) {
            value = element.asString
        }
    }
}