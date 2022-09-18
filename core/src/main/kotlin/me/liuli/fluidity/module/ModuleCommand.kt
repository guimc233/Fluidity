package me.liuli.fluidity.module

import me.liuli.fluidity.command.Command
import me.liuli.fluidity.module.value.*
import me.liuli.fluidity.util.render.colorToHexString
import me.liuli.fluidity.util.world.getBlockName
import net.minecraft.block.Block
import java.awt.Color

/**
 * Module command
 * @author SenkJu
 */
class ModuleCommand(private val module: Module, private val values: List<Value<*>>) : Command(module.name, module.description) {

    /**
     * Execute commands with provided [args]
     */
    override fun exec(args: Array<String>) {
        val valueNames = values
            .joinToString(separator = "/") { it.name.lowercase() }

        if (args.isEmpty()) {
            chatSyntax(if (values.size == 1) "$valueNames <value>" else "<$valueNames>")
            return
        }

        val value = module.getValue(args[0])

        if (value == null) {
            chatSyntax("<$valueNames>")
            return
        }

        if (args.size < 2) {
            if (value is ColorValue) {
                chatSyntax("${args[0].lowercase()} <color> (now=${colorToHexString(value.get())})")
            } else if (value is BlockValue) {
                chatSyntax("${args[0].lowercase()} <block> (now=${getBlockName(value.get())})")
            } else if (value is IntValue || value is FloatValue || value is StringValue || value is BoolValue) {
                chatSyntax("${args[0].lowercase()} <value> (now=${value.get()})")
            } else if (value is ListValue) {
                chatSyntax("${args[0].lowercase()} <${value.values.joinToString(separator = "/").lowercase()}> (now=${value.get()})")
            }
            return
        }

        try {
            when (value) {
                is ColorValue -> {
                    try {
                        val color = if (args[1].startsWith("#")) {
                            args[1] = args[1].substring(1)
                            if (args[1].length == 6) {
                                Color(args[1].substring(0, 2).toInt(16), args[1].substring(2, 4).toInt(16), args[1].substring(4, 6).toInt(16)).rgb
                            } else if (args[1].length == 8) {
                                Color(args[1].substring(0, 2).toInt(16), args[1].substring(2, 4).toInt(16), args[1].substring(4, 6).toInt(16), args[1].substring(6, 8).toInt(16)).rgb
                            } else {
                                throw IllegalArgumentException("Invalid color format")
                            }
                        } else {
                            Integer.decode(args[1])
                        }
                        value.set(color)
                        chat("${module.name} ${args[0].lowercase()} was set to ${colorToHexString(color)}.")
                    } catch (e: Exception) {
                        e.printStackTrace()
                        chat("Invalid color (${e.message})")
                    }
                    return
                }
                is BlockValue -> {
                    var id: Int

                    try {
                        id = args[1].toInt()
                    } catch (exception: NumberFormatException) {
                        id = Block.getIdFromBlock(Block.getBlockFromName(args[1]))

                        if (id <= 0) {
                            chat("Block ${args[1]} does not exist!")
                            return
                        }
                    }

                    value.set(id)
                    chat("${module.name} ${args[0].lowercase()} was set to ${getBlockName(id)}.")
                    return
                }
                is IntValue -> value.set(args[1].toInt())
                is FloatValue -> value.set(args[1].toFloat())
                is BoolValue -> {
                    when (args[1].lowercase()) {
                        "on", "true" -> value.set(true)
                        "off", "false" -> value.set(false)
                        "!", "rev", "reverse" -> value.set(!value.get())
                        else -> value.set(!value.get())
                    }
                }
                is ListValue -> {
                    if (!value.contains(args[1])) {
                        chatSyntax("${args[0].lowercase()} <${value.values.joinToString(separator = "/").lowercase()}>")
                        return
                    }

                    value.set(args[1])
                }
                is StringValue -> value.set(args.copyOfRange(2, args.size - 1).joinToString(separator = " "))
            }

            chat("${module.name} ${args[0]} was set to ${value.get()}.")
        } catch (e: NumberFormatException) {
            chat("${args[1]} cannot be converted to number!")
        }
    }

    override fun tabComplete(args: Array<String>): List<String> {
        if (args.isEmpty()) return emptyList()

        return when (args.size) {
            1 -> values
                .filter { it.name.startsWith(args[0], true) }
                .map { it.name.lowercase() }
            2 -> {
                when (module.getValue(args[0])) {
                    is BlockValue -> {
                        return Block.blockRegistry.keys
                            .map { it.resourcePath.lowercase() }
                            .filter { it.startsWith(args[1], true) }
                    }
                    is ListValue -> {
                        values.forEach { value ->
                            if (!value.name.equals(args[0], true)) {
                                return@forEach
                            }
                            if (value is ListValue) {
                                return value.values.filter { it.startsWith(args[1], true) }
                            }
                        }
                        return emptyList()
                    }
                    else -> emptyList()
                }
            }
            else -> emptyList()
        }
    }
}