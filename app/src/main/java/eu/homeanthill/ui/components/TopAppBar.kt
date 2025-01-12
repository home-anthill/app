package eu.homeanthill.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage

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
                CircleImageWithBorder(profile.github.avatarURL)
            }
        }
    })
}

@Composable
fun CircleImageWithBorder(imageUrl: String) {
    Box(
        modifier = Modifier
            .size(100.dp)
            .clip(CircleShape)
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = "Circular Image with Border",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
        )
    }
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