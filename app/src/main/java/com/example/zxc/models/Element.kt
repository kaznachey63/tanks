package com.zxc.models

import android.view.View
import com.zxc.enums.Material

data class Element(
    val viewId: Int = View.generateViewId(),
    val material: Material,
    var coordinate: Coordinate,
    val width: Int = material.width,
    val height: Int = material.height
) {
}