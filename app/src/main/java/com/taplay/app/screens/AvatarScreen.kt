package com.taplay.app.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.animation.core.*
import androidx.compose.ui.zIndex
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taplay.app.ui.theme.*

// ── Character data model
data class AvatarCharacter(
    val id: String,
    val name: String,
    val title: String,
    val emoji: String,
    val primaryColor: Color,
    val accentColor: Color,
    val rarity: Rarity,
    val stats: CharStats,
    val unlocked: Boolean,
    val bodyShape: BodyShape,
    val auraType: AuraType
)

data class CharStats(
    val speed: Int,
    val power: Int,
    val luck: Int,
    val shield: Int
)

enum class Rarity(val label: String, val color: Color) {
    COMMON("Common",       Color(0xFF9E9E9E)),
    RARE("Rare",           Color(0xFF42A5F5)),
    EPIC("Epic",           Color(0xFFAB47BC)),
    LEGENDARY("Legendary", Color(0xFFFFB300))
}

enum class BodyShape { WARRIOR, MAGE, SPEEDER, TANK }
enum class AuraType  { FIRE, ICE, LIGHTNING, VOID, NATURE }

data class Accessory(
    val id: String,
    val label: String,
    val emoji: String,
    val unlocked: Boolean
)

// ── Full character roster
val allCharacters = listOf(
    AvatarCharacter(
        id = "blaze", name = "Blaze", title = "The Inferno Striker",
        emoji = "🔥", primaryColor = Color(0xFFFF5722), accentColor = Color(0xFFFF9800),
        rarity = Rarity.EPIC, stats = CharStats(88, 75, 60, 45),
        unlocked = true, bodyShape = BodyShape.WARRIOR, auraType = AuraType.FIRE
    ),
    AvatarCharacter(
        id = "frost", name = "Frost", title = "The Arctic Ghost",
        emoji = "❄️", primaryColor = Color(0xFF00BCD4), accentColor = Color(0xFF80DEEA),
        rarity = Rarity.RARE, stats = CharStats(72, 55, 80, 70),
        unlocked = true, bodyShape = BodyShape.SPEEDER, auraType = AuraType.ICE
    ),
    AvatarCharacter(
        id = "volt", name = "Volt", title = "The Storm Phantom",
        emoji = "⚡", primaryColor = Color(0xFFFFEB3B), accentColor = Color(0xFFFFF176),
        rarity = Rarity.LEGENDARY, stats = CharStats(95, 90, 70, 50),
        unlocked = true, bodyShape = BodyShape.MAGE, auraType = AuraType.LIGHTNING
    ),
    AvatarCharacter(
        id = "shadow", name = "Shadow", title = "The Void Walker",
        emoji = "🌑", primaryColor = Color(0xFF7C4DFF), accentColor = Color(0xFFB388FF),
        rarity = Rarity.LEGENDARY, stats = CharStats(80, 85, 95, 55),
        unlocked = false, bodyShape = BodyShape.MAGE, auraType = AuraType.VOID
    ),
    AvatarCharacter(
        id = "terra", name = "Terra", title = "The Iron Fortress",
        emoji = "🌿", primaryColor = Color(0xFF4CAF50), accentColor = Color(0xFFA5D6A7),
        rarity = Rarity.COMMON, stats = CharStats(50, 65, 55, 98),
        unlocked = true, bodyShape = BodyShape.TANK, auraType = AuraType.NATURE
    ),
    AvatarCharacter(
        id = "nova", name = "Nova", title = "The Cosmic Hunter",
        emoji = "✨", primaryColor = Color(0xFFE91E63), accentColor = Color(0xFFF48FB1),
        rarity = Rarity.EPIC, stats = CharStats(78, 82, 88, 62),
        unlocked = false, bodyShape = BodyShape.SPEEDER, auraType = AuraType.FIRE
    )
)

