package me.liuli.fluidity.pathfinder.goals

import me.liuli.fluidity.pathfinder.path.PathMove
import net.minecraft.util.Vec3i
import kotlin.math.abs

/**
 * Goal is a Y coordinate
 */
open class GoalY(val y: Int) : IGoal {

    override fun heuristic(node: PathMove): Double {
        return abs(this.y - node.y).toDouble()
    }

    override fun isEnd(pos: Vec3i): Boolean {
        return pos.y == this.y
    }

    override fun hasChanged() = false

    override fun isValid() = true
}