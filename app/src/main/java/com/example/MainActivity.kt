package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.models.GameMode
import com.example.ui.screens.*
import com.example.ui.theme.MimeMasterTheme
import com.example.ui.viewmodel.GameViewModel
import com.example.ui.viewmodel.ScreenState

class MainActivity : ComponentActivity() {

    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MimeMasterTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MimeMasterApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun MimeMasterApp(viewModel: GameViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val achievements by viewModel.achievements.collectAsStateWithLifecycle()
    val communityWords by viewModel.communityWords.collectAsStateWithLifecycle()
    val gameSessions by viewModel.gameSessions.collectAsStateWithLifecycle()

    Crossfade(targetState = uiState.currentScreen, label = "screen_crossfade") { screen ->
        when (screen) {
            ScreenState.SPLASH -> {
                SplashScreen(
                    onStartClick = { viewModel.navigateTo(ScreenState.HOME) }
                )
            }

            ScreenState.HOME -> {
                HomeScreen(
                    profile = userProfile,
                    onQuickStartClick = {
                        viewModel.selectGameMode(GameMode.CLASSIC)
                        viewModel.navigateTo(ScreenState.GAME_SETUP)
                    },
                    onModeSelect = { mode ->
                        viewModel.selectGameMode(mode)
                        viewModel.navigateTo(ScreenState.GAME_SETUP)
                    },
                    onAiMasterClick = { viewModel.navigateTo(ScreenState.AI_MASTER) },
                    onCommunityClick = { viewModel.navigateTo(ScreenState.COMMUNITY) },
                    onProfileClick = { viewModel.navigateTo(ScreenState.PROFILE) }
                )
            }

            ScreenState.AI_MASTER -> {
                AiMasterScreen(
                    isAiLoading = uiState.isAiLoading,
                    aiRecommendation = uiState.aiRecommendation,
                    onRequestRecommendation = { count, age, group, mins, mood ->
                        viewModel.requestAiRecommendation(count, age, group, mins, mood)
                    },
                    onGenerateCustomTopic = { topic ->
                        viewModel.generateAiTopicWords(topic)
                    },
                    onStartRecommendedGame = {
                        viewModel.selectGameMode(GameMode.AI_CHALLENGE)
                        viewModel.startGame()
                    },
                    onBackClick = { viewModel.navigateTo(ScreenState.HOME) }
                )
            }

            ScreenState.GAME_SETUP -> {
                GameSetupScreen(
                    selectedMode = uiState.selectedMode,
                    categories = categories,
                    selectedCategories = uiState.selectedCategories,
                    selectedDifficulty = uiState.selectedDifficulty,
                    onToggleCategory = { id -> viewModel.toggleCategorySelection(id) },
                    onSelectDifficulty = { diff -> viewModel.setDifficulty(diff) },
                    onStartGame = { team1, team2, rounds, duration ->
                        viewModel.updateTeams(team1, team2, rounds, duration)
                        viewModel.startGame()
                    },
                    onBackClick = { viewModel.navigateTo(ScreenState.HOME) }
                )
            }

            ScreenState.GAMEPLAY -> {
                GameplayScreen(
                    team1Name = uiState.team1Name,
                    team2Name = uiState.team2Name,
                    team1Score = uiState.team1Score,
                    team2Score = uiState.team2Score,
                    activeTeamIndex = uiState.activeTeamIndex,
                    currentRound = uiState.currentRound,
                    totalRounds = uiState.totalRounds,
                    remainingSeconds = uiState.remainingTimerSeconds,
                    totalSeconds = uiState.roundDurationSeconds,
                    isTimerRunning = uiState.isTimerRunning,
                    isCardRevealed = uiState.isCardRevealed,
                    currentWord = uiState.wordsQueue.getOrNull(uiState.currentWordIndex),
                    comboCount = uiState.comboCount,
                    mimeoReaction = uiState.mimeoReactionText,
                    showHint = uiState.showHint,
                    onToggleReveal = { viewModel.toggleCardReveal() },
                    onStartTimer = { viewModel.startTimer() },
                    onPauseTimer = { viewModel.pauseTimer() },
                    onCorrectGuess = { viewModel.onCorrectGuess() },
                    onSkipWord = { viewModel.onSkipWord() },
                    onToggleHint = { viewModel.toggleHint() },
                    onEndGameEarly = { viewModel.endGameAndShowResults() }
                )
            }

            ScreenState.RESULTS -> {
                ResultsScreen(
                    team1Name = uiState.team1Name,
                    team2Name = uiState.team2Name,
                    team1Score = uiState.team1Score,
                    team2Score = uiState.team2Score,
                    onPlayAgainClick = { viewModel.startGame() },
                    onHomeClick = { viewModel.navigateTo(ScreenState.HOME) }
                )
            }

            ScreenState.PROFILE -> {
                ProfileScreen(
                    profile = userProfile,
                    achievements = achievements,
                    sessions = gameSessions,
                    onBackClick = { viewModel.navigateTo(ScreenState.HOME) }
                )
            }

            ScreenState.COMMUNITY -> {
                CommunityScreen(
                    communityWords = communityWords,
                    onSubmitWord = { text, category, creator ->
                        viewModel.submitCommunityWord(text, category, creator)
                    },
                    onUpvote = { id -> viewModel.upvoteCommunityWord(id) },
                    onBackClick = { viewModel.navigateTo(ScreenState.HOME) }
                )
            }
        }
    }
}
