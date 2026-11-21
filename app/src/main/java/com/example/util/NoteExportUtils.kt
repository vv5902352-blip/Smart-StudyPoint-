package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import com.example.R
import com.example.data.model.GeneratedNotes
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object NoteExportUtils {

    fun copyToClipboard(context: Context, label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
    }

    fun sharePlainText(context: Context, subject: String, text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, text)
        }
        val chooser = Intent.createChooser(intent, "Share via Smart StudyPoint").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    /**
     * Exports GeneratedNotes as an authentic Ruled Handwritten Student Notebook PDF
     * using Kalam handwriting typeface, cream notebook paper, blue horizontal rules,
     * double red margin lines, red headings, and royal blue ballpoint ink.
     */
    fun exportNotesAsPdf(context: Context, notes: GeneratedNotes): Result<String> {
        val document = PdfDocument()
        return try {
            val pageWidth = 595 // A4 width
            val pageHeight = 842 // A4 height
            val marginLeft = 64f
            val marginRight = 38f
            val contentWidth = pageWidth - marginLeft - marginRight
            val lineHeight = 20f

            val handwritingTypeface = runCatching {
                ResourcesCompat.getFont(context, R.font.kalam)
            }.getOrNull() ?: Typeface.DEFAULT
            val handwritingBold = Typeface.create(handwritingTypeface, Typeface.BOLD)

            val paperPaint = Paint().apply { color = Color.parseColor("#FFFEFA") }
            val rulePaint = Paint().apply {
                color = Color.parseColor("#DCE7F5")
                strokeWidth = 0.8f
            }
            val redMarginPaint = Paint().apply {
                color = Color.parseColor("#E57373")
                strokeWidth = 1.2f
            }
            val redHeadingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#B91C1C")
                textSize = 14f
                typeface = handwritingBold
            }
            val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#B91C1C")
                textSize = 17f
                typeface = handwritingBold
            }
            val metaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#1E293B")
                textSize = 10.5f
                typeface = handwritingBold
            }
            val blueInkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#1E3A8A")
                textSize = 12f
                typeface = handwritingTypeface
            }
            val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#64748B")
                textSize = 9f
                typeface = handwritingTypeface
            }

            data class StyledPdfLine(val text: String, val isHeading: Boolean)

            val styledLines = mutableListOf<StyledPdfLine>()
            fun addHeading(heading: String) {
                if (styledLines.isNotEmpty()) styledLines.add(StyledPdfLine("", false))
                wrapLines(heading, redHeadingPaint, contentWidth).forEach {
                    styledLines.add(StyledPdfLine(it, true))
                }
            }
            fun addBody(text: String) {
                wrapLines(text, blueInkPaint, contentWidth).forEach {
                    styledLines.add(StyledPdfLine(it, false))
                }
            }

            addHeading("1. Detailed Handwritten Notes")
            addBody(notes.detailedNotes)

            if (notes.easyExplanation.isNotBlank()) {
                addHeading("2. Easy Explanation (Teacher's Tip)")
                addBody(notes.easyExplanation)
            }
            if (notes.definitions.isNotEmpty()) {
                addHeading("3. Important Definitions")
                notes.definitions.forEach { addBody("• ${it.term}: ${it.definition}") }
            }
            if (notes.shortNotes.isNotEmpty()) {
                addHeading("4. Short Notes")
                notes.shortNotes.forEachIndexed { idx, s -> addBody("${idx + 1}. $s") }
            }
            if (notes.importantFacts.isNotEmpty()) {
                addHeading("5. Important Facts")
                notes.importantFacts.forEach { addBody("★ $it") }
            }
            if (notes.comparisonTable.headers.isNotEmpty() && notes.comparisonTable.rows.isNotEmpty()) {
                addHeading("6. ${notes.comparisonTable.title}")
                addBody(notes.comparisonTable.headers.joinToString("  |  "))
                notes.comparisonTable.rows.forEach { row ->
                    addBody(row.joinToString("  |  "))
                }
            }
            if (notes.flowchartSteps.isNotEmpty()) {
                addHeading("7. Step-by-Step Concept Flowchart")
                notes.flowchartSteps.forEachIndexed { i, step -> addBody("${i + 1} -> $step") }
            }
            if (notes.examples.isNotEmpty()) {
                addHeading("8. Examples & Illustrations")
                notes.examples.forEachIndexed { i, ex -> addBody("Ex ${i + 1}: $ex") }
            }
            if (notes.examOrientedPoints.isNotEmpty()) {
                addHeading("9. V.V. IMP Exam Scoring Points")
                notes.examOrientedPoints.forEachIndexed { i, p -> addBody("${i + 1}. $p") }
            }
            if (notes.quickRevision.isNotEmpty()) {
                addHeading("10. Quick Revision Sheet")
                notes.quickRevision.forEach { addBody("✓ $it") }
            }

            val dateText = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
            var lineIndex = 0
            var pageNumber = 1

            while (lineIndex < styledLines.size || pageNumber == 1) {
                val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                val page = document.startPage(pageInfo)
                val canvas = page.canvas

                // Cream paper background
                canvas.drawRect(0f, 0f, pageWidth.toFloat(), pageHeight.toFloat(), paperPaint)

                // Horizontal blue ruled notebook lines
                var ruleY = 64f
                while (ruleY < pageHeight - 44f) {
                    canvas.drawLine(0f, ruleY, pageWidth.toFloat(), ruleY, rulePaint)
                    ruleY += lineHeight
                }

                // Double vertical red margin
                canvas.drawLine(48f, 0f, 48f, pageHeight.toFloat(), redMarginPaint)
                canvas.drawLine(52f, 0f, 52f, pageHeight.toFloat(), redMarginPaint)

                // Top header rule (double red line)
                canvas.drawLine(0f, 44f, pageWidth.toFloat(), 44f, redMarginPaint)
                canvas.drawLine(0f, 47f, pageWidth.toFloat(), 47f, redMarginPaint)

                // Top header metadata
                canvas.drawText("Smart StudyPoint — Handwritten Register", marginLeft, 30f, metaPaint)
                canvas.drawText("Date: $dateText  |  Page ${pageNumber.toString().padStart(2, '0')}", pageWidth - 185f, 30f, metaPaint)

                var y = 64f
                if (pageNumber == 1) {
                    canvas.drawText(notes.title.take(54), marginLeft, y - 3f, titlePaint)
                    y += lineHeight
                    val sub = "${notes.classLevel} • ${notes.exam} • ${notes.subject} • ${notes.language.label}"
                    canvas.drawText(sub.take(76), marginLeft, y - 4f, metaPaint)
                    y += lineHeight * 1.5f
                }

                val bottomLimit = pageHeight - 48f
                while (lineIndex < styledLines.size && y < bottomLimit) {
                    val item = styledLines[lineIndex]
                    if (item.text.isNotEmpty()) {
                        val paint = if (item.isHeading) redHeadingPaint else blueInkPaint
                        canvas.drawText(item.text, marginLeft, y - 4f, paint)
                        if (item.isHeading) {
                            val w = paint.measureText(item.text).coerceAtMost(contentWidth)
                            canvas.drawLine(marginLeft, y - 1f, marginLeft + w, y - 1f, redMarginPaint)
                        }
                    }
                    y += lineHeight
                    lineIndex++
                }

                canvas.drawText(
                    "© Smart StudyPoint — Handwritten AI Study Notes  •  Page $pageNumber",
                    marginLeft,
                    pageHeight - 22f,
                    footerPaint
                )

                document.finishPage(page)
                pageNumber++
                if (lineIndex >= styledLines.size) break
            }

            savePdfDocument(context, document, notes.title)
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            document.close()
        }
    }

    fun exportTextAsPdf(
        context: Context,
        title: String,
        subtitle: String,
        bodyText: String
    ): Result<String> {
        val document = PdfDocument()
        return try {
            val pageWidth = 595 // A4 width in PostScript points
            val pageHeight = 842 // A4 height
            val margin = 42f
            val contentWidth = pageWidth - (margin * 2)

            val headerBgPaint = Paint().apply { color = Color.parseColor("#0F2557") }
            val accentPaint = Paint().apply { color = Color.parseColor("#1D4ED8") }
            val brandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = 16f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val subHeaderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#DBEAFE")
                textSize = 10f
            }
            val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#0F2557")
                textSize = 15f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val metaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#475569")
                textSize = 10f
            }
            val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#0F172A")
                textSize = 11f
            }
            val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#64748B")
                textSize = 9f
            }

            val wrappedLines = wrapLines(bodyText, bodyPaint, contentWidth)
            var lineIndex = 0
            var pageNumber = 1

            while (lineIndex < wrappedLines.size || pageNumber == 1) {
                val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                val page = document.startPage(pageInfo)
                val canvas = page.canvas

                var y = 0f
                if (pageNumber == 1) {
                    canvas.drawRect(0f, 0f, pageWidth.toFloat(), 66f, headerBgPaint)
                    canvas.drawRect(0f, 66f, pageWidth.toFloat(), 70f, accentPaint)
                    canvas.drawText("Smart StudyPoint", margin, 32f, brandPaint)
                    canvas.drawText("Your Smart Partner in Exam Preparation.", margin, 50f, subHeaderPaint)
                    y = 96f
                    canvas.drawText(title.take(65), margin, y, titlePaint)
                    y += 18f
                    canvas.drawText(subtitle.take(85), margin, y, metaPaint)
                    y += 22f
                } else {
                    y = 54f
                }

                val bottomLimit = pageHeight - 54f
                while (lineIndex < wrappedLines.size && y < bottomLimit) {
                    canvas.drawText(wrappedLines[lineIndex], margin, y, bodyPaint)
                    y += 16f
                    lineIndex++
                }

                canvas.drawText(
                    "© Smart StudyPoint  •  Page $pageNumber",
                    margin,
                    pageHeight - 26f,
                    footerPaint
                )
                document.finishPage(page)
                pageNumber++
                if (lineIndex >= wrappedLines.size) break
            }

            savePdfDocument(context, document, title)
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            document.close()
        }
    }

    private fun savePdfDocument(context: Context, document: PdfDocument, title: String): Result<String> {
        val safeName = title.lowercase(Locale.ROOT)
            .replace(Regex("[^a-z0-9]+"), "_")
            .trim('_')
            .take(36)
            .ifBlank { "study_notes" }
        val stamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())
        val fileName = "SmartStudyPoint_${safeName}_$stamp.pdf"

        val savedLocation = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                put(MediaStore.Downloads.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/SmartStudyPoint")
            }
            val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                ?: throw IllegalStateException("Could not create PDF file in Downloads.")
            context.contentResolver.openOutputStream(uri)?.use { out ->
                document.writeTo(out)
            } ?: throw IllegalStateException("Could not open PDF output stream.")
            "Downloads/SmartStudyPoint/$fileName"
        } else {
            val dir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "SmartStudyPoint")
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, fileName)
            FileOutputStream(file).use { out -> document.writeTo(out) }
            sharePdfFile(context, file)
            file.name
        }

        return Result.success("PDF saved: $savedLocation")
    }

    private fun sharePdfFile(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, "Open or Share Study PDF").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

    private fun wrapLines(text: String, paint: Paint, maxWidth: Float): List<String> {
        val result = mutableListOf<String>()
        text.lines().forEach { paragraph ->
            if (paragraph.isBlank()) {
                result.add("")
            } else {
                val words = paragraph.split(" ")
                var current = StringBuilder()
                for (word in words) {
                    val candidate = if (current.isEmpty()) word else "$current $word"
                    if (paint.measureText(candidate) <= maxWidth) {
                        current = StringBuilder(candidate)
                    } else {
                        if (current.isNotEmpty()) result.add(current.toString())
                        current = StringBuilder(word)
                    }
                }
                if (current.isNotEmpty()) result.add(current.toString())
            }
        }
        return result
    }
}
