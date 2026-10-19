package com.android.axion.compose.about

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.SystemProperties
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.SdStorage
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File
import kotlin.math.roundToLong

/**
 * About-phone screen, ColorOS/OxygenOS style:
 *  banner  ->  device name + storage  ->  model  ->  processor  ->  RAM
 *
 * Signature is kept identical to the one AxionAboutFragment / AxionAboutActivity
 * already call, so no other file has to change:
 *   AxionAboutScreen(Function0, Function0, Function1<String>, Boolean, Modifier)
 *
 * onItemClick keys: "update", "device_name", "ram"
 */
@Composable
fun AxionAboutScreen(
    onBackClick: () -> Unit,
    onDeviceInfoClick: () -> Unit,
    onItemClick: (String) -> Unit,
    showBackButton: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val info = remember { DeviceInfo.read(context) }

    Box(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (showBackButton) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null)
                }
            }

            Banner(info, onClick = { onItemClick("update") })

            // Device name + storage
            SectionCard {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { onItemClick("device_name") }
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Device name", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.weight(1f))
                    Text(info.deviceName, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.size(8.dp))
                    Icon(Icons.Rounded.Edit, null, Modifier.size(20.dp), MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Divider()
                Column(Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Storage", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.weight(1f))
                        Text(
                            "${fmtGb(info.storageUsed)} / ${info.storageTotalMarketingGb} GB",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    StorageBar(info.storageUsed.toFloat() / info.storageTotal.coerceAtLeast(1))
                }
            }

            InfoCard(
                title = info.marketName,
                badge = info.model,
                subtitle = info.buildId,
                icon = Icons.Rounded.PhoneAndroid,
                onClick = onDeviceInfoClick,
            )
            InfoCard(
                title = info.soc,
                subtitle = "Processor",
                icon = Icons.Rounded.Memory,
            )
            InfoCard(
                title = "${info.ramGb} GB",
                badge = if (info.swapGb > 0) "+ ${info.swapGb} GB" else null,
                subtitle = "RAM",
                icon = Icons.Rounded.SdStorage,
                chevron = true,
                onClick = { onItemClick("ram") },
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

// ---------------------------------------------------------------- UI pieces

@Composable
private fun Banner(info: DeviceInfo, onClick: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(380.dp)
            .clip(RoundedCornerShape(36.dp))
            .background(
                Brush.linearGradient(
                    0f to Color(0xFF3B2A8C),
                    0.45f to Color(0xFF8F5BD6),
                    0.75f to Color(0xFFE8552B),
                    1f to Color(0xFF2B2563),
                )
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("NeRvOS", color = Color.White, fontSize = 64.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text("1.0 | Angel Attack", color = Color.White.copy(alpha = 0.85f), fontSize = 18.sp)
            Spacer(Modifier.height(4.dp))
            Text("Maintainer: Aciss.?", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
        }
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 36.dp)
                .clip(RoundedCornerShape(50))
                .background(Color.White.copy(alpha = 0.22f))
                .clickable(onClick = onClick)
                .padding(horizontal = 32.dp, vertical = 14.dp),
        ) {
            Text("Check for updates", color = Color.White, fontSize = 16.sp)
        }
    }
}

@Composable
private fun SectionCard(content: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer),
    ) { content() }
}

@Composable
private fun Divider() {
    Box(
        Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    )
}

@Composable
private fun InfoCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    badge: String? = null,
    chevron: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    SectionCard {
        Row(
            Modifier
                .fillMaxWidth()
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Medium)
                    if (badge != null) {
                        Spacer(Modifier.size(8.dp))
                        Text(
                            badge,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (chevron) Icon(Icons.Rounded.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Icon(icon, null, Modifier.size(30.dp), MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun StorageBar(usedFraction: Float) {
    val f = usedFraction.coerceIn(0f, 1f)
    Row(
        Modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
    ) {
        // apps / media / system / other (visual split of the used part)
        val segs = listOf(0.12f to Color(0xFF3F51F5), 0.45f to Color(0xFF3A4A78), 0.18f to Color(0xFF9AA3BF), 0.25f to Color(0xFFC2C8E0))
        segs.forEach { (w, c) ->
            if (f * w > 0f) Box(Modifier.weight(f * w).fillMaxSize().background(c))
        }
        if (1f - f > 0f) Box(Modifier.weight(1f - f).fillMaxSize())
    }
}

// ---------------------------------------------------------------- data

private fun fmtGb(bytes: Long) = String.format("%.1f GB", bytes / 1_000_000_000.0)

private data class DeviceInfo(
    val version: String,
    val codename: String,
    val deviceName: String,
    val marketName: String,
    val model: String,
    val buildId: String,
    val soc: String,
    val ramGb: Long,
    val swapGb: Long,
    val storageUsed: Long,
    val storageTotal: Long,
    val storageTotalMarketingGb: Long,
) {
    companion object {
        fun read(ctx: Context): DeviceInfo {
            val sp = { k: String -> SystemProperties.get(k, "") }

            val stat = StatFs(Environment.getDataDirectory().path)
            val total = stat.totalBytes
            val used = total - stat.availableBytes
            // 119 GiB usable -> "128 GB" on the box: round up to next power of two
            var marketing = 8L
            while (marketing * 1_000_000_000L < total) marketing *= 2

            val mi = ActivityManager.MemoryInfo()
            (ctx.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).getMemoryInfo(mi)
            val ramGb = (mi.totalMem / 1_073_741_824.0).roundToLong().let { if (it < 1) 1 else it }

            val swapKb = runCatching {
                File("/proc/meminfo").readLines().first { it.startsWith("SwapTotal:") }
                    .filter { it.isDigit() }.toLong()
            }.getOrDefault(0L)

            val cores = Runtime.getRuntime().availableProcessors()
            val coreName = when (cores) {
                2 -> "Dual-core"; 4 -> "Quad-core"; 6 -> "Hexa-core"; 8 -> "Octa-core"
                10 -> "Deca-core"; else -> "$cores-core"
            }
            val socName = listOf(Build.SOC_MANUFACTURER, Build.SOC_MODEL)
                .filter { it.isNotBlank() && it != Build.UNKNOWN }.joinToString(" ")
                .ifBlank { Build.HARDWARE }

            return DeviceInfo(
                version = sp("ro.axion.version").ifBlank { "Android ${Build.VERSION.RELEASE}" },
                codename = Build.DEVICE,
                deviceName = Settings.Global.getString(ctx.contentResolver, Settings.Global.DEVICE_NAME)
                    ?: Build.MODEL,
                marketName = sp("ro.product.marketname").ifBlank { Build.MODEL },
                model = Build.MODEL,
                buildId = Build.DISPLAY,
                soc = "$socName $coreName",
                ramGb = ramGb,
                swapGb = (swapKb / 1_048_576.0).roundToLong(),
                storageUsed = used,
                storageTotal = total,
                storageTotalMarketingGb = marketing,
            )
        }
    }
}
