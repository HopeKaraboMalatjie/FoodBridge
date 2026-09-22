package com.foodbridge.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.foodbridge.app.FoodBridgeApp
import com.foodbridge.app.data.model.User
import com.foodbridge.app.ui.auth.AuthViewModel
import com.foodbridge.app.ui.auth.LoginScreen
import com.foodbridge.app.ui.auth.RegisterScreen
import com.foodbridge.app.ui.chat.ChatScreen
import com.foodbridge.app.ui.chat.ChatViewModel
import com.foodbridge.app.ui.home.HomeFeedScreen
import com.foodbridge.app.ui.home.HomeViewModel
import com.foodbridge.app.ui.impact.ImpactProfileScreen
import com.foodbridge.app.ui.impact.ImpactViewModel
import com.foodbridge.app.ui.listing.ListingDetailScreen
import com.foodbridge.app.ui.listing.ListingDetailViewModel
import com.foodbridge.app.ui.map.MapViewScreen
import com.foodbridge.app.ui.post.PostListingScreen
import com.foodbridge.app.ui.post.PostListingViewModel
import com.foodbridge.app.util.UiState
import com.foodbridge.app.util.rememberUserLocation

/**
 * Wires up the screens in the order shown by Figure 7 of the design doc:
 * splash/login -> bottom-tab Home Feed -> Map / Post / Impact / Listing Detail
 * one tap away -> reserving opens the chat/confirmation screen.
 */
@Composable
fun FoodBridgeNavGraph(app: FoodBridgeApp) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val authViewModel: AuthViewModel = viewModel(
        factory = viewModelFactory { initializer { AuthViewModel(app.authRepository) } }
    )
    val authState by authViewModel.authState.collectAsState()
    val currentUser = (authState as? UiState.Success<User>)?.data

    val showBottomBar = currentRoute in Routes.bottomTabs

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                FoodBridgeBottomBar(currentRoute = currentRoute) { route ->
                    navController.navigate(route) {
                        popUpTo(Routes.HOME_FEED) { inclusive = false }
                        launchSingleTop = true
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = if (currentUser != null) Routes.HOME_FEED else Routes.LOGIN,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.LOGIN) {
                LoginScreen(
                    authState = authState,
                    onLogin = { email, password -> authViewModel.login(email, password) },
                    onNavigateToRegister = { navController.navigate(Routes.REGISTER) }
                )
            }
            composable(Routes.REGISTER) {
                RegisterScreen(
                    authState = authState,
                    onRegister = { name, email, password, role -> authViewModel.register(name, email, password, role) },
                    onNavigateToLogin = { navController.navigate(Routes.LOGIN) }
                )
            }

            composable(Routes.HOME_FEED) {
                val homeViewModel: HomeViewModel = viewModel(
                    factory = viewModelFactory { initializer { HomeViewModel(app.listingRepository) } }
                )
                val listingsState by homeViewModel.listingsState.collectAsState()
                val selectedCategory by homeViewModel.selectedCategory.collectAsState()
                HomeFeedScreen(
                    listingsState = listingsState,
                    selectedCategory = selectedCategory,
                    onCategorySelected = homeViewModel::setCategory,
                    onListingClick = { listing -> navController.navigate(Routes.listingDetail(listing.listingId)) }
                )
            }

            composable(Routes.MAP_VIEW) {
                val homeViewModel: HomeViewModel = viewModel(
                    factory = viewModelFactory { initializer { HomeViewModel(app.listingRepository) } }
                )
                val listingsState by homeViewModel.listingsState.collectAsState()
                val location = rememberUserLocation()
                MapViewScreen(
                    listingsState = listingsState,
                    userLat = location.lat,
                    userLng = location.lng,
                    onMarkerClick = { listing -> navController.navigate(Routes.listingDetail(listing.listingId)) }
                )
            }

            composable(Routes.POST_LISTING) {
                val postViewModel: PostListingViewModel = viewModel(
                    factory = viewModelFactory {
                        initializer { PostListingViewModel(app.listingRepository, app.storageRepository) }
                    }
                )
                val postState by postViewModel.postState.collectAsState()
                val location = rememberUserLocation()
                PostListingScreen(
                    postState = postState,
                    latitude = location.lat,
                    longitude = location.lng
                ) { title, category, quantity, description, start, end, photoUri ->
                    currentUser?.let { user ->
                        postViewModel.submit(
                            donorId = user.userId,
                            donorName = user.name,
                            title = title,
                            category = category,
                            quantity = quantity,
                            description = description,
                            pickupWindowStart = start,
                            pickupWindowEnd = end,
                            latitude = location.lat,
                            longitude = location.lng,
                            photoUri = photoUri
                        )
                    }
                }
            }

            composable(Routes.IMPACT_PROFILE) {
                val impactViewModel: ImpactViewModel = viewModel(
                    factory = viewModelFactory { initializer { ImpactViewModel(app.impactRepository) } }
                )
                val impactState by impactViewModel.impactState.collectAsState()
                androidx.compose.runtime.LaunchedEffect(currentUser?.userId) {
                    currentUser?.userId?.let { impactViewModel.load(it) }
                }
                ImpactProfileScreen(impactState = impactState)
            }

            composable(Routes.LISTING_DETAIL) { backStackEntry ->
                val listingId = backStackEntry.arguments?.getString("listingId").orEmpty()
                val detailViewModel: ListingDetailViewModel = viewModel(
                    factory = viewModelFactory {
                        initializer { ListingDetailViewModel(app.listingRepository, listingId) }
                    }
                )
                val listingState by detailViewModel.listingState.collectAsState()
                val reserveState by detailViewModel.reserveState.collectAsState()
                ListingDetailScreen(
                    listingState = listingState,
                    reserveState = reserveState,
                    onReserve = { currentUser?.let { detailViewModel.reserve(it.userId) } },
                    onMessageDonor = { navController.navigate(Routes.chat(listingId)) }
                )
            }

            composable(Routes.CHAT) { backStackEntry ->
                val reservationId = backStackEntry.arguments?.getString("reservationId").orEmpty()
                val chatViewModel: ChatViewModel = viewModel(
                    factory = viewModelFactory {
                        initializer { ChatViewModel(app.chatRepository, reservationId) }
                    }
                )
                val messagesState by chatViewModel.messagesState.collectAsState()
                ChatScreen(
                    messagesState = messagesState,
                    currentUserId = currentUser?.userId.orEmpty(),
                    onSend = { text -> currentUser?.let { chatViewModel.send(it.userId, text) } }
                )
            }
        }
    }
}
