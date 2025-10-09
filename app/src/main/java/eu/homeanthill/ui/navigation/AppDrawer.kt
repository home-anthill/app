package eu.homeanthill.ui.navigation

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.gson.Gson

import eu.homeanthill.R
import eu.homeanthill.api.model.Profile
import eu.homeanthill.mainKey
import eu.homeanthill.profileKey
import eu.homeanthill.ui.components.CircleAsyncImage
import eu.homeanthill.ui.navigation.Destinations.DEVICES
import eu.homeanthill.ui.navigation.Destinations.HOME
import eu.homeanthill.ui.navigation.Destinations.HOMES
import eu.homeanthill.ui.navigation.Destinations.PROFILE


@Composable
fun AppDrawer(
  route: String,
  modifier: Modifier = Modifier,
  navigateToHome: () -> Unit = {},
  navigateToProfile: () -> Unit = {},
  navigateToHomes: () -> Unit = {},
  navigateToDevices: () -> Unit = {},
  closeDrawer: () -> Unit = {}
) {
  val context = LocalContext.current
  val sharedPreference = context.getSharedPreferences(mainKey, Context.MODE_PRIVATE)
  val json: String? = sharedPreference.getString(profileKey, null)
  val profile: Profile? = if (json != null) Gson().fromJson(json, Profile::class.java) else null

  ModalDrawerSheet(modifier = Modifier) {
    DrawerHeader(modifier, profile)
    Spacer(modifier = Modifier.padding(5.dp))
    NavigationDrawerItem(
      label = {
        Text(
          text = stringResource(id = R.string.home),
          style = MaterialTheme.typography.labelMedium
        )
      },
      selected = route == HOME,
      onClick = {
        navigateToHome()
        closeDrawer()
      },
      icon = { Icon(imageVector = Icons.Default.Home, contentDescription = null) },
      shape = MaterialTheme.shapes.small
    )
    NavigationDrawerItem(
      label = {
        Text(
          text = stringResource(id = R.string.profile),
          style = MaterialTheme.typography.labelMedium
        )
      },
      selected = route == PROFILE,
      onClick = {
        navigateToProfile()
        closeDrawer()
      },
      icon = { Icon(imageVector = Icons.Default.Person, contentDescription = null) },
      shape = MaterialTheme.shapes.small
    )
    NavigationDrawerItem(
      label = {
        Text(
          text = stringResource(id = R.string.homes),
          style = MaterialTheme.typography.labelMedium
        )
      },
      selected = route == HOMES,
      onClick = {
        navigateToHomes()
        closeDrawer()
      },
      icon = { Icon(imageVector = Icons.Default.Home, contentDescription = null) },
      shape = MaterialTheme.shapes.small
    )
    NavigationDrawerItem(
      label = {
        Text(
          text = stringResource(id = R.string.devices),
          style = MaterialTheme.typography.labelMedium
        )
      },
      selected = route == DEVICES,
      onClick = {
        navigateToDevices()
        closeDrawer()
      },
      icon = { Icon(imageVector = Icons.Default.Home, contentDescription = null) },
      shape = MaterialTheme.shapes.small
    )
  }
}

@Composable
fun DrawerHeader(modifier: Modifier, profile: Profile?) {
  Column(
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.Start,
    modifier = modifier
      .background(MaterialTheme.colorScheme.secondary)
      .padding(15.dp)
      .fillMaxWidth()
  ) {
    if (profile?.github?.avatarURL != null) {
      CircleAsyncImage(profile.github.avatarURL, 70.dp)
    }
    Spacer(modifier = Modifier.padding(5.dp))
    Text(
      text = stringResource(id = R.string.app_name),
      textAlign = TextAlign.Center,
      style = MaterialTheme.typography.bodyLarge,
      color = MaterialTheme.colorScheme.onPrimary,
    )
  }
}

