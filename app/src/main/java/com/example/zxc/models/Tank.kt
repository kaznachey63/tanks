package com.zxc.models

import android.view.View
import android.widget.FrameLayout
import com.zxc.activities.CELL_SIZE
import com.zxc.activities.binding
import com.zxc.drawers.EnemyDrawer
import com.zxc.enums.Direction
import com.zxc.enums.Material
import com.zxc.utils.checkIfChanceBiggerThanRandom
import com.zxc.utils.checkViewCanMoveThroughBorder
import com.zxc.utils.getElementByCoordinates
import com.zxc.utils.getViewCoordinate
import com.zxc.utils.runOnUiThread
import kotlin.random.Random

// класс танка
class Tank (
    val element: Element,
    var direction: Direction,
    private val enemyDrawer: EnemyDrawer
) {
    // метод 
    fun move (
        direction: Direction,
        container: FrameLayout,
        elementsOnContainer: List<Element>
    ) {
        val view = container.findViewById<View>(element.viewId)?: return
        val currentCoordinate = view.getViewCoordinate()
        this.direction = direction
        view.rotation = direction.rotation
        val nextCoordinate = getTankNextCoordinate(view)

        // если не выходит за рамки И может пройти сквозь, то
        if (view.checkViewCanMoveThroughBorder(nextCoordinate) && 
            element.checkTankCanMoveThroughMaterial(nextCoordinate, elementsOnContainer)
        ) {
            emulateViewMoving(container, view)
            element.coordinate = nextCoordinate // обновление координат
            generateRandomDirectionForEnemyTank() // ранд. ход врага
        } 
        else {
            element.coordinate = currentCoordinate // возврат к базовым координатам

            // приведение к единому типу и установка отступов
            (view.layoutParams as FrameLayout.LayoutParams).topMargin = currentCoordinate.top
            (view.layoutParams as FrameLayout.LayoutParams).leftMargin = currentCoordinate.left

            changeDirectionForEnemyTank() // смена ран. хода врага
        }
    }

    // метод для рандомного движения врага
    private fun generateRandomDirectionForEnemyTank() {
        if (element.material != Material.ENEMY_TANK) return // не враг
        if (checkIfChanceBiggerThanRandom(10)) changeDirectionForEnemyTank()
    }

    // метод изменения направления движения врага
    private fun changeDirectionForEnemyTank() {
        if (element.material == Material.ENEMY_TANK) {
            val randomDirection = Direction.entries[Random.nextInt(Direction.entries.size)]
            this.direction = randomDirection // новое движение
        }
    }

    // метод иммитации движения танка
    private fun emulateViewMoving(container: FrameLayout, view: View) {
        // запуск в главном потоке (ui)
        container.runOnUiThread { // удаляет \ добавлет представление
            binding.container.removeView(view)
            binding.container.addView(view, 0) // доб. в самый нижний (0) слой
        }
    }
    
    // метод для получения новых координат танка (куда он передвинется)
    private fun getTankNextCoordinate(view: View): Coordinate {
        // получаю параметры размещения танка, чтобы изменить его позицию на экране
        // привожу к единому типу, который поймет LayoutParams
        val layoutParams = view.layoutParams as FrameLayout.LayoutParams

        when (direction) { // выбор ориентации и изменение отступов
            Direction.UP -> {
                (view.layoutParams as FrameLayout.LayoutParams).topMargin -= CELL_SIZE
            }
            Direction.DOWN -> {
                (view.layoutParams as FrameLayout.LayoutParams).topMargin += CELL_SIZE
            }
            Direction.LEFT -> {
                (view.layoutParams as FrameLayout.LayoutParams).leftMargin -= CELL_SIZE
            }
            Direction.RIGHT -> {
                (view.layoutParams as FrameLayout.LayoutParams).leftMargin += CELL_SIZE
            }
        }
        // создание новой коордианты для движения танка
        return Coordinate(layoutParams.topMargin, layoutParams.leftMargin)
    }

    // метод для проверки на проход сквощь материал
    private fun Element.checkTankCanMoveThroughMaterial(
        coordinate: Coordinate,
        elementsOnContainer: List<Element> // список всех элементов на контейнере
    ): Boolean {
        for (anyCoordinate in getElementByCoordinates(coordinate)) { // проход по всем координатам
            var element = getElementByCoordinates(anyCoordinate, elementsOnContainer) // получение элемента соотв. коорд.
            if (element == null) {
               // element = getElementByCoordinates(anyCoordinate, enemyDrawer.tanks)
            }

            // элемент существует И материал элемента не проходим для танка
            if (element != null && !element.material.tankCanGoThrough) {
                if (this == element) continue // совпало - пропускаем
                return false
            }
        }
        return true
    }

    /*private fun checkTankCanMoveThroughBorder(coordinate: Coordinate, myTank: View): Boolean {
        if(coordinate.top >= 0 &&
            coordinate.top + myTank.height < binding.container.height &&
            coordinate.left >= 0 &&
            coordinate.left + myTank.width < binding.container.width
        ) {
            return true
        }
        return false
    }*/

    // метод для получения списка координат элемента
    private fun getElementByCoordinates(topLeftCoordinate: Coordinate): List<Coordinate> {
        // список коорд. для хран. результатов
        val coordinateList = mutableListOf<Coordinate>() 

        // исх. верх. левая координата
        coordinateList.add(topLeftCoordinate) 

        // координата, ниже на одну ячейку размером одной клетки
        coordinateList.add(Coordinate(topLeftCoordinate.top + CELL_SIZE, topLeftCoordinate.left))

        // координата, находящаяся правее на одну ячейку
        coordinateList.add(Coordinate(topLeftCoordinate.top, topLeftCoordinate.left + CELL_SIZE))
        
        // диагональная координата, которая находится и ниже, и правее на одну ячейку
        coordinateList.add(
            Coordinate(
                topLeftCoordinate.top + CELL_SIZE,
                topLeftCoordinate.left + CELL_SIZE
            )
        )
        return coordinateList // список из 4-х координат
    }
}