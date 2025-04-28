package com.zxc

import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.zxc.databinding.ActivityMainBinding
import android.view.KeyEvent
import android.view.KeyEvent.KEYCODE_DPAD_UP
import android.view.KeyEvent.KEYCODE_DPAD_DOWN
import android.view.KeyEvent.KEYCODE_DPAD_LEFT
import android.view.KeyEvent.KEYCODE_DPAD_RIGHT
import android.view.Menu
import android.view.MenuItem
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import androidx.core.view.marginLeft
import androidx.core.view.marginTop
import com.zxc.drawers.ElementsDrawer
import com.zxc.drawers.GridDrawer
import com.zxc.enums.Direction
import com.zxc.enums.Direction.DOWN
import com.zxc.enums.Direction.LEFT
import com.zxc.enums.Direction.RIGHT
import com.zxc.enums.Direction.UP
import com.zxc.enums.Material
import com.zxc.models.Coordinate

const val CELL_SIZE = 50
lateinit var binding: ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private var editMode = false
    private val gridDrawer by lazy {
        GridDrawer(binding.container)
    }

    private val elementsDrawer by lazy {
        ElementsDrawer(binding.container)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Menu"

        binding.editorClear.setOnClickListener { elementsDrawer.currentMaterial = Material.EMPTY }
        binding.editorBrick.setOnClickListener { elementsDrawer.currentMaterial = Material.BRICK }
        binding.editorConcrete.setOnClickListener {
            elementsDrawer.currentMaterial = Material.CONCRETE
        }
        binding.editorGrass.setOnClickListener{ elementsDrawer.currentMaterial = Material.GRASS }
        binding.container.setOnTouchListener { _, event ->
            elementsDrawer.drawView(Coordinate(event.y.toInt(), event.x.toInt()))
            return@setOnTouchListener true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.settings, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_settings -> {
                switchEditMode()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KEYCODE_DPAD_UP -> elementsDrawer.move(binding.myTank, UP)
            KEYCODE_DPAD_DOWN -> elementsDrawer.move(binding.myTank, DOWN)
            KEYCODE_DPAD_LEFT -> elementsDrawer.move(binding.myTank, LEFT)
            KEYCODE_DPAD_RIGHT -> elementsDrawer.move(binding.myTank, RIGHT)
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun switchEditMode() {
        if (editMode) {
            gridDrawer.removeGrid()
            binding.materialsContainer.visibility = INVISIBLE
        } else {
            gridDrawer.drawGrid()
            binding.materialsContainer.visibility = VISIBLE
        }
        editMode = !editMode
    }
}