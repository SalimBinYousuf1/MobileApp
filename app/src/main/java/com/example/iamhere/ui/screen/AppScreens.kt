package com.example.iamhere.ui.screen

import android.Manifest
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.iamhere.domain.model.Message
import com.example.iamhere.domain.repository.ContactRepository
import com.example.iamhere.domain.repository.MessageRepository
import com.example.iamhere.domain.repository.NetworkRepository
import com.example.iamhere.domain.usecase.GetMessagesUseCase
import com.example.iamhere.domain.usecase.SendMessageUseCase
import com.example.iamhere.ui.components.MessageBubble
import com.example.iamhere.ui.components.NetworkStatusBanner
import com.example.iamhere.ui.components.QRCodeGenerator
import com.example.iamhere.ui.components.QRScanner
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@Composable
fun AppRoot(vm: MainVm = hiltViewModel()) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = { NetworkStatusBanner(vm.status.collectAsState().value) },
        floatingActionButton = {
            FloatingActionButton(onClick = { selectedTab = 1 }) {
                Icon(Icons.Default.Add, contentDescription = "New chat")
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                listOf("Home", "Chat", "Contacts", "Settings").forEachIndexed { index, label ->
                    Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = { Text(label) })
                }
            }
            when (selectedTab) {
                0 -> HomeScreen(vm)
                1 -> ChatScreen(vm)
                2 -> ContactsScreen(vm)
                else -> SettingsScreen(vm)
            }
        }
    }
}

@Composable
fun SplashScreen(onReady: () -> Unit) {
    val requiredPermissions = buildList {
        add(Manifest.permission.BLUETOOTH_SCAN)
        add(Manifest.permission.BLUETOOTH_CONNECT)
        add(Manifest.permission.BLUETOOTH_ADVERTISE)
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) add(Manifest.permission.ACCESS_FINE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) add(Manifest.permission.POST_NOTIFICATIONS)
    }
    LaunchedEffect(Unit) { onReady() }
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("I Am Here")
        Text("Required permissions: ${requiredPermissions.size}")
    }
}

@Composable
fun HomeScreen(vm: MainVm) {
    val messages by vm.messages.collectAsState()
    val status by vm.status.collectAsState()
    var showHint by remember { mutableStateOf(false) }

    LaunchedEffect(status) {
        if (status == "Searching") {
            delay(10_000)
            if (vm.status.value == "Searching") showHint = true
        } else showHint = false
    }

    Column(Modifier.fillMaxSize().padding(12.dp)) {
        if (showHint) {
            Card(Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                Text(
                    "No peers nearby. Try sending a message to yourself to test encryption!",
                    Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        LazyColumn {
            items(messages.groupBy { it.senderId }.entries.toList()) { (sender, thread) ->
                val latest = thread.maxByOrNull { it.timestamp }
                Card(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Text(sender.take(18), style = MaterialTheme.typography.titleSmall)
                        Text(latest?.content ?: "", maxLines = 1)
                        Text("Unread: ${thread.count { !it.isRead }}")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(vm: MainVm) {
    val messages by vm.messages.collectAsState()
    val status by vm.status.collectAsState()
    var recipient by remember { mutableStateOf(TextFieldValue(vm.myPubKey)) }
    var text by remember { mutableStateOf(TextFieldValue("")) }

    Column(Modifier.fillMaxSize().padding(12.dp)) {
        OutlinedTextField(
            value = recipient,
            onValueChange = { recipient = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Recipient public key (empty = self)") }
        )
        LazyColumn(modifier = Modifier.weight(1f).padding(top = 8.dp)) {
            items(messages) { msg: Message ->
                MessageBubble(msg.content, msg.senderId == vm.myPubKey, msg.isVerified)
            }
        }
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Message") }
        )
        Button(
            onClick = {
                vm.send(text.text.trim(), recipient.text.trim())
                text = TextFieldValue("")
            },
            modifier = Modifier.align(Alignment.End).padding(top = 8.dp),
            enabled = text.text.isNotBlank()
        ) { Text(if (status == "Offline") "Queue Message" else "Send") }
    }
}

@Composable
fun ContactsScreen(vm: MainVm) {
    val contacts by vm.contacts.collectAsState()
    var showScanner by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().padding(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { showScanner = true }) { Text("Add Contact") }
            Button(onClick = {}) { Text("My QR Code") }
        }
        QRCodeGenerator(vm.myPubKey)
        if (showScanner) {
            QRScanner { scanned ->
                vm.addTrustedContact(scanned)
                showScanner = false
            }
        }
        LazyColumn {
            items(contacts) { c ->
                Card(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                    Column(Modifier.padding(10.dp)) {
                        Text(c.alias.ifBlank { c.pubKey.take(20) })
                        Text(if (c.isTrusted) "Trusted" else "Unverified")
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(vm: MainVm) {
    var batterySaver by remember { mutableStateOf(false) }
    var autoDelete by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Settings", style = MaterialTheme.typography.titleLarge)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text("Battery Saver Mode")
            Switch(checked = batterySaver, onCheckedChange = { batterySaver = it })
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text("Auto-Delete Messages after 24h")
            Switch(checked = autoDelete, onCheckedChange = { autoDelete = it; vm.toggleAutoDelete(it) })
        }
        Text("Export/Import Database")
        Text("About/Open Source Licenses")
        if (com.example.iamhere.BuildConfig.DEBUG) DebugMenu(vm)
    }
}

@Composable
fun DebugMenu(vm: MainVm) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Debug Menu")
        Button(onClick = { vm.clearDb() }) { Text("Clear all database tables") }
        Button(onClick = { vm.resetKeys() }) { Text("Reset Keys") }
        Button(onClick = { vm.simulatePeerFound() }) { Text("Simulate Peer Found") }
    }
}

@HiltViewModel
class MainVm @Inject constructor(
    networkRepository: NetworkRepository,
    private val messageRepository: MessageRepository,
    private val contactRepository: ContactRepository,
    private val sendMessageUseCase: SendMessageUseCase,
    getMessagesUseCase: GetMessagesUseCase
) : ViewModel() {
    val status: StateFlow<String> = networkRepository.networkStatus.stateIn(viewModelScope, SharingStarted.Eagerly, "Offline")
    val messages: StateFlow<List<Message>> = getMessagesUseCase().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val contacts = contactRepository.contacts().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val myPubKey: String = networkRepository.myPublicKey

    fun send(text: String, recipient: String) {
        if (text.isBlank()) return
        viewModelScope.launch { sendMessageUseCase(text, recipient) }
    }

    fun addTrustedContact(pubKey: String) {
        viewModelScope.launch {
            contactRepository.addOrUpdate(com.example.iamhere.domain.model.Contact(pubKey, "Contact ${pubKey.take(6)}", pubKey, true))
        }
    }

    fun toggleAutoDelete(enabled: Boolean) {
        if (!enabled) return
        viewModelScope.launch { messageRepository.clearAll() }
    }

    fun clearDb() {
        viewModelScope.launch {
            messageRepository.clearAll()
            contactRepository.clear()
        }
    }

    fun resetKeys() {
        viewModelScope.launch { networkRepository.resetKeys() }
    }

    fun simulatePeerFound() {
        viewModelScope.launch { networkRepository.simulatePeerFoundForDebug() }
    }
}
