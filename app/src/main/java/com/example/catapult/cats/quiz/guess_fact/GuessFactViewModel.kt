package com.example.catapult.cats.quiz.guess_fact

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.catapult.cats.db.CatsService
import com.example.catapult.cats.quiz.guess_fact.IGuessFactContract.GuessFactState
import com.example.catapult.core.seeResults
import com.example.catapult.di.DispatcherProvider
import com.example.catapult.users.Result
import com.example.catapult.users.UsersDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class GuessFactViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val dispatcherProvider: DispatcherProvider,
    private val catsService: CatsService,
    private val usersDataStore: UsersDataStore
) : ViewModel() {

    //private val catId: String = savedStateHandle.catId
    private val _guessFactState = MutableStateFlow(GuessFactState(usersData =  usersDataStore.data.value))
    val guessFactState = _guessFactState.asStateFlow()

    private val _factsEvents = MutableSharedFlow<IGuessFactContract.GuessFactUIEvent>()
    fun setEvent(event: IGuessFactContract.GuessFactUIEvent) = viewModelScope.launch { _factsEvents.emit(event) }
    private fun setGuessFactState (update: GuessFactState.() -> GuessFactState) =
        _guessFactState.getAndUpdate(update)

    private var timerJob: Job? = null

    init {
        observeGuessFact()
        observeEvents()
        startTimer()
    }

    private fun observeGuessFact() {
        viewModelScope.launch {
            setGuessFactState { copy(isLoading = true) }
            val list = catsService.getAllCatsFlow().first()
            setGuessFactState { copy(cats = list) }
            createQuestion()
        }
    }
    private fun observeEvents() {
        viewModelScope.launch {
            _factsEvents.collect { guessFactUIEvent ->
                when (guessFactUIEvent) {
                    is IGuessFactContract.GuessFactUIEvent.CalculatePoints -> calculatePoints(guessFactUIEvent.answerUser)
                }
            }
        }
    }
    private fun startTimer() {
        timerJob?.cancel()
        timerJob =  viewModelScope.launch {
            while (true) {
                delay(1000)
                setGuessFactState { copy(timer = timer - 1) }
                if(guessFactState.value.timer <= 0){
                    pauseTimer()
                    val result = Result(
                        result = seeResults(guessFactState.value.timer,  guessFactState.value.points),
                        createdAt = System.currentTimeMillis()
                    )
                    addResult(result)
                }
            }

        }
    }

    private fun pauseTimer() {
        timerJob?.cancel()
    }

    private fun addResult(result: Result) {
        viewModelScope.launch {
            usersDataStore.addGuessFactResult(result)
            setGuessFactState { copy(result = result) }
        }
    }



    private fun calculatePoints(answerUser: String) {
        if (guessFactState.value.rightAnswer == answerUser) {
            setGuessFactState { copy(points = points + 1) }
        }
        if(guessFactState.value.questionIndex<20 && guessFactState.value.timer > 0)
            createQuestion()
        else { //End Screen
            pauseTimer()
            val result = Result(
                result = seeResults(guessFactState.value.timer,  guessFactState.value.points),
                createdAt = System.currentTimeMillis()
            )
            addResult(result)
        }
    }
    private fun createQuestion(){
        viewModelScope.launch {
           // setGuessFactState { copy(isLoading = true) }
            val list = guessFactState.value.cats.shuffled()
            var newPhotos = catsService.getAllCatImagesByIdFlow(id = list[0].id).first()
            if(newPhotos.isEmpty()) {
                withContext(dispatcherProvider.io()) {
                    catsService.getAllCatsPhotosApi(id = list[0].id)
                }
                newPhotos = catsService.getAllCatImagesByIdFlow(id = list[0].id).first()
            }

            if(newPhotos.isEmpty()){
                createQuestion()
                return@launch
            }
            val image = newPhotos.shuffled()[0]

            val randomQuestion = Random.nextInt(1,4)
            val temperaments = list[0].temperament.replace(" ","").lowercase().split(",").shuffled()
            if(temperaments.size < 3){
                createQuestion()
                return@launch
            }
            val others = list
                .flatMap { it.temperament.replace(" ","").lowercase().split(",")}
                .distinct()
                .filter { !temperaments.contains(it) }
                .shuffled()
                .take(3)
            val (tmp,ans) = when(randomQuestion){
                1 -> Pair(list.take(4).shuffled().map { cat -> cat.name }, list[0].name)
                2 -> Pair((temperaments.take(3)+ others.take(1)).shuffled(),others[0])
                3 -> Pair((temperaments.take(1)+others.take(3)).shuffled(),temperaments[0])
                else -> {throw IllegalStateException()}
            }


            setGuessFactState {
                copy(
                    cats = list,
                    questionIndex = questionIndex + 1,
                    rightAnswer = ans,
                    answers = tmp,
                    isLoading = false,
                    image = image,
                    question = randomQuestion,
                    answerUser = ""
                )
            }
        }
    }
}

