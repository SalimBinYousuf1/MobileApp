package com.example.iamhere.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.iamhere.data.network.MeshEngine
import com.example.iamhere.domain.model.Message
import com.example.iamhere.domain.repository.MessageRepository
import com.example.iamhere.domain.repository.NetworkRepository
import com.example.iamhere.domain.usecase.SendMessageUseCase
import com.example.iamhere.ui.components.MessageBubble
import com.example.iamhere.ui.components.NetworkStatusBanner
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@Composable
fun AppRoot(vm: MainVm = hiltViewModel()) {
    var tab by remember { mutableStateOf(0) }
    Scaffold(topBar = { NetworkStatusBanner(vm.status.collectAsState().value) }, floatingActionButton = {
        FloatingActionButton(onClick = { tab = 1 }) { Icon(Icons.Default.Add, null) }
    }) { p ->
        Column(Modifier.padding(p)) {
            TabRow(selectedTabIndex = tab) {
                listOf("Home", "Chat", "Contacts", "Settings").forEachIndexed { i, t -> Tab(selected = tab==i, onClick = { tab=i }, text = { Text(t) }) }
            }
            when(tab){0->HomeScreen(vm);1->ChatScreen(vm);2->ContactsScreen(vm);else->SettingsScreen()}
        }
    }
}

@Composable fun SplashScreen() { Text("Checking permissions...") }

@Composable fun HomeScreen(vm: MainVm) {
    val msgs by vm.messages.collectAsState()
    if (msgs.isEmpty()) Text("No peers nearby. Try sending a message to yourself to test encryption!")
    LazyColumn { items(msgs) { Text("${it.senderId.take(12)}: ${it.content}") } }
}

@Composable fun ChatScreen(vm: MainVm) {
    val msgs by vm.messages.collectAsState()
    var text by remember { mutableStateOf(TextFieldValue("")) }
    Column(Modifier.fillMaxSize().padding(12.dp)) {
        LazyColumn(modifier = Modifier.weight(1f)) { items(msgs) { MessageBubble(it.content, it.senderId == vm.myPubKey, it.isVerified) } }
        OutlinedTextField(value = text, onValueChange = { text = it }, modifier = Modifier.fillMaxSize().weight(0.2f), placeholder = { Text("Type message") })
        Button(onClick = {
            vm.send(text.text, vm.myPubKey)
            text = TextFieldValue("")
        }) { Text(if (vm.status.collectAsState().value == "Offline") "Will send when peer found" else "Send") }
    }
}

@Composable fun ContactsScreen() { Text("Trusted contacts + QR tools") }
@Composable fun SettingsScreen() {
    var saver by remember { mutableStateOf(false) }
    var autoDelete by remember { mutableStateOf(false) }
    Column(Modifier.padding(16.dp)) {
        Text("Settings")
        Switch(checked = saver, onCheckedChange = { saver = it }); Text("Battery Saver Mode")
        Switch(checked = autoDelete, onCheckedChange = { autoDelete = it }); Text("Auto-Delete Messages after 24h")
        Text("Export/Import Database")
        Text("About/Open Source Licenses")
        if (com.example.iamhere.BuildConfig.DEBUG) DebugMenu()
    }
}

@Composable
fun DebugMenu(vm: MainVm = hiltViewModel()) {
    Column { Text("Debug Menu")
        Button(onClick = { vm.simulatePeerFound() }) { Text("Simulate Peer Found") }
        Button(onClick = { vm.resetKeys() }) { Text("Reset Keys") }
        Button(onClick = { vm.clearDb() }) { Text("Clear DB") }
    }
}

@HiltViewModel
class MainVm @Inject constructor(
    networkRepo: NetworkRepository,
    messageRepo: MessageRepository,
    private val sendMessageUseCase: SendMessageUseCase,
    private val meshEngine: MeshEngine
): ViewModel() {
    val status: StateFlow<String> = networkRepo.networkStatus.stateIn(viewModelScope, SharingStarted.Eagerly, "Offline")
    val messages = messageRepo.getAllMessages().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val myPubKey = meshEngine.myPublicKey
    fun send(text: String, recipient: String) { viewModelScope.launch { sendMessageUseCase(text, recipient) } }
    fun simulatePeerFound() { meshEngine.connectedPeers["debug"] = myPubKey }
    fun resetKeys() {}
    fun clearDb() {}
}
