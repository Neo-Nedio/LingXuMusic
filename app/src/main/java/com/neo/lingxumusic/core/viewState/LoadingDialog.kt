package com.neo.lingxumusic.core.viewState

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun LoadingDialog(show: Boolean) {
    if (!show) return  // 不显示时直接返回，不渲染 Dialog

    Dialog(
        onDismissRequest = { /* 点击外部不关闭，什么都不做 */ },
        properties = DialogProperties(
            dismissOnBackPress = false,  // 返回键不关闭
            dismissOnClickOutside = false  // 点击外部不关闭
        )
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color.White  // Material 3 用 containerColor
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .size(100.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                LoadingComponent()
            }
        }
    }
}