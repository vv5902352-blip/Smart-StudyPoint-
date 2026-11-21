package com.example.data.ai

import com.example.data.model.ChapterIntro
import com.example.data.model.ChapterQnARequest
import com.example.data.model.ChapterStudyPack
import com.example.data.model.DefinitionItem
import com.example.data.model.DiagramKind
import com.example.data.model.DiagramQuestionItem
import com.example.data.model.MatchPair
import com.example.data.model.MindMapBranch
import com.example.data.model.MindMapData
import com.example.data.model.ObjectiveKind
import com.example.data.model.ObjectiveQuestionItem
import com.example.data.model.QuickRevisionQA
import com.example.data.model.StudyLanguage
import com.example.data.model.SubjectiveCategory
import com.example.data.model.SubjectiveQuestionItem
import com.example.data.model.TableData

/**
 * Generates structured, chapter-aligned Objective and Subjective Q&A study packs
 * organized into all 8 sections for Classes 6–12 in English and Hindi.
 */
object ChapterPackGenerator {

    fun buildPack(request: ChapterQnARequest): ChapterStudyPack {
        val chapter = request.chapterName.trim()
        val isHindi = request.language == StudyLanguage.HINDI
        val isLifeProcesses = chapter.contains("life process", ignoreCase = true) ||
            chapter.contains("photosynthesis", ignoreCase = true) ||
            chapter.contains("जैव प्रक्रम", ignoreCase = true)
        val isElectricity = chapter.contains("electric", ignoreCase = true) ||
            chapter.contains("ohm", ignoreCase = true) ||
            chapter.contains("विद्युत", ignoreCase = true)

        return when {
            isLifeProcesses && isHindi -> buildLifeProcessesHindi(request)
            isLifeProcesses -> buildLifeProcessesEnglish(request)
            isElectricity && !isHindi -> buildElectricityEnglish(request)
            isHindi -> buildGeneralHindiPack(request, chapter)
            else -> buildGeneralEnglishPack(request, chapter)
        }
    }

    fun generateMoreObjectives(
        existingPack: ChapterStudyPack,
        additionalCount: Int
    ): List<ObjectiveQuestionItem> {
        val startNum = (existingPack.objectiveQuestions.maxOfOrNull { it.number } ?: 0) + 1
        val count = additionalCount.coerceIn(5, 50)
        val req = existingPack.request
        val chapter = req.chapterName.trim()
        val isHindi = req.language == StudyLanguage.HINDI
        return buildScaledObjectives(
            chapter = chapter,
            subject = req.subject,
            classLevel = req.classLevel,
            board = req.boardExam,
            isHindi = isHindi,
            baseBank = if (isHindi) generalHindiObjectives(chapter, req.subject) else generalEnglishObjectives(chapter, req.subject),
            requestedCount = count,
            startNumber = startNum
        )
    }

