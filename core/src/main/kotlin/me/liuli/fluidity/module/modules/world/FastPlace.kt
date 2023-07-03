/*
 * This file is part of Fluidity Utility Mod.
 * Use of this source code is governed by the GPLv3 license that can be found in the LICENSE file.
 */

package me.liuli.fluidity.module.modules.world

import me.liuli.fluidity.event.Listen
import me.liuli.fluidity.event.PreMotionEvent
import me.liuli.fluidity.event.Render3DEvent
import me.liuli.fluidity.module.Module
import me.liuli.fluidity.module.ModuleCategory
import me.liuli.fluidity.util.mc
import net.minecraft.client.settings.KeyBinding
import net.minecraft.item.EnumAction
import net.minecraft.item.ItemBlock
import net.minecraft.util.BlockPos

class FastPlace : Module("FastPlace", "Make you place more fast (Only block)", ModuleCategory.WORLD) {
    fun update() {
        if (mc.gameSettings.keyBindUseItem.pressed) {
            val sideHit = mc.objectMouseOver.sideHit
            val currentItemStack = mc.thePlayer.inventory.getCurrentItem()
            val originalBlockPos = mc.objectMouseOver.blockPos ?: BlockPos(-1, -1, -1)
            val placeableBlockPos = if (mc.theWorld.getBlockState(originalBlockPos).block.isReplaceable(mc.theWorld, originalBlockPos)) originalBlockPos else originalBlockPos.offset(sideHit)

            // displayChatMessage((placeableBlockPos.distanceSqToCenter(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ)).toString())
            if (currentItemStack == null) {
                return
            } else if (currentItemStack.item is ItemBlock && mc.thePlayer.canPlayerEdit(placeableBlockPos, sideHit, currentItemStack) &&
                mc.theWorld.canBlockBePlaced((currentItemStack.item as ItemBlock).block, placeableBlockPos, false, sideHit, null, currentItemStack) &&
                !(placeableBlockPos.distanceSqToCenter(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ) <= 0.5)) {
                KeyBinding.onTick(mc.gameSettings.keyBindUseItem.keyCode)
            } else if (currentItemStack.item !is ItemBlock && currentItemStack.item.getItemUseAction(currentItemStack) == EnumAction.NONE) {
                KeyBinding.onTick(mc.gameSettings.keyBindUseItem.keyCode)
            }
        }
    }

    @Listen
    fun onRender3D(e: Render3DEvent) {
        update()
    }

    @Listen
    fun onPreMotion(e: PreMotionEvent) {
        update()
    }
}