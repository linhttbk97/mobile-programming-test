package co.linhtt.githubuser.ui.user_details

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.linhtt.domain.model.GithubUserDetails
import co.linhtt.githubuser.R
import coil.compose.AsyncImage

sealed class UserDetailsScreenState {
    data object Initialized : UserDetailsScreenState()
    data object Loading : UserDetailsScreenState()
    data class Success(val userDetails: GithubUserDetails) : UserDetailsScreenState()
    data class Error(val message: String) : UserDetailsScreenState()
}

@Composable
fun UserDetailsScreen(state: UserDetailsScreenState, onBackPressed: () -> Unit) {
    Box(
        modifier = Modifier
            .systemBarsPadding()
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (state) {

            UserDetailsScreenState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            is UserDetailsScreenState.Success -> {
                UserDetailsPage(state.userDetails, onBackPressed)
            }

            is UserDetailsScreenState.Error -> {
                ErrorPage(state.message)
            }

            else -> {
                // ignore Initialized state for now
            }
        }
    }
}

@Composable
fun UserDetailsPage(userDetails: GithubUserDetails, onBackPressed: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Image(
                imageVector = Icons.AutoMirrored.Default.ArrowBack,
                contentDescription = null,
                modifier = Modifier
                    .padding(10.dp)
                    .size(44.dp)
                    .clickable {
                        onBackPressed.invoke()
                    },
                contentScale = ContentScale.Inside
            )
            Text(
                text = stringResource(R.string.user_details_screen_title),
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp),
                modifier = Modifier.align(Alignment.Center)
            )

        }
        UserCardDetail(userDetails)
        Row(
            modifier = Modifier
                .padding(vertical = 20.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            CommonUserDetailsDataCard(
                label = stringResource(R.string.followers),
                value = userDetails.followers,
                dataIconResId = R.drawable.ic_followers
            )
            Spacer(modifier = Modifier.size(40.dp))
            CommonUserDetailsDataCard(
                label = stringResource(R.string.following),
                value = userDetails.following,
                dataIconResId = R.drawable.ic_following
            )
        }
        Text(
            text = stringResource(R.string.blog_label),
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp),
            modifier = Modifier.padding(start = 20.dp, top = 10.dp),
        )
        Text(
            text = userDetails.htmlUrl,
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
            modifier = Modifier.padding(start = 20.dp, top = 10.dp),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )

    }
}

@Composable
fun ErrorPage(errorMsg: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.cannot_load_user_details),
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
        )
        Text(
            text = errorMsg,
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
            modifier = Modifier.padding(top = 10.dp)
        )
    }
}

@Composable
fun UserCardDetail(user: GithubUserDetails) {
    Card(
        modifier = Modifier
            .padding(20.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background,
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            AsyncImage(
                model = user.avatarUrl,
                contentDescription = null,
                modifier = Modifier
                    .padding(20.dp)
                    .clip(CircleShape)
                    .size(90.dp)
            )
            Column(
                modifier = Modifier
                    .padding(top = 20.dp)
                    .weight(1f)
            ) {
                Text(
                    text = user.name ?: user.login,
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(
                    Modifier
                        .padding(end = 10.dp, top = 10.dp)
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                )
                when {
                    user.location != null -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(R.drawable.ic_location),
                                contentDescription = null,
                                modifier = Modifier
                                    .padding(horizontal = 2.dp)
                                    .size(16.dp),
                                contentScale = ContentScale.Inside
                            )
                            Text(
                                text = user.location!!,
                                style = MaterialTheme.typography.titleMedium.copy(fontSize = 12.sp),
                                modifier = Modifier
                                    .padding(vertical = 10.dp)
                                    .fillMaxWidth(),
                            )
                        }
                    }

                    else -> {
                        Text(
                            text = user.htmlUrl,
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 12.sp),
                            color = Color(0xFF007AFF),
                            modifier = Modifier
                                .padding(vertical = 10.dp)
                                .fillMaxWidth(),
                            textDecoration = TextDecoration.Underline
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CommonUserDetailsDataCard(label: String, value: Int, dataIconResId: Int) {
    Column(
        modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(dataIconResId),
            contentDescription = null,
            modifier = Modifier
                .size(40.dp)
                .background(MaterialTheme.colorScheme.surfaceDim, CircleShape),
            contentScale = ContentScale.Inside
        )
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 16.sp),
            modifier = Modifier.padding(vertical = 10.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
            modifier = Modifier,
        )
    }
}