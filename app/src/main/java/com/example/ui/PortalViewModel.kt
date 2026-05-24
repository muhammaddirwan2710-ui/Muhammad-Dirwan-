package com.example.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.Article
import com.example.data.ArticleRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class PortalViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ArticleRepository
    
    // Reactive flow of state inputs
    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow("Semua") // "Semua", "Karya Ilmiah", "Pendapat Hukum"
    
    // List of articles combining database, search query, and category filters
    private val _articles = MutableStateFlow<List<Article>>(emptyList())
    val articles: StateFlow<List<Article>> = _articles.asStateFlow()

    // Screen navigation model
    val activeTab = MutableStateFlow(0) // 0 = Beranda, 1 = Diskusi, 2 = Unggah, 3 = Hubungi S.H.
    val selectedArticle = MutableStateFlow<Article?>(null)

    // Form upload state variables
    val formTitle = MutableStateFlow("")
    val formCategory = MutableStateFlow("Karya Ilmiah") // "Karya Ilmiah" or "Pendapat Hukum"
    val formDescription = MutableStateFlow("")
    val formContent = MutableStateFlow("")
    val formCategoryShortcut = MutableStateFlow("Hukum Perdata") // Custom subcategory default
    val formAuthorName = MutableStateFlow("")
    val isUploadingAsAdmin = MutableStateFlow(true) // True = Muhammad Dirwan, S.H., False = Custom visitor name
    
    // User session and authentication state inputs
    val loggedInUser = MutableStateFlow<com.example.data.User?>(null)
    val authEmail = MutableStateFlow("")
    val authPassword = MutableStateFlow("")
    val authName = MutableStateFlow("")
    val isRegisterMode = MutableStateFlow(false)

    // Chat discussion room inputs
    val chatSenderName = MutableStateFlow("")
    val chatSenderTitle = MutableStateFlow("Masyarakat Umum")
    val chatMessageInput = MutableStateFlow("")
    
    // Comments section inputs
    val commentAuthorName = MutableStateFlow("")
    val commentTextInput = MutableStateFlow("")
    
    private val _comments = MutableStateFlow<List<com.example.data.Comment>>(emptyList())
    val comments: StateFlow<List<com.example.data.Comment>> = _comments.asStateFlow()

    val chatMessages: StateFlow<List<com.example.data.ChatMessage>> by lazy {
        repository.allChatMessages.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    // Holds details of currently chosen local PDF file to import
    private val _attachedPdfUri = MutableStateFlow<Uri?>(null)
    val attachedPdfUri: StateFlow<Uri?> = _attachedPdfUri.asStateFlow()
    
    private val _attachedPdfName = MutableStateFlow<String?>(null)
    val attachedPdfName: StateFlow<String?> = _attachedPdfName.asStateFlow()

    // For snackbars/notifications
    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage = _toastMessage.asSharedFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = ArticleRepository(
            database.articleDao(),
            database.commentDao(),
            database.chatMessageDao(),
            database.userDao()
        )
        
        // Ensure database has some initial professional legal writings on first launch
        viewModelScope.launch {
            repository.checkAndSeedDatabase()
            repository.checkAndSeedAdmin()
        }

        // Keep active comments synchronized to the currently selected article
        viewModelScope.launch {
            selectedArticle.collectLatest { article ->
                if (article != null) {
                    repository.getCommentsForArticle(article.id).collect {
                        _comments.value = it
                    }
                } else {
                    _comments.value = emptyList()
                }
            }
        }

        // Keep articles list synchronized reactively according to search query and category filters
        combine(
            repository.allArticles,
            searchQuery,
            selectedCategory
        ) { list, query, category ->
            var filtered = list
            
            // Apply category filter
            if (category != "Semua") {
                filtered = filtered.filter { it.category.equals(category, ignoreCase = true) }
            }
            
            // Apply search filter
            if (query.isNotBlank()) {
                filtered = filtered.filter {
                    it.title.contains(query, ignoreCase = true) ||
                    it.description.contains(query, ignoreCase = true) ||
                    it.content.contains(query, ignoreCase = true) ||
                    it.categoryShortcut.contains(query, ignoreCase = true)
                }
            }
            filtered
        }.onEach {
            _articles.value = it
        }.launchIn(viewModelScope)
    }

    fun selectCategory(category: String) {
        selectedCategory.value = category
    }

    fun selectArticle(article: Article?) {
        selectedArticle.value = article
    }

    fun selectTab(index: Int) {
        activeTab.value = index
        // Clear active selection on tab change to go back to directory
        if (index != 0) {
            selectedArticle.value = null
        }
    }

    fun attachPdf(uri: Uri?, context: Context) {
        if (uri == null) {
            _attachedPdfUri.value = null
            _attachedPdfName.value = null
            return
        }

        _attachedPdfUri.value = uri
        
        // Resolve file name
        var name: String? = null
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) {
                    name = it.getString(index)
                }
            }
        }
        _attachedPdfName.value = name ?: "dokumen_hukum.pdf"
    }

    fun unattachPdf() {
        _attachedPdfUri.value = null
        _attachedPdfName.value = null
    }

    // Authentication handlers
    fun handleAuth() {
        val email = authEmail.value.trim()
        val password = authPassword.value
        val name = authName.value.trim()

        if (email.isEmpty()) {
            triggerToast("Email / Gmail tidak boleh kosong")
            return
        }
        if (!email.contains("@")) {
            triggerToast("Alamat email tidak valid (harus mengandung @)")
            return
        }
        if (password.length < 6) {
            triggerToast("Password minimal terdiri dari 6 karakter")
            return
        }

        viewModelScope.launch {
            if (isRegisterMode.value) {
                // Register
                if (name.isEmpty()) {
                    triggerToast("Nama lengkap tidak boleh kosong untuk pendaftaran")
                    return@launch
                }
                
                // Check if email already registered
                val existing = repository.getUserByEmail(email)
                if (existing != null) {
                    triggerToast("Email tersebut sudah terdaftar")
                    return@launch
                }

                val isAdmin = email.equals("muhammaddirwan2710@gmail.com", ignoreCase = true)
                val newUser = com.example.data.User(
                    email = email,
                    password = password,
                    name = name,
                    isAdmin = isAdmin
                )
                val resultId = repository.registerUser(newUser)
                if (resultId > 0) {
                    val registeredUser = newUser.copy(id = resultId.toInt())
                    loggedInUser.value = registeredUser
                    
                    // Set default names in fields
                    commentAuthorName.value = registeredUser.name
                    chatSenderName.value = registeredUser.name
                    formAuthorName.value = registeredUser.name
                    isUploadingAsAdmin.value = registeredUser.isAdmin
                    
                    triggerToast("Pendaftaran berhasil! Selamat datang, ${registeredUser.name}")
                    
                    // Reset fields
                    authEmail.value = ""
                    authPassword.value = ""
                    authName.value = ""
                } else {
                    triggerToast("Gagal melakukan pendaftaran")
                }
            } else {
                // Login
                val user = repository.getUserByEmail(email)
                if (user == null) {
                    triggerToast("Email belum terdaftar.")
                    return@launch
                }
                if (user.password != password) {
                    triggerToast("Kata sandi salah. Silakan coba lagi.")
                    return@launch
                }

                loggedInUser.value = user
                
                // Set default names in fields
                commentAuthorName.value = user.name
                chatSenderName.value = user.name
                formAuthorName.value = user.name
                isUploadingAsAdmin.value = user.isAdmin
                
                triggerToast("Berhasil masuk! Selamat datang kembali, ${user.name}")
                
                // Reset fields
                authEmail.value = ""
                authPassword.value = ""
                authName.value = ""
            }
        }
    }

    fun logout() {
        loggedInUser.value = null
        triggerToast("Anda telah keluar dari akun.")
    }

    // Main action: Save upload directly to local Room DB
    fun saveArticleAndPublish(context: Context) {
        val currentUser = loggedInUser.value
        if (currentUser == null) {
            triggerToast("Silakan login/masuk terlebih dahulu guna memposting naskah Anda")
            return
        }

        val title = formTitle.value.trim()
        val category = formCategory.value
        val description = formDescription.value.trim()
        val content = formContent.value.trim()
        val shortcut = formCategoryShortcut.value.trim()
        val pdfUri = _attachedPdfUri.value
        
        // Define Author name based on logged-in identity
        val authorName = if (currentUser.isAdmin && isUploadingAsAdmin.value) {
            "Muhammad Dirwan, S.H."
        } else {
            currentUser.name
        }

        if (title.isEmpty()) {
            triggerToast("Judul tulisan tidak boleh kosong")
            return
        }
        if (description.isEmpty()) {
            triggerToast("Ringkasan tidak boleh kosong")
            return
        }
        if (content.isEmpty() && pdfUri == null) {
            triggerToast("Silakan isi konten utama tulisan atau lampirkan file PDF")
            return
        }

        viewModelScope.launch {
            var finalPdfPath: String? = null
            var finalPdfName: String? = null

            // If a valid PDF file lies selected, clone it to permanent sandboxed storage
            if (pdfUri != null) {
                val fileName = _attachedPdfName.value ?: "dokumen_${System.currentTimeMillis()}.pdf"
                finalPdfPath = copyDocumentToStorage(context, pdfUri, fileName)
                finalPdfName = fileName
            }

            // Estimate reading pacing
            val wordsCount = content.split("\\s+".toRegex()).size
            val estimatedMinutes = if (wordsCount > 0) (wordsCount / 180).coerceAtLeast(1) else 3
            val readTimeText = if (pdfUri != null && content.isEmpty()) "File PDF" else "$estimatedMinutes menit"

            val dateString = getIndonesianCurrentDate()

            val newArticle = Article(
                title = title,
                category = category,
                description = description,
                content = if (content.isEmpty() && pdfUri != null) "Dokumen PDF terlampir. Gunakan penampil tulisan untuk membaca berkas." else content,
                author = authorName,
                pdfUri = finalPdfPath,
                pdfFileName = finalPdfName,
                date = dateString,
                readTime = readTimeText,
                categoryShortcut = if (shortcut.isEmpty()) "Hukum Umum" else shortcut
            )

            val newId = repository.insert(newArticle)
            if (newId > 0) {
                triggerToast("Karya tulis ilmiah berhasil diterbitkan secara lokal!")
                
                // Clear Form inputs
                formTitle.value = ""
                formDescription.value = ""
                formContent.value = ""
                formCategoryShortcut.value = "Hukum Perdata"
                formAuthorName.value = ""
                _attachedPdfUri.value = null
                _attachedPdfName.value = null
                
                // Navigate back to Beranda tab and focus on this new entry
                activeTab.value = 0
                viewModelScope.launch {
                    val actualInserted = repository.getArticleById(newId.toInt()).first()
                    selectedArticle.value = actualInserted
                }
            } else {
                triggerToast("Kesalahan saat menyimpan berkas.")
            }
        }
    }

    // Discussion room actions
    fun sendChatMessage() {
        val currentUser = loggedInUser.value
        if (currentUser == null) {
            triggerToast("Silakan masuk terlebih dahulu untuk ikut berdiskusi")
            return
        }

        val name = currentUser.name
        val message = chatMessageInput.value.trim()
        val title = chatSenderTitle.value

        if (message.isEmpty()) {
            triggerToast("Pesan komentar diskusi tidak boleh kosong")
            return
        }

        viewModelScope.launch {
            val timestamp = getIndonesianCurrentTime()
            val chat = com.example.data.ChatMessage(
                senderName = name,
                messageText = message,
                senderTitle = title,
                timestamp = timestamp,
                isSenderAdmin = currentUser.isAdmin
            )
            repository.insertChatMessage(chat)
            chatMessageInput.value = "" // Clear input text
            triggerToast("Diskusi dikirim!")
        }
    }

    // Article comments logic
    fun submitComment() {
        val article = selectedArticle.value ?: return
        val currentUser = loggedInUser.value
        if (currentUser == null) {
            triggerToast("Silakan masuk terlebih dahulu untuk mengirim tanggapan ilmiah")
            return
        }

        val name = currentUser.name
        val text = commentTextInput.value.trim()

        if (text.isEmpty()) {
            triggerToast("Isi tanggapan komentar tidak boleh kosong")
            return
        }

        viewModelScope.launch {
            val calendar = java.util.Calendar.getInstance()
            val hour = String.format("%02d", calendar.get(java.util.Calendar.HOUR_OF_DAY))
            val minute = String.format("%02d", calendar.get(java.util.Calendar.MINUTE))
            val dateStr = getIndonesianCurrentDate()
            val timestamp = "$dateStr • $hour:$minute WIB"

            val newComment = com.example.data.Comment(
                articleId = article.id,
                authorName = name,
                commentText = text,
                timestamp = timestamp,
                isAuthorAdmin = currentUser.isAdmin
            )
            repository.insertComment(newComment)
            commentTextInput.value = "" // Clear text area
            triggerToast("Tanggapan berhasil diterbitkan!")
        }
    }

    fun deleteComment(comment: com.example.data.Comment) {
        viewModelScope.launch {
            repository.deleteComment(comment)
            triggerToast("Komentar telah dihapus.")
        }
    }

    private fun getIndonesianCurrentTime(): String {
        val calendar = java.util.Calendar.getInstance()
        val hour = String.format("%02d", calendar.get(java.util.Calendar.HOUR_OF_DAY))
        val minute = String.format("%02d", calendar.get(java.util.Calendar.MINUTE))
        return "$hour:$minute"
    }

    fun deleteArticle(article: Article) {
        viewModelScope.launch {
            repository.delete(article)
            triggerToast("Tulisan '${article.title}' berhasil dihapus.")
            if (selectedArticle.value?.id == article.id) {
                selectedArticle.value = null
            }
        }
    }

    private fun triggerToast(message: String) {
        viewModelScope.launch {
            _toastMessage.emit(message)
        }
    }

    // Save PDF Stream into private files sandbox directory
    private fun copyDocumentToStorage(context: Context, sourceUri: Uri, destinationName: String): String? {
        return try {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(sourceUri) ?: return null
            
            // Ensure unique naming inside application space
            val safeName = "published_pdf_${System.currentTimeMillis()}_$destinationName"
            val destinationFile = File(context.filesDir, safeName)
            
            FileOutputStream(destinationFile).use { fos ->
                inputStream.copyTo(fos)
            }
            destinationFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getIndonesianCurrentDate(): String {
        val calendar = java.util.Calendar.getInstance()
        val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)
        val monthIndex = calendar.get(java.util.Calendar.MONTH)
        val year = calendar.get(java.util.Calendar.YEAR)
        
        val months = listOf(
            "Januari", "Februari", "Maret", "April", "Mei", "Juni",
            "Juli", "Agustus", "September", "Oktober", "November", "Desember"
        )
        val monthName = if (monthIndex in 0..11) months[monthIndex] else ""
        return "$day $monthName $year"
    }
}
