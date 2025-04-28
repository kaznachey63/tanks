package com.zxc.drawers

import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.view.marginLeft
import androidx.core.view.marginTop
import com.zxc.CELL_SIZE
import com.zxc.R
import com.zxc.binding
import com.zxc.enums.Direction
import com.zxc.enums.Direction.DOWN
import com.zxc.enums.Direction.LEFT
import com.zxc.enums.Direction.RIGHT
import com.zxc.enums.Direction.UP
import com.zxc.enums.Material
import com.zxc.models.Coordinate
import com.zxc.models.Element

class ElementsDrawer(val container: FrameLayout) {
    var currentMaterial = Material.EMPTY
    private val elementsOnContainer = mutableListOf<Element>()

    fun move(myTank:View, direction: Direction)
    {
        val layoutParams = myTank.layoutParams as FrameLayout.LayoutParams
        val currentCoordinate = Coordinate(layoutParams.topMargin, layoutParams.leftMargin)
        when (direction) {
            UP -> {
                myTank.rotation = 0f
                    (myTank.layoutParams as FrameLayout.LayoutParams).topMargin -= CELL_SIZE
            }
            DOWN -> {
                myTank.rotation = 180f
                    (myTank.layoutParams as FrameLayout.LayoutParams).topMargin += CELL_SIZE
            }
            LEFT -> {
                myTank.rotation = 270f
                    (myTank.layoutParams as FrameLayout.LayoutParams).leftMargin -= CELL_SIZE
            }
            RIGHT -> {
                myTank.rotation = 90f
                    (myTank.layoutParams as FrameLayout.LayoutParams).leftMargin += CELL_SIZE
            }
        }

        val nextCoordinate = Coordinate(layoutParams.topMargin, layoutParams.leftMargin)
        if (checkTankCanMoveThroughBorder(
            nextCoordinate,
            myTank
        ) && checkTankCanMoveThroughMaterial(nextCoordinate)
            ) {
            binding.container.removeView(myTank)
            binding.container.addView(myTank)
        } else {
            (myTank.layoutParams as FrameLayout.LayoutParams).topMargin = currentCoordinate.top
            (myTank.layoutParams as FrameLayout.LayoutParams).leftMargin = currentCoordinate.left
        }
    }

    fun onTouchContainer(x:Float, y:Float) {
        val topMargin = y.toInt() - (y.toInt() % CELL_SIZE)
        val leftMargin = x.toInt() - (x.toInt() % CELL_SIZE)
        val coordinate = Coordinate(topMargin, leftMargin)
        drawView(coordinate)
    }

    fun drawView(coordinate: Coordinate){
        val view = ImageView(container.context)
        val layoutParams = FrameLayout.LayoutParams(CELL_SIZE, CELL_SIZE)
        when (currentMaterial) {
            Material.EMPTY -> {

            }

            Material.BRICK -> view.setImageResource(R.drawable.brick)
            Material.CONCRETE -> view.setImageResource(R.drawable.concrete)
            Material.GRASS -> view.setImageResource(R.drawable.grass)
        }
        layoutParams.topMargin = coordinate.top
        layoutParams.leftMargin = coordinate.left
        val viewId = View.generateViewId()
        view.id = viewId
        view.layoutParams = layoutParams
        container.addView(view)
        elementsOnContainer.add(Element(viewId, currentMaterial, coordinate))
    }
    private fun checkTankCanMoveThroughMaterial(coordinate: Coordinate): Boolean {
        getTankCoordinates(coordinate).forEach{
            val element = getElementByCoordinates(it)
            if (element != null && !element.material.tankCanGoThrough) {
                return false
            }
        }
        return true
    }

    private fun checkTankCanMoveThroughBorder(coordinate: Coordinate, myTank: View): Boolean {
        if(coordinate.top >= 0 &&
            coordinate.top + myTank.height < binding.container.height &&
            coordinate.left >= 0 &&
            coordinate.left + myTank.width < binding.container.width
        ) {
            return true
        }
        return false
    }

    private fun getElementByCoordinates(coordinate: Coordinate) =
        elementsOnContainer.firstOrNull { it.coordinate == coordinate }

    private fun getTankCoordinates(topLeftCoordinate: Coordinate): List<Coordinate> {
        val coordinateList = mutableListOf<Coordinate>()
        coordinateList.add(topLeftCoordinate)
        coordinateList.add(Coordinate(topLeftCoordinate.top + CELL_SIZE, topLeftCoordinate.left))
        coordinateList.add(Coordinate(topLeftCoordinate.top, topLeftCoordinate.left + CELL_SIZE))
        coordinateList.add(
            Coordinate(
                topLeftCoordinate.top + CELL_SIZE,
                topLeftCoordinate.left + CELL_SIZE
            )
        )
        return coordinateList
    }
}