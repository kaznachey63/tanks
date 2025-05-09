package com.zxc.drawers

import android.graphics.Color
import android.view.View
import android.widget.FrameLayout
import com.zxc.activities.CELL_SIZE
import com.zxc.activities.binding

// класс отрисовки сетки
class GridDrawer(private val context: FrameLayout) {
    private val allLines = mutableListOf<View>() // список линий сетки

    // метод для отрисовки сетки
    fun drawGrid() {
        val container = binding.container // получаем контейнер
        drawHorizontalLines(container) // рис. гор. линии
        drawVerticalLines(container) // рис. вер. линии
    }

    // метод для удаления сетки
    fun removeGrid() {
        val container = binding.container
        allLines.forEach { // перребор линий списка и удаление
            container.removeView(it)
        }
    }

    // метод для отрисовки линий по горизонтали
    private fun drawHorizontalLines(container: FrameLayout?) { 
        var topMargin = 0

        // пока отступ верх.края контейнера меньше высоты контейнера (!! - не нулл)
        while (topMargin <= container!!.height) {
            val horizontalLine = View(container.context) // создание линии

            // создание параметров для размещения
            val layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, 1)
            topMargin += CELL_SIZE // увелечение отступа на размер ячейки
            layoutParams.topMargin = topMargin // уст. отступа линии
            horizontalLine.layoutParams = layoutParams // прим. параметров
            horizontalLine.setBackgroundColor(Color.WHITE) // окрашивание
            allLines.add(horizontalLine) // добавление линии в список
            container.addView(horizontalLine) // добавление линии в контейнер
        }
    }

    // метод для отрисовки линий по вертикали
    private fun drawVerticalLines(container: FrameLayout?) {
        var leftMargin = 0
        while (leftMargin <= container!!.width) {
            val verticalLine = View(container.context)
            val layoutParams = FrameLayout.LayoutParams(1, FrameLayout.LayoutParams.MATCH_PARENT)
            leftMargin += CELL_SIZE
            layoutParams.leftMargin = leftMargin
            verticalLine.layoutParams = layoutParams
            verticalLine.setBackgroundColor(Color.WHITE)
            allLines.add(verticalLine)
            container.addView(verticalLine)
        }
    }
}