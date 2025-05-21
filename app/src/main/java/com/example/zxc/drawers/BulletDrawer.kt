package com.zxc.drawers

import android.app.Activity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import com.zxc.activities.CELL_SIZE
import com.zxc.GameCore
import com.zxc.R
import com.zxc.sounds.MainSoundPlayer
import com.zxc.enums.Direction
import com.zxc.enums.Material
import com.zxc.models.Bullet
import com.zxc.models.Coordinate
import com.zxc.models.Element
import com.zxc.models.Tank
import com.zxc.utils.checkViewCanMoveThroughBorder
import com.zxc.utils.getElementByCoordinates
import com.zxc.utils.getTankByCoordinates
import com.zxc.utils.getViewCoordinate
import com.zxc.utils.runOnUiThread

// размеры пули
private const val BULLET_HEIGHT = 15
private const val BULLET_WIDTH = 15

// класс отрисовки пули
class BulletDrawer(
    private val container: FrameLayout,
    private val elements: MutableList<Element>,
    private val enemyDrawer: EnemyDrawer,
    private val soundManager: MainSoundPlayer,
    private val gameCore: GameCore

) {
    // вып. при создании объекта класса (после иниц. всех свойств , но до того как др. методы станут доступны)
    init {
        moveAllBullets() // перемещение всех пуль 
    }

    // список хранения всех пуль
    private val allBullets = mutableListOf<Bullet>()

    // метод для создания новой пули
    fun addNewBulletForTank(tank: Tank) {
        // находим танк (если нулл - выходим)
        val view = container.findViewById<View>(tank.element.viewId) ?: return
        if (tank.alreadyHasBullet()) return // если танк уже имеет пулю - выходим

        // создание и добавление пули
        allBullets.add(Bullet(
            createBullet(view, tank.direction),
            tank.direction, 
            tank))
        soundManager.bulletShot() // звук стрельбы
    }

    // метод для проверки наличия пули 
    private fun Tank.alreadyHasBullet(): Boolean =
        // метод для поиска пули (первой подходящей)
        allBullets.firstOrNull { it.tank == this } != null

    // метод для движения всех пуль 
    private fun moveAllBullets() {
        // создается новый поток с помощью конструктора для параллельного выполнения
        Thread( 
            Runnable {
                while (true) {
                    if (!gameCore.isPlaying()) continue // если нет игры
                    interactWithAllBullets() // взаймодействуем 
                    Thread.sleep(30) // задержка 30 м\с
                }
            }
        ).start()
    }

    // метод для обработки взаимодействия пуль
    private fun interactWithAllBullets() {
        for (bullet in allBullets.toList()) { // перебор пули из списка
            bullet.stopIntersectingBullets() // проверяем на столкновение

            // если пуля не может двигаться - останавливаем, пропускаем
            if (!bullet.canBulletGoFurther()){
                stopBullet(bullet)
                continue
            }

            val view = bullet.view
            when (bullet.direction) { // определение направления движения пули
                Direction.UP -> (view.layoutParams as FrameLayout.LayoutParams).topMargin -= BULLET_HEIGHT
                Direction.DOWN -> (view.layoutParams as FrameLayout.LayoutParams).topMargin += BULLET_HEIGHT
                Direction.LEFT -> (view.layoutParams as FrameLayout.LayoutParams).leftMargin -= BULLET_HEIGHT
                Direction.RIGHT -> (view.layoutParams as FrameLayout.LayoutParams).leftMargin += BULLET_HEIGHT
            }
            
            // проверка на столкновения с объектами в зависимости направления пули
            chooseBehaviourInTermsOfDirections(bullet)

            // обновление позиции пули
            // (Удаляем пульку со старого места и сразу же вставляем её заново)
            container.runOnUiThread { // выполнение кода в осн. потоке
                container.removeView(view) 
                container.addView(view)
            }
        }

        // удаляем пули которые закончили свою жизнь
        removeInconsistentBullets()
    }

    // метод для удаления пуль, закончивших движение
    private fun removeInconsistentBullets() {
        // оставляем только те, которые НЕ могут двигаться дальше
        val removingList = allBullets.filter { !it.canMoveFurther }

        // для каждой запускаем в UI-потоке удаление её виз. представления из контейнера
        removingList.forEach {
            container.runOnUiThread {
                container.removeView(it.view)
            }
        }

        // удаление из списка
        allBullets.removeAll(removingList)
    }

    // метод для обработки столкновения пуль
    private fun Bullet.stopIntersectingBullets() {
        // получение текущих координат пули
        val bulletCoordinate = this.view.getViewCoordinate()

        for (bulletInList in allBullets) { // перебор всех пуль со списка
            // получение координат другой пули
            val coordinateList = bulletInList.view.getViewCoordinate()

            if (this == bulletInList) continue // не является ли пуля самой собой

            // если столкновение двух пуль (совпали координаты)
            if (coordinateList == bulletCoordinate) {
                stopBullet(this) // стоп эту пулю
                stopBullet(bulletInList) // стоп пулю в которую врезалиись
                return
            }
        }
    }

    // метод для проверки продолжения движения пули
    private fun Bullet.canBulletGoFurther() =
        // не выходит ли пуля за рамки (с полученнным координатами передаем)
        // И
        // может ли пуля рподолжать движение
        this.view.checkViewCanMoveThroughBorder(this.view.getViewCoordinate()) && this.canMoveFurther

    // определяет направление движения пули
    private fun chooseBehaviourInTermsOfDirections(bullet: Bullet) {
        // напрвавление пули
        when (bullet.direction) {
            // вертикальное направление
            Direction.DOWN, Direction.UP -> {
                // получение списка координат для проверки
                compareCollections(getCoordinatesForTopOrBottomDirection(bullet), bullet)
            }

            // горизонтальное направление
            Direction.LEFT, Direction.RIGHT -> {
                compareCollections(getCoordinatesForLeftOrRightDirection(bullet), bullet)
            }
        }
    }

    // метод для обнаружения и уничтожения элемента
    private fun compareCollections(
        detectedCoordinatesList: List<Coordinate>, 
        bullet: Bullet
    ) {
        // перебор координат в списке
        for (coordinate in detectedCoordinatesList) {
            // посик танка по коордианатм
            val element = getTankByCoordinates(coordinate, enemyDrawer.tanks)
                ?: // не нашел и ищет элемент по координатам
                getElementByCoordinates(coordinate, elements) 

            // не является ли элемент самим танком
            if (element == bullet.tank.element) continue

            // удаление элемента и остановка пули 
            removeElementsAndStopBullet(element, bullet)  
        }
    }
    
    // метод для удаления элемнетов и остановки пули
    private fun removeElementsAndStopBullet(
        element: Element?, 
        bullet: Bullet
    ) {
        if (element == null) return // не нулл ли

        // не стреляет ли враг в другого врага
        if (bullet.tank.element.material == Material.ENEMY_TANK
            && element.material == Material.ENEMY_TANK) {
            stopBullet(bullet) // останавливаем пулю
            return
        }

        // пуля может проходить через материал 
        if (element.material.bulletCanGoThrough) return

        // пуля может уничтожить элемент
        if (element.material.simpleBulletCanDestroy) {
            removeView(element) // удалили представление
            removeElement(element) // удалили элемент
            stopGameIfNecessary(element) // победили игрока
            removeTank(element) // удаление танка
        }

        // останавливаем пулю
        stopBullet(bullet)
    }

    // метод удаления элемента 
    private fun removeElement(element: Element?) {
        elements.remove(element)
    }

    // метод остановки игры при уничтожении игрока \ базы
    private fun stopGameIfNecessary(element: Element) {
        // если танк игрока ИЛИ база игрока
        if (element.material == Material.PLAYER_TANK || element.material == Material.EAGLE)
            // игрока победили, передача очков
            gameCore.destroyPlayerOrBase(enemyDrawer.getPlayerScore())
    }

    // метод для удаления танка
    private fun removeTank(element: Element) {
        // создание списка элементов всех врагов
        val tanksElements = enemyDrawer.tanks.map { it.element }

        // находим индекс элемента в списке
        val tankIndex = tanksElements.indexOf(element)

        if (tankIndex < 0) return // найден ли элемент

        soundManager.bulletBurst() // звук взрыва
        enemyDrawer.removeTank(tankIndex) // удаление врага из списка
    }

    // метод остановки движения пули
    private fun stopBullet(bullet: Bullet) {
        bullet.canMoveFurther = false
    }

    // метод удаления элемента с виду
    private fun removeView(element: Element?) {
        // получение контекста контейнера и приведение его к типу Activity
        val activity = container.context as Activity
        activity.runOnUiThread { // выполнение кода в основном потоке
            // ненулл ли И затем удаление элемента с виду
            if (element != null) container.removeView(activity.findViewById(element.viewId))
        }
    }

    // метод для получения списка координат верхних углов ячейки сетки, в которой находится пуля,
    // с учемтом направления вверх / низ
    private fun getCoordinatesForTopOrBottomDirection(bullet: Bullet): List<Coordinate> {
        // текущие координаты пули
        val bulletCoordinate = bullet.view.getViewCoordinate()

        // левый край
        val leftCell = bulletCoordinate.left - bulletCoordinate.left % CELL_SIZE

        // правый край
        val rightCell = leftCell + CELL_SIZE

        // верх. коордианат
        val topCoordinate = bulletCoordinate.top - bulletCoordinate.top % CELL_SIZE

        // список из 2-х коордиант
        return listOf(
            Coordinate(topCoordinate, leftCell),
            Coordinate(topCoordinate, rightCell)
        )
    }

    // такой же как и выше
    // метод для получения списка координат верхних углов ячейки сетки, в которой находится пуля,
     // с учемтом направления лево / право
    private fun getCoordinatesForLeftOrRightDirection(bullet: Bullet): List<Coordinate> {
        val bulletCoordinate = bullet.view.getViewCoordinate()
        val topCell = bulletCoordinate.top - bulletCoordinate.top % CELL_SIZE
        val bottomCell = topCell + CELL_SIZE
        val leftCoordinate = bulletCoordinate.left - bulletCoordinate.left % CELL_SIZE
        return listOf(
            Coordinate(topCell, leftCoordinate),
            Coordinate(bottomCell, leftCoordinate)
        )
    }

    // метод создания пули
    fun createBullet(myTank: View, currentDirection: Direction): ImageView {
        return ImageView(container.context) // создание нового представления пули
            .apply { // задание свойств
                this.setImageResource(R.drawable.bullet)
                this.layoutParams = FrameLayout.LayoutParams(BULLET_WIDTH, BULLET_HEIGHT)

                // расчет коордиант появления пули
                val bulletCoordinate = getBulletCoordinates(this, myTank, currentDirection)
                (this.layoutParams as FrameLayout.LayoutParams).topMargin = bulletCoordinate.top
                (this.layoutParams as FrameLayout.LayoutParams).leftMargin = bulletCoordinate.left
                this.rotation = currentDirection.rotation
            }
    }

    // метод для вычисления нач. кооорд. пули
    private fun getBulletCoordinates(
        bullet: ImageView,
        myTank: View,
        currentDirection: Direction
    ): Coordinate {
        // получение левого и верхнего отсупа для преобраз. в коордианты
        val tankLeftTopCoordinate = Coordinate(myTank.top, myTank.left)

        // определение коордиант в зависимости от направления движения танка
        when(currentDirection) {
            Direction.UP -> {
                // вверх - выше танка
                return Coordinate(
                    // выше танка на высоту пули
                    top = tankLeftTopCoordinate.top - bullet.layoutParams.height,

                    // по центру танка по горизонтали
                    left = getDistanceToMiddleOfTanks(tankLeftTopCoordinate.left, bullet.layoutParams.width)
                )
            }

            Direction.DOWN -> {
                // вниз - под танком
                return Coordinate(
                    top = tankLeftTopCoordinate.top + myTank.layoutParams.height,
                    left = getDistanceToMiddleOfTanks(tankLeftTopCoordinate.left, bullet.layoutParams.width)
                )
            }

            Direction.LEFT -> {
                // влево - левее танка
                return Coordinate(
                    top = getDistanceToMiddleOfTanks(tankLeftTopCoordinate.top, bullet.layoutParams.height),
                    left = tankLeftTopCoordinate.left - bullet.layoutParams.width
                )
            }

            Direction.RIGHT -> {
                // вправо - правее танка
                return Coordinate(
                    top = getDistanceToMiddleOfTanks(tankLeftTopCoordinate.top, bullet.layoutParams.height),
                    left = tankLeftTopCoordinate.left + myTank.layoutParams.width
                )
            }
        }

        // не распознано, возврат исх. значений
        return tankLeftTopCoordinate
    }

    // метод для смещения пули в центр танка по нужной оси
    private fun getDistanceToMiddleOfTanks(startCoordinate: Int, bulletSize: Int): Int {
        // центр ячейки минус половина размера пули
        return startCoordinate + (CELL_SIZE - bulletSize / 2)
    }
}