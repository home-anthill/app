package eu.homeanthill.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

data class SpinnerItemObj(
    var key: String,
    var value: String,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialSpinner(
    title: String,
    options: List<SpinnerItemObj>,
    onSelect: (option: SpinnerItemObj) -> Unit,
    modifier: Modifier = Modifier,
    selectedOption: SpinnerItemObj? = null,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier.then(modifier)
    ) {
        TextField(
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
            value = selectedOption?.value ?: SpinnerItemObj("---", "---").value,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = { Text(title, style = MaterialTheme.typography.labelSmall) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.value, style = MaterialTheme.typography.bodyLarge) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}

@Preview(widthDp = 320, heightDp = 500, showBackground = true)
@Composable
fun MaterialSpinnerPreview() {
    val dataModels = listOf(
        SpinnerItemObj(key = "123", value = "Foo"), SpinnerItemObj(key = "321", value = "Bar")
    )
    MaterialTheme {
        Column {
            MaterialSpinner(
                "Spinner",
                dataModels,
                {},
                Modifier.padding(10.dp),
                SpinnerItemObj(key = "123", value = "Foo"),
            )
        }
    }
}