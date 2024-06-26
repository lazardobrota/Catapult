package com.example.catapult.cats.list

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import coil.compose.SubcomposeAsyncImage
import com.example.catapult.R
import com.example.catapult.cats.db.Cat
import com.example.catapult.cats.list.ICatsContract.CatsListState
import com.example.catapult.cats.list.ICatsContract.CatsListUIEvent
import com.example.catapult.core.AppIconButton
import com.example.catapult.core.ListInfo
import com.example.catapult.core.SimpleInfo
import com.example.catapult.core.defaultImage
import com.example.catapult.core.getPic
import com.example.catapult.core.isDefaultImage
import com.example.catapult.users.User
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
fun NavGraphBuilder.catsListScreen(
    route: String,
    navController: NavController,
    goToQuiz: () -> Unit,
) = composable(route = route) {
    val catsViewModel: CatsViewModel = hiltViewModel()
    val catsState by catsViewModel.catsState.collectAsState()

    val scope = rememberCoroutineScope()
    val drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    BackHandler(enabled = drawerState.isOpen) {
        scope.launch { drawerState.close() }
    }

    if (catsState.usersData.pick == -1) {
        navController.navigate("login")
    }
    else {
        Surface(
            tonalElevation = 1.dp
        ) {
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    UserInfoDrawer(
                        catsState = catsState,
                        catsViewModel = catsViewModel,
                        addNewUser = { navController.navigate("login?add-new-user=${true}") },
                        navigateToHistory = { navController.navigate("history") },
                        navigateToEdit = { navController.navigate("user/edit") },
                        leaderboard = { category ->
                            navController.navigate("quiz/leaderboard/${category}")
                        }
                    )
                }
            ) {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(text = "Catapult", style = MaterialTheme.typography.labelLarge)
                            },
                            navigationIcon = {
                                AppIconButton(
                                    imageVector = Icons.Default.Menu,
                                    onClick = { scope.launch { drawerState.open() } }
                                )
                            },
                            actions = {
                                AppIconButton(
                                    imageVector = if (catsState.darkTheme) Icons.Outlined.LightMode else Icons.Filled.LightMode,
                                    onClick = {
                                        catsViewModel.setCatsEvent(CatsListUIEvent.ChangeTheme(!catsState.darkTheme))
                                    })
                            }
                        )
                    },
                    floatingActionButton = {
                        LargeFloatingActionButton(
                            onClick = {
                                goToQuiz()
                            },
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.tertiary,
                        ) {
                            Icon(
                                painterResource(id = R.drawable.quiz_icon),
                                contentDescription = "Floating action button."
                            )
                        }
                    },
                    content = {
                        CatsList(
                            catsState = catsState,
                            paddingValues = it,
                            eventPublisher = { uiEvent -> catsViewModel.setCatsEvent(uiEvent) },
                            onClick = { catInfoDetail -> navController.navigate("cats/${catInfoDetail.id}") }
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun UserInfoDrawer(
    catsState: CatsListState,
    catsViewModel: CatsViewModel,
    addNewUser: () -> Unit,
    navigateToHistory: () -> Unit,
    navigateToEdit: () -> Unit,
    leaderboard: (Int) -> Unit
) {
    BoxWithConstraints {
        val box = this
        ModalDrawerSheet(
            modifier = Modifier.width(box.maxWidth * 3 / 4)
        ) {

            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {

                Column {
                    Text(
                        text = "Accounts",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(start = 16.dp, top = 16.dp)
                    )

                    NavigationDrawerItem(
                        icon = {
                            AppIconButton(
                                imageVector = Icons.Filled.Add,
                                onClick = addNewUser
                            )
                        },
                        label = {
                            Text(
                                text = "Add Account",
                                style = MaterialTheme.typography.labelLarge
                            )
                        },
                        selected = false,
                        onClick = addNewUser
                    )

                    LazyColumn(
                        modifier = Modifier.heightIn(max = 180.dp)
                    ) {
                        itemsIndexed(catsState.usersData.users) { index, user ->
                            UserItemDrawer(
                                user = user,
                                index = index,
                                catsState = catsState,
                                changeUserOnClick = { catsViewModel.changeMainUser(pick = index) },
                                navigateToEdit = navigateToEdit
                            )
                        }
                    }
                }

                HorizontalDivider()

                Column {
                    Text(
                        text = "History",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(start = 16.dp)
                    )

                    NavigationDrawerItem(
                        icon = {
                            AppIconButton(
                                imageVector = Icons.Filled.Category,
                                onClick = addNewUser
                            )
                        },
                        label = {
                            Text(
                                text = "See quiz's history",
                                style = MaterialTheme.typography.labelLarge
                            )
                        },
                        selected = false,
                        onClick = navigateToHistory
                    )
                }

                HorizontalDivider()

                Column {
                    Text(
                        text = "Leaderboards",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(start = 16.dp)
                    )

                    NavigationDrawerItem(
                        icon = {
                            AppIconButton(
                                imageVector = Icons.Filled.Leaderboard,
                                onClick = {
                                    leaderboard(2)
                                }
                            )
                        },
                        label = {
                            Text(
                                text = "Guess Fact",
                                style = MaterialTheme.typography.labelLarge
                            )
                        },
                        selected = false,
                        onClick = {
                            leaderboard(2)
                        }
                    )
                    NavigationDrawerItem(
                        icon = {
                            AppIconButton(
                                imageVector = Icons.Filled.Leaderboard,
                                onClick = {
                                    leaderboard(1)
                                }
                            )
                        },
                        label = {
                            Text(
                                text = "Guess Cat",
                                style = MaterialTheme.typography.labelLarge
                            )
                        },
                        selected = false,
                        onClick = {
                            leaderboard(1)
                        }
                    )
                    NavigationDrawerItem(
                        icon = {
                            AppIconButton(
                                imageVector = Icons.Filled.Leaderboard,
                                onClick = {
                                    leaderboard(3)
                                }
                            )
                        },
                        label = {
                            Text(
                                text = "Left Right Cat",
                                style = MaterialTheme.typography.labelLarge
                            )
                        },
                        selected = false,
                        onClick = {
                            leaderboard(3)
                        }
                    )
                }

                Spacer(modifier = Modifier.padding(top = 20.dp))
                NavigationDrawerItem(
                    icon = {
                        AppIconButton(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            onClick = {
                                catsViewModel.setCatsEvent(
                                    CatsListUIEvent.Logout(
                                        user = catsState.usersData.users[catsState.usersData.pick]
                                    )
                                )
                            }
                        )
                    },
                    label = {
                        Text(
                            text = "Logout",
                            style = MaterialTheme.typography.labelLarge
                        )
                    },
                    selected = false,
                    onClick = {
                        catsViewModel.setCatsEvent(
                            CatsListUIEvent.Logout(
                                user = catsState.usersData.users[catsState.usersData.pick]
                            )
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun UserItemDrawer(
    user: User,
    index: Int,
    catsState: CatsListState,
    changeUserOnClick: () -> Unit,
    navigateToEdit: () -> Unit,
) {

    val bitmap by remember {
        mutableStateOf(
            if (isDefaultImage(user.image)) null else getPic(user.image)
        )
    }

    NavigationDrawerItem(
        icon = {
            SubcomposeAsyncImage(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
                model = bitmap ?: defaultImage(),
                contentDescription = null,
                loading = {
                    Box(modifier = Modifier.fillMaxSize()) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            )
        },
        label = {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Absolute.SpaceBetween
            ) {
                Column {
                    Text(
                        text = user.nickname,
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                if (index == catsState.usersData.pick) {
                    AppIconButton(
                        imageVector = Icons.Filled.Edit,
                        onClick = if (index == (catsState.usersData.pick)) navigateToEdit else changeUserOnClick
                    )
                }
            }
        },
        selected = index == (catsState.usersData.pick),
        onClick = if (index == (catsState.usersData.pick)) navigateToEdit else changeUserOnClick,
    )
}

@Composable
fun CatsList(
    catsState: CatsListState,
    paddingValues: PaddingValues,
    eventPublisher: (uiEvent: CatsListUIEvent) -> Unit,
    onClick: (Cat) -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {

        TextField(
            value = catsState.searchText,
            onValueChange = { text ->
                eventPublisher(CatsListUIEvent.SearchQueryChanged(query = text))
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
            placeholder = { Text(text = "Search") },
            shape = CircleShape,
            leadingIcon = { AppIconButton(imageVector = Icons.Default.Search, onClick = { }) }
        )

        if (catsState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        } else if (catsState.error != null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                val errorMessage = when (catsState.error) {
                    is CatsListState.DetailsError.DataUpdateFailed ->
                        "Failed to load. Error message: ${catsState.error.cause?.message}."
                }

                Text(text = errorMessage, fontSize = 20.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(
                    items = catsState.catsFiltered,
                    key = { catInfo -> catInfo.id }
                ) { catDetail ->
                    CatDetails(
                        cat = catDetail,
                        onClick = { onClick(catDetail) }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun CatDetails(
    cat: Cat,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
            .clickable { onClick() }
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            SimpleInfo(title = "Race Of Cat", description = cat.name)
            Spacer(modifier = Modifier.height(16.dp))

            if (!cat.altNames.isNullOrEmpty()) {
                ListInfo(
                    title = "Alternative Names",
                    items = cat.altNames.replace(" ", "").split(",")
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            SimpleInfo(title = "Description", description = cat.description)
            Spacer(modifier = Modifier.height(16.dp))

            Row {
                cat.temperament.replace(" ", "").split(",").take(3).forEach {
                    AssistChip(
                        onClick = { },
                        label = { Text(text = it) }
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                }
            }
        }
    }
}

