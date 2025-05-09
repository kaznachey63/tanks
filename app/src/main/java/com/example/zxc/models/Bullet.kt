package com.zxc.models

import android.view.View
import com.zxc.enums.Direction

// класс представляет пулю в игре
class Bullet (
    val view: View,
    val direction: Direction,
    val tank: Tank, // ссылка на танк
    var canMoveFurther: Boolean = true // может ли пуля продолжать движение
) { }
