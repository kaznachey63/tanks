package com.zxc.drawers

import android.widget.FrameLayout
import com.zxc.activities.CELL_SIZE
import com.zxc.GameCore
import com.zxc.sounds.MainSoundPlayer
import com.zxc.enums.CELLS_TANKS_SIZE
import com.zxc.enums.Direction
import com.zxc.enums.Material
import com.zxc.models.Coordinate
import com.zxc.models.Element
import com.zxc.models.Tank
import com.zxc.utils.checkIfChanceBiggerThanRandom
import com.zxc.utils.drawElement
import com.zxc.enums.Material.ENEMY_TANK

// макс. кол-во врагов
private const val MAX_ENEMY_AMOUNT = 20

// класс отрисовки врагов
class EnemyDrawer(
    private val container: FrameLayout,
    private val elements: MutableList<Element>,
    private val soundManager: MainSoundPlayer,
    private val gameCore: GameCore
) {
    private val respawnList: List<Coordinate>
    private var enemyAmount = 0
    private var currentCoordinate: Coordinate
    val tanks = mutableListOf<Tank>()
    lateinit var bulletDrawer: BulletDrawer
    private var gameStarted = false
    private var enemyMurders = 0 // кол-во убитых врагов

    // вып. при создании объекта класса (после иниц. всех свойств , но до того как др. методы станут доступны)
    init {
        respawnList = getRespawnList() // список мест появления вргов
        currentCoordinate = respawnList[0] // 1-й элемент исп. для появления врагов
    }

    // метод для создания списка точек появления врагов 
    private fun getRespawnList(): List<Coordinate> {
        val respawnList = mutableListOf<Coordinate>() // список для точек

        // создается три точки для появления врагов 
        respawnList.add(Coordinate(0, 0)) // левый верхний угол
        respawnList.add( // центр верхней границы
            Coordinate(
                0,
                ((container.width - container.width % CELL_SIZE) / CELL_SIZE -
                    (container.width - container.width % CELL_SIZE) / CELL_SIZE % 2) *
                    CELL_SIZE / 2 - CELL_SIZE * CELLS_TANKS_SIZE
            )
        )
        respawnList.add( // правый верхний угол
            Coordinate(
                0,
                (container.width - container.width % CELL_SIZE) - CELL_SIZE * CELLS_TANKS_SIZE
            )
        )
        return respawnList
    }

    // метод для создания и отрисовки врага
    private fun drawEnemy() {
        // хранения индекса следующей точки возрождения
        var index = respawnList.indexOf(currentCoordinate) + 1 

        // если дошли до конца списка - обнуление
        if (index == respawnList.size) index = 0

        // обновление координаты возрождения 
        currentCoordinate = respawnList[index]

        // новый объект врага
        val enemyTank = Tank(
            Element(
                material = ENEMY_TANK,
                coordinate = currentCoordinate
            ), 
            Direction.DOWN,
            this
        )
        
        enemyTank.element.drawElement(container) // отрисовка врага 
        tanks.add(enemyTank) // добавление в список врагов
    }

    // метод для управления движением врагов
    private fun moveEnemyTanks() {
        // создается новый поток для движения врагов с помощью конструктора для параллельного выполнения
        Thread( 
            // передача объекта Runnable в конструктор потока, что позволяет потоку выполнить метод run() объекта Runnable    
            Runnable { 
                while (true) {
                    // не играется - проверяется
                    if (!gameCore.isPlaying()) continue

                    goThroughAllTanks() // перемещение всех танков 
                    Thread.sleep(400) // задержка 400 м\с
                }
            }   
        ).start() // используем метод для запуска потока
    }

    // метод который отвечает за ЖЦ врагов
    private fun goThroughAllTanks() {
        // если танки есть - звук движения
        if (tanks.isNotEmpty()) soundManager.tankMove() 
        else soundManager.tankStop() 
        
        // преобразование списка танков в неизменяемый список и перебор врагов
        tanks.toList().forEach {
            it.move(it.direction, container, elements) // перемещение

            // враг стреляет в зависимости от вероятности 
            if (checkIfChanceBiggerThanRandom(10)) bulletDrawer.addNewBulletForTank(it)
        }
    }

    // метод для создания врага
    fun startEnemyCreation() {
        if (gameStarted) return // игра начата

        gameStarted = true
        Thread( // создание нового потока для создания врагов
            Runnable {
                // пока врагам можно появляться
                while (enemyAmount < MAX_ENEMY_AMOUNT) { 
                    if (!gameCore.isPlaying()) continue

                    drawEnemy() // отрисвока врага
                    enemyAmount++ // увел. кол-во врагов
                    Thread.sleep(3000) // задержка 3 с.
                }
            }
        ).start()
        moveEnemyTanks() // перемещение врага
    }

    // метод для проверки на уничтожение всех врагов
    fun isAllTanksDestroyed(): Boolean {
        return enemyMurders == MAX_ENEMY_AMOUNT
    }

    // метод для получения очков игрока 
    fun getPlayerScore() = enemyMurders * 100

    // метод для удаления врага
    fun removeTank(tankIndex: Int) {
        tanks.removeAt(tankIndex)
        enemyMurders++ // увел. кол-во убитых врагов
        
        // если все враги убиты - победа
        if (isAllTanksDestroyed()) gameCore.playerWon(getPlayerScore())
    }
}
