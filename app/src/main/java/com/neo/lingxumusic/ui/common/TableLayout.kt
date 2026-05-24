package com.neo.lingxumusic.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout


//表格布局组件，可以将子元素排列成指定列数的网格
/*
┌─────────────────────────────────┐
│  元素1    元素2    元素3         │
│  元素4    元素5    元素6         │
│  元素7                          │
└─────────────────────────────────┘*/
@Composable
fun TableLayout(
    modifier: Modifier = Modifier,
    cellsCount: Int,  // 每行多少个单元格
    content: @Composable () -> Unit  // 要排列的子元素
) {
    Layout(
        content = content,
        modifier = modifier,
    ) { measurables, constraints ->// measurables：所有子元素的列表 constraints：父容器给的尺寸约束
        val parentWidth = constraints.maxWidth   // 父容器总宽度
        val cellWidth = parentWidth / cellsCount // 每个单元格宽度 = 总宽度 ÷ 列数

        var totalHeight = 0                    // 总高度
        val rowHeights = mutableListOf<Int>()  // 每行的高度
        val cellsHeightPerRow = mutableListOf<Int>()  // 当前行的每个单元格高度

        //测量每行高度和总高度
        val placeables = measurables.mapIndexed { index, measurable ->
            // 限制每个单元格的宽度为 cellWidth
            val newConstraints = constraints.copy(
                minWidth = cellWidth,
                maxWidth = cellWidth
            )
            val placeable = measurable.measure(newConstraints)
            val childHeight = placeable.height
            cellsHeightPerRow.add(childHeight)  // 记录当前行每个单元格的高度

            // 判断是否到行尾或是最后一个元素
            if (cellsHeightPerRow.size == cellsCount || index == measurables.size - 1) {
                // 取当前行中最大的高度作为该行的行高
                var maxChildHeight = 0
                cellsHeightPerRow.forEach { if (it > maxChildHeight) maxChildHeight = it }
                totalHeight += maxChildHeight    // 累加到总高度
                rowHeights.add(maxChildHeight)   // 记录该行高度
                cellsHeightPerRow.clear()        // 清空，准备下一行
            }
            placeable
        }

        //布局子元素
        layout(parentWidth, totalHeight) {
            placeables.forEachIndexed { index, placeable ->
                val column = index % cellsCount   // 计算在哪一列（0-2）
                val row = index / cellsCount      // 计算在哪一行（0,1,2...）

                val positionX = cellWidth * column  // X 坐标

                // Y 坐标：累加前面所有行的高度
                var positionY = 0
                for (i in 0 until row) {
                    positionY += rowHeights[i]
                }

                placeable.placeRelative(positionX, positionY)
            }
        }
    }
}