val allAccessories = listOf(
    Accessory("helm1",  "Flame Helm",    "🪖", unlocked = true),
    Accessory("helm2",  "Ice Crown",     "👑", unlocked = true),
    Accessory("helm3",  "Storm Hood",    "🎭", unlocked = false),
    Accessory("back1",  "Fire Wings",    "🪶", unlocked = true),
    Accessory("back2",  "Shadow Cloak",  "🦇", unlocked = false),
    Accessory("back3",  "Lightning Cape","⚡", unlocked = true),
    Accessory("trail1", "Flame Trail",   "🔥", unlocked = true),
    Accessory("trail2", "Ice Crystal",   "💎", unlocked = false),
    Accessory("trail3", "Star Dust",     "🌟", unlocked = true)
)

enum class AvatarTab(val label: String) {
    CHARACTER("Character"),
    ACCESSORIES("Gear"),
    STATS("Stats")
}

// ─────────────────────────────────────────────────────────────────
// MAIN AVATAR SCREEN
// ─────────────────────────────────────────────────────────────────
@Composable
fun AvatarScreen(
    preSelectedCharId: String? = null,
    onBack: () -> Unit,
    onCharacterConfirmed: (String) -> Unit = {}
) {
    var selectedChar by remember {
        mutableStateOf(
            allCharacters.find { it.id == preSelectedCharId && it.unlocked }
                ?: allCharacters.first { it.unlocked }
        )
    }
    var activeTab    by remember { mutableStateOf(AvatarTab.CHARACTER) }
    var selectedHelm  by remember { mutableStateOf<String?>(null) }
    var selectedBack  by remember { mutableStateOf<String?>(null) }
    var selectedTrail by remember { mutableStateOf<String?>(null) }
    var showSavedToast by remember { mutableStateOf(false) }

    LaunchedEffect(showSavedToast) {
        if (showSavedToast) {
            kotlinx.coroutines.delay(1800)
            showSavedToast = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0D0A18), Color(0xFF0F0C1F), Color(0xFF0A0814))
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            AvatarTopBar(
                characterName = selectedChar.name,
                onBack = onBack,
                onSave = {
                    onCharacterConfirmed(selectedChar.id)
                    showSavedToast = true
                }
            )

            CharacterShowcase(character = selectedChar)

            AvatarTabRow(activeTab = activeTab, onTabSelected = { activeTab = it })

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 24.dp)
            ) {
                AnimatedContent(
                    targetState = activeTab,
                    transitionSpec = {
                        (fadeIn(tween(280)) + slideInVertically { 30 }).togetherWith(
                            fadeOut(tween(180))
                        )
                    },
                    label = "avatarTab"
                ) { tab ->
                    when (tab) {
                        AvatarTab.CHARACTER -> CharacterSelectGrid(
                            characters = allCharacters,
                            selected   = selectedChar,
                            onSelect   = { if (it.unlocked) selectedChar = it }
                        )
                        AvatarTab.ACCESSORIES -> AccessoriesPanel(
                            accessories   = allAccessories,
                            selectedHelm  = selectedHelm,
                            selectedBack  = selectedBack,
                            selectedTrail = selectedTrail,
                            onHelmSelect  = { selectedHelm  = if (selectedHelm  == it) null else it },
                            onBackSelect  = { selectedBack  = if (selectedBack  == it) null else it },
                            onTrailSelect = { selectedTrail = if (selectedTrail == it) null else it }
                        )
                        AvatarTab.STATS -> StatsPanel(character = selectedChar)
                    }
                }
            }
        }

        // ── Saved toast
        AnimatedVisibility(
            visible  = showSavedToast,
            enter    = fadeIn() + slideInVertically { -40 },
            exit     = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 90.dp)
        ) {
            Surface(
                shape  = RoundedCornerShape(24.dp),
                color  = Color(0xFF00E5FF).copy(alpha = 0.15f),
                border = BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Filled.Check, null, tint = Color(0xFF00E5FF), modifier = Modifier.size(18.dp))
                    Text("Avatar saved!", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// TOP BAR
// ─────────────────────────────────────────────────────────────────
@Composable
private fun AvatarTopBar(
    characterName: String,
    onBack: () -> Unit,
    onSave: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Color(0xFF1E1930))
        ) {
            Icon(Icons.Filled.ArrowBack, "Back", tint = Color.White)
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text("My Avatar", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(
                "Playing as $characterName",
                color = Color(0xFF00E5FF),
                fontSize = 12.sp
            )
        }

        Button(
            onClick = onSave,
            shape  = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp)
        ) {
            Text("Save", color = Color(0xFF0D0A18), fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// 3D-STYLE CHARACTER SHOWCASE
// ─────────────────────────────────────────────────────────────────
@Composable
private fun CharacterShowcase(character: AvatarCharacter) {
    val infinite = rememberInfiniteTransition(label = "showcase")

    val floatY by infinite.animateFloat(
        initialValue = 0f, targetValue = -14f,
        animationSpec = infiniteRepeatable(tween(2200, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "float"
    )
    val glowPulse by infinite.animateFloat(
        initialValue = 0.45f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1800, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "glow"
    )
    val ringRot by infinite.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(9000, easing = LinearEasing), RepeatMode.Restart),
        label = "ring"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp),
        contentAlignment = Alignment.Center
    ) {
        // Rotating aura rings
        androidx.compose.foundation.Canvas(
            modifier = Modifier.size(210.dp).rotate(ringRot)
        ) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            listOf(0.33f, 0.52f, 0.72f).forEachIndexed { i, frac ->
                val r = size.minDimension * frac
                drawCircle(
                    color = character.primaryColor.copy(alpha = (0.14f - i * 0.03f) * glowPulse),
                    radius = r, center = Offset(cx, cy),
                    style = Stroke(width = 2.5f - i * 0.5f)
                )
            }
            // Orbital dot
            val orbR = size.minDimension * 0.52f
            drawCircle(
                color = character.accentColor.copy(alpha = 0.85f),
                radius = 5.5f, center = Offset(cx + orbR, cy)
            )
            drawCircle(
                color = character.primaryColor.copy(alpha = 0.6f),
                radius = 4f, center = Offset(cx - orbR * 0.6f, cy - orbR * 0.8f)
            )
        }

        // Drop shadow
        Box(
            modifier = Modifier
                .offset(y = 86.dp)
                .size(width = 90.dp, height = 14.dp)
                .clip(RoundedCornerShape(50))
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            character.primaryColor.copy(alpha = 0.35f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Floating character
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.offset(y = floatY.dp)
        ) {
            // Rarity badge
            Surface(
                shape  = RoundedCornerShape(12.dp),
                color  = character.rarity.color.copy(alpha = 0.18f),
                border = BorderStroke(1.dp, character.rarity.color.copy(alpha = 0.55f)),
                modifier = Modifier.padding(bottom = 6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (character.rarity == Rarity.LEGENDARY)
                        Icon(Icons.Filled.Star, null, tint = character.rarity.color, modifier = Modifier.size(10.dp))
                    Text(
                        text = character.rarity.label.uppercase(),
                        color = character.rarity.color,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.5.sp
                    )
                }
            }

            // Character emoji in glowing ring
            Box(
                modifier = Modifier
                    .size(114.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                character.primaryColor.copy(alpha = 0.32f * glowPulse),
                                character.primaryColor.copy(alpha = 0.08f),
                                Color.Transparent
                            )
                        )
                    )
                    .border(
                        width = 2.dp,
                        brush = Brush.linearGradient(
                            listOf(character.primaryColor, character.accentColor)
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = character.emoji, fontSize = 56.sp)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = character.name,
                color = Color.White,
                fontSize = 23.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
            Text(
                text = character.title,
                color = character.accentColor.copy(alpha = 0.75f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.5.sp
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// TAB ROW
// ─────────────────────────────────────────────────────────────────
@Composable
private fun AvatarTabRow(
    activeTab: AvatarTab,
    onTabSelected: (AvatarTab) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF1A1630)),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        AvatarTab.values().forEach { tab ->
            val isActive = tab == activeTab
            val bgColor by animateColorAsState(
                targetValue = if (isActive) Color(0xFF2D2650) else Color.Transparent,
                animationSpec = tween(250), label = "tabBg"
            )
            val textColor by animateColorAsState(
                targetValue = if (isActive) Color(0xFF00E5FF) else Color(0xFF6B7280),
                animationSpec = tween(250), label = "tabText"
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(bgColor)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onTabSelected(tab) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tab.label,
                    color = textColor,
                    fontSize = 13.sp,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// TAB 1 — CHARACTER SELECT GRID
// ─────────────────────────────────────────────────────────────────
@Composable
private fun CharacterSelectGrid(
    characters: List<AvatarCharacter>,
    selected: AvatarCharacter,
    onSelect: (AvatarCharacter) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            "Choose Your Fighter",
            color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        characters.chunked(2).forEach { rowChars ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowChars.forEach { char ->
                    CharacterCard(
                        character  = char,
                        isSelected = char.id == selected.id,
                        modifier   = Modifier.weight(1f),
                        onSelect   = { onSelect(char) }
                    )
                }
                if (rowChars.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun CharacterCard(
    character: AvatarCharacter,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onSelect: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.03f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "cardScale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isSelected)
                    Brush.linearGradient(
                        listOf(character.primaryColor.copy(0.25f), character.accentColor.copy(0.12f))
                    )
                else
                    Brush.linearGradient(listOf(Color(0xFF1E1A30), Color(0xFF1A1628)))
            )
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                brush = if (isSelected)
                    Brush.linearGradient(listOf(character.primaryColor, character.accentColor))
                else
                    Brush.linearGradient(listOf(Color(0xFF2D2A40), Color(0xFF2D2A40))),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onSelect() }
            .padding(12.dp)
    ) {
        // Lock overlay
        if (!character.unlocked) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF0D0A18).copy(alpha = 0.68f))
                    .zIndex(1f),
                contentAlignment = Alignment.Center
            ) { Text("🔒", fontSize = 28.sp) }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(character.rarity.color)
                )
            }

            Text(character.emoji, fontSize = 42.sp, modifier = Modifier.padding(vertical = 4.dp))

            Text(
                character.name,
                color = if (isSelected) Color.White else Color(0xFFCCCCCC),
                fontSize = 14.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center
            )
            Text(
                character.rarity.label,
                color = character.rarity.color, fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))
            MiniStatBar("SPD", character.stats.speed,  character.primaryColor)
            MiniStatBar("PWR", character.stats.power,  character.accentColor)
        }

        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(character.primaryColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(13.dp))
            }
        }
    }
}