    // -------------------------------------------------------------------------
    // CLASS 10 SCIENCE FEATURED CHAPTER: LIFE PROCESSES (ENGLISH)
    // -------------------------------------------------------------------------
    private fun buildLifeProcessesEnglish(req: ChapterQnARequest): ChapterStudyPack {
        val baseObjectives = lifeProcessesEnglishObjectives()
        val objectives = buildScaledObjectives(
            chapter = req.chapterName,
            subject = req.subject,
            classLevel = req.classLevel,
            board = req.boardExam,
            isHindi = false,
            baseBank = baseObjectives,
            requestedCount = req.objectiveCount,
            startNumber = 1
        )

        val shortAnswers = listOf(
            SubjectiveQuestionItem(
                number = 1,
                category = SubjectiveCategory.VERY_SHORT,
                marks = 1,
                question = "Name the green pigment present in plant leaves that traps solar energy for photosynthesis.",
                answerHeading = "Photosynthetic Pigment",
                answerParagraph = "Chlorophyll is the green pigment located inside chloroplasts of plant leaves that absorbs sunlight for photosynthesis.",
                bulletPoints = listOf("Present inside thylakoid membranes of chloroplasts.", "Essential for converting light energy into chemical energy."),
                keywordsToHighlight = listOf("Chlorophyll", "chloroplasts", "sunlight", "photosynthesis"),
                definitionBox = DefinitionItem("Chlorophyll", "The green light-absorbing pigment found in plant chloroplasts that drives photosynthesis.")
            ),
            SubjectiveQuestionItem(
                number = 2,
                category = SubjectiveCategory.VERY_SHORT,
                marks = 1,
                question = "Which enzyme present in human saliva begins the digestion of starch in the mouth?",
                answerHeading = "Salivary Enzyme",
                answerParagraph = "Salivary amylase (ptyalin) breaks down complex starch molecules into simpler sugars (maltose) inside the buccal cavity.",
                bulletPoints = listOf("Secreted by salivary glands.", "Acts optimally at slightly alkaline/neutral pH (~6.8)."),
                keywordsToHighlight = listOf("Salivary amylase", "starch", "maltose", "buccal cavity")
            ),
            SubjectiveQuestionItem(
                number = 3,
                category = SubjectiveCategory.DEFINE_DESCRIBE,
                marks = 2,
                question = "Define peristaltic movement. Why is it essential in the human alimentary canal?",
                answerHeading = "Peristalsis in Alimentary Canal",
                answerParagraph = "Peristaltic movement is the rhythmic wave-like contraction and relaxation of smooth muscles lining the walls of the digestive tract.",
                bulletPoints = listOf(
                    "Pushes food smoothly forward from the oesophagus to the stomach and intestines.",
                    "Ensures food moves in a regulated manner so digestive enzymes can act completely."
                ),
                keywordsToHighlight = listOf("Peristaltic movement", "contraction and relaxation", "oesophagus", "digestive enzymes"),
                definitionBox = DefinitionItem("Peristalsis", "Rhythmic muscular wave contractions along the alimentary canal that propel food forward."),
                examples = listOf("Swallowing a food bolus down the oesophagus even against gravity.")
            ),
            SubjectiveQuestionItem(
                number = 4,
                category = SubjectiveCategory.SHORT,
                marks = 3,
                question = "State three main events that occur during the process of photosynthesis in green plants.",
                answerHeading = "Three Key Events of Photosynthesis",
                answerParagraph = "Photosynthesis converts carbon dioxide and water into glucose using sunlight and chlorophyll through three sequential events:",
                bulletPoints = listOf(
                    "Absorption of light energy by chlorophyll molecules.",
                    "Conversion of light energy into chemical energy and splitting (photolysis) of water into hydrogen and oxygen.",
                    "Reduction of carbon dioxide (CO₂) into carbohydrates (glucose)."
                ),
                keywordsToHighlight = listOf("chlorophyll", "chemical energy", "photolysis", "Reduction of carbon dioxide", "glucose"),
                flowchartSteps = listOf(
                    "Sunlight absorbed by Chlorophyll",
                    "H₂O splits into H₂ + O₂ (Photolysis)",
                    "CO₂ reduced to Glucose (C₆H₁₂O₆)",
                    "Excess glucose stored as Starch"
                ),
                examples = listOf("Equation: 6CO₂ + 12H₂O —(Sunlight / Chlorophyll)→ C₆H₁₂O₆ + 6O₂ + 6H₂O")
            ),
            SubjectiveQuestionItem(
                number = 5,
                category = SubjectiveCategory.DIFFERENCE_BETWEEN,
                marks = 3,
                question = "Write four key differences between Aerobic Respiration and Anaerobic Respiration.",
                answerHeading = "Aerobic vs Anaerobic Respiration",
                answerParagraph = "Respiration breaks down glucose to release ATP energy either in the presence of oxygen (aerobic) or in its absence (anaerobic).",
                bulletPoints = listOf(
                    "Aerobic respiration occurs in mitochondria and releases 38 ATP per glucose molecule.",
                    "Anaerobic respiration occurs in cytoplasm and yields only 2 ATP."
                ),
                keywordsToHighlight = listOf("Aerobic", "Anaerobic", "mitochondria", "38 ATP", "2 ATP", "lactic acid"),
                comparisonTable = TableData(
                    title = "Aerobic vs Anaerobic Respiration",
                    headers = listOf("Basis", "Aerobic Respiration", "Anaerobic Respiration"),
                    rows = listOf(
                        listOf("Oxygen", "Takes place in presence of O₂", "Takes place in absence of O₂"),
                        listOf("Site", "Cytoplasm & Mitochondria", "Cytoplasm only"),
                        listOf("End Products", "CO₂ + H₂O + High Energy", "Ethanol + CO₂ (Yeast) or Lactic Acid (Muscles)"),
                        listOf("Energy Yield", "High (38 ATP)", "Very Low (2 ATP)")
                    )
                )
            ),
            SubjectiveQuestionItem(
                number = 6,
                category = SubjectiveCategory.DIFFERENCE_BETWEEN,
                marks = 3,
                question = "Differentiate between Arteries and Veins on the basis of blood flow, walls, and valves.",
                answerHeading = "Arteries vs Veins",
                answerParagraph = "Arteries carry blood away from the heart under high pressure, whereas veins bring blood back toward the heart under lower pressure.",
                bulletPoints = listOf(
                    "Pulmonary artery is an exception carrying deoxygenated blood.",
                    "Pulmonary vein is an exception carrying oxygenated blood."
                ),
                keywordsToHighlight = listOf("Arteries", "Veins", "high pressure", "valves", "Pulmonary artery"),
                comparisonTable = TableData(
                    title = "Comparison: Arteries vs Veins",
                    headers = listOf("Feature", "Arteries", "Veins"),
                    rows = listOf(
                        listOf("Direction", "Away from Heart to organs", "From organs back to Heart"),
                        listOf("Wall Thickness", "Thick and elastic walls", "Thin and less elastic walls"),
                        listOf("Valves", "Absent (high pressure flow)", "Present (prevent backflow)"),
                        listOf("Blood Type", "Oxygenated (except pulmonary artery)", "Deoxygenated (except pulmonary vein)")
                    )
                )
            )
        )

        val longAnswers = listOf(
            SubjectiveQuestionItem(
                number = 1,
                category = SubjectiveCategory.LONG,
                marks = 5,
                question = "Explain the process of digestion of food in the human alimentary canal with enzymes and organs involved.",
                answerHeading = "Human Digestive System — Step-by-Step",
                answerParagraph = "Human digestion is a stepwise breakdown of complex insoluble food into simple absorbable nutrients across the alimentary canal, assisted by digestive glands.",
                bulletPoints = listOf(
                    "Mouth (Buccal Cavity): Teeth chew food mechanically; salivary amylase converts starch into simple sugar.",
                    "Stomach: Gastric glands secrete HCl (kills germs & creates acidic medium), pepsin (digests proteins), and mucus (protects stomach lining).",
                    "Small Intestine (Complete Digestion): Bile from liver emulsifies fats; pancreatic juice supplies trypsin (proteins) and lipase (fats); intestinal juice finishes digestion into glucose, amino acids, and fatty acids.",
                    "Absorption: Finger-like villi massively increase surface area for absorbing digested nutrients into blood.",
                    "Large Intestine: Absorbs excess water and egests undigested waste through the anus."
                ),
                keywordsToHighlight = listOf("salivary amylase", "HCl", "pepsin", "Bile", "emulsifies fats", "trypsin", "lipase", "villi"),
                definitionBox = DefinitionItem("Emulsification of Fats", "Breaking down large fat globules into tiny droplets by bile salts to increase enzyme action area."),
                flowchartSteps = listOf(
                    "Mouth: Salivary Amylase digests Starch",
                    "Stomach: HCl + Pepsin digests Proteins",
                    "Small Intestine: Bile + Trypsin + Lipase complete digestion",
                    "Villi absorb nutrients into bloodstream",
                    "Large Intestine absorbs water & egests waste"
                ),
                mindMap = MindMapData(
                    centralTopic = "Human Digestion",
                    branches = listOf(
                        MindMapBranch("Mouth", listOf("Teeth chew food", "Salivary Amylase → Starch")),
                        MindMapBranch("Stomach", listOf("HCl acidic pH", "Pepsin → Protein", "Mucus protection")),
                        MindMapBranch("Small Intestine", listOf("Bile emulsifies fat", "Trypsin & Lipase", "Villi absorption")),
                        MindMapBranch("Large Intestine", listOf("Water reabsorption", "Egestion via Anus"))
                    )
                ),
                examples = listOf("Small intestine is longer in herbivores (cellulose digestion) than in carnivores.")
            ),
            SubjectiveQuestionItem(
                number = 2,
                category = SubjectiveCategory.EXPLAIN_DETAIL,
                marks = 5,
                question = "Explain Double Circulation in human beings. Why is separation of oxygenated and deoxygenated blood necessary in mammals and birds?",
                answerHeading = "Double Circulation in Human Heart",
                answerParagraph = "In humans, blood passes through the heart twice during one complete cycle of the body. This mechanism is called double circulation and consists of pulmonary circulation and systemic circulation.",
                bulletPoints = listOf(
                    "Pulmonary Circulation: Right ventricle pumps deoxygenated blood via pulmonary artery to lungs for oxygenation; pulmonary vein returns oxygenated blood to left atrium.",
                    "Systemic Circulation: Left ventricle pumps oxygenated blood via aorta to all body tissues; vena cava returns deoxygenated blood to right atrium.",
                    "Why Separation Matters: Birds and mammals are warm-blooded animals with high energy needs to maintain constant body temperature. Complete four-chamber separation guarantees maximum oxygen delivery for high ATP production."
                ),
                keywordsToHighlight = listOf("double circulation", "Pulmonary Circulation", "Systemic Circulation", "aorta", "warm-blooded", "four-chamber"),
                flowchartSteps = listOf(
                    "Body Organs (Deoxygenated) → Vena Cava → Right Atrium",
                    "Right Ventricle → Pulmonary Artery → Lungs (O₂ added)",
                    "Lungs → Pulmonary Vein → Left Atrium → Left Ventricle",
                    "Left Ventricle → Aorta → Oxygenated blood to Body"
                ),
                mindMap = MindMapData(
                    centralTopic = "Double Circulation",
                    branches = listOf(
                        MindMapBranch("Right Side (Deoxy)", listOf("Right Atrium & Ventricle", "Pumps blood to Lungs")),
                        MindMapBranch("Left Side (Oxy)", listOf("Left Atrium & Ventricle", "Pumps blood via Aorta")),
                        MindMapBranch("Blood Vessels", listOf("Arteries (thick)", "Veins (valves)", "Capillaries (exchange)")),
                        MindMapBranch("Advantage", listOf("No mixing of blood", "High O₂ supply", "Constant body temp"))
                    )
                )
            ),
            SubjectiveQuestionItem(
                number = 3,
                category = SubjectiveCategory.LONG,
                marks = 5,
                question = "Describe the structure and functioning of a Nephron in urine formation inside the human kidney.",
                answerHeading = "Nephron — Functional Unit of Kidney",
                answerParagraph = "Each kidney contains over one million microscopic filtration units called nephrons. A nephron consists of Bowman's capsule enclosing a capillary tuft (glomerulus) followed by a coiled renal tubule.",
                bulletPoints = listOf(
                    "Ultrafiltration: Blood enters glomerulus under high pressure; water, glucose, amino acids, salts, and urea filter into Bowman's capsule.",
                    "Selective Reabsorption: As filtrate flows through the tubule, useful substances like glucose, amino acids, salts, and major water are reabsorbed back into surrounding blood capillaries.",
                    "Tubular Secretion & Collection: Remaining urea, uric acid, and excess water form urine, collected by the collecting duct and carried via ureters to the urinary bladder."
                ),
                keywordsToHighlight = listOf("nephrons", "Bowman's capsule", "glomerulus", "Ultrafiltration", "Selective Reabsorption", "urea"),
                definitionBox = DefinitionItem("Nephron", "The basic structural and functional filtration unit of the human kidney responsible for removing nitrogenous waste."),
                flowchartSteps = listOf(
                    "Renal Artery brings blood to Glomerulus",
                    "Ultrafiltration into Bowman's Capsule",
                    "Selective Reabsorption in Renal Tubule",
                    "Urine passes Collecting Duct → Ureter → Bladder"
                ),
                mindMap = MindMapData(
                    centralTopic = "Excretion & Nephron",
                    branches = listOf(
                        MindMapBranch("Parts", listOf("Glomerulus", "Bowman's Capsule", "Renal Tubule")),
                        MindMapBranch("Step 1: Filter", listOf("High-pressure blood filtration")),
                        MindMapBranch("Step 2: Reabsorb", listOf("Glucose, Amino acids", "Essential water & salts")),
                        MindMapBranch("Step 3: Excrete", listOf("Urea + excess water", "Stored in Bladder"))
                    )
                )
            )
        )

        val diagramQuestions = listOf(
            DiagramQuestionItem(
                number = 1,
                diagramKind = DiagramKind.STOMATA,
                diagramCaption = "Labeled Diagram of Open & Closed Stomatal Pore",
                labels = listOf("Guard Cells", "Stomatal Pore", "Chloroplasts", "Epidermal Cell Wall"),
                question = "Draw a neat labeled diagram of an open stomatal pore and explain how guard cells regulate its opening and closing.",
                answerPoints = listOf(
                    "Stomata are tiny pores on leaf epidermis flanked by two bean-shaped guard cells.",
                    "When water enters guard cells by osmosis, they swell and become turgid, opening the stomatal pore for CO₂ intake.",
                    "When guard cells lose water, they shrink (flaccid), closing the pore to prevent excess transpiration."
                ),
                marks = 3
            ),
            DiagramQuestionItem(
                number = 2,
                diagramKind = DiagramKind.PHOTOSYNTHESIS_LEAF,
                diagramCaption = "Cross-Section of Leaf Showing Photosynthesis Inputs & Outputs",
                labels = listOf("Sunlight", "Upper Cuticle", "Chloroplasts", "CO₂ In / O₂ Out", "Xylem & Phloem"),
                question = "Explain with a labeled leaf diagram how raw materials reach the site of photosynthesis.",
                answerPoints = listOf(
                    "Carbon dioxide enters through stomata on the lower epidermis.",
                    "Water and minerals are transported from roots up to leaves via xylem vessels.",
                    "Chloroplasts inside palisade mesophyll trap sunlight to synthesize glucose, which phloem translocates to all plant parts."
                ),
                marks = 4
            ),
            DiagramQuestionItem(
                number = 3,
                diagramKind = DiagramKind.HUMAN_HEART_FLOW,
                diagramCaption = "Schematic Diagram of Human Double Circulation",
                labels = listOf("Lungs (Gas Exchange)", "Pulmonary Artery", "Pulmonary Vein", "Aorta", "Vena Cava", "Body Tissues"),
                question = "Trace the path of oxygenated and deoxygenated blood in humans using a schematic double circulation diagram.",
                answerPoints = listOf(
                    "Deoxygenated blood travels: Body → Vena Cava → Right Atrium → Right Ventricle → Pulmonary Artery → Lungs.",
                    "Oxygenated blood travels: Lungs → Pulmonary Vein → Left Atrium → Left Ventricle → Aorta → Body Organs."
                ),
                marks = 4
            )
        )

        val importantExamQuestions = listOf(
            SubjectiveQuestionItem(
                number = 1,
                category = SubjectiveCategory.IMPORTANT,
                marks = 3,
                question = "Why do herbivorous animals like cows have a longer small intestine than carnivorous animals like tigers?",
                answerHeading = "Small Intestine Length & Diet",
                answerParagraph = "Herbivores eat grass and plant food rich in cellulose, which takes significantly longer to digest than meat.",
                bulletPoints = listOf(
                    "Longer small intestine gives extra time and surface area for complete cellulose breakdown.",
                    "Meat eaten by carnivores is protein-rich and easier to digest, so a shorter small intestine suffices."
                ),
                keywordsToHighlight = listOf("cellulose", "longer small intestine", "Herbivores", "carnivores")
            ),
            SubjectiveQuestionItem(
                number = 2,
                category = SubjectiveCategory.IMPORTANT,
                marks = 3,
                question = "Why does a sprinter experience muscle cramps after vigorous running?",
                answerHeading = "Anaerobic Respiration & Lactic Acid",
                answerParagraph = "During intense running, oxygen supply to skeletal muscle cells becomes insufficient for aerobic respiration.",
                bulletPoints = listOf(
                    "Muscle cells switch temporarily to anaerobic respiration to keep generating ATP quickly.",
                    "Glucose breaks down into lactic acid (3-carbon molecule) whose sudden accumulation causes painful muscle cramps."
                ),
                keywordsToHighlight = listOf("oxygen supply", "anaerobic respiration", "lactic acid", "muscle cramps")
            ),
            SubjectiveQuestionItem(
                number = 3,
                category = SubjectiveCategory.IMPORTANT,
                marks = 3,
                question = "How are water and food transported differently in plants through Xylem and Phloem?",
                answerHeading = "Transport in Plants (Xylem vs Phloem)",
                answerParagraph = "Plants have two vascular tissues: xylem transports water/minerals upward, while phloem translocates food in both directions.",
                bulletPoints = listOf(
                    "Xylem transport uses root pressure and transpirational pull (physical forces, unidirectional upward).",
                    "Phloem translocation is an active process requiring ATP energy to move sucrose bidirectionally."
                ),
                keywordsToHighlight = listOf("Xylem", "Phloem", "transpirational pull", "ATP", "bidirectionally"),
                comparisonTable = TableData(
                    title = "Xylem vs Phloem",
                    headers = listOf("Property", "Xylem", "Phloem"),
                    rows = listOf(
                        listOf("Material", "Water & Minerals", "Food (Sucrose) & Amino acids"),
                        listOf("Direction", "Unidirectional (Roots → Leaves)", "Bidirectional (Up & Down)"),
                        listOf("Energy Needed", "No ATP (Passive pull)", "Uses ATP (Active translocation)")
                    )
                )
            )
        )

        val quickRevision = listOf(
            QuickRevisionQA(1, "What is the energy currency of the cell?", "ATP (Adenosine Triphosphate)."),
            QuickRevisionQA(2, "Which blood cells help in clotting of blood at injury sites?", "Platelets (Thrombocytes)."),
            QuickRevisionQA(3, "Where does complete digestion of carbohydrates, proteins, and fats occur?", "Small intestine."),
            QuickRevisionQA(4, "What is the normal systolic/diastolic blood pressure of a healthy adult?", "120 / 80 mm Hg."),
            QuickRevisionQA(5, "Which respiratory pigment carries oxygen in human red blood cells?", "Haemoglobin."),
            QuickRevisionQA(6, "In which form is extra carbohydrate stored in plants and in animals?", "Starch in plants; Glycogen in animals."),
            QuickRevisionQA(7, "What prevents the collapse of trachea when there is less air?", "C-shaped rings of cartilage."),
            QuickRevisionQA(8, "Name the functional filtration unit of the human kidney.", "Nephron.")
        )

        return ChapterStudyPack(
            request = req,
            chapterIntro = ChapterIntro(
                overview = "Life Processes (Class 10 Science, Chapter 6) covers the four essential maintenance functions required to sustain living organisms: Nutrition, Respiration, Transportation, and Excretion. It is one of the highest-weightage Biology chapters in CBSE & State Board exams.",
                keyTopics = listOf("Autotrophic & Heterotrophic Nutrition", "Human Digestive System", "Aerobic & Anaerobic Respiration", "Human Heart & Double Circulation", "Xylem & Phloem Transport", "Excretion & Nephron"),
                definitionBox = DefinitionItem(
                    term = "Life Processes",
                    definition = "The basic biological functions performed by living organisms—nutrition, respiration, transportation, and excretion—to maintain and repair their bodies."
                ),
                examWeightageTip = "High Weightage (${req.boardExam}): 8–11 Marks expected — focus on Stomata/Heart/Nephron diagrams, Aerobic vs Anaerobic tables, and digestive enzymes."
            ),
            objectiveQuestions = objectives,
            shortAnswers = shortAnswers,
            longAnswers = longAnswers,
            diagramQuestions = diagramQuestions,
            mostImportantExamQuestions = importantExamQuestions,
            quickRevisionQuestions = quickRevision,
            isSamplePreview = true
        )
    }

