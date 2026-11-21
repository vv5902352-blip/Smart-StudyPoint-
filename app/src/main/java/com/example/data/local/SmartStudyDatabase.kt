package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.CatalogType
import com.example.data.model.DefinitionItem
import com.example.data.model.GeneratedNotes
import com.example.data.model.NoteCategory
import com.example.data.model.NotesType
import com.example.data.model.StudyLanguage
import com.example.data.model.TableData

@Database(
    entities = [
        StudyNoteEntity::class,
        SearchHistoryEntity::class,
        QuizAttemptEntity::class,
        CatalogItemEntity::class,
        ManagedUserEntity::class,
        ChatMessageEntity::class,
        FavoriteQuestionEntity::class,
        FlashcardDeckEntity::class,
        FlashcardEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class SmartStudyDatabase : RoomDatabase() {
    abstract fun studyNoteDao(): StudyNoteDao
    abstract fun searchHistoryDao(): SearchHistoryDao
    abstract fun quizAttemptDao(): QuizAttemptDao
    abstract fun catalogDao(): CatalogDao
    abstract fun managedUserDao(): ManagedUserDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun favoriteQuestionDao(): FavoriteQuestionDao
    abstract fun flashcardDao(): FlashcardDao

    companion object {
        @Volatile
        private var INSTANCE: SmartStudyDatabase? = null

        fun getInstance(context: Context): SmartStudyDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    SmartStudyDatabase::class.java,
                    "smart_studypoint.db"
                ).fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}

object SeedDataProvider {
    fun defaultCatalogItems(): List<CatalogItemEntity> {
        val classes = listOf(
            "Class 6th", "Class 7th", "Class 8th", "Class 9th",
            "Class 10th", "Class 11th", "Class 12th", "Graduate / Competitive"
        ).map { CatalogItemEntity(type = CatalogType.CLASS_LEVEL.name, name = it) }

        val exams = listOf(
            "CBSE Board", "UPSC CSE", "SSC CGL / CHSL", "Railway RRB NTPC",
            "BPSC", "State PCS / Exams", "NEET UG", "JEE Main"
        ).map { CatalogItemEntity(type = CatalogType.EXAM.name, name = it) }

        val subjects = listOf(
            "Indian Polity", "History", "Geography", "General Science",
            "Physics", "Chemistry", "Biology", "Mathematics",
            "Economics", "English Grammar", "Hindi Vyakaran", "Current Affairs & GK"
        ).map { CatalogItemEntity(type = CatalogType.SUBJECT.name, name = it) }

        return classes + exams + subjects
    }

    fun defaultUsers(): List<ManagedUserEntity> = listOf(
        ManagedUserEntity(
            fullName = "Aarav Sharma",
            email = "aarav.sharma@studypoint.in",
            role = "Student",
            targetExam = "UPSC CSE",
            isActive = true
        ),
        ManagedUserEntity(
            fullName = "Priya Verma",
            email = "priya.verma@studypoint.in",
            role = "Student",
            targetExam = "CBSE Board",
            isActive = true
        ),
        ManagedUserEntity(
            fullName = "Rohit Kumar",
            email = "rohit.kumar@studypoint.in",
            role = "Student",
            targetExam = "BPSC",
            isActive = true
        ),
        ManagedUserEntity(
            fullName = "Dr. Meera Iyer",
            email = "meera.iyer@studypoint.in",
            role = "Educator",
            targetExam = "SSC CGL / CHSL",
            isActive = true
        ),
        ManagedUserEntity(
            fullName = "Vikram Singh",
            email = "admin@studypoint.in",
            role = "Admin",
            targetExam = "All Exams",
            isActive = true
        )
    )

    fun defaultStudyNotes(): List<StudyNoteEntity> {
        val now = System.currentTimeMillis()
        val polityNote = GeneratedNotes(
            title = "Fundamental Rights (Articles 12–35)",
            classLevel = "Graduate / Competitive",
            exam = "UPSC CSE",
            subject = "Indian Polity",
            topic = "Fundamental Rights",
            language = StudyLanguage.ENGLISH,
            notesType = NotesType.COMPLETE_PACK,
            detailedNotes = "Part III of the Indian Constitution (Articles 12 to 35) enshrines Fundamental Rights, often called the Magna Carta of India. They are justiciable in nature, meaning any citizen can approach the Supreme Court (Article 32) or High Courts (Article 226) if these rights are violated.",
            shortNotes = listOf(
                "Originally 7 Fundamental Rights; Right to Property removed by 44th Amendment (1978).",
                "Article 14–18: Right to Equality | Article 19–22: Right to Freedom.",
                "Article 23–24: Against Exploitation | Article 25–28: Freedom of Religion.",
                "Article 29–30: Cultural & Educational | Article 32: Constitutional Remedies."
            ),
            easyExplanation = "Fundamental Rights act as a legal shield protecting individual liberty against arbitrary state action, ensuring equality and dignity for every citizen.",
            definitions = listOf(
                DefinitionItem("Article 12 (State)", "Includes Union & State Governments, Parliament, Legislatures, and local authorities."),
                DefinitionItem("Article 13 (Judicial Review)", "Any law inconsistent with Fundamental Rights is void to the extent of inconsistency."),
                DefinitionItem("Writs (Art. 32)", "Habeas Corpus, Mandamus, Prohibition, Certiorari, and Quo Warranto.")
            ),
            importantFacts = listOf(
                "Articles 15, 16, 19, 29, and 30 are available exclusively to Indian citizens.",
                "Articles 20 and 21 cannot be suspended even during a National Emergency.",
                "Inspired primarily by the Bill of Rights of the US Constitution."
            ),
            comparisonTable = TableData(
                title = "Six Fundamental Rights at a Glance",
                headers = listOf("Right", "Articles", "Key Highlight"),
                rows = listOf(
                    listOf("Right to Equality", "Art. 14–18", "Abolition of Untouchability (Art. 17)"),
                    listOf("Right to Freedom", "Art. 19–22", "Right to Life & Liberty (Art. 21)"),
                    listOf("Against Exploitation", "Art. 23–24", "Prohibits child labour below 14 yrs"),
                    listOf("Constitutional Remedies", "Art. 32", "5 Writs by Supreme Court")
                )
            ),
            flowchartSteps = listOf(
                "Citizen's Fundamental Right is infringed",
                "Petition filed under Art. 32 (SC) or Art. 226 (HC)",
                "Court examines law/action under Art. 13",
                "Appropriate Writ issued to enforce the Right"
            ),
            examples = listOf(
                "Kesavananda Bharati Case (1973): Established Basic Structure Doctrine.",
                "Maneka Gandhi Case (1978): Expanded Art. 21 to 'Due Process of Law'."
            ),
            examOrientedPoints = listOf(
                "High-frequency UPSC Prelims & BPSC topic (2–3 questions almost every year).",
                "Remember the exact difference between SC writ jurisdiction (Art. 32) and HC writ jurisdiction (Art. 226)."
            ),
            quickRevision = listOf(
                "Part III • Articles 12–35 • Justiciable • 6 Rights",
                "44th Amendment (1978) moved Right to Property to Art. 300A.",
                "Art. 21A (Right to Education 6–14 yrs) added by 86th Amendment (2002)."
            ),
            isSamplePreview = false
        ).toEntity(
            category = NoteCategory.REVISION_NOTES,
            isFavorite = true,
            isPublishedByAdmin = true
        ).copy(createdAt = now - 86_400_000L * 4, lastOpenedAt = now - 600_000L)

        val photosynthesisNote = GeneratedNotes(
            title = "Life Processes — Photosynthesis Quick Notes",
            classLevel = "Class 10th",
            exam = "CBSE Board",
            subject = "Biology",
            topic = "Photosynthesis",
            language = StudyLanguage.ENGLISH,
            notesType = NotesType.SHORT_NOTES,
            detailedNotes = "Photosynthesis is the autotrophic process by which green plants synthesize glucose from carbon dioxide and water using sunlight trapped by chlorophyll inside chloroplasts, releasing oxygen as a vital by-product.",
            shortNotes = listOf(
                "Equation: 6CO₂ + 12H₂O —(Sunlight/Chlorophyll)→ C₆H₁₂O₆ + 6O₂ + 6H₂O",
                "Site: Chloroplasts containing green pigment chlorophyll.",
                "Occurs in 3 key events: Light absorption, Water photolysis, CO₂ reduction."
            ),
            easyExplanation = "Think of a green leaf as a solar-powered kitchen: sunlight is the stove, water and CO₂ are raw ingredients, chlorophyll is the chef, and glucose is the cooked food.",
            definitions = listOf(
                DefinitionItem("Stomata", "Tiny pores on leaf surfaces surrounded by guard cells controlling gas exchange and transpiration."),
                DefinitionItem("Photolysis", "Splitting of water molecules into hydrogen and oxygen using light energy.")
            ),
            importantFacts = listOf(
                "Desert plants (CAM plants) open stomata at night to minimize water loss.",
                "Excess carbohydrates in plants are stored in the form of starch."
            ),
            comparisonTable = TableData(
                title = "Light Reaction vs Dark Reaction",
                headers = listOf("Feature", "Light Reaction", "Dark Reaction (Calvin)"),
                rows = listOf(
                    listOf("Site in Chloroplast", "Grana / Thylakoid", "Stroma"),
                    listOf("Direct Sunlight", "Required", "Not directly required"),
                    listOf("Main Output", "ATP, NADPH, O₂", "Glucose (C₆H₁₂O₆)")
                )
            ),
            flowchartSteps = listOf(
                "Chlorophyll absorbs sunlight energy",
                "Light energy converts to chemical energy & splits H₂O",
                "Oxygen (O₂) released via stomata",
                "CO₂ reduced to Glucose (stored as Starch)"
            ),
            examples = listOf(
                "Iodine Test: Blue-black colour confirms presence of starch in illuminated leaf.",
                "KOH Experiment: Proves carbon dioxide is essential for photosynthesis."
            ),
            examOrientedPoints = listOf(
                "CBSE Class 10 Board favourite 3-mark diagram question: Open & Closed Stomata.",
                "Always write the balanced chemical equation with sunlight & chlorophyll on the arrow."
            ),
            quickRevision = listOf(
                "Site: Chloroplast | Storage: Starch | Gas Released: O₂",
                "Guard cells swell → Stomata open; Guard cells shrink → Stomata close."
            ),
            isSamplePreview = false
        ).toEntity(
            category = NoteCategory.SHORT_NOTES,
            isFavorite = true,
            isPublishedByAdmin = true
        ).copy(createdAt = now - 86_400_000L * 2, lastOpenedAt = now - 1800_000L)

        val pyqNote = GeneratedNotes(
            title = "SSC & Railway Previous Year Science Questions",
            classLevel = "Graduate / Competitive",
            exam = "SSC CGL / CHSL",
            subject = "General Science",
            topic = "Important PYQ Capsule",
            language = StudyLanguage.ENGLISH,
            notesType = NotesType.PYQ_ANALYSIS,
            detailedNotes = "Curated high-frequency Previous Year Questions from SSC CGL, CHSL, and RRB NTPC exams covering Physics SI units, Chemistry everyday compounds, and Human Biology essentials.",
            shortNotes = listOf(
                "Baking Soda: Sodium Bicarbonate (NaHCO₃) | Washing Soda: Na₂CO₃·10H₂O.",
                "Vitamin C deficiency → Scurvy | Vitamin D deficiency → Rickets.",
                "Normal human blood pH lies narrowly between 7.35 and 7.45."
            ),
            easyExplanation = "Most SSC and Railway science questions repeat core NCERT Class 8–10 formulas and everyday chemistry names—mastering one capsule covers 60% of scoring items.",
            definitions = listOf(
                DefinitionItem("Ohm's Law", "V = I × R at constant temperature."),
                DefinitionItem("Isotopes", "Atoms with the same atomic number but different mass numbers.")
            ),
            importantFacts = listOf(
                "Sound travels fastest in solids (e.g. steel) and cannot travel in vacuum.",
                "Insulin hormone is secreted by Beta cells of the Pancreas (Islets of Langerhans)."
            ),
            comparisonTable = TableData(
                title = "Top Repeated SSC / RRB Chemical Names",
                headers = listOf("Common Name", "Chemical Name", "Formula"),
                rows = listOf(
                    listOf("Plaster of Paris", "Calcium Sulphate Hemihydrate", "CaSO₄·½H₂O"),
                    listOf("Bleaching Powder", "Calcium Oxychloride", "CaOCl₂"),
                    listOf("Quick Lime", "Calcium Oxide", "CaO")
                )
            ),
            flowchartSteps = listOf(
                "Scan last 5 years SSC / RRB PYQs",
                "Group repeated formulas & vitamins",
                "Practice 30-second rapid recall drills",
                "Revise weak entries before mock tests"
            ),
            examples = listOf(
                "RRB NTPC PYQ: SI unit of Frequency is Hertz (Hz).",
                "SSC CGL PYQ: Universal Donor blood group is O Negative."
            ),
            examOrientedPoints = listOf(
                "Expect 4–6 direct factual General Science questions in SSC Tier-1 and 20+ in RRB.",
                "Revise Vitamin scientific names (A-Retinol, B1-Thiamine, C-Ascorbic Acid)."
            ),
            quickRevision = listOf(
                "Blood pH: 7.4 | Universal Donor: O- | Universal Recipient: AB+",
                "Mirror in vehicle headlights: Concave | Rear-view mirror: Convex"
            ),
            isSamplePreview = false
        ).toEntity(
            category = NoteCategory.PYQ,
            isFavorite = false,
            isPublishedByAdmin = true
        ).copy(createdAt = now - 86_400_000L * 1, lastOpenedAt = now - 3600_000L)

        val importantQNote = GeneratedNotes(
            title = "1857 Revolt & Bihar's Role (Important Qs)",
            classLevel = "Graduate / Competitive",
            exam = "BPSC",
            subject = "History",
            topic = "Revolt of 1857",
            language = StudyLanguage.HINGLISH,
            notesType = NotesType.EXAM_FOCUSED,
            detailedNotes = "1857 ka Vidroh Bharat ka Pratham Swatantrata Sangram mana jata hai. Bihar mein Veer Kunwar Singh (Jagdishpur, Arrah) ne 80 saal ki umar mein British forces ke khilaf shandar netritva kiya. BPSC Prelims aur Mains dono ke liye yeh sabse important topic hai.",
            shortNotes = listOf(
                "10 May 1857 ko Meerut se vidroh ki shuruat hui.",
                "Bihar mein Patna se Pir Ali aur Jagdishpur se Babu Kunwar Singh pramukh neta the.",
                "Immediate cause: Enfield Rifle ke greased cartridges."
            ),
            easyExplanation = "1857 ka revolt sirf ek military mutiny nahi tha—ismein kisan, zamindar aur aam janata ka British exploitation ke khilaf gussa ek saath phoot pada.",
            definitions = listOf(
                DefinitionItem("Doctrine of Lapse", "Lord Dalhousie ki neeti jiske tahat Satara, Jhansi aur Nagpur par British kabza hua."),
                DefinitionItem("Veer Kunwar Singh", "Jagdishpur ke zamindar jinhone Arrah aur Azamgarh mein angrezon ko haraya.")
            ),
            importantFacts = listOf(
                "1858 ke Government of India Act ke baad East India Company ka shasan samapt hua.",
                "Lord Canning Bharat ke aakhiri Governor-General aur pehle Viceroy bane."
            ),
            comparisonTable = TableData(
                title = "1857 Revolt — Major Centres & Leaders",
                headers = listOf("Centre", "Indian Leader", "British Suppressor"),
                rows = listOf(
                    listOf("Delhi", "Bahadur Shah II & Bakht Khan", "John Nicholson"),
                    listOf("Kanpur", "Nana Saheb & Tatya Tope", "Colin Campbell"),
                    listOf("Jhansi", "Rani Lakshmibai", "Hugh Rose"),
                    listOf("Bihar (Jagdishpur)", "Veer Kunwar Singh", "William Taylor & Vincent Eyre")
                )
            ),
            flowchartSteps = listOf(
                "Political, Economic & Military causes build up",
                "March 1857: Mangal Pandey (Barrackpore) protest",
                "May 1857: Meerut uprising spreads across North India & Bihar",
                "1858 Act: Crown takes direct governance of India"
            ),
            examples = listOf(
                "BPSC Mains Repeated Q: 'Evaluate the role of Kunwar Singh in the Revolt of 1857 in Bihar.'"
            ),
            examOrientedPoints = listOf(
                "BPSC Prelims mein har baar Kunwar Singh aur Pir Ali se 1–2 direct MCQs aate hain.",
                "Mains answer mein Bihar ka map outline bana kar Jagdishpur, Patna, Danapur mark karein."
            ),
            quickRevision = listOf(
                "Start: 10 May 1857 (Meerut) | Bihar Hero: Veer Kunwar Singh",
                "Result: Government of India Act 1858 & Queen Victoria's Proclamation."
            ),
            isSamplePreview = false
        ).toEntity(
            category = NoteCategory.IMPORTANT_QUESTIONS,
            isFavorite = false,
            isPublishedByAdmin = true
        ).copy(createdAt = now - 3600_000L * 3, lastOpenedAt = now - 1200_000L)

        return listOf(polityNote, photosynthesisNote, pyqNote, importantQNote)
    }

    fun defaultQuizAttempts(): List<QuizAttemptEntity> {
        val now = System.currentTimeMillis()
        val day = 86_400_000L
        return listOf(
            QuizAttemptEntity(
                title = "History — Revolt of 1857",
                exam = "BPSC",
                subject = "History",
                totalQuestions = 10,
                correctAnswers = 6,
                isDailyGk = false,
                completedAt = now - day * 5
            ),
            QuizAttemptEntity(
                title = "Daily GK Quiz",
                exam = "UPSC / SSC",
                subject = "General Knowledge",
                totalQuestions = 10,
                correctAnswers = 7,
                isDailyGk = true,
                completedAt = now - day * 4
            ),
            QuizAttemptEntity(
                title = "Life Processes (Class 10th)",
                exam = "CBSE Board",
                subject = "Biology",
                totalQuestions = 20,
                correctAnswers = 15,
                isDailyGk = false,
                completedAt = now - day * 3
            ),
            QuizAttemptEntity(
                title = "General Science PYQ Drill",
                exam = "SSC CGL / CHSL",
                subject = "General Science",
                totalQuestions = 20,
                correctAnswers = 16,
                isDailyGk = false,
                completedAt = now - day * 2
            ),
            QuizAttemptEntity(
                title = "Daily GK Quiz",
                exam = "UPSC / SSC",
                subject = "General Knowledge",
                totalQuestions = 10,
                correctAnswers = 8,
                isDailyGk = true,
                completedAt = now - day * 1
            ),
            QuizAttemptEntity(
                title = "Indian Polity — Fundamental Rights",
                exam = "UPSC CSE",
                subject = "Indian Polity",
                totalQuestions = 20,
                correctAnswers = 18,
                isDailyGk = false,
                completedAt = now - 3600_000L * 2
            )
        )
    }

    fun defaultFlashcardDecks(): List<Pair<FlashcardDeckEntity, List<FlashcardEntity>>> {
        val now = System.currentTimeMillis()
        val scienceDeck = FlashcardDeckEntity(
            title = "Class 10 Science — Life Processes",
            subject = "Biology",
            examTag = "CBSE Board",
            description = "High-yield NCERT definitions & active recall terms for nutrition, respiration, transport, and excretion.",
            createdAt = now - 86_400_000L * 2
        )
        val scienceCards = listOf(
            FlashcardEntity(
                deckId = 0L,
                term = "Photosynthesis",
                definition = "Autotrophic process by which green plants convert CO₂ and H₂O into glucose (C₆H₁₂O₆) and O₂ using sunlight trapped by chlorophyll inside chloroplasts.",
                hint = "Occurs in chloroplast; releases oxygen",
                isMastered = true,
                reviewCount = 2,
                correctCount = 2
            ),
            FlashcardEntity(
                deckId = 0L,
                term = "Stomata",
                definition = "Microscopic pores on leaf surfaces surrounded by bean-shaped guard cells that regulate gas exchange (CO₂ / O₂) and transpiration.",
                hint = "Controlled by swelling & shrinking of guard cells",
                isMastered = true,
                reviewCount = 2,
                correctCount = 2
            ),
            FlashcardEntity(
                deckId = 0L,
                term = "Peristalsis",
                definition = "Rhythmic wave-like contraction and relaxation of smooth muscles along the alimentary canal that pushes food forward.",
                hint = "Movement in oesophagus and digestive tract",
                isMastered = false,
                reviewCount = 1,
                correctCount = 0
            ),
            FlashcardEntity(
                deckId = 0L,
                term = "Double Circulation",
                definition = "Circulation in which blood passes through the human heart twice during one complete cycle — pulmonary circulation (to lungs) and systemic circulation (to body).",
                hint = "Separates oxygenated and deoxygenated blood",
                isMastered = false,
                reviewCount = 0,
                correctCount = 0
            ),
            FlashcardEntity(
                deckId = 0L,
                term = "Nephron",
                definition = "Structural and functional filtration unit of the kidney consisting of Bowman's capsule, glomerulus, and renal tubule that forms urine.",
                hint = "Each kidney has about one million units",
                isMastered = false,
                reviewCount = 0,
                correctCount = 0
            )
        )

        val polityDeck = FlashcardDeckEntity(
            title = "Indian Polity — Key Constitutional Articles",
            subject = "Indian Polity",
            examTag = "UPSC / SSC / BPSC",
            description = "Quick recall deck for must-know Articles of the Constitution of India.",
            createdAt = now - 86_400_000L
        )
        val polityCards = listOf(
            FlashcardEntity(
                deckId = 0L,
                term = "Article 14",
                definition = "Equality before law and equal protection of the laws within the territory of India (Right to Equality).",
                hint = "Part III — First article of Right to Equality",
                isMastered = true,
                reviewCount = 1,
                correctCount = 1
            ),
            FlashcardEntity(
                deckId = 0L,
                term = "Article 17",
                definition = "Abolition of Untouchability and prohibition of its practice in any form, punishable by law.",
                hint = "Absolute fundamental right",
                isMastered = false,
                reviewCount = 0,
                correctCount = 0
            ),
            FlashcardEntity(
                deckId = 0L,
                term = "Article 21",
                definition = "Protection of Life and Personal Liberty — no person shall be deprived of life or personal liberty except according to procedure established by law.",
                hint = "Expanded in Maneka Gandhi case (1978)",
                isMastered = false,
                reviewCount = 0,
                correctCount = 0
            ),
            FlashcardEntity(
                deckId = 0L,
                term = "Article 32",
                definition = "Right to Constitutional Remedies — empowers citizens to move the Supreme Court directly for enforcement of Fundamental Rights via 5 Writs.",
                hint = "Called 'Heart and Soul of the Constitution' by Dr. B.R. Ambedkar",
                isMastered = false,
                reviewCount = 0,
                correctCount = 0
            )
        )

        return listOf(scienceDeck to scienceCards, polityDeck to polityCards)
    }
}
