package zelgius.com.budgetmanager.entities

import androidx.lifecycle.LiveData
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "spare_entry", foreignKeys = [ForeignKey(entity = Budget::class, parentColumns = ["id"], childColumns = ["ref_budget"])])
data class SpareEntry(
        @PrimaryKey(autoGenerate = true) var id: Long? = null,
        var comment: String = "",
        val date: LocalDateTime = LocalDateTime.now(),
        val amount: Double = 0.0,
        @ColumnInfo(index = true, name = "ref_budget") var refBudget: Long? = null
)