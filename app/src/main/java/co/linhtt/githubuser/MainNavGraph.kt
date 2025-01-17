package co.linhtt.githubuser

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import co.linhtt.githubuser.ui.user_details.UserDetailsRoute
import co.linhtt.githubuser.ui.users.UserListRoute

@Composable
fun MainNavGraph(navController: NavHostController, startDestination: String = Destination.USERS) {
    NavHost(
        modifier = Modifier, navController = navController, startDestination = startDestination
    ) {
        composable(Destination.USERS) {
            UserListRoute(navController)
        }
        composable(Destination.USER_DETAIL, arguments = listOf(
            navArgument("login") {
                type = NavType.StringType
                nullable = false
            }
        )) {
            UserDetailsRoute(navController)
        }
    }
}