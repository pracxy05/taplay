package com.taplay.app.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.taplay.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

enum class PaymentMethod(val label: String, val emoji: String, val subtitle: String) {
    UPI("UPI",             "📲", "Google Pay, PhonePe, Paytm"),
    CARD("Debit / Credit", "💳", "Visa, Mastercard, RuPay"),
    NETBANKING("Net Banking","🏦","All major banks supported"),
    WALLET("Other Wallets","👜", "Amazon Pay, Mobikwik")
}

val topUpPresets = listOf(50, 100, 200, 500)

data class TxnRecord(
    val id: String,
    val title: String,
    val subtitle: String,
    val amount: Int,
    val timestamp: Long,
    val status: TxnStatus,
    val type: TxnType
)

enum class TxnStatus { SUCCESS, PENDING, FAILED }
enum class TxnType   { TOPUP, GAME_PLAY, REFUND }

val mockTransactions = listOf(
    TxnRecord("txn001","Air Hockey Blitz",  "Machine #A14 · Blaze", -10, System.currentTimeMillis()-3_600_000,   TxnStatus.SUCCESS, TxnType.GAME_PLAY),
    TxnRecord("txn002","Wallet Top-Up",     "via UPI · success",   +200, System.currentTimeMillis()-86_400_000,  TxnStatus.SUCCESS, TxnType.TOPUP),
    TxnRecord("txn003","Racing Turbo",      "Machine #B02 · Frost", -15, System.currentTimeMillis()-90_000_000,  TxnStatus.SUCCESS, TxnType.GAME_PLAY),
    TxnRecord("txn004","Wallet Top-Up",     "via Debit Card · fail",+0,  System.currentTimeMillis()-100_000_000, TxnStatus.FAILED,  TxnType.TOPUP),
    TxnRecord("txn005","Wallet Top-Up",     "via UPI · success",   +100, System.currentTimeMillis()-172_800_000, TxnStatus.SUCCESS, TxnType.TOPUP),
    TxnRecord("txn006","Pinball Classic",   "Machine #C07 · Volt",  -8,  System.currentTimeMillis()-200_000_000, TxnStatus.SUCCESS, TxnType.GAME_PLAY),
    TxnRecord("txn007","Refund · cancelled","Air Hockey session",  +10,  System.currentTimeMillis()-250_000_000, TxnStatus.SUCCESS, TxnType.REFUND)
)

enum class WalletStage {
    HOME, SELECT_AMOUNT, SELECT_METHOD, PROCESSING, SUCCESS, HISTORY
}

@Composable
fun TransactionScreen(onBack: () -> Unit) {
    var stage          by remember { mutableStateOf(WalletStage.HOME) }
    var selectedAmt    by remember { mutableStateOf(100) }
    var customAmt      by remember { mutableStateOf("") }
    var selectedMethod by remember { mutableStateOf(PaymentMethod.UPI) }
    var upiId          by remember { mutableStateOf("") }
    var currentBalance by remember { mutableStateOf(85) }
    var lastTxnId      by remember { mutableStateOf("") }

    LaunchedEffect(stage) {
        if (stage == WalletStage.PROCESSING) {
            delay(2800)
            currentBalance += selectedAmt
            lastTxnId = "RZP${System.currentTimeMillis().toString().takeLast(8)}"
            stage = WalletStage.SUCCESS
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color(0xFF0D0A18), Color(0xFF0F0C1F), Color(0xFF0A0814)))
        )
    ) {
        AnimatedContent(
            targetState = stage,
            transitionSpec = {
                if (targetState.ordinal > initialState.ordinal) {
                    (fadeIn(tween(320)) + slideInHorizontally { it / 2 }).togetherWith(
                        fadeOut(tween(200)) + slideOutHorizontally { -it / 2 })
                } else {
                    (fadeIn(tween(320)) + slideInHorizontally { -it / 2 }).togetherWith(
                        fadeOut(tween(200)) + slideOutHorizontally { it / 2 })
                }
            }, label = "walletStage"
        ) { s ->
            when (s) {
                WalletStage.HOME -> WalletHome(
                    balance    = currentBalance,
                    onAddMoney = { stage = WalletStage.SELECT_AMOUNT },
                    onHistory  = { stage = WalletStage.HISTORY },
                    onBack     = onBack,
                    recentTxns = mockTransactions.take(3)
                )
                WalletStage.SELECT_AMOUNT -> SelectAmountScreen(
                    presetAmounts = topUpPresets,
                    selectedAmt   = selectedAmt,
                    customAmt     = customAmt,
                    onPresetPick  = { selectedAmt = it; customAmt = "" },
                    onCustomType  = { customAmt = it; if (it.isNotEmpty()) selectedAmt = it.toIntOrNull() ?: selectedAmt },
                    onNext        = { stage = WalletStage.SELECT_METHOD },
                    onBack        = { stage = WalletStage.HOME }
                )
                WalletStage.SELECT_METHOD -> SelectMethodScreen(
                    amount         = selectedAmt,
                    selectedMethod = selectedMethod,
                    upiId          = upiId,
                    onMethodPick   = { selectedMethod = it },
                    onUpiType      = { upiId = it },
                    onConfirmPay   = { stage = WalletStage.PROCESSING },
                    onBack         = { stage = WalletStage.SELECT_AMOUNT }
                )
                WalletStage.PROCESSING -> ProcessingOverlay()
                WalletStage.SUCCESS -> SuccessReceipt(
                    amount     = selectedAmt,
                    method     = selectedMethod,
                    newBalance = currentBalance,
                    txnId      = lastTxnId,
                    onDone     = { stage = WalletStage.HOME }
                )
                WalletStage.HISTORY -> HistoryScreen(
                    transactions = mockTransactions,
                    onBack       = { stage = WalletStage.HOME }
                )
            }
        }
    }
}

