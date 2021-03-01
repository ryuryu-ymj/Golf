package io.github.ryuryu_ymj.golf.play

/**
 * 地面の種類を表す列挙型
 * @param linearDamping ボールがその地面に触れているときに受ける線形速度減衰
 */
enum class GroundType(val linearDamping: Float) {
    FAIRWAY(8f), GREEN(1f), BUNKER(8f)
}