    private fun lifeProcessesEnglishObjectives(): List<ObjectiveQuestionItem> = listOf(
        ObjectiveQuestionItem(
            number = 1,
            kind = ObjectiveKind.MCQ,
            question = "Which of the following equations correctly represents photosynthesis in green plants?",
            options = listOf(
                "6CO₂ + 12H₂O → C₆H₁₂O₆ + 6O₂ + 6H₂O (in presence of sunlight & chlorophyll)",
                "C₆H₁₂O₆ + 6O₂ → 6CO₂ + 6H₂O + Energy",
                "6CO₂ + 6O₂ → C₆H₁₂O₆ + 6H₂O",
                "6H₂O + 6O₂ → C₆H₁₂O₆ + 6CO₂"
            ),
            correctOptionIndex = 0,
            correctAnswerText = "(A) 6CO₂ + 12H₂O → C₆H₁₂O₆ + 6O₂ + 6H₂O",
            explanation = "Green plants utilize carbon dioxide and water in the presence of sunlight and chlorophyll to synthesize glucose and release oxygen."
        ),
        ObjectiveQuestionItem(
            number = 2,
            kind = ObjectiveKind.MCQ,
            question = "The breakdown of pyruvate to give carbon dioxide, water, and high energy takes place inside:",
            options = listOf("Cytoplasm", "Mitochondria", "Chloroplast", "Nucleus"),
            correctOptionIndex = 1,
            correctAnswerText = "(B) Mitochondria",
            explanation = "Aerobic breakdown of pyruvate into CO₂, H₂O, and 38 ATP occurs inside mitochondria."
        ),
        ObjectiveQuestionItem(
            number = 3,
            kind = ObjectiveKind.TRUE_FALSE,
            question = "Bile juice secreted by the liver contains powerful fat-digesting enzymes.",
            options = listOf("True", "False"),
            correctOptionIndex = 1,
            correctAnswerText = "False",
            explanation = "Bile contains NO digestive enzymes; instead, bile salts emulsify large fat globules and make acidic stomach food alkaline for pancreatic lipase."
        ),
        ObjectiveQuestionItem(
            number = 4,
            kind = ObjectiveKind.FILL_BLANK,
            question = "The opening and closing of stomatal pores is controlled by the swelling and shrinking of ______.",
            options = listOf("Guard cells", "Red blood cells", "Alveoli", "Nephrons"),
            correctOptionIndex = 0,
            correctAnswerText = "Guard cells",
            explanation = "Water intake makes guard cells turgid (opening the pore), while water loss makes them flaccid (closing the pore)."
        ),
        ObjectiveQuestionItem(
            number = 5,
            kind = ObjectiveKind.ONE_WORD,
            question = "Give one word for the finger-like projections on the inner lining of the small intestine that absorb digested food.",
            options = listOf("Villi", "Cilia", "Alveoli", "Stomata"),
            correctOptionIndex = 0,
            correctAnswerText = "Villi",
            explanation = "Villi richly supplied with blood vessels greatly increase the internal surface area for efficient nutrient absorption."
        ),
        ObjectiveQuestionItem(
            number = 6,
            kind = ObjectiveKind.MATCH_FOLLOWING,
            question = "Match each digestive secretion (Column A) with its primary function (Column B):",
            options = listOf(
                "1-b, 2-c, 3-a, 4-d",
                "1-a, 2-b, 3-c, 4-d",
                "1-c, 2-a, 3-d, 4-b",
                "1-d, 2-c, 3-b, 4-a"
            ),
            matchPairs = listOf(
                MatchPair("1. Salivary Amylase", "a. Emulsifies large fat globules"),
                MatchPair("2. Pepsin (Stomach)", "b. Breaks starch into simple sugar"),
                MatchPair("3. Bile Juice (Liver)", "c. Digests proteins in acidic medium"),
                MatchPair("4. Lipase (Pancreas)", "d. Digests emulsified fats into fatty acids")
            ),
            correctOptionIndex = 0,
            correctAnswerText = "1-b, 2-c, 3-a, 4-d",
            explanation = "Salivary amylase acts on starch, pepsin digests proteins in HCl medium, bile emulsifies fats, and pancreatic lipase digests fats."
        ),
        ObjectiveQuestionItem(
            number = 7,
            kind = ObjectiveKind.MCQ,
            question = "Which chamber of the human heart receives oxygenated blood directly from the lungs?",
            options = listOf("Right Atrium", "Right Ventricle", "Left Atrium", "Left Ventricle"),
            correctOptionIndex = 2,
            correctAnswerText = "(C) Left Atrium",
            explanation = "Pulmonary veins bring oxygen-rich blood from the lungs directly into the thin-walled Left Atrium."
        ),
        ObjectiveQuestionItem(
            number = 8,
            kind = ObjectiveKind.MCQ,
            question = "Xylem in plants is mainly responsible for the transport of:",
            options = listOf("Water and dissolved minerals", "Prepared food (sucrose)", "Amino acids only", "Oxygen gas"),
            correctOptionIndex = 0,
            correctAnswerText = "(A) Water and dissolved minerals",
            explanation = "Xylem vessels and tracheids form a continuous water-conducting network from roots to leaves."
        ),
        ObjectiveQuestionItem(
            number = 9,
            kind = ObjectiveKind.TRUE_FALSE,
            question = "Rings of cartilage are present in the human trachea to ensure that the air passage does not collapse.",
            options = listOf("True", "False"),
            correctOptionIndex = 0,
            correctAnswerText = "True",
            explanation = "C-shaped cartilaginous rings keep the windpipe (trachea) open even when air pressure drops during exhalation."
        ),
        ObjectiveQuestionItem(
            number = 10,
            kind = ObjectiveKind.ONE_WORD,
            question = "Name the blood instrument used to measure human arterial blood pressure.",
            options = listOf("Sphygmomanometer", "Barometer", "Thermometer", "Lactometer"),
            correctOptionIndex = 0,
            correctAnswerText = "Sphygmomanometer",
            explanation = "A sphygmomanometer measures systolic (~120 mm Hg) and diastolic (~80 mm Hg) blood pressure."
        ),
        ObjectiveQuestionItem(
            number = 11,
            kind = ObjectiveKind.MCQ,
            question = "In yeast during anaerobic fermentation, pyruvate is converted into:",
            options = listOf("Lactic acid + Energy", "Ethanol + Carbon dioxide + Energy", "Glucose + Oxygen", "Water + Oxygen only"),
            correctOptionIndex = 1,
            correctAnswerText = "(B) Ethanol + Carbon dioxide + Energy",
            explanation = "Yeast performs alcoholic fermentation in the absence of oxygen, yielding ethanol (2-carbon), CO₂, and 2 ATP."
        ),
        ObjectiveQuestionItem(
            number = 12,
            kind = ObjectiveKind.FILL_BLANK,
            question = "The structural and functional unit of the human excretory kidney is the ______.",
            options = listOf("Neuron", "Nephron", "Alveolus", "Capillary"),
            correctOptionIndex = 1,
            correctAnswerText = "Nephron",
            explanation = "Each kidney contains about one million nephrons that filter nitrogenous waste (urea) from blood."
        ),
        ObjectiveQuestionItem(
            number = 13,
            kind = ObjectiveKind.MCQ,
            question = "Which blood vessel carries deoxygenated blood from the right ventricle to the lungs?",
            options = listOf("Aorta", "Pulmonary vein", "Pulmonary artery", "Vena cava"),
            correctOptionIndex = 2,
            correctAnswerText = "(C) Pulmonary artery",
            explanation = "The pulmonary artery is the only artery that carries deoxygenated blood from the heart to the lungs."
        ),
        ObjectiveQuestionItem(
            number = 14,
            kind = ObjectiveKind.TRUE_FALSE,
            question = "Translocation of soluble food through phloem requires energy in the form of ATP.",
            options = listOf("True", "False"),
            correctOptionIndex = 0,
            correctAnswerText = "True",
            explanation = "Loading sucrose into phloem sieve tubes utilizes ATP, increasing osmotic pressure to move food to growing tissues."
        ),
        ObjectiveQuestionItem(
            number = 15,
            kind = ObjectiveKind.MATCH_FOLLOWING,
            question = "Match each organism/organ (Column A) with its respiratory/excretory structure (Column B):",
            options = listOf(
                "1-c, 2-a, 3-d, 4-b",
                "1-a, 2-b, 3-c, 4-d",
                "1-b, 2-d, 3-a, 4-c",
                "1-d, 2-c, 3-b, 4-a"
            ),
            matchPairs = listOf(
                MatchPair("1. Fish", "a. Alveoli (Lungs)"),
                MatchPair("2. Human Lungs", "b. Stomata & Lenticels"),
                MatchPair("3. Human Kidney", "c. Gills"),
                MatchPair("4. Plant Leaves", "d. Nephron")
            ),
            correctOptionIndex = 0,
            correctAnswerText = "1-c, 2-a, 3-d, 4-b",
            explanation = "Fish breathe dissolved O₂ through gills, humans exchange gases in alveoli, kidneys filter via nephrons, and plants exchange gases through stomata."
        ),
        ObjectiveQuestionItem(
            number = 16,
            kind = ObjectiveKind.ONE_WORD,
            question = "Give one word for the colourless fluid containing less protein than plasma that drains intercellular tissue spaces.",
            options = listOf("Lymph", "Bile", "Serum", "Urine"),
            correctOptionIndex = 0,
            correctAnswerText = "Lymph",
            explanation = "Lymph (tissue fluid) carries digested and absorbed fat from the intestine and drains excess fluid back into the bloodstream."
        ),
        ObjectiveQuestionItem(
            number = 17,
            kind = ObjectiveKind.MCQ,
            question = "Why do ventricles have thicker muscular walls than atria in the human heart?",
            options = listOf(
                "Ventricles have to pump blood under high pressure to lungs and the entire body",
                "Atria pump blood to the brain directly",
                "Ventricles store urine",
                "Atria contain valves that weigh more"
            ),
            correctOptionIndex = 0,
            correctAnswerText = "(A) Ventricles pump blood under high pressure to organs",
            explanation = "Because ventricles push blood across long circulatory distances (especially the left ventricle to the entire body), their muscular walls are thicker."
        ),
        ObjectiveQuestionItem(
            number = 18,
            kind = ObjectiveKind.FILL_BLANK,
            question = "In Amoeba, food is engulfed using temporary finger-like extensions of the cell surface called ______.",
            options = listOf("Pseudopodia", "Flagella", "Tentacles", "Root hairs"),
            correctOptionIndex = 0,
            correctAnswerText = "Pseudopodia",
            explanation = "Amoeba extends pseudopodia around a food particle to form a food vacuole where digestion occurs."
        ),
        ObjectiveQuestionItem(
            number = 19,
            kind = ObjectiveKind.MCQ,
            question = "Which reagent turns blue-black in the presence of starch during a photosynthesis leaf experiment?",
            options = listOf("Lime water", "Iodine solution", "Potassium hydroxide", "Hydrochloric acid"),
            correctOptionIndex = 1,
            correctAnswerText = "(B) Iodine solution",
            explanation = "Iodine solution reacts with starch stored in illuminated chlorophyll-containing regions of a leaf to form a blue-black colour."
        ),
        ObjectiveQuestionItem(
            number = 20,
            kind = ObjectiveKind.TRUE_FALSE,
            question = "Aquatic organisms breathe faster than terrestrial organisms because dissolved oxygen in water is much lower than oxygen in air.",
            options = listOf("True", "False"),
            correctOptionIndex = 0,
            correctAnswerText = "True",
            explanation = "Since dissolved O₂ concentration in water is low compared to atmospheric air (~21%), aquatic animals like fishes have a much faster breathing rate."
        )
    )

