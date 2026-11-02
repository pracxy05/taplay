package com.taplay.app.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.nfc.NfcAdapter
import android.provider.Settings
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.List
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taplay.app.ui.theme.TaplayCyan
import com.taplay.app.ui.theme.TaplayTextSecondary

enum class HomeBottomTab {
    HOME, WALLET, FORUM, CONTACT
}

@Composable
fun HomeScreen(
    onTapToPlay: () -> Unit,
    onViewTransactions: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenAvatar: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(HomeBottomTab.HOME) }
    var tapPressed by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val walletRepo = remember { com.taplay.app.data.repository.WalletRepository(context) }
    val userBalance by walletRepo.cachedBalance.collectAsState(initial = 540.0)
    val userName by walletRepo.cachedUsername.collectAsState(initial = "Praharsh")
    val nfcAdapter = remember { NfcAdapter.getDefaultAdapter(context) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF18131F),
                        Color(0xFF120F18),
                        Color(0xFF0C0A12)
                    )
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp),
            contentPadding = PaddingValues(top = 18.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Box {
                    HomeTopBar(
                        onMenuClick = { showMenu = true },
                        onProfileClick = onOpenProfile
                    )

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("🎭 My Avatar") },
                            onClick = {
                                showMenu = false
                                onOpenAvatar()
                            }
                        )
                    }
                }
            }

            item {
                LocationStrip()
            }

            item {
                HeroTapCard(
                    userName = "Praharsh",
                    tapPressed = tapPressed,
                    onTapCircleClick = {
                        tapPressed = true
                        if (nfcAdapter?.isEnabled == true) {
                            onTapToPlay()
                        } else {
                            context.findActivity()?.startActivity(
                                Intent(Settings.ACTION_NFC_SETTINGS)
                            )
                        }
                    }
                )
            }

            item {
                WalletOverviewCard(balance = userBalance)
            }

            item {
                AchievementSection()
            }

            item {
                ActiveSessionCard()
            }
        }

        NavigationBar(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .navigationBarsPadding()
                .clip(RoundedCornerShape(28.dp)),
            containerColor = Color(0xFF16111E),
            tonalElevation = 0.dp
        ) {
            NavigationBarItem(
                selected = selectedTab == HomeBottomTab.HOME,
                onClick = { selectedTab = HomeBottomTab.HOME },
                icon = {
                    Icon(
                        imageVector = if (selectedTab == HomeBottomTab.HOME)
                            Icons.Filled.Home else Icons.Outlined.Home,
                        contentDescription = "Home"
                    )
                },
                label = { Text("Home") },
                colors = bottomNavColors()
            )

            NavigationBarItem(
                selected = selectedTab == HomeBottomTab.WALLET,
                onClick = {
                    selectedTab = HomeBottomTab.WALLET
                    onViewTransactions()
                },
                icon = {
                    Icon(
                        imageVector = if (selectedTab == HomeBottomTab.WALLET)
                            Icons.Filled.List else Icons.Outlined.List,
                        contentDescription = "Wallet"
                    )
                },
                label = { Text("Wallet") },
                colors = bottomNavColors()
            )

            NavigationBarItem(
                selected = selectedTab == HomeBottomTab.FORUM,
                onClick = { selectedTab = HomeBottomTab.FORUM },
                icon = {
                    Icon(
                        imageVector = if (selectedTab == HomeBottomTab.FORUM)
                            Icons.Filled.Info else Icons.Outlined.Info,
                        contentDescription = "Forum"
                    )
                },
                label = { Text("Forum") },
                colors = bottomNavColors()
            )

            NavigationBarItem(
                selected = selectedTab == HomeBottomTab.CONTACT,
                onClick = { selectedTab = HomeBottomTab.CONTACT },
                icon = {
                    Icon(
                        imageVector = if (selectedTab == HomeBottomTab.CONTACT)
                            Icons.Filled.AccountCircle else Icons.Outlined.AccountCircle,
                        contentDescription = "Contact"
                    )
                },
                label = { Text("Contact") },
                colors = bottomNavColors()
            )
        }
    }
}

