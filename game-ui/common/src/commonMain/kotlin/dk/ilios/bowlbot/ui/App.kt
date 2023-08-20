package dk.ilios.bowlbot.ui

import androidx.compose.runtime.Composable
import dk.ilios.bloodbowl.ui.model.FieldViewModel
import dk.ilios.bloodbowl.ui.model.SidebarViewModel

@Composable
fun App() {
    Screen(FieldViewModel(), SidebarViewModel(), SidebarViewModel())
}
