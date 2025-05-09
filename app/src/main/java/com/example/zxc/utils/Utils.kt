package com.zxc.utils

import android.app.Activity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import com.zxc.activities.CELL_SIZE
import com.zxc.activities.binding
import com.zxc.models.Coordinate
import com.zxc.models.Element
import com.zxc.models.Tank
import kotlin.random.Random

// для вероятностей (100%)
const val TOTAL_PERCENT = 100

// метод, определяющий, тможет ли перемещаться элемент, в зависимости от координат (не выходя за рамки)
fun View.checkViewCanMoveThroughBorder(coordinate: Coordinate): Boolean {
    return coordinate.top >= 0 &&
        coordinate.top + this.height <= binding.container.height &&
        coordinate.left >= 0 &&
        coordinate.left + this.width <= binding.container.width
}

// метод для поиска элементов по координатам
fun getElementByCoordinates(
    coordinate: Coordinate,
    elementsOnContainer: List<Element>
): Element? { // ? - может быть нулл
    for (element in elementsOnContainer.toList()) { // проход по всем эл. в списке + копия списка
        for (height in 0  until   element.height) { // цикл по высоте элемента
            for (width in 0 until  element.width) { // цикл по ширине элемента
                val searchingCoordinate = Coordinate( // создание координаты для текущей клетки
                    top = element.coordinate.top + height * CELL_SIZE,
                    left = element.coordinate.left + width * CELL_SIZE
                )

                // если найдено совпадение 
                if (coordinate == searchingCoordinate) return element
            }
        }
    }
    return null
}

// метод для поиска танка по координатам
fun getTankByCoordinates(coordinate: Coordinate, tankList: List<Tank>): Element? {
    // через map список танков преобразуется в список элементов
    return getElementByCoordinates(coordinate, tankList.map { it.element })
}

// метод для отрисовки элемента
fun Element.drawElement(container: FrameLayout) {
    val view = ImageView(container.context) // для отображения
    val layoutParams = FrameLayout.LayoutParams( // создание с размерами:
        this.material.width * CELL_SIZE,
        this.material.height * CELL_SIZE
    )

    this.material.image?.let { view.setImageResource(it) } // вып. если значение != нулл
    layoutParams.topMargin = this.coordinate.top
    layoutParams.leftMargin = this.coordinate.left
    view.id = this.viewId
    view.layoutParams = layoutParams // прмиенение параметров макета
    view.scaleType  = ImageView.ScaleType.FIT_XY // FIT_XY - масштабирует изображение, чтобы оно зап. все пространство
    container.runOnUiThread { container.addView(view) } // добавляет вью в главный контейнер через главный поток
}

// метод для безопасного добавления вью в контейнер
fun FrameLayout.runOnUiThread(block:() -> Unit) { // тип функции; без возвращения значений
    (this.context as Activity).runOnUiThread { block() } // приведение контекста к типу активити
}

// метод проверки шанса (%) 
fun checkIfChanceBiggerThanRandom(percentChance: Int): Boolean {
    return Random.nextInt(TOTAL_PERCENT) <= percentChance
}

// метод для получения текущих координат вью на экране 
fun View.getViewCoordinate(): Coordinate {
    return Coordinate( // приведение типов , т.к. вью находится в фрейм
        (this.layoutParams as FrameLayout.LayoutParams).topMargin,
        (this.layoutParams as FrameLayout.LayoutParams).leftMargin
    )
}