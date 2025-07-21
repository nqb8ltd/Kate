package co.nqb8.kate.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import kate.composeapp.generated.resources.*
import kate.composeapp.generated.resources.Res
import kate.composeapp.generated.resources.bold
import kate.composeapp.generated.resources.dynalight
import kate.composeapp.generated.resources.regular
import org.jetbrains.compose.resources.Font

@Composable
fun Inter(): FontFamily {
    return FontFamily(
        Font(Res.font.regular, weight = FontWeight.Normal),
        Font(Res.font.dynalight, weight = FontWeight.Normal),
        Font(Res.font.bold, weight = FontWeight.Bold),
        Font(Res.font.medium, weight = FontWeight.Medium)
    )
}