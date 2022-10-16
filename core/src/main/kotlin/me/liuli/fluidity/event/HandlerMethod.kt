/*
 * This file is part of Fluidity Utility Mod.
 * Use of this source code is governed by the GPLv3 license that can be found in the LICENSE file.
 */

package me.liuli.fluidity.event

import java.lang.reflect.Method

class HandlerMethod(private val method: Method, private val listener: Listener) : Handler {
    init {
        if (!method.isAccessible) {
            method.isAccessible = true
        }
    }

    override val target = method.parameterTypes[0] as Class<out Event>

    override fun invoke(event: Event) {
        if (listener.listen()) {
            try {
                method.invoke(listener, event)
            } catch (t: Throwable) {
                Exception("An error occurred while handling the event: ", t).printStackTrace()
            }
        }
    }
}

interface Listener {
    fun listen(): Boolean
}

annotation class Listen()