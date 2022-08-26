package me.liuli.fluidity.pathfinder.path

import net.minecraft.block.Block
import net.minecraft.util.BlockPos

class PathBlock(x: Int, y: Int, z: Int, val block: Block,
                     val replaceable: Boolean, val canFall: Boolean, val safe: Boolean,
                     val physical: Boolean, val liquid: Boolean, val climbable: Boolean,
                     var height: Double, val openable: Boolean) : BlockPos(x, y, z)