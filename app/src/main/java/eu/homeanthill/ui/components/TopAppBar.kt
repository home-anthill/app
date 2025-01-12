package eu.homeanthill.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

import eu.homeanthill.R
import eu.homeanthill.api.model.Profile
import eu.homeanthill.ui.navigation.MainRoute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(
    appbarTitle: String = stringResource(id = R.string.app_name),
    onBackPressed: (() -> Unit)? = null,
    navController: NavController? = null,
    // manually injected repository via screens
    profile: Profile? = null,
) {
    CenterAlignedTopAppBar(colors = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
//        titleContentColor = MaterialTheme.colorScheme.secondary
    ), title = {
        Text(
            text = appbarTitle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.titleLarge
        )
    }, navigationIcon = {
        if (onBackPressed != null) {
            IconButton(onClick = { onBackPressed() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                )
            }
        }
    }, actions = {
        if (profile != null) {
            IconButton(onClick = {
                navController?.navigate(route = MainRoute.Profile.name)
            }) {
                CircleImage(profile.github.avatarURL, 100.dp)
            }
        }
    })
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
fun TopAppBarPreview() {
    TopAppBar(
        appbarTitle = stringResource(R.string.home),
        onBackPressed = {},
        navController = null,
        profile = null,
    )
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
fun TopAppBarDetailPreview() {
    TopAppBar(
        appbarTitle = stringResource(R.string.home),
        onBackPressed = {},
        navController = null,
        profile = null,
    )
}