package ca.gbc.comp3074.labex4.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {

    // writes are suspend so they run off the main thread (Room crashes if I touch the db on the UI thread)
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(customer: Customer)

    // Room finds the row to delete by its primary key (id)
    @Delete
    suspend fun delete(customer: Customer)

    // Flow = Room re-runs this and sends a new list every time the table changes
    @Query("SELECT * FROM customers ORDER BY id")
    fun getAllCustomers(): Flow<List<Customer>>
}
