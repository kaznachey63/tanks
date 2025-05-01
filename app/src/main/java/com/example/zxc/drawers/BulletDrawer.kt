package com.zxc.drawers

import android.app.Activity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import com.zxc.CELL_SIZE
import com.zxc.GameCore.isPlaying
import com.zxc.R
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
import kotlin.math.tan

private const val BULLET_WIDTH = 15
private const val BULLET_HEIGHT = 15

class BulletDrawer(
    private val container: FrameLayout,
    private val elements: MutableList<Element>,
    private val enemyDrawer: EnemyDrawer
) {
    init {
        moveAllBullets()
    }

    private val allBullets = mutableListOf<Bullet>()

     fun addNewBulletForTank(tank: Tank) {
        val view = container.findViewById<View>(tank.element.viewId)?: return
        if (tank.alreadyHasBullets()) return
        allBullets.add(Bullet(createBullet(view, tank.direction), tank.direction, tank))
    }

    private fun Tank.alreadyHasBullets(): Boolean =
        allBullets.firstOrNull { it.tank == this } != null

    private fun moveAllBullets() {
        Thread(Runnable {
            while (true) {
                if (!isPlaying()) {
                    continue
                }
                interactWithAllBulleys()
                Thread.sleep(30)
            }
        }).start()
    }

    private fun interactWithAllBulleys() {
        allBullets.toList().forEach { bullet ->
            val view = bullet.view
            if(bullet.canMoveFuther) {
                when (bullet.direction) {
                    Direction.UP -> (view.layoutParams as FrameLayout.LayoutParams).topMargin -= BULLET_HEIGHT
                    Direction.DOWN -> (view.layoutParams as FrameLayout.LayoutParams).topMargin += BULLET_HEIGHT
                    Direction.LEFT -> (view.layoutParams as FrameLayout.LayoutParams).leftMargin -= BULLET_HEIGHT
                    Direction.RIGHT -> (view.layoutParams as FrameLayout.LayoutParams).leftMargin += BULLET_HEIGHT
                }
                chooseBehaviorInTermsOfDirections(bullet)
                container.runOnUiThread {
                    container.removeView(view)
                    container.addView(view)
                }
            } else {
                stopBullet(bullet)
            }
            bullet.stopIntersectingBullets()
        }
        removeInconsistenBullets()
    }

    private fun removeInconsistenBullets() {
        val removingList = allBullets.filter { !it.canMoveFuther }
        removingList.forEach {
            stopBullet(it)
            container.runOnUiThread {
                container.removeView(it.view)
            }
        }
        allBullets.removeAll(removingList)
    }

    private fun Bullet.stopIntersectingBullets() {
        val bulletCoordinate = this.view.getViewCoordinate()
        for (bulletInList in allBullets) {
            val coordinateList = bulletInList.view.getViewCoordinate()
            if(this == bulletInList) {
                continue
            }
            if(coordinateList == bulletCoordinate) {
                stopBullet(this)
                stopBullet(bulletInList)
                return
            }
        }
    }

    /*private var canBulletGoFuther = true
    private var bulletThread: Thread? = null
    private lateinit var tank: Tank*/

   //private fun checkBulletThreadDlive() = bulletThread != null && bulletThread!!.isAlive

    private fun Bullet.canBulletGoFuther() =
        this.view.checkViewCanMoveThroughBorder(this.view.getViewCoordinate())
                && this.canMoveFuther

    private fun chooseBehaviorInTermsOfDirections(bullet: Bullet) {
        when(bullet.direction) {
            Direction.DOWN, Direction.UP -> {
                compareCollections(getCoordinatesForTopOrBottomDirection(bullet), bullet)
            }

            Direction.LEFT, Direction.RIGHT -> {
                compareCollections(getCoordinatesForLeftOrRightDirection(bullet), bullet)
            }
        }
    }

    private fun compareCollections(detectedCoordinateList: List<Coordinate>, bullet: Bullet) {
            for (coordinate in detectedCoordinateList) {
                var element = getElementByCoordinates(coordinate, elements)
                if (element == null) {
                    element = getTankByCoordinates(coordinate, enemyDrawer.tanks)
                }
                if(element == bullet.tank.element) {
                    continue
                }
                removeElementsAndStopBullet(element, bullet)
            }
    }

    private fun removeElementsAndStopBullet(element: Element?, bullet: Bullet) {
        if (element != null) {
            if (element.material.bulletCanGoThrough)
                return
            if(bullet.tank.element.material == Material.ENEMY_TANK
                && element.material == Material.ENEMY_TANK
                ) {
                stopBullet(bullet)
                return
            }
            if (element.material.simpleBulletCanDestroy) {
                stopBullet(bullet)
                removeView(element)
                elements.remove(element)
                removeTank(element)
            } else {
                stopBullet(bullet)
            }
        }
    }

    private fun removeTank(element: Element) {
        val tanksElements = enemyDrawer.tanks.map { it.element }
        val tankIndex = tanksElements.indexOf(element)
        enemyDrawer.removeTank(tankIndex)
    }

    private fun stopBullet(bullet: Bullet) {
        bullet.canMoveFuther = false
    }

    private fun removeView( element: Element?) {
        val activity = container.context as Activity
        activity.runOnUiThread {
            if (element != null) {
                container.removeView(activity.findViewById(element.viewId))
            }
        }
    }

    private fun getCoordinatesForTopOrBottomDirection(bullet: Bullet): List<Coordinate>{
        val bulletCoordinate = bullet.view.getViewCoordinate()
        val leftCell = bulletCoordinate.left - bulletCoordinate.left % CELL_SIZE
        val rightCell = leftCell + CELL_SIZE
        val topCoordinate = bulletCoordinate.top - bulletCoordinate.top % CELL_SIZE
        return listOf(
            Coordinate(topCoordinate, leftCell),
            Coordinate(topCoordinate, rightCell)
        )
    }

    private fun getCoordinatesForLeftOrRightDirection(bullet: Bullet): List<Coordinate>{
        val bulletCoordinate = bullet.view.getViewCoordinate()
        val topCell = bulletCoordinate.top - bulletCoordinate.top % CELL_SIZE
        val bottomCell = topCell + CELL_SIZE
        val leftCoordinate = bulletCoordinate.left - bulletCoordinate.left % CELL_SIZE
        return listOf(
            Coordinate(topCell, leftCoordinate),
            Coordinate(bottomCell, leftCoordinate)
        )
    }

    private fun createBullet(myTank: View, currentDirection: Direction): ImageView {
        return ImageView(container.context)
            .apply {
                this.setImageResource(R.drawable.bullet)
                this.layoutParams = FrameLayout.LayoutParams(BULLET_WIDTH, BULLET_HEIGHT)
                val bulletCoordinate = getBulletCoordinates(this, myTank, currentDirection)
                (this.layoutParams as FrameLayout.LayoutParams).topMargin = bulletCoordinate.top
                (this.layoutParams as FrameLayout.LayoutParams).leftMargin = bulletCoordinate.left
                this.rotation = currentDirection.rotation
            }
    }

    private fun getBulletCoordinates(
        bullet: ImageView,
        myTank: View,
        currentDirection: Direction
    ): Coordinate {
        val tankLeftTopCoordinate = Coordinate(myTank.top, myTank.left)
        return when (currentDirection) {
            Direction.UP -> {
                return Coordinate(
                    top = tankLeftTopCoordinate.top - bullet.layoutParams.height,
                    left = getDistanceToMiddleOfTank(
                        tankLeftTopCoordinate.left, bullet.layoutParams.width
                    )
                )
            }

            Direction.DOWN -> {
                return Coordinate(
                    top = tankLeftTopCoordinate.top + myTank.layoutParams.height,
                    left = getDistanceToMiddleOfTank(
                        tankLeftTopCoordinate.left, bullet.layoutParams.width
                    )
                )
            }

            Direction.LEFT-> {
                return Coordinate(
                    top = getDistanceToMiddleOfTank(
                        tankLeftTopCoordinate.top, bullet.layoutParams.height
                    ),
                    left = tankLeftTopCoordinate.left - bullet.layoutParams.width
                )
            }

            Direction.RIGHT-> {
                return Coordinate(
                    top = getDistanceToMiddleOfTank(
                        tankLeftTopCoordinate.top, bullet.layoutParams.height
                    ),
                    left = tankLeftTopCoordinate.left + myTank.layoutParams.width
                )
            }
        }
    }

    private fun getDistanceToMiddleOfTank(startCoordinate: Int, bulletSize: Int): Int {
        return startCoordinate + (CELL_SIZE - bulletSize / 2)
    }
}