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

private const val BULLET_HEIGHT = 15
private const val BULLET_WIDTH = 15

class BulletDrawer(
    private val container: FrameLayout,
    private val elements: MutableList<Element>,
    private val enemyDrawer: EnemyDrawer,
    private val soundManager: MainSoundPlayer,
    private val gameCore: GameCore

) {
    init {
        moveAllBullets()
    }

    private val allBullets = mutableListOf<Bullet>()

    fun addNewBulletForTank(tank: Tank) {
        val view = container.findViewById<View>(tank.element.viewId) ?: return
        if (tank.alreadyHasBullet()) return
        allBullets.add(Bullet(createBullet(view, tank.direction), tank.direction, tank))
        soundManager.bulletShot()
    }

    private fun Tank.alreadyHasBullet(): Boolean =
        allBullets.firstOrNull { it.tank == this } != null

    private fun moveAllBullets() {
        Thread(Runnable {
            while (true) {
                if (!gameCore.isPlaying())
                    continue
                interactWithAllBullets()
                Thread.sleep(30)
            }
        }).start()
    }

    private fun interactWithAllBullets() {
        for (bullet in allBullets.toList()) {

            bullet.stopIntersectingBullets()

            if (!bullet.canBulletGoFurther()) {
                stopBullet(bullet)
                continue
            }

            val view = bullet.view
            when (bullet.direction) {
                Direction.UP -> (view.layoutParams as FrameLayout.LayoutParams).topMargin -= BULLET_HEIGHT
                Direction.DOWN -> (view.layoutParams as FrameLayout.LayoutParams).topMargin += BULLET_HEIGHT
                Direction.LEFT -> (view.layoutParams as FrameLayout.LayoutParams).leftMargin -= BULLET_HEIGHT
                Direction.RIGHT -> (view.layoutParams as FrameLayout.LayoutParams).rightMargin += BULLET_HEIGHT
            }

            chooseBehaviourInTermsOfDirections(bullet)

            container.runOnUiThread {
                container.removeView(view)
                container.addView(view)
            }
        }

        removeInconsistentBullets()
    }

    private fun removeInconsistentBullets() {
        val removingList = allBullets.filter { !it.canMoveFurther }
        removingList.forEach {
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
            if (this == bulletInList) continue

            if (coordinateList == bulletCoordinate) {
                stopBullet(this)
                stopBullet(bulletInList)
                return
            }
        }
    }

    private fun Bullet.canBulletGoFurther() =
        this.view.checkViewCanMoveThroughBorder(this.view.getViewCoordinate()) && this.canMoveFurther

    private fun chooseBehaviourInTermsOfDirections(bullet: Bullet) {
        when (bullet.direction) {
            Direction.DOWN, Direction.UP -> {
                compareCollections(getCoordinatesForTopOrBottomDirection(bullet), bullet)
            }

            Direction.LEFT, Direction.RIGHT -> {
                compareCollections(getCoordinatesForLeftOrRightDirection(bullet), bullet)
            }
        }
    }

    private fun compareCollections(detectedCoordinatesList: List<Coordinate>, bullet: Bullet) {
        for (coordinate in detectedCoordinatesList) {
            val element = getTankByCoordinates(coordinate, enemyDrawer.tanks)
                ?: getElementByCoordinates(coordinate, elements)

            if (element == bullet.tank.element) continue

            removeElementsAndStopBullet(element, bullet)
        }
    }

    private fun removeElementsAndStopBullet(element: Element?, bullet: Bullet) {
        if (element == null) return

        if (bullet.tank.element.material == Material.ENEMY_TANK
            && element.material == Material.ENEMY_TANK) {
            stopBullet(bullet)
            return
        }

        if (element.material.bulletCanGoThrough) return

        if (element.material.simpleBulletCanDestroy) {
            removeView(element)
            removeElement(element)
            stopGameIfNecessary(element)
            removeTank(element)
        }

        stopBullet(bullet)
    }

    private fun removeElement(element: Element?) {
        elements.remove(element)
    }

    private fun stopGameIfNecessary(element: Element) {
        if (element.material == Material.PLAYER_TANK || element.material == Material.EAGLE)
            gameCore.destroyPlayerOrBase(enemyDrawer.getPlayerScore())
    }


    private fun removeTank(element: Element) {
        val tanksElements = enemyDrawer.tanks.map { it.element }
        val tankIndex = tanksElements.indexOf(element)
        if (tankIndex < 0) return

        soundManager.bulletBurst()
        enemyDrawer.removeTank(tankIndex)
    }

    private fun stopBullet(bullet: Bullet) {
        bullet.canMoveFurther = false
    }

    private fun removeView(element: Element?) {
        val activity = container.context as Activity
        activity.runOnUiThread {
            if (element != null) {
                container.removeView(activity.findViewById(element.viewId))
            }
        }
    }

    private fun getCoordinatesForTopOrBottomDirection(bullet: Bullet): List<Coordinate> {
        val bulletCoordinate = bullet.view.getViewCoordinate()
        val leftCell = bulletCoordinate.left - bulletCoordinate.left % CELL_SIZE
        val rightCell = leftCell + CELL_SIZE
        val topCoordinate = bulletCoordinate.top - bulletCoordinate.top % CELL_SIZE
        return listOf(
            Coordinate(topCoordinate, leftCell),
            Coordinate(topCoordinate, rightCell)
        )
    }

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

    fun createBullet(myTank: View, currentDirection: Direction): ImageView {
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

        when(currentDirection){
            Direction.UP -> {
                return Coordinate(
                    top = tankLeftTopCoordinate.top - bullet.layoutParams.height,
                    left = getDistanceToMiddleOfTanks(tankLeftTopCoordinate.left, bullet.layoutParams.width))
            }

            Direction.DOWN -> {
                return Coordinate(
                    top = tankLeftTopCoordinate.top + myTank.layoutParams.height,
                    left = getDistanceToMiddleOfTanks(tankLeftTopCoordinate.left, bullet.layoutParams.width))
            }
            Direction.LEFT -> {
                return Coordinate(
                    top = getDistanceToMiddleOfTanks(tankLeftTopCoordinate.top, bullet.layoutParams.height),
                    left = tankLeftTopCoordinate.left  - bullet.layoutParams.width)
            }
            Direction.RIGHT -> {
                return Coordinate(
                    top = getDistanceToMiddleOfTanks(tankLeftTopCoordinate.top, bullet.layoutParams.height),
                    left = tankLeftTopCoordinate.left  + myTank.layoutParams.width)
            }
        }

        return tankLeftTopCoordinate
    }
    private fun getDistanceToMiddleOfTanks(startCoordinate: Int, bulletSize: Int):Int {
        return startCoordinate + (CELL_SIZE - bulletSize/2)
    }
}