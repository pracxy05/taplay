package com.taplay.app.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taplay.app.ui.theme.*

// ── Which tab is active
enum class AuthTab { LOGIN, SIGNUP }

// ── Sign-up steps
enum class SignUpStep { CHOOSE_METHOD, ENTER_CONTACT, VERIFY_OTP, SET_CREDENTIALS }

// ── Contact method choice
enum class ContactMethod { PHONE, EMAIL }

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {

    var activeTab by remember { mutableStateOf(AuthTab.LOGIN) }

    Box(modifier = Modifier.fillMaxSize()) {

        // ── TOP HALF — dark gradient with logo
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.42f)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF12093A), Color(0xFF1A0F4A))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // NFC watermark behind logo text
                Text(
                    text = "TAPLAY",
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Black,
                    color = TaplayTextPrimary,
                    letterSpacing = 8.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Tap. Play. Win.",
                    fontSize = 13.sp,
                    color = TaplayCyan,
                    letterSpacing = 3.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // ── BOTTOM CARD — rounded white-ish surface sliding up
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.72f)
                .align(Alignment.BottomCenter)
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(Color(0xFF1C1530))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 28.dp)
                    .padding(top = 28.dp, bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // ── LOGIN / SIGN UP tab toggle
                TabToggle(activeTab = activeTab, onTabSelected = { activeTab = it })

                Spacer(modifier = Modifier.height(28.dp))

                // ── Content switches based on tab
                AnimatedContent(
                    targetState = activeTab,
                    transitionSpec = {
                        if (targetState == AuthTab.SIGNUP) {
                            (slideInHorizontally { it } + fadeIn(tween(280))).togetherWith(
                                slideOutHorizontally { -it } + fadeOut(tween(280))
                            )
                        } else {
                            (slideInHorizontally { -it } + fadeIn(tween(280))).togetherWith(
                                slideOutHorizontally { it } + fadeOut(tween(280))
                            )
                        }
                    },
                    label = "authTab"
                ) { tab ->
                    when (tab) {
                        AuthTab.LOGIN  -> LoginForm(onLoginSuccess = onLoginSuccess)
                        AuthTab.SIGNUP -> SignUpFlow(onSignUpSuccess = onLoginSuccess)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ── Divider
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Divider(modifier = Modifier.weight(1f), color = TaplayDivider)
                    Text(
                        text = "  or  ",
                        fontSize = 12.sp,
                        color = TaplayTextSecondary
                    )
                    Divider(modifier = Modifier.weight(1f), color = TaplayDivider)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ── Google Sign-In button
                OutlinedButton(
                    onClick = { onLoginSuccess() },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, TaplayDivider),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color(0xFF231C3A)
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Google "G" drawn with Text — no image needed
                        Text(
                            text = "G",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4285F4)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Continue with Google",
                            fontSize = 14.sp,
                            color = TaplayTextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // ── Continue as Guest
                TextButton(
                    onClick = { onLoginSuccess() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Continue as Guest  →",
                        fontSize = 13.sp,
                        color = TaplayTextSecondary,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────
// LOGIN FORM
// ─────────────────────────────────────────
@Composable
fun LoginForm(onLoginSuccess: () -> Unit) {
    val focusManager = LocalFocusManager.current
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showForgotDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {

        // Username field
        TaplayTextField(
            value = username,
            onValueChange = { username = it },
            label = "Username",
            leadingIcon = Icons.Default.Person,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Password field
        TaplayTextField(
            value = password,
            onValueChange = { password = it },
            label = "Password",
            leadingIcon = Icons.Default.Lock,
            isPassword = true,
            passwordVisible = passwordVisible,
            onPasswordToggle = { passwordVisible = !passwordVisible },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus(); onLoginSuccess() }
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Forgot password
        Text(
            text = "Forgot Password?",
            fontSize = 12.sp,
            color = TaplayCyan,
            modifier = Modifier
                .align(Alignment.End)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { showForgotDialog = true }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Login button
        Button(
            onClick = { onLoginSuccess() },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TaplayCyan)
        ) {
            Text(
                text = "LOG IN",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                color = TaplayBgDeep
            )
        }
    }

    // Forgot Password Dialog
    if (showForgotDialog) {
        ForgotPasswordDialog(onDismiss = { showForgotDialog = false })
    }
}

// ─────────────────────────────────────────
// SIGN UP FLOW  (multi-step)
// ─────────────────────────────────────────
@Composable
fun SignUpFlow(onSignUpSuccess: () -> Unit) {
    var step by remember { mutableStateOf(SignUpStep.CHOOSE_METHOD) }
    var contactMethod by remember { mutableStateOf(ContactMethod.PHONE) }
    var contactValue by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    AnimatedContent(
        targetState = step,
        transitionSpec = {
            (slideInHorizontally { it } + fadeIn(tween(280))).togetherWith(
                slideOutHorizontally { -it } + fadeOut(tween(280))
            )
        },
        label = "signupStep"
    ) { currentStep ->
        when (currentStep) {

            // ── Step 1: Choose Phone or Email
            SignUpStep.CHOOSE_METHOD -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Create your account",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TaplayTextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "How do you want to verify?",
                        fontSize = 13.sp,
                        color = TaplayTextSecondary
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    // Phone option
                    ContactMethodCard(
                        icon = "📱",
                        label = "Mobile Number",
                        desc = "We'll send an OTP to verify",
                        selected = contactMethod == ContactMethod.PHONE,
                        onClick = { contactMethod = ContactMethod.PHONE }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Email option
                    ContactMethodCard(
                        icon = "✉️",
                        label = "Email Address",
                        desc = "We'll send a verification link",
                        selected = contactMethod == ContactMethod.EMAIL,
                        onClick = { contactMethod = ContactMethod.EMAIL }
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    Button(
                        onClick = { step = SignUpStep.ENTER_CONTACT },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TaplayCyan)
                    ) {
                        Text(
                            "CONTINUE",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            color = TaplayBgDeep
                        )
                    }
                }
            }

            // ── Step 2: Enter phone/email
            SignUpStep.ENTER_CONTACT -> {
                val focusManager = LocalFocusManager.current
                Column(modifier = Modifier.fillMaxWidth()) {
                    StepHeader(
                        title = if (contactMethod == ContactMethod.PHONE)
                            "Enter your mobile number"
                        else "Enter your email address",
                        onBack = { step = SignUpStep.CHOOSE_METHOD }
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    TaplayTextField(
                        value = contactValue,
                        onValueChange = { contactValue = it },
                        label = if (contactMethod == ContactMethod.PHONE)
                            "Mobile Number" else "Email Address",
                        leadingIcon = if (contactMethod == ContactMethod.PHONE)
                            Icons.Default.Phone else Icons.Default.Email,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = if (contactMethod == ContactMethod.PHONE)
                                KeyboardType.Phone else KeyboardType.Email,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus() }
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { step = SignUpStep.VERIFY_OTP },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TaplayCyan),
                        enabled = contactValue.isNotBlank()
                    ) {
                        Text(
                            "SEND OTP",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            color = TaplayBgDeep
                        )
                    }
                }
            }

            // ── Step 3: Verify OTP
            SignUpStep.VERIFY_OTP -> {
                Column(modifier = Modifier.fillMaxWidth()) {
                    StepHeader(
                        title = "Enter verification code",
                        onBack = { step = SignUpStep.ENTER_CONTACT }
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Sent to $contactValue",
                        fontSize = 12.sp,
                        color = TaplayCyan,
                        modifier = Modifier.padding(start = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // 6-digit OTP boxes
                    OtpInputRow(
                        value = otp,
                        onValueChange = { if (it.length <= 6) otp = it }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Resend OTP",
                        fontSize = 12.sp,
                        color = TaplayTextSecondary,
                        modifier = Modifier
                            .align(Alignment.End)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { otp = "" }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { step = SignUpStep.SET_CREDENTIALS },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TaplayCyan),
                        enabled = otp.length == 6
                    ) {
                        Text(
                            "VERIFY",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            color = TaplayBgDeep
                        )
                    }
                }
            }

            // ── Step 4: Set username + password
            SignUpStep.SET_CREDENTIALS -> {
                val focusManager = LocalFocusManager.current
                Column(modifier = Modifier.fillMaxWidth()) {
                    StepHeader(
                        title = "Set your credentials",
                        onBack = { step = SignUpStep.VERIFY_OTP }
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    TaplayTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = "Choose a Username",
                        leadingIcon = Icons.Default.Person,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    TaplayTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Create Password",
                        leadingIcon = Icons.Default.Lock,
                        isPassword = true,
                        passwordVisible = passwordVisible,
                        onPasswordToggle = { passwordVisible = !passwordVisible },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus() }
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { onSignUpSuccess() },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TaplayCyan),
                        enabled = username.isNotBlank() && password.length >= 6
                    ) {
                        Text(
                            "CREATE ACCOUNT",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            color = TaplayBgDeep
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────
// REUSABLE COMPONENTS
// ─────────────────────────────────────────

@Composable
fun TabToggle(activeTab: AuthTab, onTabSelected: (AuthTab) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF120E28)),
        horizontalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        listOf(AuthTab.LOGIN to "Log In", AuthTab.SIGNUP to "Sign Up").forEach { (tab, label) ->
            val isActive = activeTab == tab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isActive) TaplayCyan else Color.Transparent)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onTabSelected(tab) }
                    .padding(vertical = 13.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    fontSize = 14.sp,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                    color = if (isActive) TaplayBgDeep else TaplayTextSecondary
                )
            }
        }
    }
}

@Composable
fun TaplayTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onPasswordToggle: (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 13.sp) },
        leadingIcon = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = TaplayTextSecondary,
                modifier = Modifier.size(20.dp)
            )
        },
        trailingIcon = if (isPassword) {
            {
                IconButton(onClick = { onPasswordToggle?.invoke() }) {
                    Icon(
                        imageVector = if (passwordVisible)
                            Icons.Filled.Lock else Icons.Filled.Person,
                        contentDescription = null,
                        tint = TaplayTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        } else null,
        visualTransformation = if (isPassword && !passwordVisible)
            PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = TaplayCyan,
            unfocusedBorderColor = TaplayDivider,
            focusedLabelColor = TaplayCyan,
            unfocusedLabelColor = TaplayTextSecondary,
            cursorColor = TaplayCyan,
            focusedTextColor = TaplayTextPrimary,
            unfocusedTextColor = TaplayTextPrimary,
            focusedContainerColor = Color(0xFF1A1430),
            unfocusedContainerColor = Color(0xFF1A1430)
        )
    )
}