@Composable
private fun MiniStatBar(label: String, value: Int, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(label, color = Color(0xFF6B7280), fontSize = 8.sp, modifier = Modifier.width(24.dp))
        Box(
            modifier = Modifier
                .weight(1f).height(3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color(0xFF2D2A40))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(value / 100f)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Brush.horizontalGradient(listOf(color.copy(0.7f), color)))
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// TAB 2 — ACCESSORIES PANEL
// ─────────────────────────────────────────────────────────────────
@Composable
private fun AccessoriesPanel(
    accessories: List<Accessory>,
    selectedHelm: String?,
    selectedBack: String?,
    selectedTrail: String?,
    onHelmSelect: (String) -> Unit,
    onBackSelect: (String) -> Unit,
    onTrailSelect: (String) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        AccessorySection("🪖  Helmets",    accessories.filter { it.id.startsWith("helm") },  selectedHelm,  onHelmSelect)
        Spacer(modifier = Modifier.height(20.dp))
        AccessorySection("🎒  Back Items", accessories.filter { it.id.startsWith("back") },  selectedBack,  onBackSelect)
        Spacer(modifier = Modifier.height(20.dp))
        AccessorySection("✨  Trails",     accessories.filter { it.id.startsWith("trail") }, selectedTrail, onTrailSelect)
    }
}

