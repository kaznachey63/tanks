package com.zxc.enums

import com.zxc.R

// размеры объектов (в клетках)
const val CELLS_SIMPLE_ELEMENT = 1
const val CELLS_EAGLE_WIDTH = 4
const val CELLS_EAGLE_HEIGHT = 3
const val CELLS_TANKS_SIZE = 2

enum class Material(
    val tankCanGoThrough: Boolean, // может ли танк проходить 
    val bulletCanGoThrough: Boolean, // может ли пуля проходить 
    val simpleBulletCanDestroy: Boolean, // может ли пуля разрушить 
    val elementsAmountOnScreen: Int, // кол-во элементов на экране
    val width: Int,
    val height: Int,
    val image: Int? // ? - может быть нулл
) {
    // пустота
    EMPTY(
        true,
        true,
        true,
        0,
        0,
        0,
        null,
    ),

    // кирпич
    BRICK(
        false,
        false,
        true,
        0,
        CELLS_SIMPLE_ELEMENT,
        CELLS_SIMPLE_ELEMENT,
        R.drawable.brick,
    ),

    // трава
    CONCRETE(
        false,
        false,
        false,
        0,
        CELLS_SIMPLE_ELEMENT,
        CELLS_SIMPLE_ELEMENT,
        R.drawable.concrete,
    ),

    // трава
    GRASS(
        true,
        true,
        true,
        0,
        CELLS_SIMPLE_ELEMENT,
        CELLS_SIMPLE_ELEMENT,
        R.drawable.grass,
    ),

    // орел
    EAGLE(
        false,
        false,
        true,
        1,
        CELLS_EAGLE_WIDTH,
        CELLS_EAGLE_HEIGHT,
        R.drawable.eagle,
    ),

    // точка восстановления врага
    ENEMY_TANK_RESPAWN(
        true,
        true,
        false,
        3,
        CELLS_TANKS_SIZE,
        CELLS_TANKS_SIZE,
        R.drawable.enemy_tank,
    ),

    // точка восстановления игрока
    PLAYER_TANK_RESPAWN(
        true,
        true,
        false,
        1,
        CELLS_TANKS_SIZE,
        CELLS_TANKS_SIZE,
        R.drawable.tank,
    ),

    // враг
    ENEMY_TANK(
        false,
        false,
        true,
        0,
        CELLS_TANKS_SIZE,
        CELLS_TANKS_SIZE,
        R.drawable.enemy_tank
    ),

    // игрок
    PLAYER_TANK(
        false,
        false,
        true,
        1,
        CELLS_TANKS_SIZE,
        CELLS_TANKS_SIZE,
        R.drawable.tank
    )
}