    // -------------------------------------------------------------------------
    // CLASS 10 SCIENCE FEATURED CHAPTER: LIFE PROCESSES (HINDI)
    // -------------------------------------------------------------------------
    private fun buildLifeProcessesHindi(req: ChapterQnARequest): ChapterStudyPack {
        val baseBank = listOf(
            ObjectiveQuestionItem(
                number = 1,
                kind = ObjectiveKind.MCQ,
                question = "हरे पौधों में प्रकाश संश्लेषण (Photosynthesis) के लिए कौन-सा वर्णक सूर्य के प्रकाश को अवशोषित करता है?",
                options = listOf("क्लोरोफिल (पर्णहरित)", "हीमोग्लोबिन", "पित्त रस", "इंसुलिन"),
                correctOptionIndex = 0,
                correctAnswerText = "(A) क्लोरोफिल (पर्णहरित)",
                explanation = "पत्तों के क्लोरोप्लास्ट में पाया जाने वाला क्लोरोफिल सूर्य की प्रकाश ऊर्जा को अवशोषित कर रासायनिक ऊर्जा में बदलता है।"
            ),
            ObjectiveQuestionItem(
                number = 2,
                kind = ObjectiveKind.MCQ,
                question = "पायरुवेट का विखंडन होकर कार्बन डाइऑक्साइड, जल तथा ऊर्जा किस कोशिकांग में बनती है?",
                options = listOf("कोशिकाद्रव्य", "माइटोकॉन्ड्रिया", "हरितलवक", "केंद्रक"),
                correctOptionIndex = 1,
                correctAnswerText = "(B) माइटोकॉन्ड्रिया",
                explanation = "वायवीय श्वसन में ऑक्सीजन की उपस्थिति में पायरुवेट का पूर्ण ऑक्सीकरण माइटोकॉन्ड्रिया में होता है जिससे 38 ATP ऊर्जा बनती है।"
            ),
            ObjectiveQuestionItem(
                number = 3,
                kind = ObjectiveKind.TRUE_FALSE,
                question = "यकृत (Liver) द्वारा स्रावित पित्त रस में वसा पाचन का कोई एंजाइम नहीं होता, बल्कि यह वसा का इमल्सीकरण करता है।",
                options = listOf("सत्य (True)", "असत्य (False)"),
                correctOptionIndex = 0,
                correctAnswerText = "सत्य (True)",
                explanation = "पित्त लवण बड़ी वसा गोलिकाओं को छोटी गोलिकाओं में तोड़ते हैं (इमल्सीकरण) ताकि लाइपेज एंजाइम तेजी से कार्य कर सके।"
            ),
            ObjectiveQuestionItem(
                number = 4,
                kind = ObjectiveKind.FILL_BLANK,
                question = "मानव वृक्क (किडनी) की संरचनात्मक एवं क्रियात्मक इकाई को ______ कहते हैं।",
                options = listOf("नेफ्रॉन (वृक्काणु)", "न्यूरॉन", "कूपिका", "रंध्र"),
                correctOptionIndex = 0,
                correctAnswerText = "नेफ्रॉन (वृक्काणु)",
                explanation = "प्रत्येक वृक्क में लगभग 10 लाख नेफ्रॉन होते हैं जो रक्त से यूरिया जैसे नाइट्रोजनी अपशिष्ट को छानते हैं।"
            ),
            ObjectiveQuestionItem(
                number = 5,
                kind = ObjectiveKind.ONE_WORD,
                question = "मानव लार में पाए जाने वाले उस एंजाइम का एक शब्द में नाम लिखें जो स्टार्च को शर्करा में बदलता है।",
                options = listOf("लार एमाइलेज", "पेप्सिन", "ट्रिप्सिन", "लाइपेज"),
                correctOptionIndex = 0,
                correctAnswerText = "लार एमाइलेज (Salivary Amylase)",
                explanation = "मुँह में भोजन चबाते समय लार एमाइलेज जटिल मंड (स्टार्च) को सरल शर्करा में तोड़ना शुरू कर देता है।"
            ),
            ObjectiveQuestionItem(
                number = 6,
                kind = ObjectiveKind.MATCH_FOLLOWING,
                question = "स्तंभ A को स्तंभ B के सही कार्य से सुमेलित कीजिए:",
                options = listOf("1-b, 2-c, 3-a, 4-d", "1-a, 2-b, 3-c, 4-d", "1-c, 2-a, 3-d, 4-b", "1-d, 2-c, 3-b, 4-a"),
                matchPairs = listOf(
                    MatchPair("1. जाइलम (Xylem)", "a. गैसों का आदान-प्रदान"),
                    MatchPair("2. फ्लोएम (Phloem)", "b. जल व खनिजों का वहन"),
                    MatchPair("3. रंध्र (Stomata)", "c. भोजन (सुक्रोज) का स्थानांतरण"),
                    MatchPair("4. प्लेटलेट्स", "d. चोट पर रक्त का थक्का जमाना")
                ),
                correctOptionIndex = 0,
                correctAnswerText = "1-b, 2-c, 3-a, 4-d",
                explanation = "जाइलम जल का वहन करता है, फ्लोएम भोजन का स्थानांतरण करता है, रंध्र गैस विनिमय करते हैं और प्लेटलेट्स रक्त थक्का जमाते हैं।"
            ),
            ObjectiveQuestionItem(
                number = 7,
                kind = ObjectiveKind.MCQ,
                question = "मानव हृदय का कौन-सा कक्ष फेफड़ों से सीधे ऑक्सीजन युक्त रक्त प्राप्त करता है?",
                options = listOf("दायाँ अलिंद", "दायाँ निलय", "बायाँ अलिंद", "बायाँ निलय"),
                correctOptionIndex = 2,
                correctAnswerText = "(C) बायाँ अलिंद (Left Atrium)",
                explanation = "फुफ्फुस शिरा फेफड़ों से शुद्ध ऑक्सीजन युक्त रक्त बाएँ अलिंद में लाती है।"
            ),
            ObjectiveQuestionItem(
                number = 8,
                kind = ObjectiveKind.TRUE_FALSE,
                question = "अवायवीय श्वसन के दौरान दौड़ते समय पेशियों में लैक्टिक अम्ल जमा होने से क्रैम्प (ऐंठन) होती है।",
                options = listOf("सत्य (True)", "असत्य (False)"),
                correctOptionIndex = 0,
                correctAnswerText = "सत्य (True)",
                explanation = "ऑक्सीजन की कमी में पेशी कोशिकाएँ पायरुवेट को 3-कार्बन लैक्टिक अम्ल में बदलती हैं जिससे दर्द व ऐंठन होती है।"
            ),
            ObjectiveQuestionItem(
                number = 9,
                kind = ObjectiveKind.FILL_BLANK,
                question = "क्षुद्रांत्र (छोटी आँत) की भीतरी सतह पर अंगुली जैसी संरचनाएँ ______ कहलाती हैं जो अवशोषण क्षेत्र बढ़ाती हैं।",
                options = listOf("दीर्घरोम (Villi)", "द्वार कोशिकाएँ", "कूपिकाएँ", "धमनियाँ"),
                correctOptionIndex = 0,
                correctAnswerText = "दीर्घरोम (Villi)",
                explanation = "दीर्घरोम में रक्त वाहिकाओं का घना जाल होता है जो पचे भोजन को तेजी से रक्त में अवशोषित करता है।"
            ),
            ObjectiveQuestionItem(
                number = 10,
                kind = ObjectiveKind.ONE_WORD,
                question = "कोशिका की 'ऊर्जा मुद्रा' (Energy Currency) को एक शब्द में क्या कहते हैं?",
                options = listOf("ATP", "DNA", "RNA", "CO₂"),
                correctOptionIndex = 0,
                correctAnswerText = "ATP (एडीनोसिन ट्राइफॉस्फेट)",
                explanation = "श्वसन में मुक्त ऊर्जा ATP अणुओं में संचित रहती है और कोशिकीय कार्यों में प्रयुक्त होती है।"
            )
        )

        val objectives = buildScaledObjectives(
            chapter = req.chapterName,
            subject = req.subject,
            classLevel = req.classLevel,
            board = req.boardExam,
            isHindi = true,
            baseBank = baseBank,
            requestedCount = req.objectiveCount,
            startNumber = 1
        )

        val shortAnswers = listOf(
            SubjectiveQuestionItem(
                number = 1,
                category = SubjectiveCategory.VERY_SHORT,
                marks = 1,
                question = "स्वपोषी पोषण के लिए आवश्यक दो कच्ची सामग्रियों के नाम लिखिए।",
                answerHeading = "प्रकाश संश्लेषण की कच्ची सामग्री",
                answerParagraph = "हरे पौधों को स्वपोषी पोषण (प्रकाश संश्लेषण) के लिए कार्बन डाइऑक्साइड (CO₂) और जल (H₂O) की आवश्यकता होती है।",
                bulletPoints = listOf("CO₂ वायुमंडल से रंध्रों द्वारा मिलती है।", "जल जड़ों द्वारा जाइलम से पत्तियों तक पहुँचता है।"),
                keywordsToHighlight = listOf("कार्बन डाइऑक्साइड", "जल", "रंध्रों", "जाइलम")
            ),
            SubjectiveQuestionItem(
                number = 2,
                category = SubjectiveCategory.SHORT,
                marks = 3,
                question = "हमारे आमाशय (Stomach) में हाइड्रोक्लोरिक अम्ल (HCl) की क्या भूमिका है?",
                answerHeading = "आमाशय में HCl की भूमिका",
                answerParagraph = "आमाशय की जठर ग्रंथियाँ हाइड्रोक्लोरिक अम्ल (HCl) स्रावित करती हैं जो पाचन में दो प्रमुख कार्य करता है:",
                bulletPoints = listOf(
                    "भोजन के साथ आए हानिकारक जीवाणुओं (कीटाणुओं) को नष्ट करता है।",
                    "अम्लीय माध्यम तैयार करता है ताकि पेप्सिन एंजाइम प्रोटीन का पाचन कर सके।"
                ),
                keywordsToHighlight = listOf("हाइड्रोक्लोरिक अम्ल", "जीवाणुओं", "अम्लीय माध्यम", "पेप्सिन"),
                definitionBox = DefinitionItem("श्लेष्मा (Mucus)", "आमाशय की आंतरिक दीवार को HCl के संक्षारण से बचाने वाली सुरक्षात्मक परत।")
            ),
            SubjectiveQuestionItem(
                number = 3,
                category = SubjectiveCategory.DIFFERENCE_BETWEEN,
                marks = 3,
                question = "वायवीय श्वसन तथा अवायवीय श्वसन में मुख्य अंतर स्पष्ट कीजिए।",
                answerHeading = "वायवीय बनाम अवायवीय श्वसन",
                answerParagraph = "ऑक्सीजन की उपस्थिति में होने वाला श्वसन वायवीय तथा अनुपस्थिति में होने वाला अवायवीय श्वसन कहलाता है।",
                bulletPoints = listOf("वायवीय श्वसन में अधिक ऊर्जा (38 ATP) मिलती है।", "अवायवीय श्वसन में केवल 2 ATP ऊर्जा उत्पन्न होती है।"),
                keywordsToHighlight = listOf("वायवीय", "अवायवीय", "38 ATP", "माइटोकॉन्ड्रिया"),
                comparisonTable = TableData(
                    title = "वायवीय व अवायवीय श्वसन में अंतर",
                    headers = listOf("आधार", "वायवीय श्वसन", "अवायवीय श्वसन"),
                    rows = listOf(
                        listOf("ऑक्सीजन", "उपस्थित रहती है", "अनुपस्थित रहती है"),
                        listOf("स्थान", "कोशिकाद्रव्य व माइटोकॉन्ड्रिया", "केवल कोशिकाद्रव्य"),
                        listOf("अंतिम उत्पाद", "CO₂ + जल + अधिक ऊर्जा", "इथेनॉल या लैक्टिक अम्ल"),
                        listOf("ऊर्जा मात्रा", "38 ATP (अधिक)", "2 ATP (कम)")
                    )
                )
            )
        )

        val longAnswers = listOf(
            SubjectiveQuestionItem(
                number = 1,
                category = SubjectiveCategory.LONG,
                marks = 5,
                question = "मनुष्य में दोहरे परिसंचरण (Double Circulation) की व्याख्या कीजिए। यह क्यों आवश्यक है?",
                answerHeading = "मानव हृदय में दोहरा परिसंचरण",
                answerParagraph = "मनुष्य में एक पूर्ण चक्र में रक्त हृदय से दो बार गुजरता है—इसे दोहरा परिसंचरण कहते हैं। इसमें फुफ्फुसीय तथा दैहिक परिसंचरण शामिल हैं।",
                bulletPoints = listOf(
                    "दायाँ अलिंद व निलय अशुद्ध (विऑक्सीजनित) रक्त को फेफड़ों तक पंप करते हैं।",
                    "बायाँ अलिंद व निलय शुद्ध (ऑक्सीजनित) रक्त को महाधमनी (Aorta) द्वारा पूरे शरीर में भेजते हैं।",
                    "महत्व: स्तनधारी व पक्षी उच्च ऊर्जा वाले जीव हैं; शुद्ध व अशुद्ध रक्त अलग रहने से शरीर को भरपूर ऑक्सीजन व स्थिर तापमान मिलता है।"
                ),
                keywordsToHighlight = listOf("दोहरा परिसंचरण", "महाधमनी", "ऑक्सीजनित", "विऑक्सीजनित", "स्थिर तापमान"),
                flowchartSteps = listOf(
                    "शरीर से अशुद्ध रक्त → महाशिरा → दायाँ अलिंद",
                    "दायाँ निलय → फुफ्फुस धमनी → फेफड़े (शुद्धिकरण)",
                    "फेफड़े → फुफ्फुस शिरा → बायाँ अलिंद → बायाँ निलय",
                    "बायाँ निलय → महाधमनी → संपूर्ण शरीर में शुद्ध रक्त"
                ),
                mindMap = MindMapData(
                    centralTopic = "दोहरा परिसंचरण",
                    branches = listOf(
                        MindMapBranch("दायाँ भाग", listOf("अशुद्ध रक्त", "फेफड़ों को पंप")),
                        MindMapBranch("बायाँ भाग", listOf("शुद्ध रक्त", "महाधमनी से शरीर")),
                        MindMapBranch("वाहिकाएँ", listOf("धमनी (मोटी दीवार)", "शिरा (कपाट युक्त)")),
                        MindMapBranch("लाभ", listOf("रक्त नहीं मिलता", "उच्च ATP ऊर्जा"))
                    )
                )
            )
        )

        val diagramQuestions = listOf(
            DiagramQuestionItem(
                number = 1,
                diagramKind = DiagramKind.STOMATA,
                diagramCaption = "खुले एवं बंद रंध्र (Stomata) का नामांकित चित्र",
                labels = listOf("द्वार कोशिकाएँ (Guard Cells)", "रंध्र छिद्र", "हरितलवक", "बाह्यत्वचा"),
                question = "रंध्र (Stomata) का नामांकित चित्र बनाकर इसके खुलने व बंद होने की क्रिया समझाइए।",
                answerPoints = listOf(
                    "जब द्वार कोशिकाओं में जल प्रवेश करता है तो वे फूल जाती हैं और रंध्र खुल जाता है।",
                    "जल बाहर निकलने पर द्वार कोशिकाएँ सिकुड़ जाती हैं और रंध्र बंद हो जाता है।"
                ),
                marks = 3
            ),
            DiagramQuestionItem(
                number = 2,
                diagramKind = DiagramKind.HUMAN_HEART_FLOW,
                diagramCaption = "मानव दोहरे परिसंचरण का आरेखीय चित्र",
                labels = listOf("फेफड़े", "फुफ्फुस धमनी", "फुफ्फुस शिरा", "महाधमनी", "महाशिरा"),
                question = "दोहरे परिसंचरण के आरेख की सहायता से ऑक्सीजनित और विऑक्सीजनित रक्त का मार्ग दर्शाइए।",
                answerPoints = listOf(
                    "हृदय के चार कक्ष शुद्ध और अशुद्ध रक्त को आपस में मिलने से रोकते हैं।",
                    "बाएँ निलय की दीवार सबसे मोटी होती है क्योंकि यह पूरे शरीर में रक्त भेजता है।"
                ),
                marks = 4
            )
        )

        val quickRev = listOf(
            QuickRevisionQA(1, "कोशिका की ऊर्जा मुद्रा क्या है?", "ATP (एडीनोसिन ट्राइफॉस्फेट)"),
            QuickRevisionQA(2, "पौधों में जल का परिवहन कौन करता है?", "जाइलम ऊतक"),
            QuickRevisionQA(3, "मानव में सामान्य रक्तदाब कितना होता है?", "120/80 mm Hg"),
            QuickRevisionQA(4, "रक्त का थक्का जमाने में कौन सहायक है?", "प्लेटलेट्स (बिंबाणु)")
        )

        return ChapterStudyPack(
            request = req,
            chapterIntro = ChapterIntro(
                overview = "जैव प्रक्रम (कक्षा 10 विज्ञान, अध्याय 6) जीवधारियों के शरीर के अनुरक्षण हेतु आवश्यक चार मुख्य प्रक्रियाओं—पोषण, श्वसन, वहन और उत्सर्जन—का विस्तारपूर्वक अध्ययन कराता है।",
                keyTopics = listOf("स्वपोषी एवं विषमपोषी पोषण", "मानव पाचन तंत्र", "वायवीय व अवायवीय श्वसन", "दोहरा परिसंचरण", "जाइलम व फ्लोएम", "नेफ्रॉन द्वारा उत्सर्जन"),
                definitionBox = DefinitionItem("जैव प्रक्रम", "वे सभी सम्मिलित प्रक्रम जो जीवों के शरीर का अनुरक्षण और मरम्मत कार्य करते हैं, जैव प्रक्रम कहलाते हैं।"),
                examWeightageTip = "उच्च वेटेज (${req.boardExam}): 8–11 अंक — रंध्र, हृदय, नेफ्रॉन के चित्र और अंतर सारणी अवश्य याद करें।"
            ),
            objectiveQuestions = objectives,
            shortAnswers = shortAnswers,
            longAnswers = longAnswers,
            diagramQuestions = diagramQuestions,
            mostImportantExamQuestions = shortAnswers.take(2),
            quickRevisionQuestions = quickRev,
            isSamplePreview = true
        )
    }