// ── WALLET HOME
@Composable
private fun WalletHome(
    balance: Int, onAddMoney: () -> Unit, onHistory: () -> Unit,
    onBack: () -> Unit, recentTxns: List<TxnRecord>
) {
    val infinite = rememberInfiniteTransition(label = "wg")
    val glowAlpha by infinite.animateFloat(
        initialValue = 0.4f, targetValue = 0.85f,
        animationSpec = infiniteRepeatable(tween(2000, easing = EaseInOutSine), RepeatMode.Reverse), label = "g"
    )

    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal=16.dp,vertical=16.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick=onBack, modifier=Modifier.size(42.dp).clip(CircleShape).background(Color(0xFF1E1930))) {
                Icon(Icons.Filled.ArrowBack,"Back",tint=Color.White)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Taplay Wallet",color=Color.White,fontSize=20.sp,fontWeight=FontWeight.Bold)
                Text("Cashless arcade credits",color=Color(0xFF6B7280),fontSize=12.sp)
            }
            IconButton(onClick=onHistory, modifier=Modifier.size(42.dp).clip(CircleShape).background(Color(0xFF1E1930))) {
                Icon(Icons.Filled.List,"History",tint=Color(0xFF00E5FF))
            }
        }

        // Balance card
        Box(
            modifier = Modifier.fillMaxWidth().padding(horizontal=20.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Brush.linearGradient(listOf(Color(0xFF1A1040),Color(0xFF0F1A35),Color(0xFF101028))))
                .border(1.5.dp, Brush.linearGradient(listOf(Color(0xFF00E5FF).copy(alpha=glowAlpha),Color(0xFF7C3AED).copy(alpha=0.4f))), RoundedCornerShape(24.dp))
                .padding(24.dp)
        ) {
            Column {
                Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.SpaceBetween, verticalAlignment=Alignment.Top) {
                    Column {
                        Text("Available Balance",color=Color(0xFF9CA3AF),fontSize=12.sp,letterSpacing=1.sp)
                        Spacer(Modifier.height(6.dp))
                        Row(verticalAlignment=Alignment.Bottom) {
                            Text("₹",color=Color(0xFF00E5FF),fontSize=22.sp,fontWeight=FontWeight.Bold)
                            Spacer(Modifier.width(4.dp))
                            Text("$balance",color=Color.White,fontSize=52.sp,fontWeight=FontWeight.Black,lineHeight=54.sp)
                        }
                        Text("≈ ${balance/10} game sessions",color=Color(0xFF6B7280),fontSize=11.sp,modifier=Modifier.padding(top=2.dp))
                    }
                    Box(Modifier.size(52.dp).clip(CircleShape).background(Color(0xFF00E5FF).copy(0.12f)).border(1.dp,Color(0xFF00E5FF).copy(0.3f),CircleShape),Alignment.Center) {
                        Text("💳",fontSize=24.sp)
                    }
                }
                Spacer(Modifier.height(20.dp))
                HorizontalDivider(color=Color(0xFF2D2A40))
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.SpaceEvenly) {
                    WalletStat("Total Added","₹300",Color(0xFF4CAF50))
                    Box(Modifier.width(1.dp).height(36.dp).background(Color(0xFF2D2A40)))
                    WalletStat("Total Spent","₹215",Color(0xFFFF5722))
                    Box(Modifier.width(1.dp).height(36.dp).background(Color(0xFF2D2A40)))
                    WalletStat("Sessions","14",Color(0xFF00E5FF))
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick=onAddMoney,
            modifier=Modifier.fillMaxWidth().padding(horizontal=20.dp).height(54.dp),
            shape=RoundedCornerShape(27.dp),
            colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF00E5FF))
        ) {
            Icon(Icons.Filled.Add,null,tint=Color(0xFF0D0A18),modifier=Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Add Money",color=Color(0xFF0D0A18),fontWeight=FontWeight.Bold,fontSize=16.sp,letterSpacing=0.5.sp)
        }

        Spacer(Modifier.height(24.dp))

        Row(Modifier.fillMaxWidth().padding(horizontal=20.dp), horizontalArrangement=Arrangement.SpaceBetween, verticalAlignment=Alignment.CenterVertically) {
            Text("Recent Activity",color=Color.White,fontSize=15.sp,fontWeight=FontWeight.Bold)
            TextButton(onClick=onHistory) { Text("See All",color=Color(0xFF00E5FF),fontSize=12.sp) }
        }
        Spacer(Modifier.height(8.dp))
        recentTxns.forEach { TxnRow(txn=it, modifier=Modifier.padding(horizontal=16.dp,vertical=4.dp)) }

        Spacer(Modifier.weight(1f))
        Row(Modifier.fillMaxWidth().padding(bottom=24.dp), horizontalArrangement=Arrangement.Center, verticalAlignment=Alignment.CenterVertically) {
            Text("🔒",fontSize=12.sp)
            Spacer(Modifier.width(6.dp))
            Text("Secured by Razorpay · PCI-DSS Compliant",color=Color(0xFF4B5563),fontSize=11.sp)
        }
    }
}

