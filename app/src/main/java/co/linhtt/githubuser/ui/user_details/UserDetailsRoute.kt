package co.linhtt.githubuser.ui.user_details

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController

@Composable
fun UserDetailsRoute(navHostController: NavHostController) {
    val viewModel: UserDetailsViewModel = hiltViewModel()
    val userDetailsResourceState by viewModel.userDetailsResourceState.collectAsState(initial = UserDetailsScreenState.Initialized)
    UserDetailsScreen(userDetailsResourceState, onBackPressed = {
        navHostController.popBackStack()
    })
}