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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
        .background(Color(0xFF2C2C2C), RoundedCornerShape(8.dp))
    ) {
      Icon(
        imageVector = Icons.Default.Edit,
        contentDescription = "Edit",
        tint = Color.White,
        modifier = Modifier.size(20.dp)
      )
    }
    if (onDelete != null) {
      IconButton(
        onClick = onDelete,
        modifier = Modifier
          .size(40.dp)
          .background(Color(0xFFB71C1C), RoundedCornerShape(8.dp))
      ) {
        Icon(
          imageVector = Icons.Default.Delete,
          contentDescription = "Delete",
          tint = Color.White,
          modifier = Modifier.size(20.dp)
        )
      }
    }
  }
}
