package zelgius.com.budgetmanager.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "budget_part", foreignKeys = [ForeignKey(entity = Budget::class, parentColumns = ["id"], childColumns = ["ref_budget"])])
data class BudgetPart(
        @PrimaryKey(autoGenerate = true) var id: Long? = null,
        var name: String = "",
        var goal: Double = -1.0,
        var closed: Boolean = false,
        var reached: Boolean = false,
        @ColumnInfo(name = "close_date") var closeDate: LocalDateTime? = null,
        @ColumnInfo(index = true, name = "ref_budget") var refBudget: Long? = null
) :Comparable<BudgetPart>{
    constructor(id: Long?,
                percent: Double,
                name: String,
                goal: Double,
                closed: Boolean,
                reached: Boolean,
                refBudget: Long?) : this(id, name, goal, closed, reached, null, refBudget) {
        this.percent = percent
    }

    var percent: Double = 0.0
    override fun compareTo(other: BudgetPart): Int =
        if (closed && !other.closed) 1
        else if (!closed && other.closed) -1
        else if (refBudget != null && other.refBudget == null) -1
        else if (refBudget == null && other.refBudget != null) 1
        else if (name == other.name) id?.compareTo(other.id?:0)?:0
        else name.compareTo(other.name)

}