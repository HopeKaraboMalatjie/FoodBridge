package com.foodbridge.app.data.repository

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

/** FR-2: photo upload for a new listing, via Firebase Cloud Storage. */
interface StorageRepository {
    suspend fun uploadListingPhoto(localUri: Uri): Result<String>
}

class FirebaseStorageRepository(
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) : StorageRepository {

    override suspend fun uploadListingPhoto(localUri: Uri): Result<String> = runCatching {
        val ref = storage.reference.child("listing_photos/${UUID.randomUUID()}.jpg")
        ref.putFile(localUri).await()
        ref.downloadUrl.await().toString()
    }
}
