package com.example.iamhere.domain.usecase

import com.example.iamhere.domain.repository.ContactRepository
import com.example.iamhere.domain.repository.MessageRepository
import javax.inject.Inject

class SendMessageUseCase @Inject constructor(private val repo: MessageRepository) {
    suspend operator fun invoke(text: String, recipient: String) = repo.sendMessage(text, recipient)
}

class GetMessagesUseCase @Inject constructor(private val repo: MessageRepository) {
    operator fun invoke() = repo.getAllMessages()
}

class VerifyContactUseCase @Inject constructor(private val repo: ContactRepository) {
    suspend operator fun invoke(pubKey: String): Boolean = repo.get(pubKey)?.isTrusted == true
}
