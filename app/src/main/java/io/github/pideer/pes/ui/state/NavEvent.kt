package io.github.pideer.pes.ui.state

sealed class NavEvent {
    object GoToMenu : NavEvent()
    object GoToConnection : NavEvent()
}