@Composable
fun ContactMethodCard(
    icon: String,
    label: String,
    desc: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (selected) TaplayCyan else TaplayDivider
    val bgColor = if (selected) TaplayCyan.copy(alpha = 0.08f) else Color(0xFF1A1430)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .then(
                Modifier.border(
                    width = if (selected) 1.5.dp else 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(14.dp)
                )
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(text = icon, fontSize = 26.sp)
        Column {
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TaplayTextPrimary
            )
            Text(
                text = desc,
                fontSize = 12.sp,
                color = TaplayTextSecondary
            )
        }
        if (selected) {
            Spacer(modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(TaplayCyan),
                contentAlignment = Alignment.Center
            ) {
                Text("✓", fontSize = 11.sp, color = TaplayBgDeep, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun OtpInputRow(value: String, onValueChange: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(6) { index ->
            val char = value.getOrNull(index)?.toString() ?: ""
            val isFocused = value.length == index

            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF1A1430))
                    .then(
                        Modifier.border(
                            width = if (isFocused) 1.5.dp else 1.dp,
                            color = if (char.isNotEmpty()) TaplayCyan
                            else if (isFocused) TaplayCyan
                            else TaplayDivider,
                            shape = RoundedCornerShape(10.dp)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = char,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TaplayTextPrimary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    // Hidden real input behind the boxes
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .size(1.dp)
            .alpha(0f),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.NumberPassword,
            imeAction = ImeAction.Done
        )
    )
}

@Composable
fun StepHeader(title: String, onBack: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "←",
            fontSize = 18.sp,
            color = TaplayCyan,
            modifier = Modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onBack
                )
                .padding(end = 12.dp)
        )
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = TaplayTextPrimary
        )
    }
}

@Composable
fun ForgotPasswordDialog(onDismiss: () -> Unit) {
    var contact by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1C1530),
        title = {
            Text(
                "Reset Password",
                color = TaplayTextPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    "Enter your email or phone number and we'll send you a reset link.",
                    fontSize = 13.sp,
                    color = TaplayTextSecondary,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(14.dp))
                TaplayTextField(
                    value = contact,
                    onValueChange = { contact = it },
                    label = "Email or Phone",
                    leadingIcon = Icons.Default.Email
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = TaplayCyan),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Send Reset Link", color = TaplayBgDeep, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TaplayTextSecondary)
            }
        }
    )
}