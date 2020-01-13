package zelgius.com.budgetmanager.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "budget_part", foreignKeys = [ForeignKey(entity = Budget::class, parentColumns = ["id"], childColumns = ["ref_budget"])])
data class BudgetPart(
        @PrimaryKey(autoGenerate = true) var id: Long? = null,
        val percent: Double = 0.0,
        var name: String = "",
        val goal: Double = -1.0,
        val closed: Boolean = false,
        val reached: Boolean = false,
        @ColumnInfo(index = true, name = "ref_budget") var refBudget: Long? = null
)