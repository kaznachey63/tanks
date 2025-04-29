package com.zxc.models

import com.zxc.enums.Material

data class Element(
    val viewId: Int,
    val material: Material,
    val coordinate: Coordinate,
    val width: Int,
    val height: Int
) {
}