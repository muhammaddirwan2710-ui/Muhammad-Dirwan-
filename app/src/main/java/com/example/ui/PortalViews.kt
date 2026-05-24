package com.example.ui

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Article
import java.io.File

/**
 * Main Portal container integrating Muhammad Dirwan S.H.'s profile and publications.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortalDashboard(
    viewModel: PortalViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()
    val selectedArticle by viewModel.selectedArticle.collectAsStateWithLifecycle()
    var showAccountInteractiveDialog by remember { mutableStateOf(false) }
    
    // Listen to ViewModel system messages and notify user
    LaunchedEffect(Unit) {
        viewModel.toastMessage.collect { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Column {
                // Main Professional Header Block
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.primary,
                                                MaterialTheme.colorScheme.secondary
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "MD",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Portal Publikasi",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Light,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    text = "M. Dirwan, S.H.",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    actions = {
                        val loggedInUser by viewModel.loggedInUser.collectAsStateWithLifecycle()
                        if (loggedInUser != null) {
                            Text(
                                text = "Halo, " + loggedInUser!!.name.split(" ")[0],
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                        }
                        IconButton(
                            onClick = {
                                showAccountInteractiveDialog = true
                            },
                            modifier = Modifier.testTag("action_account_status")
                        ) {
                            Icon(
                                imageVector = if (loggedInUser != null) Icons.Default.AccountCircle else Icons.Default.Lock,
                                contentDescription = "Status Akun Pengunjung",
                                tint = if (loggedInUser != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                            )
                        }
                        IconButton(
                            onClick = {
                                openWhatsApp(context)
                            },
                            modifier = Modifier.testTag("action_wa_quick")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = "Quick Konsultasi WhatsApp",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
            }
        },
        bottomBar = {
            // Elegant M3 Navigation Bar aligned with spacing and safe gesture zone insets
            NavigationBar(
                modifier = Modifier
                    .shadow(16.dp)
                    .navigationBarsPadding(),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = activeTab == 0,
                    onClick = { viewModel.selectTab(0) },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Beranda") },
                    label = { Text("Beranda", fontWeight = if (activeTab == 0) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("nav_tab_beranda")
                )
                NavigationBarItem(
                    selected = activeTab == 1,
                    onClick = { viewModel.selectTab(1) },
                    icon = { Icon(Icons.Default.Send, contentDescription = "Klub Diskusi Hukum") }, // Elegant Send/Forum Icon
                    label = { Text("Diskusi", fontWeight = if (activeTab == 1) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("nav_tab_diskusi")
                )
                NavigationBarItem(
                    selected = activeTab == 2,
                    onClick = { viewModel.selectTab(2) },
                    icon = { Icon(Icons.Default.Add, contentDescription = "Unggah Tulisan") },
                    label = { Text("Unggah", fontWeight = if (activeTab == 2) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("nav_tab_unggah")
                )
                NavigationBarItem(
                    selected = activeTab == 3,
                    onClick = { viewModel.selectTab(3) },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Hubungi SH") },
                    label = { Text("Hubungi SH", fontWeight = if (activeTab == 3) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("nav_tab_kontak")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Screen router
            AnimatedContent(
                targetState = if (selectedArticle != null) -1 else activeTab,
                transitionSpec = {
                    fadeIn(animationSpec = spring()) togetherWith fadeOut(animationSpec = spring())
                },
                label = "MainFadeScreen"
            ) { stateValue ->
                when (stateValue) {
                    -1 -> {
                        // Embedded Reading Mode
                        selectedArticle?.let { article ->
                            ArticleReaderView(
                                article = article,
                                viewModel = viewModel,
                                onBack = { viewModel.selectArticle(null) },
                                onDelete = {
                                    viewModel.selectArticle(null)
                                    viewModel.deleteArticle(article)
                                }
                            )
                        }
                    }
                    0 -> {
                        BerandaPortal(viewModel)
                    }
                    1 -> {
                        DiskusiGroupView(viewModel)
                    }
                    2 -> {
                        UnggahTulisanView(viewModel)
                    }
                    3 -> {
                        HubungiDirwanView()
                    }
                }
            }
        }
    }

    if (showAccountInteractiveDialog) {
        val loggedInUser by viewModel.loggedInUser.collectAsStateWithLifecycle()
        
        AlertDialog(
            onDismissRequest = { showAccountInteractiveDialog = false },
            confirmButton = {
                if (loggedInUser != null) {
                    TextButton(
                        onClick = {
                            viewModel.logout()
                            showAccountInteractiveDialog = false
                        }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Keluar (Log Out)", color = MaterialTheme.colorScheme.error)
                        }
                    }
                } else {
                    TextButton(onClick = { showAccountInteractiveDialog = false }) {
                        Text("Tutup")
                    }
                }
            },
            title = {
                Text(
                    text = "Layanan Akun Publik",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            text = {
                if (loggedInUser != null) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Anda masuk sebagai:",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = loggedInUser!!.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = loggedInUser!!.email,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                        
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (loggedInUser!!.isAdmin) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                    else MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (loggedInUser!!.isAdmin) "Hak Akses: ADVOKAT / ADMINISTRATOR" else "Hak Akses: Pengunjung Umum / Peneliti",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (loggedInUser!!.isAdmin) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Anda sekarang bebas mempublikasikan karya tulis ilmiah, menulis ulasan, dan bergabung ke ruang diskusi gropyokan hukum.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.secondary,
                            lineHeight = 15.sp
                        )
                    }
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = "Anda belum masuk. Silakan gunakan form masuk / daftar di bawah ini:",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.secondary,
                            lineHeight = 15.sp
                        )
                        
                        VisitorLoginGate(
                            viewModel = viewModel,
                            titleText = "Gerbang Validasi Akun",
                            subtitleText = "Isi email Gmail dan kata sandi Anda sendiri untuk beraktivitas secara penuh di portal m-dirwan."
                        )
                    }
                }
            }
        )
    }
}

/**
 * 1. TAB BERANDA:
 * Lists the legal documents and professional writings. Includes filters & keywords.
 */
