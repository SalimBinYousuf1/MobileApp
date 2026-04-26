package com.example.iamhere.di

import android.content.Context
import androidx.room.Room
import com.example.iamhere.data.local.AppDatabase
import com.example.iamhere.data.local.ContactDao
import com.example.iamhere.data.local.MessageDao
import com.example.iamhere.data.local.PacketDao
import com.example.iamhere.data.repository.ContactRepositoryImpl
import com.example.iamhere.data.repository.MessageRepositoryImpl
import com.example.iamhere.data.repository.NetworkRepositoryImpl
import com.example.iamhere.domain.repository.ContactRepository
import com.example.iamhere.domain.repository.MessageRepository
import com.example.iamhere.domain.repository.NetworkRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides @Singleton
    fun provideDb(@ApplicationContext context: Context): AppDatabase {
        SQLiteDatabase.loadLibs(context)
        val passphrase = SQLiteDatabase.getBytes("iamhere-secure".toCharArray())
        return Room.databaseBuilder(context, AppDatabase::class.java, "iamhere.db")
            .openHelperFactory(SupportFactory(passphrase))
            .fallbackToDestructiveMigration()
            .build()
    }
    @Provides fun packetDao(db: AppDatabase): PacketDao = db.packetDao()
    @Provides fun messageDao(db: AppDatabase): MessageDao = db.messageDao()
    @Provides fun contactDao(db: AppDatabase): ContactDao = db.contactDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepoModule {
    @Binds abstract fun bindMessageRepo(impl: MessageRepositoryImpl): MessageRepository
    @Binds abstract fun bindContactRepo(impl: ContactRepositoryImpl): ContactRepository
    @Binds abstract fun bindNetworkRepo(impl: NetworkRepositoryImpl): NetworkRepository
}
