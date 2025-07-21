package co.nqb8.kate

import androidx.compose.runtime.Composable
import co.nqb8.kate.auth.LoginScreen
import co.nqb8.kate.auth.TokenStorage
import co.nqb8.kate.theme.KateTheme

@Composable
fun App() {
    KateTheme {
        if (TokenStorage.token.isEmpty()){
            LoginScreen()
        }else {
            DashboardScreen()
        }
    }
}