@Composable
fun BerandaPortal(viewModel: PortalViewModel) {
    val articles by viewModel.articles.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Welcome Legal Quote/Intro
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "Fiat Justitia Ruat Caelum",
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 14.sp,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Selamat datang di portal literatur hukum Muhammad Dirwan, S.H. Di sini Anda dapat membaca secara mendalam karya ilmiah, penelitian, serta opini yurisprudensi terbaru.",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        }

        // Search Field styled elegantly
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.searchQuery.value = it },
            placeholder = { Text("Cari judul karya ilmiah atau topik hukum...", fontSize = 14.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Cari") },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Hapus Pencarian")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_field"),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
        )

        // Custom Horizontal Filter Pills
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val categories = listOf("Semua", "Karya Ilmiah", "Pendapat Hukum")
            categories.forEach { cat ->
                val isSelected = selectedCategory == cat
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.selectCategory(cat) },
                    label = { 
                        Text(
                            text = cat,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        ) 
                    },
                    shape = RoundedCornerShape(20.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("filter_chip_$cat")
                )
            }
        }

        // List Header and count
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (selectedCategory == "Semua") "Semua Publikasi" else selectedCategory,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "${articles.size} tulisan ditemukan",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Animated Lists / Empty state renderer
        if (articles.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Empty list",
                        modifier = Modifier.size(56.dp),
                        tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "Data tulisan tidak dtemukan",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Belum ada tulisan terunggah dalam kategori ini atau ketik kata kunci pencarian yang lain.",
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .testTag("articles_list"),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(articles, key = { it.id }) { article ->
                    ArticleListItemCard(
                        article = article,
                        onClick = { viewModel.selectArticle(article) }
                    )
                }
            }
        }
    }
}

/**
 * Custom Article Card Item styled elegantly with clear tags
 */