@Composable
private fun WalletStat(label:String,value:String,color:Color) {
    Column(horizontalAlignment=Alignment.CenterHorizontally) {
        Text(value,color=color,fontSize=15.sp,fontWeight=FontWeight.Bold)
        Text(label,color=Color(0xFF6B7280),fontSize=9.sp,letterSpacing=0.5.sp)
    }
}

// ── SELECT AMOUNT
@Composable
private fun SelectAmountScreen(
    presetAmounts:List<Int>, selectedAmt:Int, customAmt:String,
    onPresetPick:(Int)->Unit, onCustomType:(String)->Unit,
    onNext:()->Unit, onBack:()->Unit
) {
    Column(modifier=Modifier.fillMaxSize().padding(horizontal=20.dp)) {
        StageTopBar("Add Money","How much do you want to add?",onBack)
        Spacer(Modifier.height(8.dp))
        Text("Quick Select",color=Color(0xFF9CA3AF),fontSize=12.sp,letterSpacing=1.sp)
        Spacer(Modifier.height(12.dp))

        presetAmounts.chunked(2).forEach { row ->
            Row(Modifier.fillMaxWidth().padding(bottom=12.dp), horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                row.forEach { amt ->
                    val isSel = amt==selectedAmt && customAmt.isEmpty()
                    Box(
                        modifier=Modifier.weight(1f).clip(RoundedCornerShape(16.dp))
                            .background(if(isSel) Color(0xFF00E5FF).copy(0.15f) else Color(0xFF1A1630))
                            .border(if(isSel) 1.5.dp else 1.dp, if(isSel) Color(0xFF00E5FF) else Color(0xFF2D2A40), RoundedCornerShape(16.dp))
                            .clickable(interactionSource=remember{MutableInteractionSource()},indication=null) { onPresetPick(amt) }
                            .padding(vertical=18.dp),
                        contentAlignment=Alignment.Center
                    ) {
                        Column(horizontalAlignment=Alignment.CenterHorizontally) {
                            Text("₹$amt",color=if(isSel) Color(0xFF00E5FF) else Color.White,fontSize=20.sp,fontWeight=FontWeight.Black)
                            Text("≈ ${amt/10} sessions",color=Color(0xFF6B7280),fontSize=10.sp)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Text("Or Enter Amount",color=Color(0xFF9CA3AF),fontSize=12.sp,letterSpacing=1.sp)
        Spacer(Modifier.height(10.dp))

        OutlinedTextField(
            value=customAmt, onValueChange={ if(it.length<=5 && (it.isEmpty()||it.all{c->c.isDigit()})) onCustomType(it) },
            modifier=Modifier.fillMaxWidth(),
            placeholder={ Text("Enter amount (min ₹10)",color=Color(0xFF4B5563)) },
            leadingIcon={ Text("₹",color=Color(0xFF00E5FF),fontSize=18.sp,fontWeight=FontWeight.Bold,modifier=Modifier.padding(start=4.dp)) },
            singleLine=true, shape=RoundedCornerShape(14.dp),
            colors=OutlinedTextFieldDefaults.colors(
                focusedBorderColor=Color(0xFF00E5FF), unfocusedBorderColor=Color(0xFF2D2A40),
                focusedTextColor=Color.White, unfocusedTextColor=Color.White,
                cursorColor=Color(0xFF00E5FF), focusedContainerColor=Color(0xFF1A1630), unfocusedContainerColor=Color(0xFF1A1630)
            )
        )

        Spacer(Modifier.weight(1f))

        val finalAmt = if(customAmt.isNotEmpty()) customAmt.toIntOrNull()?:0 else selectedAmt

        Surface(shape=RoundedCornerShape(12.dp),color=Color(0xFF1A1630),border=BorderStroke(1.dp,Color(0xFF2D2A40)),modifier=Modifier.fillMaxWidth().padding(bottom=16.dp)) {
            Row(Modifier.padding(16.dp), horizontalArrangement=Arrangement.SpaceBetween, verticalAlignment=Alignment.CenterVertically) {
                Text("You will add",color=Color(0xFF9CA3AF),fontSize=13.sp)
                Text("₹$finalAmt credits",color=Color(0xFF00E5FF),fontSize=16.sp,fontWeight=FontWeight.Black)
            }
        }

        Button(
            onClick=onNext, enabled=finalAmt>=10,
            modifier=Modifier.fillMaxWidth().height(54.dp),
            shape=RoundedCornerShape(27.dp),
            colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF00E5FF),disabledContainerColor=Color(0xFF1A1630))
        ) {
            Text("Continue to Payment",color=if(finalAmt>=10) Color(0xFF0D0A18) else Color(0xFF4B5563),fontWeight=FontWeight.Bold,fontSize=15.sp)
        }
        Spacer(Modifier.height(24.dp))
    }
}

// ── SELECT PAYMENT METHOD
@Composable
private fun SelectMethodScreen(
    amount:Int, selectedMethod:PaymentMethod, upiId:String,
    onMethodPick:(PaymentMethod)->Unit, onUpiType:(String)->Unit,
    onConfirmPay:()->Unit, onBack:()->Unit
) {
    Column(modifier=Modifier.fillMaxSize().padding(horizontal=20.dp)) {
        StageTopBar("Payment Method","Choose how to pay ₹$amount",onBack)
        Spacer(Modifier.height(8.dp))

        PaymentMethod.values().forEach { method ->
            val isSel = method==selectedMethod
            Surface(
                modifier=Modifier.fillMaxWidth().padding(bottom=10.dp).clickable(interactionSource=remember{MutableInteractionSource()},indication=null) { onMethodPick(method) },
                shape=RoundedCornerShape(16.dp),
                color=if(isSel) Color(0xFF1E1A40) else Color(0xFF1A1630),
                border=BorderStroke(if(isSel) 1.5.dp else 1.dp, if(isSel) Color(0xFF00E5FF) else Color(0xFF2D2A40))
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment=Alignment.CenterVertically, horizontalArrangement=Arrangement.spacedBy(14.dp)) {
                    Text(method.emoji,fontSize=26.sp)
                    Column(Modifier.weight(1f)) {
                        Text(method.label,color=Color.White,fontSize=14.sp,fontWeight=FontWeight.SemiBold)
                        Text(method.subtitle,color=Color(0xFF6B7280),fontSize=11.sp)
                    }
                    RadioButton(selected=isSel, onClick={ onMethodPick(method) },
                        colors=RadioButtonDefaults.colors(selectedColor=Color(0xFF00E5FF),unselectedColor=Color(0xFF4B5563)))
                }
            }

            AnimatedVisibility(visible=isSel && method==PaymentMethod.UPI, enter=fadeIn()+expandVertically(), exit=fadeOut()+shrinkVertically()) {
                OutlinedTextField(
                    value=upiId, onValueChange=onUpiType,
                    modifier=Modifier.fillMaxWidth().padding(bottom=12.dp),
                    placeholder={ Text("yourname@upi  (e.g. success@razorpay)",color=Color(0xFF4B5563),fontSize=13.sp) },
                    label={ Text("UPI ID",color=Color(0xFF9CA3AF)) },
                    singleLine=true, shape=RoundedCornerShape(12.dp),
                    supportingText={ Text("Use  success@razorpay  for test success",color=Color(0xFF4CAF50),fontSize=10.sp) },
                    colors=OutlinedTextFieldDefaults.colors(
                        focusedBorderColor=Color(0xFF00E5FF),unfocusedBorderColor=Color(0xFF2D2A40),
                        focusedTextColor=Color.White,unfocusedTextColor=Color.White,
                        cursorColor=Color(0xFF00E5FF),focusedContainerColor=Color(0xFF1A1630),unfocusedContainerColor=Color(0xFF1A1630)
                    )
                )
            }
        }

        Spacer(Modifier.weight(1f))

        Surface(shape=RoundedCornerShape(14.dp),color=Color(0xFF1A1630),border=BorderStroke(1.dp,Color(0xFF2D2A40)),modifier=Modifier.fillMaxWidth().padding(bottom=16.dp)) {
            Column(Modifier.padding(16.dp)) {
                OrderRow("Taplay Credits","₹$amount")
                OrderRow("Processing Fee","₹0.00")
                HorizontalDivider(color=Color(0xFF2D2A40),modifier=Modifier.padding(vertical=8.dp))
                OrderRow("Total","₹$amount",highlight=true)
            }
        }

        Button(
            onClick=onConfirmPay, modifier=Modifier.fillMaxWidth().height(56.dp),
            shape=RoundedCornerShape(28.dp),
            colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF00E5FF))
        ) {
            Text("🔒  Pay ₹$amount via ${selectedMethod.label}",color=Color(0xFF0D0A18),fontWeight=FontWeight.Bold,fontSize=14.sp)
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun OrderRow(label:String,value:String,highlight:Boolean=false) {
    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) {
        Text(label,color=if(highlight) Color.White else Color(0xFF9CA3AF),fontSize=if(highlight) 14.sp else 12.sp,fontWeight=if(highlight) FontWeight.Bold else FontWeight.Normal)
        Text(value,color=if(highlight) Color(0xFF00E5FF) else Color.White,fontSize=if(highlight) 14.sp else 12.sp,fontWeight=if(highlight) FontWeight.Bold else FontWeight.Normal)
    }
}

// ── PROCESSING
@Composable
private fun ProcessingOverlay() {
    val inf = rememberInfiniteTransition(label="proc")
    val rot by inf.animateFloat(initialValue=0f,targetValue=360f, animationSpec=infiniteRepeatable(tween(1000,easing=LinearEasing),RepeatMode.Restart),label="spin")
    val pulse by inf.animateFloat(initialValue=0.7f,targetValue=1f, animationSpec=infiniteRepeatable(tween(900,easing=EaseInOutSine),RepeatMode.Reverse),label="p")

    Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center) {
        Column(horizontalAlignment=Alignment.CenterHorizontally) {
            Box(Modifier.size(100.dp).clip(CircleShape).background(Color(0xFF1A1630)).border(2.dp,Color(0xFF00E5FF).copy(0.3f),CircleShape).scale(pulse),Alignment.Center) {
                CircularProgressIndicator(modifier=Modifier.size(64.dp).rotate(rot),color=Color(0xFF00E5FF),strokeWidth=3.dp,trackColor=Color(0xFF2D2A40))
            }
            Spacer(Modifier.height(24.dp))
            Text("Processing Payment",color=Color.White,fontSize=18.sp,fontWeight=FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text("Connecting to Razorpay…",color=Color(0xFF6B7280),fontSize=13.sp)
            Spacer(Modifier.height(24.dp))
            Text("🔒  Secured · Encrypted · PCI-DSS",color=Color(0xFF374151),fontSize=11.sp)
        }
    }
}

// ── SUCCESS RECEIPT
@Composable
private fun SuccessReceipt(amount:Int,method:PaymentMethod,newBalance:Int,txnId:String,onDone:()->Unit) {
    val scale by animateFloatAsState(targetValue=1f, animationSpec=spring(Spring.DampingRatioMediumBouncy,Spring.StiffnessMediumLow),label="e")
    val dateStr = SimpleDateFormat("dd MMM yyyy, hh:mm a",Locale.getDefault()).format(Date(System.currentTimeMillis()))

    Column(Modifier.fillMaxSize().padding(horizontal=24.dp),horizontalAlignment=Alignment.CenterHorizontally) {
        Spacer(Modifier.height(32.dp))
        Box(Modifier.size(88.dp).clip(CircleShape).background(Color(0xFF4CAF50).copy(0.15f)).border(2.dp,Color(0xFF4CAF50).copy(0.5f),CircleShape).scale(scale),Alignment.Center) {
            Text("✅",fontSize=40.sp)
        }
        Spacer(Modifier.height(16.dp))
        Text("Payment Successful!",color=Color.White,fontSize=22.sp,fontWeight=FontWeight.Black)
        Text("₹$amount added to your Taplay wallet",color=Color(0xFF9CA3AF),fontSize=13.sp,textAlign=TextAlign.Center)
        Spacer(Modifier.height(24.dp))

        Surface(shape=RoundedCornerShape(20.dp),color=Color(0xFF1A1630),border=BorderStroke(1.dp,Color(0xFF4CAF50).copy(0.25f)),modifier=Modifier.fillMaxWidth().scale(scale)) {
            Column(Modifier.padding(20.dp)) {
                Text("INVOICE",color=Color(0xFF4B5563),fontSize=9.sp,letterSpacing=2.sp,modifier=Modifier.padding(bottom=12.dp))
                ReceiptRow("Amount Paid","₹$amount")
                ReceiptRow("Payment Method","${method.emoji} ${method.label}")
                ReceiptRow("New Balance","₹$newBalance",Color(0xFF00E5FF))
                ReceiptRow("Date & Time",dateStr)
                HorizontalDivider(color=Color(0xFF2D2A40),modifier=Modifier.padding(vertical=10.dp))
                ReceiptRow("Transaction ID",txnId)
                ReceiptRow("Status","✅ SUCCESS",Color(0xFF4CAF50))
                ReceiptRow("Secured by","Razorpay · PCI-DSS")
            }
        }

        Spacer(Modifier.weight(1f))
        Button(onClick=onDone,modifier=Modifier.fillMaxWidth().height(54.dp),shape=RoundedCornerShape(27.dp),colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF00E5FF))) {
            Text("Back to Wallet",color=Color(0xFF0D0A18),fontWeight=FontWeight.Bold,fontSize=15.sp)
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun ReceiptRow(label:String,value:String,valueColor:Color=Color(0xFFCCCCCC)) {
    Row(Modifier.fillMaxWidth().padding(vertical=5.dp),horizontalArrangement=Arrangement.SpaceBetween) {
        Text(label,color=Color(0xFF6B7280),fontSize=12.sp)
        Text(value,color=valueColor,fontSize=12.sp,fontWeight=FontWeight.SemiBold)
    }
}

// ── HISTORY
@Composable
private fun HistoryScreen(transactions:List<TxnRecord>,onBack:()->Unit) {
    var filter by remember { mutableStateOf("All") }
    val filters = listOf("All","Top-Up","Games","Refunds")

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(horizontal=16.dp,vertical=16.dp),verticalAlignment=Alignment.CenterVertically) {
            IconButton(onClick=onBack,modifier=Modifier.size(42.dp).clip(CircleShape).background(Color(0xFF1E1930))) {
                Icon(Icons.Filled.ArrowBack,"Back",tint=Color.White)
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text("All Transactions",color=Color.White,fontSize=20.sp,fontWeight=FontWeight.Bold)
                Text("${transactions.size} records",color=Color(0xFF6B7280),fontSize=12.sp)
            }
        }

        Row(Modifier.fillMaxWidth().padding(horizontal=16.dp,vertical=4.dp),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            filters.forEach { f ->
                val isActive = f==filter
                FilterChip(selected=isActive,onClick={ filter=f },label={ Text(f,fontSize=11.sp) },
                    colors=FilterChipDefaults.filterChipColors(
                        selectedContainerColor=Color(0xFF00E5FF).copy(0.15f),selectedLabelColor=Color(0xFF00E5FF),
                        containerColor=Color(0xFF1A1630),labelColor=Color(0xFF6B7280)
                    ),
                    border=FilterChipDefaults.filterChipBorder(enabled=true,selected=isActive,
                        selectedBorderColor=Color(0xFF00E5FF).copy(0.5f),borderColor=Color(0xFF2D2A40)))
            }
        }

        val filtered = when(filter) {
            "Top-Up"  -> transactions.filter { it.type==TxnType.TOPUP }
            "Games"   -> transactions.filter { it.type==TxnType.GAME_PLAY }
            "Refunds" -> transactions.filter { it.type==TxnType.REFUND }
            else      -> transactions
        }

        LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(horizontal=16.dp,vertical=8.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
            items(filtered,key={it.id}) { TxnRow(txn=it) }
        }
    }
}

// ── SHARED COMPONENTS
@Composable
private fun StageTopBar(title:String,subtitle:String,onBack:()->Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical=16.dp),verticalAlignment=Alignment.CenterVertically) {
        IconButton(onClick=onBack,modifier=Modifier.size(42.dp).clip(CircleShape).background(Color(0xFF1E1930))) {
            Icon(Icons.Filled.ArrowBack,"Back",tint=Color.White)
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(title,color=Color.White,fontSize=20.sp,fontWeight=FontWeight.Bold)
            Text(subtitle,color=Color(0xFF6B7280),fontSize=12.sp)
        }
    }
}

