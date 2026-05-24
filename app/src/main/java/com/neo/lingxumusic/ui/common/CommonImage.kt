package com.neo.lingxumusic.ui.common

import androidx.compose.foundation.Image
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.allowHardware
import com.neo.lingxumusic.R
import com.neo.lingxumusic.ui.theme.AppColorsProvider

@Composable
// Coil 加载本地资源的图片组件，相比普通的 Image 有更好的性能和缓存支持
fun CommonNetworkImage(
    url: Any?,
    placeholder: Int = R.drawable.ic_default_place_holder,
    error: Int = R.drawable.ic_default_place_holder,
    allowHardware: Boolean = false,
    modifier: Modifier = Modifier,
    colorFilter: ColorFilter? = null
) {
    Image(
        painter = rememberAsyncImagePainter(
            model = ImageRequest.Builder(LocalContext.current)
                .data(url)
                .allowHardware(allowHardware)
                .build(),
            placeholder = painterResource(placeholder),
            error = painterResource(error)
        ),
        contentDescription = "头像",
        contentScale = ContentScale.Crop,
        modifier = modifier,
        colorFilter = colorFilter
    )
}


@Composable
// Coil 加载本地资源的图片组件，相比普通的 Image 有更好的性能和缓存支持
/*值	                效果
false（默认）	使用软件位图，内存占用稍大，但兼容性好
true	        使用硬件位图（存储在 GPU），渲染更快，但某些操作（如读取像素）不支持*/
fun CommonLocalImage(
    resId: Int,
    allowHardware: Boolean = false,  // 是否允许使用硬件位图
    modifier: Modifier = Modifier,
    colorFilter: ColorFilter? = null //用于改变图片颜色的滤镜，可以对图片进行染色、混合颜色等操作
) {

    Image(
        painter = rememberAsyncImagePainter(
            model = ImageRequest.Builder(LocalContext.current)
                .data(resId)
                .allowHardware(allowHardware)
                .build()
        ),
        contentDescription = null,
        modifier = modifier,
        contentScale = ContentScale.Crop,
        colorFilter = colorFilter
    )
}

@Composable
fun CommonIcon(
    resId: Int,
    modifier: Modifier = Modifier,
    tint: Color = AppColorsProvider.current.primary
) {

    Icon(
        painter = painterResource(resId),
        contentDescription = null,
        modifier = modifier,
        tint = tint
    )
}