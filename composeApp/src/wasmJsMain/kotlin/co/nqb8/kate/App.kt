package co.nqb8.kate

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import co.nqb8.kate.auth.LoginScreen
import co.nqb8.kate.auth.TokenStorage
import co.nqb8.kate.theme.KateTheme
import kotlinx.browser.document

@Composable
fun App() {
    LaunchedEffect(Unit){
        val ele = document.getElementById("loading-indicator")
        ele?.remove()
    }
    KateTheme {
        if (TokenStorage.token.isEmpty()){
            LoginScreen()
        }else {
            DashboardScreen()
        }
    }
}