@Composable
private fun TxnRow(txn:TxnRecord,modifier:Modifier=Modifier) {
    val isCredit = txn.amount>0
    val dateStr  = SimpleDateFormat("dd MMM, hh:mm a",Locale.getDefault()).format(Date(txn.timestamp))
    val amtColor = when {
        txn.status==TxnStatus.FAILED -> Color(0xFF6B7280)
        isCredit                     -> Color(0xFF4CAF50)
        else                         -> Color(0xFFFF5722)
    }
    val typeEmoji = when(txn.type) {
        TxnType.TOPUP     -> "⬆️"
        TxnType.GAME_PLAY -> "🎮"
        TxnType.REFUND    -> "↩️"
    }
    Surface(shape=RoundedCornerShape(14.dp),color=Color(0xFF1A1630),border=BorderStroke(1.dp,Color(0xFF2D2A40)),modifier=modifier.fillMaxWidth()) {
        Row(Modifier.padding(14.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(40.dp).clip(CircleShape).background(Color(0xFF252240)),Alignment.Center) { Text(typeEmoji,fontSize=18.sp) }
            Column(Modifier.weight(1f)) {
                Text(txn.title,color=Color.White,fontSize=13.sp,fontWeight=FontWeight.SemiBold)
                Text(txn.subtitle,color=Color(0xFF6B7280),fontSize=10.sp)
                Text(dateStr,color=Color(0xFF374151),fontSize=9.sp,modifier=Modifier.padding(top=2.dp))
            }
            Column(horizontalAlignment=Alignment.End) {
                Text(
                    if(txn.status==TxnStatus.FAILED) "FAILED"
                    else "${if(isCredit)"+" else ""}${if(txn.amount!=0) "₹${kotlin.math.abs(txn.amount)}" else "–"}",
                    color=amtColor,fontSize=14.sp,fontWeight=FontWeight.Bold
                )
                if(txn.status==TxnStatus.PENDING) Text("Pending",color=Color(0xFFFFB300),fontSize=9.sp)
            }
        }
    }
}