@Composable
private fun AccessorySection(
    title: String,
    items: List<Accessory>,
    selected: String?,
    onSelect: (String) -> Unit
) {
    Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 10.dp))

    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(items) { acc ->
            val isSelected = acc.id == selected
            Box(
                modifier = Modifier
                    .size(74.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isSelected) Color(0xFF2D2A50) else Color(0xFF1A1630))
                    .border(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) Color(0xFF00E5FF) else Color(0xFF2D2A40),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { if (acc.unlocked) onSelect(acc.id) }
                    .alpha(if (acc.unlocked) 1f else 0.4f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(acc.emoji, fontSize = 28.sp)
                    Text(
                        acc.label.take(8),
                        color = if (isSelected) Color(0xFF00E5FF) else Color(0xFF6B7280),
                        fontSize = 7.sp, textAlign = TextAlign.Center, maxLines = 1
                    )
                }
                if (!acc.unlocked)
                    Text("🔒", fontSize = 13.sp, modifier = Modifier.align(Alignment.TopEnd).padding(3.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// TAB 3 — STATS PANEL
// ─────────────────────────────────────────────────────────────────
@Composable
private fun StatsPanel(character: AvatarCharacter) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {

        // Character bio card
        Surface(
            shape  = RoundedCornerShape(16.dp),
            color  = Color(0xFF1A1630),
            border = BorderStroke(1.dp, character.primaryColor.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(character.emoji, fontSize = 44.sp)
                Column {
                    Text(character.name, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
                    Text(character.title, color = character.accentColor, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        TagChip(character.bodyShape.name.lowercase().replaceFirstChar { it.uppercase() }, character.primaryColor)
                        TagChip(character.auraType.name.lowercase().replaceFirstChar { it.uppercase() } + " Aura", character.accentColor)
                    }
                }
            }
        }

        Text("Base Stats", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))

        listOf(
            Triple("⚡  Speed",  character.stats.speed,  character.primaryColor),
            Triple("💥  Power",  character.stats.power,  Color(0xFFFF5722)),
            Triple("🍀  Luck",   character.stats.luck,   Color(0xFF4CAF50)),
            Triple("🛡️  Shield", character.stats.shield, Color(0xFF42A5F5))
        ).forEach { (label, value, color) ->
            FullStatBar(label = label, value = value, color = color)
            Spacer(modifier = Modifier.height(10.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text("Play Tips", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 10.dp))

        listOf(
            "💡  Master ${character.name}'s special ability early — it recharges faster than you think",
            "🎯  ${character.name} performs best in fast-paced games like racing and air hockey",
            "🔥  Chain combos: tap quickly in rhythm to activate ${character.name}'s bonus multiplier",
            "🏆  Challenge your friends — ${character.name}'s aura gives a +15% boost in multiplayer"
        ).forEach { tip ->
            TipCard(text = tip, accentColor = character.accentColor)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun TagChip(label: String, color: Color) {
    Surface(
        shape  = RoundedCornerShape(8.dp),
        color  = color.copy(alpha = 0.15f),
        border = BorderStroke(0.5.dp, color.copy(alpha = 0.4f))
    ) {
        Text(
            text  = label, color = color,
            fontSize = 9.sp, fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun FullStatBar(label: String, value: Int, color: Color) {
    val animWidth by animateFloatAsState(
        targetValue = value / 100f,
        animationSpec = tween(900, easing = EaseOutCubic),
        label = "statW"
    )
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, color = Color(0xFFCCCCCC), fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Text("$value", color = color, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth().height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF2D2A40))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animWidth)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Brush.horizontalGradient(listOf(color.copy(0.7f), color)))
            )
        }
    }
}

@Composable
private fun TipCard(text: String, accentColor: Color) {
    Surface(
        shape  = RoundedCornerShape(12.dp),
        color  = Color(0xFF1A1630),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.2f))
    ) {
        Text(
            text  = text,
            color = Color(0xFFB0B8CC),
            fontSize = 12.sp,
            lineHeight = 18.sp,
            modifier = Modifier.padding(12.dp)
        )
    }
}