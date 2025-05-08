package com.zxc.activities

import android.app.Activity
import android.content.Intent 
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.zxc.databinding.ActivityMainBinding
import android.view.KeyEvent
import android.view.KeyEvent.KEYCODE_DPAD_UP
import android.view.KeyEvent.KEYCODE_DPAD_DOWN
import android.view.KeyEvent.KEYCODE_DPAD_LEFT
import android.view.KeyEvent.KEYCODE_DPAD_RIGHT
import android.view.KeyEvent.KEYCODE_SPACE
import android.view.Menu
import android.view.MenuItem
import android.view.View.GONE
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.core.content.ContextCompat
import com.zxc.GameCore
import com.zxc.LevelStorage
import com.zxc.R
import com.zxc.drawers.BulletDrawer
import com.zxc.drawers.ElementsDrawer
import com.zxc.drawers.EnemyDrawer
import com.zxc.drawers.GridDrawer
import com.zxc.enums.Direction
import com.zxc.enums.Direction.DOWN
import com.zxc.enums.Direction.LEFT
import com.zxc.enums.Direction.RIGHT
import com.zxc.enums.Direction.UP
import com.zxc.enums.Material
import com.zxc.models.Coordinate
import com.zxc.models.Element
import com.zxc.models.Tank
import com.zxc.sounds.MainSoundPlayer
import com.example.zxc.utils.ProgressIndicator

const val CELL_SIZE = 50

lateinit var binding: ActivityMainBinding

class MainActivity : AppCompatActivity(), ProgressIndicator {
    private var editMode = false // режим редактирования
    private lateinit var item: MenuItem

    private lateinit var playerTank: Tank
    private lateinit var eagle: Element

    private var gameStarted = false // начало игры

    // отвечает за отрисовку и управление пуль в игре
    private val bulletDrawer by lazy {
        BulletDrawer(
            binding.container,
            elementsDrawer.elementsOnContainer, // для определения столкновений
            enemyDrawer,
            soundManager,
            gameCore
        )
    }

    // объект базы игры
    private val gameCore by lazy {
        GameCore(this)
    }

    // объект менеджера звуков
    private val soundManager by lazy {
        MainSoundPlayer(this, this)
    }

    // объект для отрисовки сетки
    private val gridDrawer by lazy {
        GridDrawer(binding.container)
    }

    // объект для отрисовк игровых элементов
    private val elementsDrawer by lazy {
        ElementsDrawer(binding.container)
    }

    // объект для загрузки и сохранения уровней
    private val levelStorage by lazy {
        LevelStorage (this)
    }

    // объект для отрисовки и управления врагами
    private val enemyDrawer by lazy {
        EnemyDrawer(binding.container, elementsDrawer.elementsOnContainer, soundManager, gameCore)
    }

    // метод для создания танка
    private fun createTank(elementWidth: Int, elementHeight: Int): Tank {
        playerTank = Tank( // new
            Element(
                material = Material.PLAYER_TANK,
                coordinate = getPlayerTankCoordinate(elementWidth, elementHeight)
            ), UP,
            enemyDrawer
        )
        return playerTank
    }

    // метод для создания врага
    private fun createEagle(elementWidth: Int, elementHeight: Int): Element {
        eagle = Element(
            material = Material.EAGLE,
            coordinate = getEagleCoordinate(elementWidth, elementHeight)
        )
        return eagle
    }

    // метод рассчитывает координаты для размещения танка 
    private fun getPlayerTankCoordinate(width: Int, height: Int) = Coordinate(
        top = (height - height % 2)
                - (height - height % 2) % CELL_SIZE
                - Material.PLAYER_TANK.height * CELL_SIZE,
        left = (width - width % (2 * CELL_SIZE)) / 2
                - Material.EAGLE.width / 2 * CELL_SIZE
                - Material.PLAYER_TANK.width * CELL_SIZE
    )

    // метод рассчитывает координаты для размещения орла 
    private fun getEagleCoordinate(width: Int, height: Int) = Coordinate(
        top = (height - height % 2)
                - (height - height % 2) % CELL_SIZE
                - Material.EAGLE.height * CELL_SIZE,
        left = (width - width % (2 * CELL_SIZE)) / 2
                - Material.EAGLE.width / 2 * CELL_SIZE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Menu"

        // обработчики кнопок
        binding.editorClear.setOnClickListener { elementsDrawer.currentMaterial = Material.EMPTY }
        binding.editorBrick.setOnClickListener { elementsDrawer.currentMaterial = Material.BRICK }
        binding.editorConcrete.setOnClickListener { elementsDrawer.currentMaterial = Material.CONCRETE }
        binding.editorGrass.setOnClickListener { elementsDrawer.currentMaterial = Material.GRASS }

        // лямбда-функция для обработки нажатий если редакт = вкл
        binding.container.setOnTouchListener { _, event ->
            if (editMode) elementsDrawer.onTouchContainer(event.x, event.y)
            return@setOnTouchListener true
        }

        // отображение уровня
        elementsDrawer.drawElementsList(levelStorage.loadLevel())
        hideSettings()
        countWidthHeight()
    }