@Composable
fun ArticleListItemCard(
    article: Article,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag("article_card_${article.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Document Category labels of distinction
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (article.category == "Karya Ilmiah") {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                            } else {
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = article.category,
                        color = if (article.category == "Karya Ilmiah") {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.secondary
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Sub-domain Shortcut tag
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = article.categoryShortcut,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Document Title
            Text(
                text = article.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 22.sp
            )

            // Short Description Summary
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = article.description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
            Spacer(modifier = Modifier.height(8.dp))

            // Meta indicators: Calendar, Reading Length, File attachment presence
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Date Stamp icon row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info, // Placeholder for date
                            contentDescription = "Tanggal Terbit",
                            tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = article.date,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Light
                        )
                    }

                    // Reading time length
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share, // Placeholder for duration
                            contentDescription = "Lama Bacaan",
                            tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = article.readTime,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Light
                        )
                    }
                }

                // If actual PDF document is bound, show attachment clip icon
                if (article.pdfUri != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Done, // Placeholder for clip attachment
                            contentDescription = "File PDF Terlampir",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "PDF",
                            color = MaterialTheme.colorScheme.secondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * 2. EMBEDDED READER MODE:
 * High-immersion legal reader mode that renders core study text in rich book typography
 * or handles local sandboxed PDF visual rendering page-by-page.
 */
@Composable
fun ArticleReaderView(
    article: Article,
    viewModel: PortalViewModel,
    onBack: () -> Unit,
    onDelete: () -> Unit
) {
    var viewModePdf by remember { mutableStateOf(article.pdfUri != null) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("article_reader_screener")
    ) {
        // App bar controls inside reading mode
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("reader_back_button")) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Kembali ke Beranda")
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        text = article.category,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = article.categoryShortcut,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.width(180.dp)
                    )
                }
            }

            // Quick Actions: PDF/Text toggle, Trash and Share
            Row(verticalAlignment = Alignment.CenterVertically) {
                // If PDF is attached, allow toggling view modes
                if (article.pdfUri != null) {
                    IconButton(
                        onClick = { viewModePdf = !viewModePdf },
                        modifier = Modifier.testTag("reader_toggle_pdf")
                    ) {
                        Icon(
                            imageVector = if (viewModePdf) Icons.Default.Info else Icons.Default.Done,
                            contentDescription = if (viewModePdf) "Lihat Teks Utama" else "Buka Visual PDF",
                            tint = if (viewModePdf) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Share intent
                IconButton(onClick = { shareArticleContent(context, article) }) {
                    Icon(Icons.Default.Share, contentDescription = "Membagikan karya")
                }

                // Delete only if not prepopulated seed data (mock ID > 3 represents user uploaded publications)
                if (article.id > 3) {
                    IconButton(onClick = onDelete, modifier = Modifier.testTag("reader_delete_button")) {
                        Icon(Icons.Default.Delete, contentDescription = "Hapus publikasi", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))

        // Reader Area
        if (viewModePdf && article.pdfUri != null) {
            // Native PDF Renderer Active
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFE5E5E5))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Visual Dokumen PDF: ${article.pdfFileName ?: "file.pdf"}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "TAP IKON DI ATAS UNTUK LIHAT TEKS",
                            fontSize = 9.sp,
                            letterSpacing = 0.5.sp,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
                
                NativePdfPageList(
                    pdfUriPath = article.pdfUri,
                    modifier = Modifier.weight(1f)
                )
            }
        } else {
            // Elegant Text Reader Mode Active
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(24.dp)
            ) {
                Text(
                    text = article.title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    lineHeight = 30.sp
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Author Tag & Date card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.background)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Advokat",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Penulis: ${article.author}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Diterbitkan tanggal: ${article.date} • ${article.readTime}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(20.dp))

                // Structured Scholarly/Academic Body Typography
                Text(
                    text = article.content,
                    fontSize = 15.sp,
                    lineHeight = 26.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = FontFamily.Serif,
                    textAlign = TextAlign.Justify
                )
                
                Spacer(modifier = Modifier.height(36.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Layanan Konsultasi hukum terkait tulisan di atas dapat didiskusikan langsung secara eksklusif bersama Advokat Muhammad Dirwan, S.H.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    textAlign = TextAlign.Center,
                    fontStyle = FontStyle.Italic,
                    lineHeight = 16.sp,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(24.dp))

                // Interconnected Scholarly Comments feed
                ArticleCommentsSection(viewModel = viewModel, article = article)
                
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

/**
 * Native PDF Decoder utilizing core android.graphics.pdf.PdfRenderer (API Level 21+)
 * Renders pages in order into clean high-contrast canvas Bitmaps.
 */
@Composable
fun NativePdfPageList(
    pdfUriPath: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val pagesList = remember(pdfUriPath) { mutableStateListOf<Bitmap>() }
    var renderError by remember { mutableStateOf(false) }
    var renderErrorMessage by remember { mutableStateOf("") }

    LaunchedEffect(pdfUriPath) {
        try {
            val file = File(pdfUriPath)
            if (file.exists() && file.length() > 0) {
                // Read local descriptor stream
                val fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                val pdfRenderer = PdfRenderer(fileDescriptor)
                val pageCount = pdfRenderer.pageCount
                val bitmapCollector = mutableListOf<Bitmap>()
                
                for (i in 0 until pageCount) {
                    val page = pdfRenderer.openPage(i)
                    // Apply crisp scales for rendering screen targets
                    val upscaleFactor = 2
                    val width = page.width * upscaleFactor
                    val height = page.height * upscaleFactor
                    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    
                    // Core PDF canvas requires drawing a base white background layer
                    val canvas = android.graphics.Canvas(bitmap)
                    canvas.drawColor(android.graphics.Color.WHITE)
                    
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    bitmapCollector.add(bitmap)
                    page.close()
                }
                
                pdfRenderer.close()
                fileDescriptor.close()
                pagesList.clear()
                pagesList.addAll(bitmapCollector)
            } else {
                renderError = true
                renderErrorMessage = "Berkas PDF tidak ditemukan atau kosong."
            }
        } catch (e: Exception) {
            e.printStackTrace()
            renderError = true
            renderErrorMessage = "Gagal memproses file PDF secara teknis: ${e.localizedMessage}"
        }
    }

    if (renderError) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            ElevatedCard(
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Warning, contentDescription = "Gagal", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(40.dp))
                    Text(
                        text = "Visual PDF Tidak Dapat Dimuat",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "$renderErrorMessage\nSistem merekomendasikan Anda membaca via 'Mode Teks Utama' dengan mengetuk tombol navigasi visual di sebelah tombol share.",
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.secondary,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    } else if (pagesList.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                Text(
                    text = "Mempersiapkan visual dokumen akademis...",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(12.dp)
        ) {
            items(pagesList.size) { index ->
                val pageBitmap = pagesList[index]
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    elevation = CardDefaults.elevatedCardElevation(4.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Image(
                        bitmap = pageBitmap.asImageBitmap(),
                        contentDescription = "Halaman PDF nomor ${index + 1}",
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(pageBitmap.width.toFloat() / pageBitmap.height.toFloat())
                            .padding(4.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}

/**
 * 3. TAB UNGGAH TULISAN:
 * Secure local publishing terminal mimicking a Content Management System (CMS) Panel.
 */
@Composable
fun UnggahTulisanView(viewModel: PortalViewModel) {
    val context = LocalContext.current
    val loggedInUser by viewModel.loggedInUser.collectAsStateWithLifecycle()
    
    if (loggedInUser == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            VisitorLoginGate(
                viewModel = viewModel,
                titleText = "Publikasikan Karya Baru",
                subtitleText = "Silakan masuk ke dalam akun hukum Anda guna mempublikasikan karya ilmiah / pendapat hukum terbaru Anda."
            )
        }
        return
    }

    val title by viewModel.formTitle.collectAsStateWithLifecycle()
    val category by viewModel.formCategory.collectAsStateWithLifecycle()
    val shortcut by viewModel.formCategoryShortcut.collectAsStateWithLifecycle()
    val description by viewModel.formDescription.collectAsStateWithLifecycle()
    val content by viewModel.formContent.collectAsStateWithLifecycle()
    
    val attachedPdfName by viewModel.attachedPdfName.collectAsStateWithLifecycle()
    val attachedPdfUri by viewModel.attachedPdfUri.collectAsStateWithLifecycle()

    // Android Storage Document selector launcher (limited to PDFs)
    val fileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.attachPdf(uri, context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcoming administration banner
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Share, // Placeholder for administration badge
                    contentDescription = "Menu Admin",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Publikasikan Karya Baru",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Semua tulisan baru langsung tersimpan aman ke dalam pangkalan data offline (Room) HP ini dan otomatis terbit ke menu Beranda Publik.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        val isUploadingAsAdmin by viewModel.isUploadingAsAdmin.collectAsStateWithLifecycle()
        val formAuthorName by viewModel.formAuthorName.collectAsStateWithLifecycle()

        // Identity Selector: Admin vs Visitor
        val user = loggedInUser ?: return@Column
        
        if (user.isAdmin) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Identitas Pengunggah Karya (Penulis) *",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    listOf(true to "M. Dirwan, S.H. (Admin)", false to "Pengunjung / Umum").forEach { (isAdminOpt, label) ->
                        val isOptChecked = isUploadingAsAdmin == isAdminOpt
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    width = 1.6.dp,
                                    color = if (isOptChecked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .background(
                                    if (isOptChecked) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f) else Color.Transparent
                                )
                                .clickable { viewModel.isUploadingAsAdmin.value = isAdminOpt }
                                .padding(horizontal = 10.dp, vertical = 10.dp)
                                .testTag("form_uploader_opt_${if(isAdminOpt) "admin" else "visitor"}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (isOptChecked) Icons.Default.CheckCircle else Icons.Default.Lock,
                                    contentDescription = "Pilihan",
                                    tint = if (isOptChecked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = label,
                                    color = if (isOptChecked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                                    fontSize = 11.sp,
                                    fontWeight = if (isOptChecked) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                // If selected Visitor, display standard Name Text Input
                AnimatedVisibility(
                    visible = !isUploadingAsAdmin,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Nama Anda (Penulis / Institusi) *",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        OutlinedTextField(
                            value = formAuthorName,
                            onValueChange = { viewModel.formAuthorName.value = it },
                            placeholder = { Text("Contoh: Budi Cahyono, S.H. / Univ. Indonesia", fontSize = 13.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("form_input_author_name"),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    }
                }
            }
        } else {
            // Display static viewer name preview
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Nama Anda sebagai Penulis:",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = user.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Title text Input
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "Judul Karya Ilmiah / Pendapat Hukum *",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary
            )
            OutlinedTextField(
                value = title,
                onValueChange = { viewModel.formTitle.value = it },
                placeholder = { Text("Contoh: Tinjauan Yuridis Hak Kekayaan Intelektual...", fontSize = 13.sp) },
                singleLine = false,
                maxLines = 3,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("form_input_title"),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }

        // Dropdown Selection for Category
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "Klasifikasi Kategori Publikasi",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                listOf("Karya Ilmiah", "Pendapat Hukum").forEach { categoryItem ->
                    val isChecked = category == categoryItem
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .border(
                                width = 1.dp,
                                color = if (isChecked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .background(
                                if (isChecked) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f) else Color.Transparent
                            )
                            .clickable { viewModel.formCategory.value = categoryItem }
                            .padding(horizontal = 12.dp, vertical = 12.dp)
                            .testTag("form_category_opt_$categoryItem"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (isChecked) Icons.Default.Check else Icons.Default.Info,
                                contentDescription = "Dipilih",
                                tint = if (isChecked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = categoryItem,
                                color = if (isChecked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                                fontSize = 13.sp,
                                fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

        // Subcategory Shortcut Tag input
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "Tag Bidang Hukum (Sub-Kategori)",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary
            )
            OutlinedTextField(
                value = shortcut,
                onValueChange = { viewModel.formCategoryShortcut.value = it },
                placeholder = { Text("Contoh: Hukum Perdata, Hukum Pidana, Hukum Tata Negara", fontSize = 13.sp) },
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("form_input_shortcut"),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }

        // Short Description input
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "Ringkasan Singkat (Abstrak) *",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary
            )
            OutlinedTextField(
                value = description,
                onValueChange = { viewModel.formDescription.value = it },
                placeholder = { Text("Tuliskan ringkasan sebanyak 1-2 paragraf mengenai isi atau kesimpulan utama tulisan Anda...", fontSize = 13.sp) },
                singleLine = false,
                maxLines = 4,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .testTag("form_input_description"),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }

        // Attachment visual zone representing native PDF upload
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "Dokumen PDF Fisik (Sangat Direkomendasikan)",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary
            )
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        RoundedCornerShape(8.dp)
                    )
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp)
            ) {
                if (attachedPdfUri != null) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Done, // Representing doc sheet
                            contentDescription = "Attached File",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = attachedPdfName ?: "dokumen.pdf",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = { fileLauncher.launch("application/pdf") },
                                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("Ganti File PDF", fontSize = 12.sp)
                            }
                            TextButton(
                                onClick = { viewModel.unattachPdf() },
                                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("Hapus Berkas", fontSize = 12.sp)
                            }
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { fileLauncher.launch("application/pdf") }
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share, // Click layout
                            contentDescription = "Pilih Berkas PDF",
                            tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
                            modifier = Modifier.size(40.dp)
                        )
                        Text(
                            text = "Ketuk untuk Memilih Berkas PDF dari HP",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Berkas PDF akan diduplikasi secara native untuk penayangan",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }

        // Textual Content Area (Fallback reading base)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "Isi Konten Utama Tulisan (Sesuai isi berkas PDF / Tulis Manual)",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary
            )
            OutlinedTextField(
                value = content,
                onValueChange = { viewModel.formContent.value = it },
                placeholder = { Text("Tulis atau tempelkan draf materi lengkap disini agar materi tetap terbaca apabila visual PDF tidak dimuat...", fontSize = 13.sp) },
                singleLine = false,
                minLines = 6,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("form_input_content"),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Large Premium Golden Publish Button
        Button(
            onClick = { viewModel.saveArticleAndPublish(context) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .shadow(8.dp, RoundedCornerShape(12.dp))
                .testTag("publish_form_submit"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = "Terbitkan")
                Text(
                    text = "TERBITKAN KARYA S.H. SEKARANG",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    letterSpacing = 1.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(36.dp))
    }
}

/**
 * 4. TAB HUBUNGI / KONSULTASI HUKUM:
 * Virtual law firm bio showing Dirwan's contact points with click-intent automations
 */
@Composable
fun HubungiDirwanView() {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Prestige Lawyer Core Profile Bio
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp, RoundedCornerShape(18.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Circle Monogram instead of placeholder image for premium look
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.secondary,
                                    MaterialTheme.colorScheme.primary
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "M.D",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title details
                Text(
                    text = "Muhammad Dirwan, S.H.",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Advokat & Praktisi Hukum Indonesia",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(top = 4.dp),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(12.dp))

                // Description of expertise/credentials
                Text(
                    text = "Siap mendampingi dan memberikan solusi hukum terbaik untuk kebutuhan perorangan maupun perusahaan secara transparan, akurat, dan menjunjung tinggi kode etik advokat.",
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Expertise Cards
        Text(
            text = "Bidang Penanganan Hukum",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ExpertiseBadge(title = "Hukum Perdata", desc = "Kontrak, Sengketa Waris/Keluarga & Gugatan Wanprestasi.")
                ExpertiseBadge(title = "Hukum Perusahaan", desc = "Legalitas Pendirian, Klausul Baku & Kepatuhan Korporasi.")
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ExpertiseBadge(title = "Hukum Pidana", desc = "Pembelaan Hak, Laporan Kepolisian & Pendampingan Sidang.")
                ExpertiseBadge(title = "Hukum Agraria", desc = "Sengketa Tanah, Hak Milik & Sertifikasi Sertifikat.")
            }
        }

        // Interactive legal CTA buttons block
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Konsultasi Hukum Otomatis",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.primary
        )

        // WhatsApp direct action button
        Button(
            onClick = { openWhatsApp(context) },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .shadow(4.dp, RoundedCornerShape(12.dp))
                .testTag("cta_whatsapp"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)) // WhatsApp Signature Green
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Default.Phone,
                    contentDescription = "Contact WA",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "HUBUNGI VIA WHATSAPP (AKTIF)",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }

        // Instagram Button
        Button(
            onClick = { openInstagram(context) },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .shadow(4.dp, RoundedCornerShape(12.dp))
                .testTag("cta_instagram"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE1306C)) // Instagram Pinkish
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Contact IG",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "IKUTI DI INSTAGRAM UTAMA",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }

        // Gmail mailto launch
        Button(
            onClick = { openGmailEmail(context) },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .shadow(4.dp, RoundedCornerShape(12.dp))
                .testTag("cta_gmail"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA4335)) // Gmail Red
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Default.Email,
                    contentDescription = "Contact Mail",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "KIRIM PERTANYAAN EMAIL HUKUM",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }

        // Legal Disclaimer details
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.03f)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.padding(vertical = 12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Catatan Penting Konsultasi:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Konsultasi melalui tombol otomatis ini terhubung langsung ke kontak pribadi Muhammad Dirwan, S.H. Silakan sampaikan kronologi permasalahan secara padat dan terstruktur.",
                    fontSize = 10.sp,
                    lineHeight = 15.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun ExpertiseBadge(title: String, desc: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = desc,
                fontSize = 10.sp,
                lineHeight = 14.sp,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

// ==========================================
// INTENT ROUTERS (WA / IG / GMAIL)
// ==========================================

fun openWhatsApp(context: Context) {
    try {
        val waNumber = "6282364455421" // International phone schema
        val baseMessage = Uri.encode("Halo Muhammad Dirwan, S.H., saya menemukan kontak Bapak melalui aplikasi Portal Publikasi Hukum. Saya ingin berkonsultasi mengenai permasalahan hukum.")
        val waUrl = "https://api.whatsapp.com/send?phone=$waNumber&text=$baseMessage"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(waUrl))
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Gagal membuka WhatsApp. Silakan install aplikasi WhatsApp terlebih dahulu.", Toast.LENGTH_SHORT).show()
    }
}

fun openInstagram(context: Context) {
    try {
        val igUrl = "https://www.instagram.com/muhammad_dirwan27?igsh=MWp6cHlxZ2tudWpodA=="
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(igUrl))
        // Attempt opening in IG directly
        intent.setPackage("com.instagram.android")
        try {
            context.startActivity(intent)
        } catch (subException: Exception) {
            // Fallback opening in default device browser
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(igUrl))
            context.startActivity(webIntent)
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Gagal membuka akun Instagram.", Toast.LENGTH_SHORT).show()
    }
}

fun openGmailEmail(context: Context) {
    try {
        val email = "muhammaddirwan2710@gmail.com"
        val subject = Uri.encode("KONSULTASI HUKUM - PORTAL ADVOKAT")
        val body = Uri.encode("Halo Bapak Muhammad Dirwan, S.H.,\n\nNama saya: \nNo. HP/WA: \nPermasalahan hukum yang dihadapi:\n")
        val mailToQuery = "mailto:$email?subject=$subject&body=$body"
        
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse(mailToQuery)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        // Broad message client launch
        try {
            val fallbackIntent = Intent(Intent.ACTION_SEND).apply {
                type = "message/rfc822"
                putExtra(Intent.EXTRA_EMAIL, arrayOf("muhammaddirwan2710@gmail.com"))
                putExtra(Intent.EXTRA_SUBJECT, "KONSULTASI HUKUM - PORTAL ADVOKAT")
            }
            context.startActivity(Intent.createChooser(fallbackIntent, "Kirim email melalui:"))
        } catch (err: Exception) {
            Toast.makeText(context, "Gagal membuka aplikasi email Gmail.", Toast.LENGTH_SHORT).show()
        }
    }
}

fun shareArticleContent(context: Context, article: Article) {
    try {
        val shareBody = """
            Karya Hukum Muhammad Dirwan, S.H.
            
            Judul: ${article.title}
            Kategori: ${article.category} (${article.categoryShortcut})
            Publikasi: ${article.date}

            Ringkasan: ${article.description}

            ---
            Konsultasi hukum langsung bersama Muhammad Dirwan, S.H., dapat diakses melalui aplikasi seluler kami atau Whatsapp: 082364455421
        """.trimIndent()
        
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, article.title)
            putExtra(Intent.EXTRA_TEXT, shareBody)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Bagikan literatur hukum via:"))
    } catch (e: Exception) {
        Toast.makeText(context, "Gagal membagikan tulisan.", Toast.LENGTH_SHORT).show()
    }
}

/**
 * 5. DISKUSI GROUP VIEW (FORUM & CHAT ANTAR PENGUNJUNG):
 * Interactive live messaging wall representing general law discourse and forums.
 */
@Composable
fun DiskusiGroupView(viewModel: PortalViewModel) {
    val context = LocalContext.current
    val loggedInUser by viewModel.loggedInUser.collectAsStateWithLifecycle()
    
    if (loggedInUser == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            VisitorLoginGate(
                viewModel = viewModel,
                titleText = "Ruang Diskusi & Gropyokan Hukum",
                subtitleText = "Untuk masuk dan ikut serta berdiskusi hangat dengan rekan hukum lainnya di forum, silakan login dengan akun Gmail Anda."
            )
        }
        return
    }

    val messages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val senderName by viewModel.chatSenderName.collectAsStateWithLifecycle()
    val senderTitle by viewModel.chatSenderTitle.collectAsStateWithLifecycle()
    val messageInput by viewModel.chatMessageInput.collectAsStateWithLifecycle()
    val scrollState = rememberLazyListState()

    // Automatically scroll to bottom on new messages
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            scrollState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("diskusi_chatroom_view")
    ) {
        // Forum Welcome Board Header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Forum Icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Ruang Diskusi & Gropyokan Hukum",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Layanan bebas bagi seluruh pengunjung, mahasiswa, praktisi, dan akademisi untuk bertukar pikiran mengenai isu hukum terhangat di Indonesia secara tertib.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    lineHeight = 15.sp
                )
            }
        }

        // Message list feed area
        if (messages.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MailOutline,
                        contentDescription = "No Message Found",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                    )
                    Text(
                        text = "Belum Ada Obrolan Masuk",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Kirim pesan pertama Anda untuk memicu diskusi hukum dinamis!",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            }
        } else {
            androidx.compose.foundation.lazy.LazyColumn(
                state = scrollState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    val isMsgAdmin = msg.isSenderAdmin
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = if (isMsgAdmin) 0.dp else 24.dp, start = if (isMsgAdmin) 24.dp else 0.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = if (isMsgAdmin) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                            } else {
                                MaterialTheme.colorScheme.surface
                            }
                        ),
                        shape = RoundedCornerShape(
                            topStart = 12.dp,
                            topEnd = 12.dp,
                            bottomStart = if (isMsgAdmin) 12.dp else 2.dp,
                            bottomEnd = if (isMsgAdmin) 2.dp else 12.dp
                        ),
                        elevation = CardDefaults.elevatedCardElevation(if (isMsgAdmin) 3.dp else 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            // Message Sender Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = msg.senderName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (isMsgAdmin) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                    // Professional badge
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(
                                                if (isMsgAdmin) {
                                                    MaterialTheme.colorScheme.primary
                                                } else {
                                                    when (msg.senderTitle) {
                                                        "Praktisi Hukum" -> Color(0xFFD4AF37).copy(alpha = 0.15f)
                                                        "Akademisi" -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                                                        "Mahasiswa Hukum" -> Color(0xFF2196F3).copy(alpha = 0.15f)
                                                        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                                                    }
                                                }
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (isMsgAdmin) "ADVOKAT / ADMIN" else msg.senderTitle,
                                            color = if (isMsgAdmin) {
                                                MaterialTheme.colorScheme.onPrimary
                                            } else {
                                                when (msg.senderTitle) {
                                                    "Praktisi Hukum" -> Color(0xFFD4AF37)
                                                    "Akademisi" -> MaterialTheme.colorScheme.secondary
                                                    "Mahasiswa Hukum" -> Color(0xFF2196F3)
                                                    else -> MaterialTheme.colorScheme.secondary
                                                }
                                            },
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Text(
                                    text = msg.timestamp,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = msg.messageText,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }

        // Active chat text sender console
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(16.dp)
                .background(MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Sender metadata credentials row (Name and Designation Option selector)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Chat sender Name Input
                    OutlinedTextField(
                        value = senderName,
                        onValueChange = { viewModel.chatSenderName.value = it },
                        placeholder = { Text("Nama Anda * (e.g. Budi S.H.)", fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("chat_sender_name_input"),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
                        shape = RoundedCornerShape(8.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.background,
                            unfocusedContainerColor = MaterialTheme.colorScheme.background
                        ),
                        maxLines = 1
                    )

                    // Title Designation drop-chips row
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.background)
                            .clickable {
                                // Toggle options inside a small loop
                                val options = listOf("Masyarakat Umum", "Mahasiswa Hukum", "Akademisi", "Praktisi Hukum")
                                val currentIndex = options.indexOf(senderTitle)
                                val nextIndex = (currentIndex + 1) % options.size
                                viewModel.chatSenderTitle.value = options[nextIndex]
                            }
                            .padding(horizontal = 8.dp, vertical = 12.dp)
                            .testTag("chat_sender_title_toggle"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.secondary)
                            Text(
                                text = senderTitle,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Message text area input with send action Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = messageInput,
                        onValueChange = { viewModel.chatMessageInput.value = it },
                        placeholder = { Text("Tulis pesan obrolan hukum...", fontSize = 12.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_message_text_input"),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp),
                        shape = RoundedCornerShape(24.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.background,
                            unfocusedContainerColor = MaterialTheme.colorScheme.background
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { viewModel.sendChatMessage() })
                    )

                    IconButton(
                        onClick = { viewModel.sendChatMessage() },
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .testTag("chat_message_submit_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Kirim Pesan",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 6. SUB-COMPOSABLE FOR ARTICLES SCIENTIFIC COMMENTS AND FEEDBACK:
 * Displays dynamic academic responses underneath any published material.
 */
@Composable
fun ArticleCommentsSection(
    viewModel: PortalViewModel,
    article: Article
) {
    val comments by viewModel.comments.collectAsStateWithLifecycle()
    val authorName by viewModel.commentAuthorName.collectAsStateWithLifecycle()
    val textInput by viewModel.commentTextInput.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("article_comments_panel"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Comments Title label and Count marker
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Tanggapan Akademik (${comments.size})",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Komentar Ilmiah",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Bold
            )
        }

        // List of Active Comments Under this Article
        if (comments.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.background)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Belum Ada Komentar",
                        tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Belum Ada Tanggapan",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Jadilah yang pertama memberikan review akademis atau pertanyaan hukum di sini.",
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(11.dp)
            ) {
                comments.forEach { comment ->
                    val isCommentAdmin = comment.isAuthorAdmin
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("comment_item_${comment.id}"),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCommentAdmin) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.04f)
                            } else {
                                MaterialTheme.colorScheme.background
                            }
                        ),
                        border = if (isCommentAdmin) {
                            BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                        } else null
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            // Header: Name and Date and Trash controller
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = comment.authorName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (isCommentAdmin) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (isCommentAdmin) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(MaterialTheme.colorScheme.primary)
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "ADMIN",
                                                color = MaterialTheme.colorScheme.onPrimary,
                                                fontSize = 7.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        }
                                    }
                                }
                                
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = comment.timestamp,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    // Let users delete comments in this local offline sandbox
                                    IconButton(
                                        onClick = { viewModel.deleteComment(comment) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Hapus Komentar",
                                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = comment.commentText,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }

        // Add Comment Form Input Panel
        val loggedInUser by viewModel.loggedInUser.collectAsStateWithLifecycle()

        if (loggedInUser == null) {
            ElevatedCard(
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.03f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().testTag("comment_auth_gate_card")
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Gunakan Akun untuk Menanggapi",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = "Silakan masuk ke dalam akun hukum Anda guna memberikan tanggapan ilmiah pada analisis hukum ini.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        lineHeight = 15.sp
                    )

                    var inlineEmail by remember { mutableStateOf("") }
                    var inlinePassword by remember { mutableStateOf("") }

                    OutlinedTextField(
                        value = inlineEmail,
                        onValueChange = { inlineEmail = it; viewModel.authEmail.value = it },
                        placeholder = { Text("Gmail Anda (e.g. budi@gmail.com)", fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("comment_inline_email"),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp)
                    )
                    OutlinedTextField(
                        value = inlinePassword,
                        onValueChange = { inlinePassword = it; viewModel.authPassword.value = it },
                        placeholder = { Text("Kata Sandi", fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("comment_inline_password"),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp),
                        visualTransformation = PasswordVisualTransformation()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.authEmail.value = inlineEmail
                                viewModel.authPassword.value = inlinePassword
                                viewModel.isRegisterMode.value = false
                                viewModel.handleAuth()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Masuk", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.authEmail.value = inlineEmail
                                viewModel.authPassword.value = inlinePassword
                                viewModel.isRegisterMode.value = true
                                viewModel.handleAuth()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Pendaftaran", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Quick Seeds in Comments Panel
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Isi Cepat Demo:", fontSize = 9.sp, color = MaterialTheme.colorScheme.secondary)
                        Text(
                            text = "Admin",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable {
                                inlineEmail = "muhammaddirwan2710@gmail.com"
                                inlinePassword = "adminpassword"
                            }
                        )
                        Text("|", fontSize = 9.sp, color = MaterialTheme.colorScheme.secondary)
                        Text(
                            text = "Pengunjung",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable {
                                inlineEmail = "tamu@gmail.com"
                                inlinePassword = "tamupassword"
                            }
                        )
                    }
                }
            }
        } else {
            // Add Comment Form Input Panel
            ElevatedCard(
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tulis Tanggapan Anda",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Text(
                            text = "Menanggapi sebagai: ${loggedInUser!!.name}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { viewModel.commentTextInput.value = it },
                        placeholder = { Text("Tulis review substantif atau tanggapan terkait analisis hukum di atas di sini...", fontSize = 12.sp) },
                        singleLine = false,
                        minLines = 3,
                        maxLines = 6,
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("comment_text_input"),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.background,
                            unfocusedContainerColor = MaterialTheme.colorScheme.background
                        )
                    )

                    Button(
                        onClick = { viewModel.submitComment() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .testTag("comment_submit_btn"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("Terbitkan Tanggapan", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VisitorLoginGate(
    viewModel: PortalViewModel,
    titleText: String,
    subtitleText: String
) {
    val authEmail by viewModel.authEmail.collectAsStateWithLifecycle()
    val authPassword by viewModel.authPassword.collectAsStateWithLifecycle()
    val authName by viewModel.authName.collectAsStateWithLifecycle()
    val isRegisterMode by viewModel.isRegisterMode.collectAsStateWithLifecycle()

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .testTag("visitor_login_gate"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon header
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = titleText,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = subtitleText,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))

            // Form Fields
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Register Mode Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Tab(
                        selected = !isRegisterMode,
                        onClick = { viewModel.isRegisterMode.value = false },
                        text = { Text("Masuk Saja", fontWeight = if (!isRegisterMode) FontWeight.Bold else FontWeight.Normal) }
                    )
                    Tab(
                        selected = isRegisterMode,
                        onClick = { viewModel.isRegisterMode.value = true },
                        text = { Text("Mendaftar", fontWeight = if (isRegisterMode) FontWeight.Bold else FontWeight.Normal) }
                    )
                }

                if (isRegisterMode) {
                    OutlinedTextField(
                        value = authName,
                        onValueChange = { viewModel.authName.value = it },
                        label = { Text("Nama Lengkap Anda *", fontSize = 12.sp) },
                        placeholder = { Text("Contoh: Maya Indah, S.H.", fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("auth_name_input"),
                        shape = RoundedCornerShape(8.dp),
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                }

                OutlinedTextField(
                    value = authEmail,
                    onValueChange = { viewModel.authEmail.value = it },
                    label = { Text("Email / Gmail *", fontSize = 12.sp) },
                    placeholder = { Text("Contoh: nama@gmail.com", fontSize = 12.sp) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth().testTag("auth_email_input"),
                    shape = RoundedCornerShape(8.dp),
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )

                OutlinedTextField(
                    value = authPassword,
                    onValueChange = { viewModel.authPassword.value = it },
                    label = { Text("Kata Sandi * (min 6 karakter)", fontSize = 12.sp) },
                    placeholder = { Text("Password", fontSize = 12.sp) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth().testTag("auth_password_input"),
                    shape = RoundedCornerShape(8.dp),
                    leadingIcon = { Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }

            // Submit Button
            Button(
                onClick = { viewModel.handleAuth() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("auth_submit_btn"),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (isRegisterMode) "Daftar & Masuk" else "Masuk Aplikasi",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            // Quick seeds help section
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.04f)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Demo Cepat (Klik untuk Isian Otomatis):",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.authEmail.value = "muhammaddirwan2710@gmail.com"
                                viewModel.authPassword.value = "adminpassword"
                                viewModel.isRegisterMode.value = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), contentColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.weight(1f).height(24.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Akun Admin", fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }
                        
                        Button(
                            onClick = {
                                viewModel.authEmail.value = "tamu@gmail.com"
                                viewModel.authPassword.value = "tamupassword"
                                viewModel.isRegisterMode.value = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f), contentColor = MaterialTheme.colorScheme.secondary),
                            modifier = Modifier.weight(1f).height(24.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Akun Tamu", fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