    // -------------------------------------------------------------------------
    // CLASS 10 SCIENCE FEATURED CHAPTER: ELECTRICITY (ENGLISH)
    // -------------------------------------------------------------------------
    private fun buildElectricityEnglish(req: ChapterQnARequest): ChapterStudyPack {
        val base = listOf(
            ObjectiveQuestionItem(1, ObjectiveKind.MCQ, "What is the SI unit of electric current?", listOf("Ampere (A)", "Volt (V)", "Ohm (Ω)", "Watt (W)"), emptyList(), 0, "(A) Ampere (A)", "Electric current I = Q/t is measured in Amperes (1 A = 1 Coulomb/second)."),
            ObjectiveQuestionItem(2, ObjectiveKind.MCQ, "According to Ohm's Law, at constant temperature V / I equals:", listOf("Power", "Resistance (R)", "Charge", "Time"), emptyList(), 1, "(B) Resistance (R)", "Ohm's Law states V = I × R, so the ratio V/I remains constant as Resistance R."),
            ObjectiveQuestionItem(3, ObjectiveKind.TRUE_FALSE, "An ammeter is always connected in series in an electric circuit.", listOf("True", "False"), emptyList(), 0, "True", "Ammeter has very low resistance and is placed in series so total current passes through it."),
            ObjectiveQuestionItem(4, ObjectiveKind.FILL_BLANK, "A voltmeter is always connected in ______ across the points where potential difference is measured.", listOf("Parallel", "Series", "Diagonal", "Open loop"), emptyList(), 0, "Parallel", "Voltmeter has very high resistance and is wired in parallel across a component."),
            ObjectiveQuestionItem(5, ObjectiveKind.ONE_WORD, "Name the commercial unit of electrical energy equal to 3.6 × 10⁶ Joules.", listOf("Kilowatt-hour (kWh)", "Joule", "Coulomb", "Ohm-meter"), emptyList(), 0, "Kilowatt-hour (kWh)", "1 kWh (1 commercial unit) = 1000 W × 3600 s = 3.6 × 10⁶ J."),
            ObjectiveQuestionItem(6, ObjectiveKind.MATCH_FOLLOWING, "Match the physical quantity with its SI Unit:", listOf("1-b, 2-c, 3-a, 4-d", "1-a, 2-b, 3-c, 4-d", "1-c, 2-a, 3-d, 4-b", "1-d, 2-c, 3-b, 4-a"), listOf(MatchPair("1. Potential Difference", "a. Ohm (Ω)"), MatchPair("2. Electric Power", "b. Volt (V)"), MatchPair("3. Resistance", "c. Watt (W)"), MatchPair("4. Electric Charge", "d. Coulomb (C)")), 0, "1-b, 2-c, 3-a, 4-d", "Standard SI units: Volt (V), Watt (W), Ohm (Ω), and Coulomb (C)."),
            ObjectiveQuestionItem(7, ObjectiveKind.MCQ, "When resistors R₁ and R₂ are connected in parallel, their equivalent resistance is:", listOf("Greater than both R₁ and R₂", "Smaller than the smallest individual resistor", "Equal to R₁ + R₂", "Zero always"), emptyList(), 1, "(B) Smaller than the smallest resistor", "1/Rp = 1/R₁ + 1/R₂ reduces overall circuit resistance."),
            ObjectiveQuestionItem(8, ObjectiveKind.TRUE_FALSE, "Nichrome alloy is used in electric heaters because it has high resistivity and does not oxidize easily at high temperatures.", listOf("True", "False"), emptyList(), 0, "True", "Alloys like Nichrome have high resistivity and high melting points, making them ideal heating elements."),
            ObjectiveQuestionItem(9, ObjectiveKind.FILL_BLANK, "Joule's Law of Heating is expressed mathematically as H = ______.", listOf("I²Rt", "IRt", "V/R", "I/Rt"), emptyList(), 0, "I²Rt", "Heat produced is proportional to square of current (I²), resistance (R), and time (t)."),
            ObjectiveQuestionItem(10, ObjectiveKind.ONE_WORD, "Which metal with a very high melting point (~3380°C) is used as the filament in incandescent electric bulbs?", listOf("Tungsten", "Copper", "Aluminium", "Silver"), emptyList(), 0, "Tungsten", "Tungsten retains strength and emits white light without melting at extreme temperatures.")
        )
        val objectives = buildScaledObjectives(req.chapterName, req.subject, req.classLevel, req.boardExam, false, base, req.objectiveCount, 1)

        return ChapterStudyPack(
            request = req,
            chapterIntro = ChapterIntro(
                overview = "Electricity covers electric current, potential difference, Ohm's Law, series and parallel resistor networks, Joule's Law of Heating, and electric power calculations.",
                keyTopics = listOf("Electric Current & Potential Difference", "Ohm's Law & V-I Graph", "Factors Affecting Resistance", "Series & Parallel Circuits", "Joule's Heating & Electric Power"),
                definitionBox = DefinitionItem("Ohm's Law", "At constant temperature, the electric current flowing through a metallic conductor is directly proportional to the potential difference across its ends (V = IR)."),
                examWeightageTip = "Board Exam Tip (${req.boardExam}): Always write formulas, units, and circuit diagrams for full marks in numericals."
            ),
            objectiveQuestions = objectives,
            shortAnswers = listOf(
                SubjectiveQuestionItem(1, SubjectiveCategory.VERY_SHORT, 1, "Define 1 Volt potential difference.", "Definition of 1 Volt", "Potential difference between two points is 1 Volt if 1 Joule of work is done in moving 1 Coulomb of charge from one point to the other.", listOf("Formula: V = W / Q", "1 V = 1 J / 1 C"), listOf("1 Volt", "1 Joule", "1 Coulomb")),
                SubjectiveQuestionItem(2, SubjectiveCategory.DIFFERENCE_BETWEEN, 3, "State three differences between Series and Parallel combination of resistors.", "Series vs Parallel Circuits", "Household circuits use parallel wiring because every appliance receives full voltage and operates independently.", listOf("Series: Rs = R₁ + R₂", "Parallel: 1/Rp = 1/R₁ + 1/R₂"), listOf("Series", "Parallel", "full voltage"), comparisonTable = TableData("Series vs Parallel Combination", listOf("Feature", "Series", "Parallel"), listOf(listOf("Current", "Same in all resistors", "Divides across branches"), listOf("Voltage", "Divides across resistors", "Same across all branches"), listOf("Failure Impact", "One break stops whole circuit", "Other appliances keep working"))))
            ),
            longAnswers = listOf(
                SubjectiveQuestionItem(1, SubjectiveCategory.LONG, 5, "State and explain Joule's Law of Heating. Mention two practical applications of heating effect of electric current.", "Joule's Law of Heating", "When electric current passes through a high-resistance conductor, electrical energy converts into heat energy: H = I²Rt.", listOf("Heat is directly proportional to square of current (I²).", "Directly proportional to resistance (R) and time (t).", "Applications: Electric iron/toaster (Nichrome element), Electric fuse (low melting point safety wire)."), listOf("H = I²Rt", "Nichrome", "Electric fuse", "resistance"), flowchartSteps = listOf("Current I flows through Resistor R", "Electrons collide with conductor atoms", "Electrical energy converts to Heat H = I²Rt", "Used in Iron, Heater, Bulb & Fuse"), mindMap = MindMapData("Electricity Formulas", listOf(MindMapBranch("Current", listOf("I = Q / t", "Unit: Ampere")), MindMapBranch("Ohm's Law", listOf("V = I × R", "R = ρL / A")), MindMapBranch("Heating", listOf("H = I²Rt", "H = VIt")), MindMapBranch("Power", listOf("P = VI = I²R", "1 kWh = 3.6×10⁶ J")))))
            ),
            diagramQuestions = listOf(
                DiagramQuestionItem(1, DiagramKind.ELECTRIC_CIRCUIT, "Labeled Circuit Diagram for Verifying Ohm's Law", listOf("Battery (+ / -)", "Plug Key", "Ammeter (Series)", "Resistor R", "Voltmeter (Parallel)"), "Draw a labeled circuit diagram to verify Ohm's Law and explain how ammeter and voltmeter are connected.", listOf("Connect battery, key, ammeter, and resistor R in series.", "Connect voltmeter in parallel across the terminals of resistor R.", "Plot V vs I graph — a straight line through the origin verifies V ∝ I."), 4)
            ),
            mostImportantExamQuestions = listOf(
                SubjectiveQuestionItem(1, SubjectiveCategory.IMPORTANT, 3, "Why are domestic electric appliances connected in parallel instead of series?", "Advantages of Parallel Domestic Wiring", "Parallel circuits ensure safe, independent operation of all home appliances at standard 220V supply.", listOf("Each appliance gets equal 220V supply.", "Switching off one bulb does not switch off others.", "Total circuit resistance decreases, allowing required current to each device."), listOf("220V", "independent", "resistance decreases"))
            ),
            quickRevisionQuestions = listOf(
                QuickRevisionQA(1, "Formula for resistivity?", "ρ = R × A / L (SI unit: Ω·m)."),
                QuickRevisionQA(2, "Value of 1 kWh in Joules?", "3.6 × 10⁶ Joules."),
                QuickRevisionQA(3, "How is a fuse wire connected?", "Always in series with the live wire.")
            ),
            isSamplePreview = true
        )
    }

