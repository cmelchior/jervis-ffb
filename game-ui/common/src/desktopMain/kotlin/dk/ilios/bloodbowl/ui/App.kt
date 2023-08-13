package dk.ilios.bloodbowl.ui

import androidx.compose.runtime.Composable
import dk.ilios.bloodbowl.model.SidebarViewModel
import dk.ilios.bloodbowl.model.FieldViewModel

@Composable
fun App() {
    Screen(FieldViewModel(), SidebarViewModel(), SidebarViewModel())
}
