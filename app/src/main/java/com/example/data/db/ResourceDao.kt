package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.QueryAuditLog
import com.example.data.model.ServiceCategory
import com.example.data.model.SupportResource
import com.example.data.model.VerificationStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface ResourceDao {

    @Query("SELECT * FROM support_resources WHERE isActive = 1 ORDER BY isEmergencyService DESC, organizationName ASC")
    fun getAllActiveResources(): Flow<List<SupportResource>>

    @Query("SELECT * FROM support_resources ORDER BY organizationName ASC")
    fun getAllResourcesForAdmin(): Flow<List<SupportResource>>

    @Query("SELECT * FROM support_resources WHERE isActive = 1 AND serviceCategory = :category ORDER BY isEmergencyService DESC, organizationName ASC")
    fun getResourcesByCategory(category: ServiceCategory): Flow<List<SupportResource>>

    @Query("SELECT * FROM support_resources WHERE isActive = 1 AND isEmergencyService = 1")
    fun getEmergencyResources(): Flow<List<SupportResource>>

    @Query("SELECT * FROM support_resources WHERE id = :id LIMIT 1")
    suspend fun getResourceById(id: String): SupportResource?

    @Query("SELECT COUNT(*) FROM support_resources")
    suspend fun getResourceCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(resources: List<SupportResource>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(resource: SupportResource)

    @Update
    suspend fun update(resource: SupportResource)

    @Query("DELETE FROM support_resources WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE support_resources SET verificationStatus = :status, verificationDate = :date, reviewer = :reviewer, notes = :notes WHERE id = :id")
    suspend fun updateVerification(id: String, status: VerificationStatus, date: String, reviewer: String, notes: String)

    @Query("UPDATE support_resources SET isActive = :isActive WHERE id = :id")
    suspend fun toggleActiveStatus(id: String, isActive: Boolean)

    // Privacy-compliant minimal query logs
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: QueryAuditLog)

    @Query("SELECT * FROM query_audit_logs ORDER BY timestamp DESC LIMIT 20")
    fun getRecentAuditLogs(): Flow<List<QueryAuditLog>>

    @Query("DELETE FROM query_audit_logs")
    suspend fun clearAllAuditLogs()
}
