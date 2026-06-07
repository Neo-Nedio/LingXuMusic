package com.neo.lingxumusic.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.neo.lingxumusic.ui.theme.AppColorsProvider
import com.neo.lingxumusic.utils.cdp
import com.neo.lingxumusic.utils.csp

@Composable
fun VipBadge(
    modifier: Modifier = Modifier,
    text: String = "VIP",
) {
    val goldColor = Color(0xFFFFD700)

    Box(
        modifier = modifier
            .height(24.cdp)
            .border(
                width = 1.cdp,
                color = goldColor,
                shape = RoundedCornerShape(4.cdp)
            )
            .background(
                color = AppColorsProvider.current.background,
                shape = RoundedCornerShape(4.cdp)
            )
            .padding(horizontal = 6.cdp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 20.csp,
            fontWeight = FontWeight.Bold,
            color = goldColor,
            lineHeight = 24.csp,
        )
    }
}