    // -------------------------------------------------------------------------
    // GENERAL CHAPTER PACKS (ANY CLASS 6–12 CHAPTER — ENGLISH & HINDI)
    // -------------------------------------------------------------------------
    private fun buildGeneralEnglishPack(req: ChapterQnARequest, chapter: String): ChapterStudyPack {
        val base = generalEnglishObjectives(chapter, req.subject)
        val objectives = buildScaledObjectives(chapter, req.subject, req.classLevel, req.boardExam, false, base, req.objectiveCount, 1)

        val shortAnswers = listOf(
            SubjectiveQuestionItem(
                number = 1,
                category = SubjectiveCategory.VERY_SHORT,
                marks = 1,
                question = "Define the core concept of $chapter in ${req.classLevel} ${req.subject}.",
                answerHeading = "Core Definition of $chapter",
                answerParagraph = "$chapter is an essential unit in ${req.subject} that explains the governing rules, structure, and real-world applications aligned with the ${req.classLevel} (${req.boardExam}) curriculum.",
                bulletPoints = listOf("Focus on exact NCERT terminology.", "Keep 1-mark answers crisp within 15–25 words."),
                keywordsToHighlight = listOf(chapter, req.subject, "NCERT", "governing rules"),
                definitionBox = DefinitionItem(chapter, "Fundamental syllabus topic in ${req.classLevel} ${req.subject} bridging basic principles with applied problem-solving.")
            ),
            SubjectiveQuestionItem(
                number = 2,
                category = SubjectiveCategory.SHORT,
                marks = 3,
                question = "State three main characteristics or principles studied in $chapter.",
                answerHeading = "Three Key Principles of $chapter",
                answerParagraph = "The study of $chapter rests on three interconnected principles frequently tested in ${req.boardExam} examinations:",
                bulletPoints = listOf(
                    "Foundational Mechanism: Defines how key components interact inside $chapter.",
                    "Systematic Classification: Groups sub-concepts clearly by properties and functions.",
                    "Applied Relevance: Connects textbook theory directly to real-world observations and numerical/case questions."
                ),
                keywordsToHighlight = listOf("Foundational Mechanism", "Systematic Classification", "Applied Relevance", chapter),
                examples = listOf("Cite one textbook example from ${req.classLevel} ${req.subject} when answering 3-mark questions.")
            ),
            SubjectiveQuestionItem(
                number = 3,
                category = SubjectiveCategory.DIFFERENCE_BETWEEN,
                marks = 3,
                question = "Distinguish between the primary theoretical concept and applied exception cases in $chapter.",
                answerHeading = "Comparison Table — $chapter",
                answerParagraph = "Writing difference-based questions in a neat tabular format secures full 3 marks in ${req.boardExam}.",
                bulletPoints = listOf("Always include a 'Basis of Comparison' column.", "Highlight contrasting keywords clearly."),
                keywordsToHighlight = listOf("tabular format", "Basis of Comparison", "contrasting keywords"),
                comparisonTable = TableData(
                    title = "Key Comparison Matrix — $chapter",
                    headers = listOf("Basis", "Core Concept", "Special / Applied Case"),
                    rows = listOf(
                        listOf("Nature", "Standard governing principle", "Conditional or boundary behaviour"),
                        listOf("Exam Type", "Direct 1–3 mark definitions", "Case-based & assertion-reason Qs"),
                        listOf("Key Recall", "Standard formula / statement", "Highlighted exception rules")
                    )
                )
            )
        )

        val longAnswers = listOf(
            SubjectiveQuestionItem(
                number = 1,
                category = SubjectiveCategory.LONG,
                marks = 5,
                question = "Explain $chapter in detail with its step-by-step process, examples, and exam-oriented summary.",
                answerHeading = "Complete Analysis of $chapter",
                answerParagraph = "$chapter is a high-scoring chapter in ${req.classLevel} ${req.subject}. A complete 5-mark answer should begin with a clear definition, followed by a stepwise mechanism, flowchart, and practical examples.",
                bulletPoints = listOf(
                    "Introduction: Define $chapter clearly in 2 crisp lines.",
                    "Core Mechanism: Break the concept into logical cause-and-effect stages.",
                    "Key Examples: Add 2 real-world or NCERT illustrations.",
                    "Conclusion: Summarize the broader significance of $chapter in ${req.subject}."
                ),
                keywordsToHighlight = listOf(chapter, "cause-and-effect", "flowchart", "NCERT"),
                flowchartSteps = listOf(
                    "Identify core definition & scope of $chapter",
                    "Examine primary components & rules",
                    "Trace step-by-step process / mechanism",
                    "Connect practical examples & exam applications"
                ),
                mindMap = MindMapData(
                    centralTopic = chapter.take(24),
                    branches = listOf(
                        MindMapBranch("Definitions", listOf("Key terms", "Standard formulas")),
                        MindMapBranch("Process", listOf("Stepwise mechanism", "Flowchart order")),
                        MindMapBranch("Differences", listOf("Comparison table", "Key exceptions")),
                        MindMapBranch("Exam Prep", listOf("1–5 Mark answers", "PYQ patterns"))
                    )
                )
            )
        )

        val diagrams = listOf(
            DiagramQuestionItem(
                number = 1,
                diagramKind = DiagramKind.CONCEPT_PROCESS,
                diagramCaption = "Labeled Concept Flow Diagram — $chapter",
                labels = listOf("Input / Cause", "Core Mechanism", "Controlling Factor", "Final Outcome"),
                question = "Draw a neat labeled concept diagram illustrating the workflow of $chapter and explain its stages.",
                answerPoints = listOf(
                    "Stage 1 establishes the initial condition or raw input in $chapter.",
                    "Stage 2 demonstrates the core governing transformation.",
                    "Stage 3 shows the measurable output and real-world application."
                ),
                marks = 4
            )
        )

        return ChapterStudyPack(
            request = req,
            chapterIntro = ChapterIntro(
                overview = "$chapter (${req.classLevel} ${req.subject}) is organized here into all 8 exam-focused sections tailored for ${req.boardExam}, combining objective drills with notebook-style subjective answers.",
                keyTopics = listOf("Core Definitions", "Governing Principles", "Comparison Tables", "Process Flowcharts", "High-Weightage Exam Qs"),
                definitionBox = DefinitionItem(chapter, "Core curriculum chapter in ${req.classLevel} ${req.subject} covering essential theory and exam applications."),
                examWeightageTip = "Preparation Tip (${req.boardExam}): Practice objective sets in Practice Mode first, then revise highlighted keywords in Subjective Notebook view."
            ),
            objectiveQuestions = objectives,
            shortAnswers = shortAnswers,
            longAnswers = longAnswers,
            diagramQuestions = diagrams,
            mostImportantExamQuestions = shortAnswers.take(2),
            quickRevisionQuestions = listOf(
                QuickRevisionQA(1, "What is the best structure for a 5-mark answer on $chapter?", "Definition + Numbered Points + Flowchart/Table + Example."),
                QuickRevisionQA(2, "What is the ideal word limit for a 1-mark Very Short question?", "15–25 crisp words with exact textbook keywords."),
                QuickRevisionQA(3, "How should Difference Between questions be presented?", "Always in a clean 2- or 3-column comparison table."),
                QuickRevisionQA(4, "How do you avoid negative marks or errors in MCQs on $chapter?", "Read every statement carefully and eliminate extreme distractors.")
            ),
            isSamplePreview = true
        )
    }