    // метод который рассчитывает размеры контейнера и создает игровые объекты
    private fun countWidthHeight() {
        val frameLayout = binding.container
        frameLayout.viewTreeObserver // отслеживание изменений в размерах контейнера
            .addOnGlobalLayoutListener(object : OnGlobalLayoutListener { 
                override fun onGlobalLayout() { // все элементы размещены (сработал слушатель)
                    frameLayout.viewTreeObserver.removeOnGlobalLayoutListener(this) // удаление слушателя
                    val elementWidth = frameLayout.width
                    val elementHeight = frameLayout.height

                    playerTank = createTank(elementWidth, elementHeight)
                    eagle = createEagle(elementWidth, elementHeight)

                    // добавление танка и орла в список элемнетов
                    elementsDrawer.drawElementsList(listOf(playerTank.element, eagle))
                    
                    // передача объекта bulletDrawer для вражеской стрельбы
                    enemyDrawer.bulletDrawer = bulletDrawer
                }
            })
    }

    // метод для переключения режим редактирования
    private fun switchEditMode() {
        editMode = !editMode
        if (editMode) showSettings()
        else hideSettings()
    }

    // метод для открытия меню
    private fun showSettings() {
        gridDrawer.drawGrid() // рисуется сетка
        binding.materialsContainer.visibility = VISIBLE // отобр. контейнер с мат-ами
    }

    // метод для скрытия меню
    private fun hideSettings() {
        gridDrawer.removeGrid()
        binding.materialsContainer.visibility = INVISIBLE

    // метод для создания меню
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        
        // отгружает меню из макета 
        menuInflater.inflate(R.menu.settings, menu)
        
        item = menu!!.findItem(R.id.menu_play)
        return true
    }

    // метод для обработки выбора элемента меню
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {

            // режим редактирования
            R.id.menu_settings -> {
                gridDrawer.drawGrid()
                switchEditMode()
                true
            }

            // сохранить уровень
            R.id.menu_save -> {
                levelStorage.saveLevel(elementsDrawer.elementsOnContainer)
                true
            }

            // начать игру
            R.id.menu_play -> {
                if(editMode)  {
                    return true
                }
                showIntro()

                // воспр. музыки и начало игры
                if (soundManager.areSoundsReady()){
                    gameCore.startOrPauseTheGame()
                    if (gameCore.isPlaying()){
                        resumeTheGame()
                    } else{
                        pauseTheGame()
                    }
                }
                true
            }

            // передаётся управление родительскому классу
            else -> super.onOptionsItemSelected(item)
        }
    }

    // метод продолжения игры
    private fun resumeTheGame(){
        item.icon = ContextCompat.getDrawable(this, R.drawable.ic_baseline_pause_24)
        gameCore.resumeTheGame() // вызов метода в базе
    }

    // метод для отображения интро игры
    private fun showIntro(){
        if (gameStarted){
            return
        }
        gameStarted = true
        soundManager.loadSounds()
    }

    // метод для паузы игры 
    private fun pauseTheGame() {
        item.icon = ContextCompat.getDrawable(this , R.drawable.ic_play)
        gameCore.pauseTheGame()
        soundManager.pauseSounds()
    }

    // переопред. метода для паузы игры
    override fun onPause(){
        super.onPause()
        pauseTheGame()
    }

    /*    private fun startTheGame() {
            enemyDrawer.startEnemyCreation()
            item.icon = ContextCompat.getDrawable(this , R.drawable.ic_baseline_pause_24)
            soundManager.playIntroMusic()
        }
    */

    // метод для обработки нажатия клавиш
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (!gameCore.isPlaying()){
            return super.onKeyDown(keyCode, event)
        } // если не играем, клавиши не перехвачены

        when(keyCode){
            KEYCODE_DPAD_UP -> move(UP)
            KEYCODE_DPAD_DOWN -> move(DOWN)
            KEYCODE_DPAD_RIGHT -> move(RIGHT)
            KEYCODE_DPAD_LEFT -> move(LEFT)
            KEYCODE_SPACE -> bulletDrawer.addNewBulletForTank(playerTank)
        }

        // передаётся управление родительскому классу
        return super.onKeyDown(keyCode, event)
    }

    // метод для обработки движения
    private fun move(direction: Direction) {
        playerTank.move(direction, binding.container, elementsDrawer.elementsOnContainer)
    }

    // метод для обработки отпускания клавиш
    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (!gameCore.isPlaying()){
            return super.onKeyUp(keyCode, event)
        } // если не играем, клавиши не перехвачены

        when (keyCode) {
            KEYCODE_DPAD_UP, 
            KEYCODE_DPAD_LEFT,
            KEYCODE_DPAD_DOWN, 
            KEYCODE_DPAD_RIGHT 
            -> onButtonReleased()
        }
        return super.onKeyUp(keyCode, event)
    }

    // метод для обработки нажатия кнопки
    private fun onButtonPressed(direction: Direction) {
        soundManager.tankMove()
        playerTank.move(direction, binding.container, elementsDrawer.elementsOnContainer)
    }

    // метод для обработки отпускания кнопки
    private fun onButtonReleased() {
        if (enemyDrawer.tanks.isEmpty()) // врагов нет (список пуст)
            soundManager.tankStop()
    }

    // метод для обработки результата активности
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == SCORE_REQUEST_CODE){
            recreate()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    // метод для отображения прогресса 
    override fun showProgress() {
        binding.container.visibility = INVISIBLE
        binding.totalContainer.setBackgroundResource(R.color.gray)
        binding.initTitle.visibility = VISIBLE
    }

    // метод для скрытия прогресса
    override fun dismissProgress() {
        binding.container.visibility = VISIBLE
        binding.totalContainer.setBackgroundResource(R.color.black)
        binding.initTitle.visibility = GONE
        enemyDrawer.startEnemyCreation() // процесс создания врагов
        soundManager.playIntroMusic()
        resumeTheGame() // продолжение игры
    }
}
