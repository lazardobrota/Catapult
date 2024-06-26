package com.example.catapult.users

import androidx.datastore.core.DataStore
import com.example.catapult.di.DispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.runBlocking
import okhttp3.internal.toImmutableList
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max

@Singleton
class UsersDataStore @Inject constructor(
    private val dispatcherProvider: DispatcherProvider,
    private val dataStore: DataStore<UsersData>
) {
    //TODO Change user to only have nickname, name and email saved, but quiz stats to be in database

    private val scope = CoroutineScope(dispatcherProvider.io())

    val data = dataStore.data.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = runBlocking { dataStore.data.first() }
    )


    private suspend fun updateList(users: List<User>, pick: Int = data.value.pick): UsersData {
        //Don't ask me why, but datastore doesn't update values inside user(it probably has some optimisation)
        //so list first needs to be empty so that optimisation doesn't kick in

        dataStore.updateData {
            it.copy(users = emptyList(), pick = -1)
        }

        return dataStore.updateData {
            it.copy(users = users, pick = pick)
        }


    }

    suspend fun changeMainUser(newPick: Int): UsersData {
        return dataStore.updateData {
            it.copy(pick = newPick)
        }
    }

    suspend fun addUser(user: User): UsersData {
        val users = data.value.users.toMutableList()
        users.add(user)

        return updateList(users = users.toImmutableList(), pick = users.size - 1)
    }

    suspend fun removeUser(user: User): UsersData {
        val users = data.value.users.toMutableList()
        users.remove(user)

        return updateList(users = users.toImmutableList(), pick = users.size - 1)
    }

    suspend fun removeAllUsers(): UsersData {
        return updateList(users = emptyList(), pick = -1)
    }


    suspend fun addGuessCatResult(result: Result): UsersData {
        val users = data.value.users.toMutableList()
        var guessCat = users[data.value.pick].guessCat
        val resultsHistory = guessCat.resultsHistory.toMutableList()

        resultsHistory.add(result)
        guessCat = guessCat.copy(
            resultsHistory = resultsHistory.toImmutableList(),
            bestResult = max(guessCat.bestResult, result.result)
        )
        users[data.value.pick] = users[data.value.pick].copy(guessCat = guessCat)

        return updateList(users = users.toImmutableList())
    }

    suspend fun addGuessFactResult(result: Result): UsersData {
        val users = data.value.users.toMutableList()
        var guessFact = users[data.value.pick].guessFact
        val resultsHistory = guessFact.resultsHistory.toMutableList()

        resultsHistory.add(result)
        guessFact = guessFact.copy(
            resultsHistory = resultsHistory.toImmutableList(),
            bestResult = max(guessFact.bestResult, result.result)
        )
        users[data.value.pick] = users[data.value.pick].copy(guessFact = guessFact)

        return updateList(users = users.toImmutableList())
    }

    suspend fun addLeftRightCatResult(result: Result): UsersData {
        val users = data.value.users.toMutableList()
        var leftRightCat = users[data.value.pick].leftRightCat
        val resultsHistory = leftRightCat.resultsHistory.toMutableList()

        resultsHistory.add(result)
        leftRightCat = leftRightCat.copy(
            resultsHistory = resultsHistory.toImmutableList(),
            bestResult = max(leftRightCat.bestResult, result.result)
        )
        users[data.value.pick] = users[data.value.pick].copy(leftRightCat = leftRightCat)

        return updateList(users = users.toImmutableList())
    }

    suspend fun updateUser(users: List<User>): UsersData {
        return updateList(users = users)
    }

}