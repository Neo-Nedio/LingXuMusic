package com.neo.lingxumusic.ui.common

import androidx.compose.foundation.basicMarquee
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit

//有走马灯效果的文字
@Composable
fun MarqueeText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign? = null,
) {
    Text(
        text = text,
        modifier = modifier.basicMarquee(),
        maxLines = 1,
        color = color,
        fontSize = fontSize,
        fontWeight = fontWeight,
        textAlign = textAlign,
    )
}

@Composable
fun MarqueeText(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    textAlign: TextAlign? = null,
) {
    Text(
        text = text,
        modifier = modifier.basicMarquee(),
        maxLines = 1,
        textAlign = textAlign,
    )
}
