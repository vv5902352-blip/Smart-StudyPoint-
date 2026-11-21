package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.ai.FirebaseAiRepository
import com.example.data.ai.configureAppCheck
import com.example.data.local.PreferencesManager
import com.example.data.local.SmartStudyDatabase
import com.example.data.repository.StudyRepository
import com.example.ui.SmartStudyApp
import com.example.ui.theme.SmartStudyPointTheme
import com.example.viewmodel.AdminViewModel
import com.example.viewmodel.ChapterQnAViewModel
import com.example.viewmodel.FlashcardViewModel
import com.example.viewmodel.StudyViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configureAppCheck(this, intent)
        enableEdgeToEdge()

        val repository = StudyRepository(
            db = SmartStudyDatabase.getInstance(applicationContext),
            preferences = PreferencesManager(applicationContext),
            aiRepository = FirebaseAiRepository()
        )

        setContent {
            val studyViewModel: StudyViewModel = viewModel(
                factory = StudyViewModel.factory(repository)
            )
            val chapterQnAViewModel: ChapterQnAViewModel = viewModel(
                factory = ChapterQnAViewModel.factory(repository)
            )
            val flashcardViewModel: FlashcardViewModel = viewModel(
                factory = FlashcardViewModel.factory(repository)
            )
            val adminViewModel: AdminViewModel = viewModel(
                factory = AdminViewModel.factory(repository)
            )
            val isDarkMode by studyViewModel.isDarkMode.collectAsStateWithLifecycle()

            SmartStudyPointTheme(darkTheme = isDarkMode) {
                SmartStudyApp(
                    studyViewModel = studyViewModel,
                    chapterQnAViewModel = chapterQnAViewModel,
                    flashcardViewModel = flashcardViewModel,
                    adminViewModel = adminViewModel
                )
            }
        }
    }
}