@Composable
private fun HomeTopBar(
    onMenuClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(
            onClick = onMenuClick,
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Color(0xFF1B1625))
        ) {
            Icon(
                imageVector = Icons.Filled.Menu,
                contentDescription = "Menu",
                tint = Color.White
            )
        }

        Text(
            text = "TAPLAY",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 4.sp
        )

        IconButton(
            onClick = onProfileClick,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color(0xFFF7F2EA))
        ) {
            Icon(
                imageVector = Icons.Filled.AccountCircle,
                contentDescription = "Profile",
                tint = Color(0xFF3B2D52)
            )
        }
    }
}

@Composable
private fun LocationStrip() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0x221EC8D7))
            .padding(horizontal = 18.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Current Location",
                color = TaplayTextSecondary,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Hyderabad • Arcade Arena",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Text(
            text = "Live",
            color = TaplayCyan,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun HeroTapCard(
    userName: String,
    tapPressed: Boolean,
    onTapCircleClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "tapPulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (tapPressed) 1.08f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tapScale"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(34.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF48C88))
    ) {
        Column(modifier = Modifier.padding(22.dp)) {
            Text(
                text = "Hi, $userName",
                color = Color(0xFF2E1830),
                fontSize = 25.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Ready to tap into your next game? Turn on NFC and start instantly.",
                color = Color(0xFF563649),
                fontSize = 13.sp,
                lineHeight = 19.sp
            )

            Spacer(modifier = Modifier.height(22.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = if (tapPressed) "NFC Ready" else "Tap to Enable NFC",
                        color = Color(0xFF2D1531),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (tapPressed)
                            "Now tap your phone on the machine tag"
                        else
                            "One press to begin the Taplay flow",
                        color = Color(0xFF694453),
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Box(
                    modifier = Modifier
                        .size(112.dp)
                        .scale(scale)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF7EE7F2),
                                    Color(0xFFB6F2F7),
                                    Color(0xFFF8FFFF)
                                )
                            )
                        )
                        .clickable { onTapCircleClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "TAP",
                            color = Color(0xFF102033),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp
                        )
                        Text(
                            text = "NFC",
                            color = Color(0xFF35526D),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WalletOverviewCard(balance: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFCBE0FF))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Wallet & Play Stats",
                color = Color(0xFF1D2D48),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SmallStatCard(
                    title = "Credit Balance",
                    value = "₹ ${balance.toInt()}",
                    bg = Color(0xFFF7F3EC),
                    modifier = Modifier.weight(1f)
                )
                SmallStatCard(
                    title = "Collected",
                    value = "86",
                    bg = Color.White,
                    modifier = Modifier.weight(1f)
                )
                SmallStatCard(
                    title = "Total Tokens",
                    value = "120",
                    bg = Color(0xFFFFF8EE),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFFEFF5FF))
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "Last Played",
                        color = Color(0xFF6A768D),
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Air Hockey Blitz",
                        color = Color(0xFF1E2E48),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Today • 18 Tokens • Won 2 bonus credits",
                        color = Color(0xFF61718A),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun SmallStatCard(
    title: String,
    value: String,
    bg: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 10.sp,
                color = Color(0xFF6E7283),
                textAlign = TextAlign.Center,
                lineHeight = 13.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                color = Color(0xFF1A1B2A),
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun AchievementSection() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Achievements",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 4.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AchievementCard(
                title = "Rank",
                value = "#18",
                bg = Color(0xFFF8F3EB),
                modifier = Modifier.weight(1f)
            )
            AchievementCard(
                title = "Badges",
                value = "12",
                bg = Color.White,
                modifier = Modifier.weight(1f)
            )
            AchievementCard(
                title = "Fav Game",
                value = "Hockey",
                bg = Color(0xFFFFF5E8),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun AchievementCard(
    title: String,
    value: String,
    bg: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = bg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                color = Color(0xFF767282),
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                color = Color(0xFF1C1830),
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ActiveSessionCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFB76AF0))
    ) {
        Column(modifier = Modifier.padding(22.dp)) {
            Text(
                text = "Active Session",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Current Timer",
                        color = Color(0xFFE9D7FA),
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "0 : 0",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color(0x33FFFFFF))
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "Waiting for game start",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun bottomNavColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = TaplayCyan,
    selectedTextColor = TaplayCyan,
    unselectedIconColor = Color(0xFF9088A6),
    unselectedTextColor = Color(0xFF9088A6),
    indicatorColor = Color(0x221EC8D7)
)

private fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}