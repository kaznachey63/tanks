package com.zxc.drawers

import android.view.View
import android.widget.FrameLayout
import com.zxc.activities.CELL_SIZE
import com.zxc.enums.Material
import com.zxc.models.Coordinate
import com.zxc.models.Element
import com.zxc.utils.drawElement
import com.zxc.utils.getElementByCoordinates

// класс отрисовки элементов
class ElementsDrawer(val container: FrameLayout) {
    var currentMaterial = Material.EMPTY // по умолчанию
    val elementsOnContainer = mutableListOf<Element>()

    // мтеод обработки нажатие на сетку
    fun onTouchContainer(x:Float, y:Float) {
        val topMargin = y.toInt() - (y.toInt() % CELL_SIZE)
        val leftMargin = x.toInt() - (x.toInt() % CELL_SIZE)
        val coordinate = Coordinate(topMargin, leftMargin)

        // стираем если ничего
        if (currentMaterial == Material.EMPTY) eraseView(coordinate)
        else drawOrReplaceView(coordinate)
    }

    // метод отрисовки или замены элемента
    private fun drawOrReplaceView(coordinate: Coordinate) {
        val viewOnCoordinate = getElementByCoordinates(coordinate, elementsOnContainer)
        
        // если элемента на координате нет, то соаздется
        if (viewOnCoordinate == null) {
            createElementDrawView(coordinate)
            return
        }

        // если материал есть, и он отличается, то замена
        if (viewOnCoordinate.material != currentMaterial) replaceView(coordinate)
    }

    // метод для добавления материалов в список элементов
    fun drawElementsList(elements: List<Element>?) {
        // рисовать нечего
        if (elements == null) return 

        // рисует каждый элемент из списка 
        for (element in elements) {
            currentMaterial = element.material
            drawElement(element)
        }
        currentMaterial = Material.EMPTY
    }

    // метод для удаления предыдущего элемента, и добавления нового
    private fun replaceView(coordinate: Coordinate) {
        eraseView(coordinate)
        createElementDrawView(coordinate)
    }

    // метод для удаления элемента c координат
    private fun eraseView(coordinate: Coordinate) {
        // сначала удаляется 1-й элемент с координат
        removeElement(getElementByCoordinates(coordinate, elementsOnContainer))

        // затем метод возвращает все элементы, у которых координаты совпадают с ячейками, занимаемыми текущим материалом
        for (element in getElementsUnderCurrentCoordinate(coordinate)) {
            removeElement(element) // удаляет элемент
        }
    }

    // метод для удаления элемнта из контейнера
    private fun removeElement(element: Element?) {
        if (element != null) {

            // нахдим по ади кого удалить
            val erasingView = container.findViewById<View>(element.viewId)
            container.removeView(erasingView) // удаляется с экрана
            elementsOnContainer.remove(element) // удаляется со списка
        }
    }

    // метод, который возвращает все элементы, попадающие в область, занимаемую материалом
    private fun getElementsUnderCurrentCoordinate(coordinate: Coordinate): List<Element> {
        val elements = mutableListOf<Element>() // для найденных элементов
        for (element in elementsOnContainer) { // перебор по каждому элементу списка
            // вложенные циклы по координатам
            for (height in 0 until currentMaterial.height) {
                for (width in 0 until currentMaterial.width) {
                    if (element.coordinate == Coordinate(
                        coordinate.top + height * CELL_SIZE,
                        coordinate.left + width * CELL_SIZE)
                    ) elements.add(element) // добавляем элемент в список
                }
            }
        }
        return elements
    }

    // метод для удаляения лишнего элемента текущего материала
    private fun removeUnwantedInstance() {
        // есть ли ограничение на кол-во элементов материала
        if (currentMaterial.elementsAmountOnScreen != 0) {
            // фильтр элементов у которых материал совпадает с текущим
            val erasingElements = elementsOnContainer.filter { it.material == currentMaterial }

            // если элеметов стало больше, чем нужно, то удаляется самый первый (старый) элемент материала по его координате 
            if (erasingElements.size >= currentMaterial.elementsAmountOnScreen) eraseView(erasingElements[0].coordinate)
        }
    }

    // метод для отрисовки элемента
    private fun drawElement(element: Element) {
        removeUnwantedInstance() // можно добавить ?
        element.drawElement(container) // рисуется элемент на экране
        elementsOnContainer.add(element) // добавляется элемент в список
    }

    // метод, который создает элемент для дальнейшей отрисовки 
    private fun createElementDrawView(coordinate: Coordinate) {
        val element = Element( // новый объект
            material = currentMaterial,
            coordinate = coordinate,
        )
        drawElement(element) // отрисовка элемента
    }
}