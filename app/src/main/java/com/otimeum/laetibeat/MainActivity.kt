package com.otimeum.laetibeat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.os.Build
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.isSystemInDarkTheme

// ==========================================
// 1. 媒体模型定义 (元数据)
// ==========================================
data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val duration: String,
    val coverColor: Color // 临时用颜色代替封面图片，方便无网络直接查看效果
)

// ==========================================
// 2. 主 Activity 启动入口
// ==========================================
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            M3MusicPlayerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}

// ==========================================
// 3. 标准 Material You 动态色彩主题配置
// ==========================================
@Composable
fun M3MusicPlayerTheme(
    // 自动检测系统当前是亮色模式还是暗色模式
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val supportsDynamicColor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    val colorScheme = when {
        // 如果支持动态取色，根据系统的亮暗色设置，分别提取对应的壁纸调色板
        supportsDynamicColor -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        // 如果系统不支持动态取色，回退到 M3 标准默认色盘
        darkTheme -> darkColorScheme(
            primary = Color(0xFFD0BCFF),
            background = Color(0xFF1C1B1F),
            surface = Color(0xFF1C1B1F),
            surfaceVariant = Color(0xFF2A282F),
            onSurface = Color(0xFFE6E1E5)
        )
        else -> lightColorScheme(
            primary = Color(0xFF6750A4),
            background = Color(0xFFFFFBFE),
            surface = Color(0xFFFFFBFE),
            surfaceVariant = Color(0xFFE7E0EC),
            onSurface = Color(0xFF1C1B1F)
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}

// ==========================================
// 4. 模拟数据源 (本地歌曲列表)
// ==========================================
val mockSongs = listOf(
    Song("1", "夜航星", "不才 / 取子", "《我的三体之章北海传》", "04:15", Color(0xFF3F51B5)),
    Song("2", "向阳而生", "华晨宇", "希忘Hope", "03:48", Color(0xFFE91E63)),
    Song("3", "New Boy", "朴树", "我去2000年", "03:55", Color(0xFF4CAF50)),
    Song("4", "凄美地", "郭顶", "飞行器的执行周期", "04:10", Color(0xFFFF9800)),
    Song("5", "那些花儿", "朴树", "我去2000年", "04:30", Color(0xFF00BCD4)),
    Song("6", "年少有为", "李荣浩", "耳朵", "04:39", Color(0xFF9C27B0))
)

// ==========================================
// 5. 核心状态与主界面布局
// ==========================================
@Composable
fun MainScreen() {
    var currentTab by remember { mutableStateOf(0) }
    var currentSong by remember { mutableStateOf<Song?>(mockSongs[0]) }
    var isPlaying by remember { mutableStateOf(false) }
    var isPlayerExpanded by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            Column {
                // 只有在全屏播放器没有展开时，才显示 MiniPlayer 和 BottomBar
                if (!isPlayerExpanded) {
                    currentSong?.let { song ->
                        MiniPlayer(
                            song = song,
                            isPlaying = isPlaying,
                            onPlayPauseClick = { isPlaying = !isPlaying },
                            onPlayerClick = { isPlayerExpanded = true }
                        )
                    }
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp
                    ) {
                        NavigationBarItem(
                            selected = currentTab == 0,
                            onClick = { currentTab = 0 },
                            icon = { Icon(Icons.Rounded.Home, contentDescription = "Home") },
                            label = { Text("首页") }
                        )
                        NavigationBarItem(
                            selected = currentTab == 1,
                            onClick = { currentTab = 1 },
                            icon = { Icon(Icons.Rounded.LibraryMusic, contentDescription = "Library") },
                            label = { Text("音乐库") }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 根据底部 Tab 切换不同主页面
            when (currentTab) {
                0 -> HomeScreen(onSongSelect = { currentSong = it; isPlaying = true })
                1 -> LibraryScreen(onSongSelect = { currentSong = it; isPlaying = true })
            }

            // 全屏播放器（使用 Compose 动画优雅地划出）
            AnimatedVisibility(
                visible = isPlayerExpanded,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(durationMillis = 400)
                ) + fadeIn(animationSpec = tween(durationMillis = 300)),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(durationMillis = 400)
                ) + fadeOut(animationSpec = tween(durationMillis = 300))
            ) {
                currentSong?.let { song ->
                    FullscreenPlayer(
                        song = song,
                        isPlaying = isPlaying,
                        onPlayPauseClick = { isPlaying = !isPlaying },
                        onCollapse = { isPlayerExpanded = false }
                    )
                }
            }
        }
    }
}

// ==========================================
// 6. 首页 UI (M3 动态卡片设计)
// ==========================================
@Composable
fun HomeScreen(onSongSelect: (Song) -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "向阳而生",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "听你想听的音乐",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }

        // 推荐区域（横向滑动卡片）
        item {
            Text(
                text = "今日推荐",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(mockSongs.shuffled()) { song ->
                    RecommendCard(song = song, onClick = { onSongSelect(song) })
                }
            }
        }

        // 最近播放列表
        item {
            Text(
                text = "最近播放",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        items(mockSongs) { song ->
            SongListItem(song = song, onClick = { onSongSelect(song) })
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ==========================================
// 7. 音乐库 UI (列表检索设计)
// ==========================================
@Composable
fun LibraryScreen(onSongSelect: (Song) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "本地音乐库",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(16.dp))

        // 快速操作栏
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {},
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Icon(Icons.Rounded.PlayArrow, contentDescription = "PlayAll")
                Spacer(modifier = Modifier.width(8.dp))
                Text("全部播放", color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            OutlinedButton(
                onClick = {},
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Rounded.Shuffle, contentDescription = "Shuffle")
                Spacer(modifier = Modifier.width(8.dp))
                Text("随机播放")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(mockSongs) { song ->
                SongListItem(song = song, onClick = { onSongSelect(song) })
            }
        }
    }
}

// ==========================================
// 8. 辅助 UI 组件 (卡片与列表项)
// ==========================================
@Composable
fun RecommendCard(song: Song, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .width(150.dp)
            .height(210.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(song.coverColor)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = song.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = song.artist,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun SongListItem(song: Song, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(song.coverColor)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = song.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${song.artist} • ${song.album}",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            text = song.duration,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        )
    }
}

// ==========================================
// 9. MiniPlayer (悬浮迷你播放控制条)
// ==========================================
@Composable
fun MiniPlayer(
    song: Song,
    isPlaying: Boolean,
    onPlayPauseClick: () -> Unit,
    onPlayerClick: () -> Unit
) {
    Card(
        onClick = onPlayerClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .height(64.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(song.coverColor)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = song.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = song.artist,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(onClick = onPlayPauseClick) {
                Icon(
                    imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = "Play/Pause",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// ==========================================
// 10. FullscreenPlayer (沉浸式全屏播放页)
// ==========================================
@Composable
fun FullscreenPlayer(
    song: Song,
    isPlaying: Boolean,
    onPlayPauseClick: () -> Unit,
    onCollapse: () -> Unit
) {
    var progress by remember { mutableStateOf(0.3f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f), // 动态壁纸的主容器色
                        MaterialTheme.colorScheme.background                         // 动态壁纸的背景底色
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 顶栏控制
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onCollapse) {
                    Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = "Collapse", tint = Color.White)
                }
                Text(
                    text = "正在播放",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                IconButton(onClick = {}) {
                    Icon(Icons.Rounded.MoreVert, contentDescription = "More", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.weight(0.1f))

            // 巨大的圆角专辑封面
            Box(
                modifier = Modifier
                    .size(320.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(song.coverColor)
            )

            Spacer(modifier = Modifier.weight(0.1f))

            // 歌名与艺术家
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = song.title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = song.artist,
                        fontSize = 16.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                IconButton(onClick = {}) {
                    Icon(Icons.Rounded.FavoriteBorder, contentDescription = "Favorite", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 进度条
            Slider(
                value = progress,
                onValueChange = { progress = it },
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "01:24", fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f))
                Text(text = song.duration, fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f))
            }

            Spacer(modifier = Modifier.weight(0.1f))

            // 播放控制按钮群 (M3 律动排版)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {}) {
                    Icon(Icons.Rounded.Shuffle, contentDescription = "Shuffle", modifier = Modifier.size(28.dp), tint = Color.White)
                }
                IconButton(onClick = {}) {
                    Icon(Icons.Rounded.SkipPrevious, contentDescription = "Prev", modifier = Modifier.size(36.dp), tint = Color.White)
                }
                // 播放暂停主按键
                FilledIconButton(
                    onClick = onPlayPauseClick,
                    modifier = Modifier.size(72.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = "Play/Pause",
                        modifier = Modifier.size(40.dp)
                    )
                }
                IconButton(onClick = {}) {
                    Icon(Icons.Rounded.SkipNext, contentDescription = "Next", modifier = Modifier.size(36.dp), tint = Color.White)
                }
                IconButton(onClick = {}) {
                    Icon(Icons.Rounded.Repeat, contentDescription = "Repeat", modifier = Modifier.size(28.dp), tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.weight(0.15f))
        }
    }
}