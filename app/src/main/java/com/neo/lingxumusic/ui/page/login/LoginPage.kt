package com.neo.lingxumusic.ui.page.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import com.google.gson.Gson
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonElement
import com.neo.lingxumusic.R
import com.neo.lingxumusic.http.api.LoginApi
import com.neo.lingxumusic.core.AppGlobalData
import com.neo.lingxumusic.core.viewState.ViewStateLoadingDialogComponent
import com.neo.lingxumusic.core.viewState.BaseViewStateViewModel
import com.neo.lingxumusic.core.viewState.ViewStateMutableLiveData
import com.neo.lingxumusic.model.BaseResult
import com.neo.lingxumusic.model.LoginData
import com.neo.lingxumusic.model.MultiUser
import com.neo.lingxumusic.model.dataAs
import com.neo.lingxumusic.ui.theme.AppColorsProvider
import com.neo.lingxumusic.utils.showToast
import dagger.hilt.android.lifecycle.HiltViewModel
import com.neo.lingxumusic.core.navigation.NavController
import com.neo.lingxumusic.core.navigation.Routes
import com.neo.lingxumusic.ui.page.login.component.MultiUserColumn
import com.neo.lingxumusic.utils.cdp
import com.neo.lingxumusic.utils.csp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

//todo 登录页调整
@Composable
fun LoginPage() {
    val viewModel: LoginViewModel = hiltViewModel()
    var phone by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var codeCountdown by remember { mutableStateOf(0) }
    var multiUsers by remember { mutableStateOf<List<MultiUser>?>(null) }

    //开启协程，codeCountdown 为key，每次变化后都重新执行
    LaunchedEffect(codeCountdown) {
        if (codeCountdown > 0) {
            delay(1000) //非阻塞延迟，不卡 UI 线程
            codeCountdown-- //触发新值，再次进入 LaunchedEffect
        }
    }


    ViewStateLoadingDialogComponent(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColorsProvider.current.primary),
        viewStateLiveData = viewModel.loginResult,
        successBlock = {
            NavController.instance.popBackStack()
            NavController.instance.navigate(Routes.HOME)
        },
        failBlock = { data ->
            multiUsers = data.toMultiUsers()
        }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            //顶部图标
            Image(
                painterResource(id = R.drawable.ic_splash_logo),
                contentDescription = "splashLogo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .padding(top = 80.dp)
                    .size(100.dp)
                    .clip(CircleShape)
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("请输入手机号") },
                modifier = Modifier
                    .padding(top = 80.cdp)
                    .focusTarget(),
                singleLine = true,
                colors = LoginTextFieldColors()
            )

            OutlinedTextField(
                value = code,
                onValueChange = { code = it },
                label = { Text("请输入验证码") },
                modifier = Modifier.padding(top = 40.cdp),
                visualTransformation = PasswordVisualTransformation(), //隐藏输入
                trailingIcon = {
                    Button(
                        onClick = {
                            if (viewModel.sendCode(phone)) {
                                codeCountdown = 60
                            }
                        },
                        enabled = codeCountdown == 0,
                        modifier = Modifier
                            .width(100.dp)      // 固定宽度
                            .height(40.dp),     // 固定高度
                        contentPadding = PaddingValues(0.dp),  // 清除内边距，让文字占满按钮
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            disabledContainerColor = Color.LightGray
                        )
                    ) {
                        Text(
                            text = if (codeCountdown == 0) "获取验证码" else "${codeCountdown}s",
                            color = Color.Black
                        )
                    }
                },
                colors = LoginTextFieldColors()
            )

            Button(
                onClick = {
                    viewModel.login(phone, code) //登录
                },
                modifier = Modifier
                    .padding(80.cdp)
                    .fillMaxWidth()
                    .height(100.cdp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White
                )
            ) {
                Text(text = "登陆", fontSize = 36.csp, color = Color.Black)
            }
        }
    }

    //监听multiUsers，当multiUsers存在是弹出弹窗
    MultiUserDialog(
        users = multiUsers,
        onDismiss = { multiUsers = null },
        onUserClick = { user ->
            multiUsers = null
            viewModel.login(phone, code, user.userid.toString())
        }
    )
}

@Composable
private fun LoginTextFieldColors(): TextFieldColors {
    return OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        focusedPlaceholderColor = Color.White,
        unfocusedPlaceholderColor = Color.White,
        focusedLabelColor = Color.White,
        unfocusedLabelColor = Color.White,
        unfocusedBorderColor = Color.White,
        focusedBorderColor = Color.White,
        cursorColor = Color.White
    )
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val api: LoginApi
) : BaseViewStateViewModel() {

    val loginResult = ViewStateMutableLiveData<BaseResult>()

    fun sendCode(phone: String): Boolean {
        if (!phone.isValidPhone()) {
            showToast("请输入正确手机号")
            return false
        }
        viewModelScope.launch {
            runCatching { api.sent(phone) }
                .onSuccess { showToast("验证码已发送") }
                .onFailure {
                    showToast("验证码发送失败")
                }
        }
        return true
    }

    fun login(username: String, code: String, userId: String? = null) {
        if (username.isEmpty()) {
            showToast("请输入手机号")
            return
        }
        if (code.isEmpty()) {
            showToast("请输入验证码")
            return
        }

        launch(loginResult) {
            val result = api.login(username, code, userId)
            AppGlobalData.sLoginData = result.dataAs<LoginData>()
            result
        }
    }
}

private fun String.isValidPhone(): Boolean {
    return matches(Regex("^1[3-9]\\d{9}$"))
}

//取出用户
private fun JsonElement?.toMultiUsers(): List<MultiUser> {
    return this?.let {
        runCatching {
            val infoList = it.asJsonObject.get("info_list")
            Gson().fromJson(infoList, Array<MultiUser>::class.java).toList()
        }.getOrNull()
    }.orEmpty()
}

@Composable
private fun MultiUserDialog(
    users: List<MultiUser>?,
    onDismiss: () -> Unit,
    onUserClick: (MultiUser) -> Unit
) {
    if (users.isNullOrEmpty()) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "请选择登录账号", fontSize = 18.sp)
        },
        text = {
            MultiUserColumn(
                users = users,
                modifier = Modifier.fillMaxWidth(),
                onUserClick = onUserClick
            )
        },
        confirmButton = {},
        dismissButton = {}
    )
}