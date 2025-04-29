package com.zxc.models

import android.view.View
import com.zxc.enums.Material

data class Element constructor(
    val viewId: Int = View.generateViewId(),
    val material: Material,
    val coordinate: Coordinate,
    val width: Int,
    val height: Int
) {
}