package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class ArticleRepository(
    private val articleDao: ArticleDao,
    private val commentDao: CommentDao,
    private val chatMessageDao: ChatMessageDao,
    private val userDao: UserDao
) {

    val allArticles: Flow<List<Article>> = articleDao.getAllArticles()

    fun getArticleById(id: Int): Flow<Article?> = articleDao.getArticleById(id)

    fun getArticlesByCategory(category: String): Flow<List<Article>> = articleDao.getArticlesByCategory(category)

    fun searchArticles(query: String): Flow<List<Article>> = articleDao.searchArticles(query)

    suspend fun insert(article: Article): Long = articleDao.insertArticle(article)

    suspend fun delete(article: Article) = articleDao.deleteArticle(article)

    // User operations
    suspend fun getUserByEmail(email: String): User? = userDao.getUserByEmail(email)

    suspend fun registerUser(user: User): Long = userDao.registerUser(user)

    suspend fun checkAndSeedAdmin() {
        if (userDao.getUserCount() == 0) {
            val defaultAdmin = User(
                email = "muhammaddirwan2710@gmail.com",
                password = "adminpassword", // Simple default admin password, user can log in with it or register any custom account
                name = "Muhammad Dirwan, S.H.",
                isAdmin = true
            )
            userDao.registerUser(defaultAdmin)
            
            // Also add a sample visitor account for easy visitor login testing
            val defaultVisitor = User(
                email = "tamu@gmail.com",
                password = "tamupassword",
                name = "Budi Cahyono, S.H.",
                isAdmin = false
            )
            userDao.registerUser(defaultVisitor)
        }
    }

    // Comments operations
    fun getCommentsForArticle(articleId: Int): Flow<List<Comment>> = commentDao.getCommentsForArticle(articleId)

    suspend fun insertComment(comment: Comment): Long = commentDao.insertComment(comment)

    suspend fun deleteComment(comment: Comment) = commentDao.deleteComment(comment)

    // Chat operations
    val allChatMessages: Flow<List<ChatMessage>> = chatMessageDao.getAllMessages()

    suspend fun insertChatMessage(message: ChatMessage): Long = chatMessageDao.insertMessage(message)

    suspend fun clearChat() = chatMessageDao.clearChat()

    suspend fun checkAndSeedDatabase() {
        val currentList = articleDao.getAllArticles().first()
        if (currentList.isEmpty()) {
            val seedData = listOf(
                Article(
                    title = "Analisis Kekuatan Hukum Alat Bukti Elektronik dalam Hukum Acara Perdata",
                    category = "Pendapat Hukum",
                    description = "Legal opinion komprehensif mengenai keabsahan tanda tangan elektronik dan screenshot percakapan sebagai alat bukti sah di muka persidangan berdasarkan UU ITE terbaru.",
                    content = """
                        I. PENDAHULUAN
                        Perkembangan teknologi informasi telah mengubah lanskap hukum pembuktian di Indonesia. Kehadiran dokumen elektronik, tanda tangan digital, hingga rekaman komunikasi digital kini menjadi realita yang dominan dalam transaksi bisnis dan interaksi sosial. Pertanyaan hukum yang mendasar adalah sejauh mana alat bukti elektronik memiliki kekuatan mengikat dalam Hukum Acara Perdata Indonesia yang bersumber pada HIR/RBg.
                        
                        II. ISU HUKUM (LEGAL ISSUES)
                        1. Apakah dokumen elektronik dan informasi elektronik memenuhi kualifikasi alat bukti tertulis (dokumen) berdasarkan Pasal 1866 KUHPerdata?
                        2. Bagaimanakah syarat formil dan materiil agar alat bukti elektronik diakui keabsahannya dalam persidangan perdata?
                        3. Bagaimana kekuatan pembuktian tanda tangan elektronik tersertifikasi dibanding tanda tangan basah?

                        III. ANALISIS YURIDIS (LEGAL ANALYSIS)
                        Berdasarkan Pasal 5 ayat (1) Undang-Undang Nomor 11 Tahun 2008 tentang Informasi dan Transaksi Elektronik (UU ITE) sebagaimana telah beberapa kali diubah, ditegaskan bahwa Informasi Elektronik dan/atau Dokumen Elektronik beserta hasil cetaknya merupakan alat bukti hukum yang sah. Ketentuan ini memperluas alat bukti yang diatur dalam Pasal 1866 KUHPerdata dan Pasal 164 HIR.
                        
                        Namun, Pasal 6 UU ITE memberikan batasan penting: Informasi dan/atau Dokumen Elektronik dianggap sah sepanjang informasi yang tercantum di dalamnya dapat diakses, ditampilkan, dijamin keutuhannya, dan dapat dipertanggungjawabkan sehingga menerangkan suatu keadaan.
                        
                        1. Bukti Screenshot Percakapan (WhatsApp/Email):
                        Yurisprudensi Mahkamah Agung menunjukkan bahwa tangkapan layar (screenshot) media sosial tidak dapat langsung berdiri sendiri sebagai alat bukti primer. Agar dinilai sah secara materiil, screenshot tersebut harus dikuatkan dengan bukti pemeriksaan digital forensik terhadap perangkat pembuatnya, atau diakui oleh pihak yang bersangkutan di persidangan (Pasal 1925 KUHPerdata tentang Pengakuan).
                        
                        2. Keandalan Tanda Tangan Elektronik:
                        Berdasarkan Pasal 11 UU ITE, Tanda Tangan Elektronik memiliki kekuatan hukum dan akibat hukum yang sah selama memenuhi persyaratan keandalan (pembuatan unik pada pemegang, kendali eksklusif, audit log perubahan). Tanda tangan elektronik tersertifikasi (registered CA) memiliki nilai pembuktian yang sempurna layaknya akta otentik karena memiliki jaminan non-repudiasi yang tinggi.

                        IV. PENDAPAT AKHIR (LEGAL CONCLUSION)
                        1. Alat bukti elektronik adalah sah dan memiliki posisi mandiri pasca-berlakunya UU ITE, memperluas cakupan Pasal 1866 KUHPerdata.
                        2. Kekuatan pembuktian dokumen elektronik bersifat bebas (vrij bewijskracht). Hakim memiliki kewenangan menilai keandalan sistem yang menerbitkannya.
                        3. Direkomendasikan kepada pihak pelaku usaha untuk selalu menggunakan tanda tangan elektronik tersertifikasi dalam setiap kontrak elektronik guna mengamankan kepastian hukum eksekusi hak jika timbul wanprestasi.
                        
                        -- Muhammad Dirwan, S.H. (Advokat & Konsultan Hukum)
                    """.trimIndent(),
                    date = "15 Mei 2026",
                    readTime = "6 menit",
                    categoryShortcut = "Hukum Pembuktian"
                ),
                Article(
                    title = "Tinjauan Yuridis Perlindungan Konsumen Terhadap Klausul Baku Eksonerasi di E-Commerce",
                    category = "Karya Ilmiah",
                    description = "Karya ilmiah yang mengkaji eksistensi klausul pengalihan tanggung jawab sepihak (eksonerasi) oleh merchant digital ditinjau dari Pasal 18 UU No. 8 Tahun 1999 tentang Perlindungan Konsumen.",
                    content = """
                        ABSTRAK
                        Penelitian ini bertujuan untuk menganalisis keabsahan penggunaan klausul baku eksonerasi yang mencantumkan pembatasan atau penghapusan tanggung jawab pelaku usaha secara sepihak di platform perdagangan elektronik (e-commerce). Dengan metode penelitian hukum normatif, hasil kajian menunjukkan bahwa mayoritas merchant e-commerce menyisipkan klausul baku yang bertentangan dengan Pasal 18 Undang-Undang Perlindungan Konsumen (UUPK), yang berakibat batal demi hukum demi melindungi hak-hak dasar konsumen.
                        
                        I. PENDAHULUAN
                        Perdagangan daring (e-commerce) menawarkan kepraktisan yang tinggi. Kontrak yang terbentuk umumnya menggunakan format 'Adhesion Contract' atau kontrak baku, di mana syarat-syarat perjanjian telah dipersiapkan terlebih dahulu oleh pelaku usaha secara sepihak melalui dokumen Syarat & Ketentuan (Terms of Service). Sifat kontrak ini adalah 'take it or leave it'. Konsumen tidak diberi ruang negosiasi.
                        
                        Seringkali pelaku usaha menyelipkan klausul eksonerasi, misalnya: "Barang yang sudah dibeli tidak dapat ditukar", atau "Kami tidak bertanggung jawab atas kerusakan barang selama pengiriman oleh kurir". Klausul semacam ini memindahkan risiko kerugian sepenuhnya kepada pundak konsumen.

                        II. PEMBAHASAN & ANALISIS YURIDIS
                        Pelarangan klausul eksonerasi secara tegas diatur dalam Pasal 18 ayat (1) Undang-Undang Nomor 8 Tahun 1999 tentang Perlindungan Konsumen (UUPK). Pelaku usaha dilarang mencantumkan klausul baku yang menyatakan pengalihan tanggung jawab, atau menyatakan menolak penyerahan kembali uang yang dibayarkan konsumen.
                        
                        Menurut Pasal 18 ayat (3) UUPK, setiap klausul baku yang telah ditetapkan oleh pelaku usaha pada dokumen yang melanggar ayat (1) dinyatakan BATAL DEMI HUKUM.
                        
                        Penegakan hukum atas pelanggaran ini seringkali mengalami kendala karena kurangnya literasi hukum konsumen. Sebagian besar konsumen menganggap Syarat & Ketentuan online adalah mutlak dan mengikat total. Padahal, pengadilan memiliki asas menafsirkan klausul ambigu demi keuntungan pihak yang lebih lemah (contra proferentem).
                        
                        III. SIMPULAN
                        Meskipun asas kebebasan berkontrak dijunjung tinggi dalam Pasal 1338 KUHPerdata, asas tersebut dibatasi oleh ketertiban umum dan perlindungan pihak lemah. Klausul eksonerasi dalam e-commerce yang melanggar Pasal 18 UUPK adalah tidak sah dan tidak memiliki daya eksekutorial. Konsumen berhak menuntut ganti rugi apabila barang yang diterima cacat tersembunyi atau tidak sesuai deskripsi, tanpa terhalang oleh disclaimer sepihak dari merchant.
                        
                        -- Peneliti utama: Muhammad Dirwan, S.H. (Diterbitkan pada Jurnal Kajian Hukum Nusantara)
                    """.trimIndent(),
                    date = "22 April 2026",
                    readTime = "8 menit",
                    categoryShortcut = "Perlindungan Konsumen"
                ),
                Article(
                    title = "Uji Kelayakan Tindakan Direksi Melalui Doktrin Business Judgment Rule",
                    category = "Pendapat Hukum",
                    description = "Opini hukum mengenai batas perlindungan bagi jajaran Direksi Perseroan Terbatas dari pertanggungjawaban pribadi atas kerugian bisnis berdasarkan Pasal 97 UU No. 40 Tahun 2007 tentang Perseroan Terbatas.",
                    content = """
                        I. PERMASALAHAN HUKUM
                        Dalam pengelolaan Perseroan Terbatas (PT), keputusan bisnis yang diambil oleh direksi tidak selalu membuahkan keuntungan. Kadangkala, keputusan tersebut berakibat pada kerugian finansial yang signifikan bagi perseroan. Pertanyaan mendasarnya: Kapankah seorang anggota Direksi dapat dimintai pertanggungjawaban secara pribadi hingga ke harta kekayaan pribadinya atas kerugian yang dialami perseroan?
                        
                        II. LANDASAN HUKUM & DOKRIN
                        Doktrin Business Judgment Rule (BJR) merupakan konsep yang lahir dari hukum korporasi anglo-saxon, yang kemudian diadopsi ke dalam Pasal 97 ayat (5) Undang-Undang Nomor 40 Tahun 2007 tentang Perseroan Terbatas (UUPT). Doktrin ini memberikan perlindungan bagi direksi atas keputusan bisnisnya selama keputusan tersebut diambil dengan iktikad baik, kehati-hatian, demi kepentingan perseroan, dan tidak ada benturan kepentingan (conflict of interest).

                        III. ANALISIS DAN SYARAT PERLINDUNGAN
                        Agar direksi terbebas dari tanggung jawab renteng pribadi atas kerugian perseroan, direksi harus membuktikan bahwa tindakannya memenuhi pengujian sebagai berikut:
                        1. Kekuasaan Hukum (Ultra Vires Test): Tindakan direksi harus berada dalam koridor ruang lingkup maksud dan tujuan perseroan sebagaimana diatur dalam Anggaran Dasar (AD).
                        2. Kehati-hatian (Duty of Care): Direksi harus melakukan penilaian yang matang, memanfaatkan jasa analis profesional jika dibutuhkan, dan tidak tergesa-gesa (uninformed decision).
                        3. Loyalitas (Duty of Loyalty): Keputusan dilakukan semata-mata untuk kemajuan perseroan, bukan untuk memuntungkan diri sendiri, keluarga, atau pihak terafiliasi.
                        
                        Apabila terbukti terdapat iktikad buruk, transaksi culas (self-dealing), atau kelalaian berat (gross negligence), maka perlindungan BJR gugur demi hukum. Berdasarkan Pasal 97 ayat (3) UUPT, setiap anggota direksi bertanggung jawab penuh secara pribadi apabila yang bersangkutan bersalah atau lalai menjalankan tugasnya.

                        IV. KESIMPULAN REKOMENDASI
                        Business Judgment Rule bukanlah tameng mutlak untuk melindungi ketidakcakapan direksi. Ini adalah instrumen keadilan yang melindungi direksi kreatif yang bertindak jujur dari risiko komersial murni. Guna memitigasi tuntutan hukum (derivatives action) dari pemegang saham minoritas, direksi wajib mendokumentasikan setiap latar belakang pengambilan keputusan (minutes of meeting) beserta kajian kelayakan (feasibility study) secara transparan.
                        
                        -- Muhammad Dirwan, S.H. (Konsultan Hukum Korporasi)
                    """.trimIndent(),
                    date = "08 Maret 2026",
                    readTime = "5 menit",
                    categoryShortcut = "Hukum Perusahaan"
                )
            )
            for (article in seedData) {
                articleDao.insertArticle(article)
            }
        }
    }
}
