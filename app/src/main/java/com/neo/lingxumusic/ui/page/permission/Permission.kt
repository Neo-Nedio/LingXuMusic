package com.neo.lingxumusic.ui.page.permission

import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.neo.lingxumusic.ui.theme.AppColorsProvider
import com.neo.lingxumusic.utils.cdp
import com.neo.lingxumusic.utils.csp

@Composable
fun Permission(
    permission: String,           // 权限名称（如 Manifest.permission.READ_EXTERNAL_STORAGE）
    permissionName: String,       // 权限显示名称（如"存储"）
) {
    val showDialog = remember { mutableStateOf(false) }
    val context = LocalContext.current

    // 权限申请启动器
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        if (isGranted) {
            showDialog.value = false
        } else {
            showDialog.value = true
        }
    }

    // 组件启动时检查权限
    LaunchedEffect(permission) {
        if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
            launcher.launch(permission) // 未授权，发起权限申请
        }
    }

    // 显示权限被拒绝的提示弹窗
    showPermissionDenyDialog(
        showDialog = showDialog,
        permissionName = permissionName,
        onRequestAgain = { launcher.launch(permission) },
    )
}

// 权限被拒绝提示弹窗
@Composable
private fun showPermissionDenyDialog(
    showDialog: MutableState<Boolean>,
    permissionName: String,
    onRequestAgain: () -> Unit,
) {
    val context = LocalContext.current
    if (showDialog.value) {
        Dialog(onDismissRequest = { showDialog.value = false }) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = AppColorsProvider.current.card,
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .width(590.cdp)
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(24.cdp)),
            ) {
                // 标题区域
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.cdp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "温馨提示",
                            fontSize = 36.csp,
                            fontWeight = FontWeight.Medium,
                            color = AppColorsProvider.current.firstText,
                        )
                    }
                    // 分割线
                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth(),
                        thickness = 2.cdp,
                        color = Color(0xFFF2F2F2),
                    )
                    // 提示内容区域
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 40.cdp, end = 40.cdp, top = 36.cdp, bottom = 56.cdp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "拒绝${permissionName}权限后，将影响app的正常使用",
                            fontSize = 36.csp,
                            fontWeight = FontWeight.Medium,
                            color = AppColorsProvider.current.firstText,
                        )
                    }
                    // 分割线
                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth(),
                        thickness = 2.cdp,
                        color = Color(0xFFF2F2F2),
                    )
                    //按钮
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.cdp),
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                ) {
                                    onRequestAgain()
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "再次申请",
                                fontSize = 36.csp,
                                fontWeight = FontWeight.Medium,
                                color = AppColorsProvider.current.firstText,
                            )
                        }
                        VerticalDivider(
                            thickness = 2.cdp,
                            color = Color(0xFFF2F2F2),
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                ) {
                                    (context as ComponentActivity).finish()
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "退出应用",
                                fontSize = 36.csp,
                                fontWeight = FontWeight.Medium,
                                color = AppColorsProvider.current.firstText,
                            )
                        }
                    }
                }
            }
        }
    }
}
