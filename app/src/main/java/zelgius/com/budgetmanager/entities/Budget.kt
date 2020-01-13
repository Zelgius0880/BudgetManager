package zelgius.com.budgetmanager.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "budget")
data class Budget(
        @PrimaryKey(autoGenerate = true) var id: Long? = null,
        var name: String = "",
        var closed: Boolean = false,
        @ColumnInfo(name = "start_date")var startDate: LocalDateTime = LocalDateTime.now()
)