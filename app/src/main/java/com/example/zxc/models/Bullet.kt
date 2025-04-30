package com.zxc.models

import android.view.View
import com.zxc.enums.Direction

class Bullet(
    val view: View,
    val direction: Direction,
    val tank: Tank,
    var canMoveFuther: Boolean = true
) {

}