    private fun buildGeneralHindiPack(req: ChapterQnARequest, chapter: String): ChapterStudyPack {
        val base = generalHindiObjectives(chapter, req.subject)
        val objectives = buildScaledObjectives(chapter, req.subject, req.classLevel, req.boardExam, true, base, req.objectiveCount, 1)

        val shortAnswers = listOf(
            SubjectiveQuestionItem(
                number = 1,
                category = SubjectiveCategory.VERY_SHORT,
                marks = 1,
                question = "${req.classLevel} ${req.subject} में '$chapter' को संक्षेप में परिभाषित कीजिए।",
                answerHeading = "$chapter की मूल परिभाषा",
                answerParagraph = "$chapter, ${req.subject} का एक प्रमुख अध्याय है जो इसके मूल सिद्धांतों, संरचना और व्यावहारिक अनुप्रयोगों को सरल रूप में समझाता है।",
                bulletPoints = listOf("1 अंक के उत्तर 15–25 शब्दों में सटीक लिखें।", "मुख्य तकनीकी शब्दों को रेखांकित करें।"),
                keywordsToHighlight = listOf(chapter, req.subject, "मूल सिद्धांतों"),
                definitionBox = DefinitionItem(chapter, "${req.boardExam} पाठ्यक्रम के अंतर्गत ${req.subject} की महत्वपूर्ण आधारभूत अवधारणा।")
            ),
            SubjectiveQuestionItem(
                number = 2,
                category = SubjectiveCategory.DIFFERENCE_BETWEEN,
                marks = 3,
                question = "$chapter में मूल सिद्धांत तथा व्यावहारिक अनुप्रयोग के बीच मुख्य अंतर तालिका में लिखिए।",
                answerHeading = "तुलनात्मक सारणी — $chapter",
                answerParagraph = "अंतर वाले प्रश्नों को स्वच्छ तालिका के रूप में लिखने पर परीक्षा में पूरे अंक प्राप्त होते हैं।",
                bulletPoints = listOf("कम से कम 3–4 स्पष्ट तुलनात्मक बिंदु लिखें।"),
                keywordsToHighlight = listOf("तालिका", "तुलनात्मक", chapter),
                comparisonTable = TableData(
                    title = "$chapter — तुलनात्मक तालिका",
                    headers = listOf("आधार", "मूल सिद्धांत", "व्यावहारिक अनुप्रयोग"),
                    rows = listOf(
                        listOf("स्वरूप", "परिभाषा व नियम", "दैनिक जीवन व परीक्षा उपयोग"),
                        listOf("प्रश्न प्रकार", "1–2 अंक अति लघु", "3–5 अंक विश्लेषणात्मक")
                    )
                )
            )
        )

        val longAnswers = listOf(
            SubjectiveQuestionItem(
                number = 1,
                category = SubjectiveCategory.LONG,
                marks = 5,
                question = "$chapter की विस्तृत व्याख्या कीजिए तथा इसकी प्रक्रिया को फ्लोचार्ट और उदाहरण सहित समझाइए।",
                answerHeading = "$chapter की विस्तृत व्याख्या",
                answerParagraph = "${req.classLevel} (${req.boardExam}) में $chapter से दीर्घ उत्तरीय प्रश्न नियमित रूप से पूछे जाते हैं। उत्तर को भूमिका, मुख्य बिंदुओं, फ्लोचार्ट और निष्कर्ष में लिखें।",
                bulletPoints = listOf(
                    "परिचय: सटीक परिभाषा से उत्तर प्रारंभ करें।",
                    "क्रमबद्ध चरण: कारण से परिणाम तक स्पष्ट प्रक्रिया दर्शाएँ।",
                    "उदाहरण: NCERT पाठ्यपुस्तक से 1–2 प्रामाणिक उदाहरण जोड़ें।"
                ),
                keywordsToHighlight = listOf(chapter, "फ्लोचार्ट", "NCERT", "निष्कर्ष"),
                flowchartSteps = listOf("परिभाषा और परिचय", "मुख्य घटक व नियम", "क्रमबद्ध कार्यप्रणाली", "उदाहरण एवं निष्कर्ष"),
                mindMap = MindMapData(
                    centralTopic = chapter.take(22),
                    branches = listOf(
                        MindMapBranch("परिभाषा", listOf("सटीक शब्दावली", "मुख्य नियम")),
                        MindMapBranch("प्रक्रिया", listOf("चरणबद्ध प्रवाह", "फ्लोचार्ट")),
                        MindMapBranch("अंतर", listOf("तालिका रूप", "अपवाद बिंदु")),
                        MindMapBranch("परीक्षा टिप्स", listOf("शब्द सीमा", "मुख्य शब्द हाइलाइट"))
                    )
                )
            )
        )

        return ChapterStudyPack(
            request = req,
            chapterIntro = ChapterIntro(
                overview = "$chapter (${req.classLevel} ${req.subject}) को ${req.boardExam} के लिए 8 सुव्यवस्थित खंडों—वस्तुनिष्ठ प्रश्न, उत्तर कुंजी, लघु व दीर्घ उत्तर, चित्र एवं त्वरित रिवीजन—में तैयार किया गया है।",
                keyTopics = listOf("मुख्य परिभाषाएँ", "वस्तुनिष्ठ अभ्यास", "तुलनात्मक सारणी", "फ्लोचार्ट", "महत्वपूर्ण परीक्षा प्रश्न"),
                definitionBox = DefinitionItem(chapter, "${req.classLevel} ${req.subject} का महत्वपूर्ण परीक्षा अध्याय।"),
                examWeightageTip = "परीक्षा टिप (${req.boardExam}): पहले Practice Mode में अभ्यास करें, फिर Study Mode से व्याख्या समझें।"
            ),
            objectiveQuestions = objectives,
            shortAnswers = shortAnswers,
            longAnswers = longAnswers,
            diagramQuestions = listOf(
                DiagramQuestionItem(1, DiagramKind.CONCEPT_PROCESS, "$chapter का नामांकित आरेख", listOf("प्रारंभिक कारक", "मुख्य प्रक्रिया", "नियंत्रक तत्व", "अंतिम परिणाम"), "$chapter की मुख्य प्रक्रिया को नामांकित आरेख द्वारा स्पष्ट कीजिए।", listOf("प्रथम चरण मूल कारण दर्शाता है।", "द्वितीय चरण मुख्य परिवर्तन स्पष्ट करता है।", "अंतिम चरण व्यावहारिक परिणाम दिखाता है।"), 4)
            ),
            mostImportantExamQuestions = shortAnswers,
            quickRevisionQuestions = listOf(
                QuickRevisionQA(1, "5 अंक के दीर्घ उत्तर की आदर्श संरचना क्या है?", "परिभाषा + बिंदुवार व्याख्या + फ्लोचार्ट/चित्र + उदाहरण।"),
                QuickRevisionQA(2, "1 अंक के प्रश्न की शब्द सीमा कितनी रखें?", "15–25 सटीक शब्द।")
            ),
            isSamplePreview = true
        )
    }

    private fun generalEnglishObjectives(chapter: String, subject: String): List<ObjectiveQuestionItem> = listOf(
        ObjectiveQuestionItem(1, ObjectiveKind.MCQ, "Which approach best builds complete mastery over $chapter in $subject?", listOf("Linking definitions, diagrams, and textbook examples", "Ignoring core definitions", "Skipping revision", "Guessing answers blindly"), emptyList(), 0, "(A) Linking definitions, diagrams, and examples", "Structured conceptual linking ensures high accuracy in both objective and descriptive papers."),
        ObjectiveQuestionItem(2, ObjectiveKind.TRUE_FALSE, "Writing Difference Between answers in a clear tabular layout improves board exam readability and marks.", listOf("True", "False"), emptyList(), 0, "True", "Examiners award quick, full credit when differences are clearly contrasted in columns."),
        ObjectiveQuestionItem(3, ObjectiveKind.FILL_BLANK, "Process-based long answers in $chapter become clearer when accompanied by a step-by-step ______.", listOf("Flowchart", "Blank margin", "Random list", "Single word"), emptyList(), 0, "Flowchart", "A labeled flowchart visually proves cause-and-effect understanding."),
        ObjectiveQuestionItem(4, ObjectiveKind.ONE_WORD, "Name the active recall method where answers remain hidden until you attempt the question.", listOf("Practice Mode", "Passive Reading", "Copying", "Skipping"), emptyList(), 0, "Practice Mode", "Testing yourself before viewing the answer key strengthens long-term memory."),
        ObjectiveQuestionItem(5, ObjectiveKind.MATCH_FOLLOWING, "Match the exam question format with its ideal presentation style for $chapter:", listOf("1-b, 2-c, 3-a, 4-d", "1-a, 2-b, 3-c, 4-d", "1-c, 2-a, 3-d, 4-b", "1-d, 2-c, 3-b, 4-a"), listOf(MatchPair("1. Very Short (1M)", "a. Table with Basis column"), MatchPair("2. Long Answer (5M)", "b. 1–2 crisp lines"), MatchPair("3. Difference Between", "c. Headings + Flowchart + Example"), MatchPair("4. Diagram Question", "d. Neat labeled sketch")), 0, "1-b, 2-c, 3-a, 4-d", "Matching answer layout to marking scheme maximizes exam scores.")
    )

