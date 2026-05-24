package com.neo.lingxumusic.ui.common

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neo.lingxumusic.ui.theme.AppColorsProvider
import kotlinx.coroutines.launch

@Composable
fun BottomNavigationBar(
    items: List<BottomNavigationItem>,
    pagerState: PagerState,
    onItemSelected: ((selectedIndex: Int) -> Unit)? = null
) {
    val scope = rememberCoroutineScope()
    val selectedIndex = pagerState.currentPage

    NavigationBar(containerColor = AppColorsProvider.current.background) {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                icon = {
                    BottomNavigationItemIcon(
                        item.icon,
                        if (selectedIndex == index) AppColorsProvider.current.primary else Color.LightGray
                    )
                },
                label = {
                    BottomNavigationItemText(
                        item.title,
                        if (selectedIndex == index) AppColorsProvider.current.primary else Color.LightGray
                    )
                },
                selected = selectedIndex == index,
                onClick = {
                    scope.launch {  // 启动协程
                        pagerState.scrollToPage(index) // 滑动到指定页面
                        onItemSelected?.invoke(index) // 回调通知外部
                    }
                })
        }
    }
}


@Composable
private fun BottomNavigationItemIcon(@DrawableRes resId: Int, color: Color) {
    Icon(
        painterResource(resId),
        null,
        tint = color,
        modifier = Modifier.size(22.dp)
    )
}

@Composable
private fun BottomNavigationItemText(text: String, color: Color) {
    Text(
        text,
        color = color,
        fontSize = 10.sp
    )
}

data class BottomNavigationItem(val title: String, val icon: Int)