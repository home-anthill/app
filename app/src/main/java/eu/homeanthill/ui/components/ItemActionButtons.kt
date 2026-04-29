package eu.homeanthill.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ItemActionButtons(
  onEdit: () -> Unit,
  modifier: Modifier = Modifier,
  onDelete: (() -> Unit)? = null,
) {
  Row(
    modifier = modifier,
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    IconButton(
      onClick = onEdit,
      modifier = Modifier
        .size(40.dp)
        .background(MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
    ) {
      Icon(
        imageVector = Icons.Default.Edit,
        contentDescription = "Edit",
        tint = MaterialTheme.colorScheme.tertiary,
        modifier = Modifier.size(20.dp)
      )
    }
    if (onDelete != null) {
      IconButton(
        onClick = onDelete,
        modifier = Modifier
          .size(40.dp)
          .background(MaterialTheme.colorScheme.error, RoundedCornerShape(8.dp))
      ) {
        Icon(
          imageVector = Icons.Default.Delete,
          contentDescription = "Delete",
          tint = MaterialTheme.colorScheme.tertiary,
          modifier = Modifier.size(20.dp)
        )
      }
    }
  }
}