    private fun generalHindiObjectives(chapter: String, subject: String): List<ObjectiveQuestionItem> = listOf(
        ObjectiveQuestionItem(1, ObjectiveKind.MCQ, "$subject में '$chapter' की मजबूत तैयारी के लिए सबसे प्रभावी विधि कौन-सी है?", listOf("परिभाषा, चित्र, फ्लोचार्ट और अभ्यास प्रश्नों का समन्वय", "बिना समझे रटना", "रिवीजन न करना", "उदाहरण छोड़ देना"), emptyList(), 0, "(A) परिभाषा, चित्र, फ्लोचार्ट और अभ्यास", "अवधारणा की स्पष्ट समझ से वस्तुनिष्ठ व वर्णनात्मक दोनों प्रश्नों में उच्च अंक आते हैं।"),
        ObjectiveQuestionItem(2, ObjectiveKind.TRUE_FALSE, "अंतर वाले प्रश्नों को तालिका (Table) में लिखना परीक्षा में अधिक प्रभावशाली होता है।", listOf("सत्य (True)", "असत्य (False)"), emptyList(), 0, "सत्य (True)", "तालिका से दोनों पक्षों के मुख्य बिंदु तुरंत स्पष्ट हो जाते हैं।"),
        ObjectiveQuestionItem(3, ObjectiveKind.FILL_BLANK, "$chapter के प्रक्रिया-आधारित दीर्घ उत्तरों में चरणों को दर्शाने हेतु ______ बनाना उपयोगी है।", listOf("फ्लोचार्ट", "खाली पृष्ठ", "अव्यवस्थित लेख", "केवल शीर्षक"), emptyList(), 0, "फ्लोचार्ट", "फ्लोचार्ट कारण-प्रभाव क्रम को स्पष्ट करता है।"),
        ObjectiveQuestionItem(4, ObjectiveKind.ONE_WORD, "प्रश्नों को स्वयं हल करने के बाद उत्तर जांचने की अभ्यास विधि को क्या कहते हैं?", listOf("Practice Mode", "रटना", "छोड़ना", "केवल पढ़ना"), emptyList(), 0, "Practice Mode", "स्वयं परीक्षण से स्मरण शक्ति और सटीकता दोनों बढ़ती हैं।"),
        ObjectiveQuestionItem(5, ObjectiveKind.MATCH_FOLLOWING, "प्रश्न के प्रकार को उपयुक्त उत्तर प्रारूप से सुमेलित कीजिए:", listOf("1-b, 2-c, 3-a, 4-d", "1-a, 2-b, 3-c, 4-d", "1-c, 2-a, 3-d, 4-b", "1-d, 2-c, 3-b, 4-a"), listOf(MatchPair("1. अति लघु (1 अंक)", "a. तुलनात्मक तालिका"), MatchPair("2. दीर्घ उत्तर (5 अंक)", "b. 1–2 सटीक पंक्तियाँ"), MatchPair("3. अंतर बताइए", "c. शीर्षक + फ्लोचार्ट + उदाहरण"), MatchPair("4. चित्र आधारित", "d. स्वच्छ नामांकित चित्र")), 0, "1-b, 2-c, 3-a, 4-d", "अंकों के अनुसार उत्तर की संरचना परीक्षा में श्रेष्ठ परिणाम देती है।")
    )

    /**
     * Scales the objective bank smoothly to 10, 20, 30, 50, or 100 unique numbered questions
     * covering all 5 objective formats (MCQ, True/False, Fill in the Blanks, Match, One Word)
     * with zero duplicate question texts.
     */
    private fun buildScaledObjectives(
        chapter: String,
        subject: String,
        classLevel: String,
        board: String,
        isHindi: Boolean,
        baseBank: List<ObjectiveQuestionItem>,
        requestedCount: Int,
        startNumber: Int
    ): List<ObjectiveQuestionItem> {
        val count = requestedCount.coerceIn(5, 100)
        val result = mutableListOf<ObjectiveQuestionItem>()
        val usedQuestions = mutableSetOf<String>()

        val conceptAnglesEn = listOf(
            "Core Concept Check", "NCERT Fact Drill", "Application Scenario", "Board Exam Focus",
            "Exception Rule Check", "Process Sequence", "Terminology Check", "Diagram Recall",
            "Formula / Principle", "Quick Revision Drill"
        )
        val conceptAnglesHi = listOf(
            "मुख्य अवधारणा जाँच", "NCERT तथ्य अभ्यास", "व्यावहारिक अनुप्रयोग", "बोर्ड परीक्षा फोकस",
            "अपवाद नियम जाँच", "प्रक्रिया क्रम", "शब्दावली अभ्यास", "चित्र आधारित स्मरण",
            "सिद्धांत परीक्षण", "त्वरित रिवीजन प्रश्न"
        )

        for (i in 0 until count) {
            val number = startNumber + i
            if (i < baseBank.size && startNumber == 1) {
                val item = baseBank[i].copy(number = number)
                usedQuestions.add(item.question)
                result.add(item)
            } else {
                val kind = ObjectiveKind.entries[i % ObjectiveKind.entries.size]
                val angleIdx = (number - 1) % conceptAnglesEn.size
                val angle = if (isHindi) conceptAnglesHi[angleIdx] else conceptAnglesEn[angleIdx]
                val generated = buildDistinctExtraObjective(
                    number = number,
                    kind = kind,
                    angle = angle,
                    chapter = chapter,
                    subject = subject,
                    classLevel = classLevel,
                    board = board,
                    isHindi = isHindi
                )
                val finalItem = if (generated.question in usedQuestions) {
                    generated.copy(question = "${generated.question} (#$number)")
                } else {
                    generated
                }
                usedQuestions.add(finalItem.question)
                result.add(finalItem)
            }
        }
        return result
    }

    private fun buildDistinctExtraObjective(
        number: Int,
        kind: ObjectiveKind,
        angle: String,
        chapter: String,
        subject: String,
        classLevel: String,
        board: String,
        isHindi: Boolean
    ): ObjectiveQuestionItem {
        return when (kind) {
            ObjectiveKind.MCQ -> ObjectiveQuestionItem(
                number = number,
                kind = ObjectiveKind.MCQ,
                question = if (isHindi) {
                    "[$angle #$number] $classLevel $subject के अध्याय '$chapter' ($board) के संदर्भ में कौन-सा कथन सर्वाधिक सटीक है?"
                } else {
                    "[$angle #$number] Regarding '$chapter' in $classLevel $subject ($board), which statement is scientifically/academically accurate?"
                },
                options = if (isHindi) {
                    listOf(
                        "यह अध्याय की मूल अवधारणा, कारण-प्रभाव और NCERT उदाहरणों को स्पष्ट करता है",
                        "इसमें किसी भी नियम या प्रमाण की आवश्यकता नहीं होती",
                        "यह केवल बिना संदर्भ के तथ्यों पर आधारित है",
                        "इसमें तुलनात्मक विश्लेषण वर्जित है"
                    )
                } else {
                    listOf(
                        "It aligns core principles of $chapter with verified NCERT concepts and structured reasoning",
                        "It disregards textbook definitions and experimental observations",
                        "It replaces logical cause-effect links with random assumptions",
                        "It has no relevance to $board examination patterns"
                    )
                },
                correctOptionIndex = 0,
                correctAnswerText = if (isHindi) "(A) NCERT आधारित मूल अवधारणा व कारण-प्रभाव" else "(A) Aligns core principles with verified NCERT reasoning",
                explanation = if (isHindi) {
                    "$board परीक्षा में '$chapter' से पूछे जाने वाले वस्तुनिष्ठ प्रश्नों में NCERT परिभाषा और तार्किक समझ सबसे निर्णायक होती है।"
                } else {
                    "For $board assessments on $chapter, verifying NCERT definitions and logical cause-effect relationships leads directly to the right option."
                }
            )

            ObjectiveKind.TRUE_FALSE -> ObjectiveQuestionItem(
                number = number,
                kind = ObjectiveKind.TRUE_FALSE,
                question = if (isHindi) {
                    "[$angle #$number] '$chapter' के उत्तरों में मुख्य शब्दों को हाइलाइट करना और सटीक उदाहरण देना अंक बढ़ाता है।"
                } else {
                    "[$angle #$number] In $classLevel $subject ($chapter), supporting definitions with labeled diagrams or concise examples improves exam scoring."
                },
                options = if (isHindi) listOf("सत्य (True)", "असत्य (False)") else listOf("True", "False"),
                correctOptionIndex = 0,
                correctAnswerText = if (isHindi) "सत्य (True)" else "True",
                explanation = if (isHindi) {
                    "स्पष्ट उदाहरण और मुख्य शब्दावली परीक्षक को उत्तर की सटीकता तुरंत दिखाती है।"
                } else {
                    "Official $board marking schemes reward accurate technical terms paired with clear textbook examples."
                }
            )

            ObjectiveKind.FILL_BLANK -> ObjectiveQuestionItem(
                number = number,
                kind = ObjectiveKind.FILL_BLANK,
                question = if (isHindi) {
                    "[$angle #$number] '$chapter' के प्रमुख चरणों को क्रमबद्ध दर्शाने के लिए ______ आरेख का उपयोग किया जाता है।"
                } else {
                    "[$angle #$number] To revise sequential processes in '$chapter' rapidly before $board exams, students use a structured ______."
                },
                options = if (isHindi) listOf("फ्लोचार्ट", "अव्यवस्थित सूची", "अनुमान", "खाली पृष्ठ") else listOf("Flowchart", "Guesswork", "Unrelated note", "Blank table"),
                correctOptionIndex = 0,
                correctAnswerText = if (isHindi) "फ्लोचार्ट" else "Flowchart",
                explanation = if (isHindi) {
                    "फ्लोचार्ट प्रक्रिया के प्रत्येक चरण को स्पष्ट क्रम में याद रखने में सहायता करता है।"
                } else {
                    "A structured flowchart visually anchors each sequential stage of $chapter for fast exam recall."
                }
            )

            ObjectiveKind.MATCH_FOLLOWING -> ObjectiveQuestionItem(
                number = number,
                kind = ObjectiveKind.MATCH_FOLLOWING,
                question = if (isHindi) {
                    "[$angle #$number] '$chapter' के अध्ययन तत्वों को उनके सही उद्देश्य से सुमेलित करें:"
                } else {
                    "[$angle #$number] Match each study element of '$chapter' (Column A) with its exam purpose (Column B):"
                },
                options = listOf("1-b, 2-a, 3-d, 4-c", "1-a, 2-b, 3-c, 4-d", "1-c, 2-d, 3-a, 4-b", "1-d, 2-c, 3-b, 4-a"),
                matchPairs = if (isHindi) {
                    listOf(
                        MatchPair("1. परिभाषा बॉक्स", "a. 3 अंक अंतर प्रश्न"),
                        MatchPair("2. तुलना सारणी", "b. 1–2 अंक सटीक अर्थ"),
                        MatchPair("3. माइंड मैप", "c. चित्र-आधारित प्रश्न"),
                        MatchPair("4. नामांकित आरेख", "d. 5 अंक दीर्घ उत्तर रिवीजन")
                    )
                } else {
                    listOf(
                        MatchPair("1. Definition Box", "a. 3-Mark Difference Between Qs"),
                        MatchPair("2. Comparison Table", "b. 1–2 Mark Crisp Recall"),
                        MatchPair("3. Mind Map", "c. Diagram-Based Questions"),
                        MatchPair("4. Labeled Diagram", "d. 5-Mark Long Answer Overview")
                    )
                },
                correctOptionIndex = 0,
                correctAnswerText = "1-b, 2-a, 3-d, 4-c",
                explanation = if (isHindi) {
                    "प्रत्येक दृश्य साधन विभिन्न अंकों के प्रश्नों की तैयारी को आसान और सटीक बनाता है।"
                } else {
                    "Each visual study element targets a specific question weightage in $board exams."
                }
            )

            ObjectiveKind.ONE_WORD -> ObjectiveQuestionItem(
                number = number,
                kind = ObjectiveKind.ONE_WORD,
                question = if (isHindi) {
                    "[$angle #$number] '$chapter' के दीर्घ उत्तर को मुख्य शाखाओं में समेटने वाले दृश्य मानचित्र को क्या कहते हैं?"
                } else {
                    "[$angle #$number] Give one term for the branching visual diagram used to summarize long answers in '$chapter'."
                },
                options = if (isHindi) listOf("माइंड मैप (Mind Map)", "पैराग्राफ", "फुटनोट", "इंडेक्स") else listOf("Mind Map", "Essay", "Footnote", "Glossary"),
                correctOptionIndex = 0,
                correctAnswerText = if (isHindi) "माइंड मैप (Mind Map)" else "Mind Map",
                explanation = if (isHindi) {
                    "माइंड मैप केंद्र में मुख्य विषय और चारों ओर शाखाओं के रूप में उप-बिंदु दर्शाता है।"
                } else {
                    "A Mind Map links the central chapter concept to key sub-topic branches for effortless 5-mark answer recall."
                }
            )
        }
    }
}
