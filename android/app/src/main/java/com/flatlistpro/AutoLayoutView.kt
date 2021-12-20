package com.flatlistpro

import android.content.Context
import android.graphics.Canvas
import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi
import com.facebook.react.views.view.ReactViewGroup

class AutoLayoutView(context: Context) : ReactViewGroup(context) {
    var horizontal: Boolean = false
    var scrollOffset: Int = 0
    var windowSize: Int = 0

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        if (childCount > 1) {
            val positionSortedIndices = Array<Int>(childCount) { it }
            positionSortedIndices.sortWith(getComparator())
            clearGaps(positionSortedIndices)
            //removeOverlaps(positionSortedIndices)
        }
        super.onLayout(changed, left, top, right, bottom)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
    }

    private fun getComparator(): Comparator<Int> {
        return if (!horizontal) {
            compareBy<Int> { getChildAt(it).top }.thenBy { getChildAt(it).left }
        } else {
            compareBy<Int> { getChildAt(it).left }.thenBy { getChildAt(it).top }
        }
    }

    private fun clearGaps(sortedItems: Array<Int>) {
        var currentMax = 0

        for (i in 0 until sortedItems.size - 1) {
            val cell = getChildAt(sortedItems[i])
            val neighbour = getChildAt(sortedItems[i + 1])
            if (isWithinBounds(cell)) {
                if (!horizontal) {
                    currentMax = kotlin.math.max(currentMax, cell.bottom);
                    if (cell.left < neighbour.left) {
                        if (cell.right != neighbour.left) {
                            neighbour.right = cell.right + neighbour.width
                            neighbour.left = cell.right
                        }
                        if (cell.top != neighbour.top) {
                            neighbour.bottom = cell.top + neighbour.height
                            neighbour.top = cell.top
                        }
                    } else {
                        neighbour.bottom = currentMax + neighbour.height
                        neighbour.top = currentMax
                    }
                } else {
                    currentMax = kotlin.math.max(currentMax, cell.right);
                    if (cell.top < neighbour.top) {
                        if (cell.bottom != neighbour.top) {
                            neighbour.bottom = cell.bottom + neighbour.height
                            neighbour.top = cell.bottom
                        }
                        if (cell.left != neighbour.left) {
                            neighbour.right = cell.left + neighbour.width
                            neighbour.left = cell.left
                        }
                    } else {
                        neighbour.right = currentMax + neighbour.width
                        neighbour.left = currentMax
                    }
                }
            }
        }
    }

    private fun removeOverlaps(sortedItems: Array<Int>) {
        val offset = arrayOf(0, 0)
        val itemCount = sortedItems.size
        for (i in 0 until itemCount) {
            for (j in 0 until itemCount) {
                if (i == j) {
                    continue
                }
                val cell = getChildAt(i)
                val neighbour = getChildAt(j)
                updateIntersection(cell.left, cell.top, cell.width, cell.height, neighbour.left, neighbour.top, offset)
                val minCorrection = kotlin.math.min(offset[0], offset[1]);
                if (minCorrection > 0) {
                    if (offset[0] < offset[1]) {
                        neighbour.offsetLeftAndRight(minCorrection)
                    } else {
                        neighbour.offsetTopAndBottom(minCorrection)
                    }
                }
            }
        }
    }

    private fun isWithinBounds(cell: View): Boolean {
        return if (!horizontal) {
            (cell.top >= scrollOffset || cell.bottom >= scrollOffset) &&
                    (cell.top <= scrollOffset + windowSize || cell.bottom <= scrollOffset + windowSize)
        } else {
            (cell.left >= scrollOffset || cell.right >= scrollOffset) &&
                    (cell.left <= scrollOffset + windowSize || cell.right <= scrollOffset + windowSize)
        }
    }

    private fun updateIntersection(x1: Int, y1: Int, w1: Int, h1: Int, x2: Int, y2: Int, offset: Array<Int>) {
        if (x2 >= x1 && x2 < x1 + w1 && y2 >= y1 && y2 < y1 + h1) {
            offset[0] = x1 + w1 - x2
            offset[1] = y1 + h1 - y2
        } else {
            offset[0] = 0
            offset[1] = 0
        }
    }
}