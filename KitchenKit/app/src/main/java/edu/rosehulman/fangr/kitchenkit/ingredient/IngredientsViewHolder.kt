package edu.rosehulman.fangr.kitchenkit.ingredient

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.squareup.picasso.Picasso
import edu.rosehulman.fangr.kitchenkit.R
import kotlinx.android.synthetic.main.ingredient_card_view.view.*
import java.text.SimpleDateFormat
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
class IngredientsViewHolder(
    itemView: View,
    adapter: IngredientsAdapter,
    private val context: Context
) :
    RecyclerView.ViewHolder(itemView) {

    private val nameTextView = itemView.ingredient_name as TextView
    private val amountNumTextView = itemView.amount_num as TextView
    private val amountUnitTextView = itemView.amount_unit as TextView
    private val isFrozenView = itemView.snow_icon as ImageButton
    private val warningView = itemView.warning_icon as ImageButton
    private val boughtTextView = itemView.bought_time as TextView
    private val imageView = itemView.ingredient_image
    private val bestBeforeTextView = itemView.best_before_date

    init {
        this.itemView.setOnClickListener {
            adapter.selectIngredientAt(this.adapterPosition)
        }
    }

    fun bind(ingredient: Ingredient) {
        var storedIng: StoredIngredient?
        val boughtTime = ingredient.bought?.toDate()?.time
        val currentTime = Timestamp.now().toDate().time

        val storedIngRef = FirebaseFirestore.getInstance().collection("storedIngredient")
        storedIngRef.get().addOnSuccessListener { snapshot: QuerySnapshot ->
            for (doc in snapshot) {
                val temp = doc.toObject(StoredIngredient::class.java)
                if (temp.name == ingredient.name) {
                    //Update Image
                    storedIng = temp
                    Picasso.get().load(storedIng!!.url).into(imageView)
                    //Update Best Before date
                    //Update warning icon
                    if (ingredient.bought != null)
                        this.updateBestBefore(storedIng!!, ingredient, ingredient.bought!!.toDate())

                    break
                }
            }
        }

        this.nameTextView.text = ingredient.name
        this.amountNumTextView.text =
            this.context.getString(R.string.amount_display, ingredient.amount)
        this.amountUnitTextView.text = ingredient.unit
        if (!ingredient.isFrozen)
            this.isFrozenView.setImageResource(android.R.color.white)
        else
            this.isFrozenView.setImageResource(R.drawable.ic_snow)

        if (ingredient.bought == null) {
            this.boughtTextView.text = this.context.getString(R.string.zero_day_display)
            return
        }

        val difference = currentTime - boughtTime!!
        val days = (difference / (1000 * 60 * 60 * 24)).toInt()
        if (days == 0)
            this.boughtTextView.text = this.context.getString(R.string.zero_day_display)
        else
            this.boughtTextView.text = this.context.resources.getQuantityString(
                R.plurals.day_display,
                days,
                days
            )
    }

    @SuppressLint("SimpleDateFormat")
    private fun updateBestBefore(
        storedIng: StoredIngredient,
        ingredient: Ingredient,
        bought: Date
    ) {
        val date1: String?
        val date2: String?
        if (ingredient.isFrozen) {
            date1 = storedIng.expireFrozen1
            date2 = storedIng.expireFrozen2
        } else {
            date1 = storedIng.expire1
            date2 = storedIng.expire2
        }

        val formatter = SimpleDateFormat("MM/dd/yyyy")
        val formatted = formatter.format(bought)

        var day = formatted.substring(3, 5).toInt()
        var month = formatted.substring(0, 2).toInt()
        var year = formatted.substring(6, 10).toInt()

        //Update Best before
        val variance = date1.toInt()
        if (date2.contains("day")) {
            day += variance
            if (day >= 31) {
                day -= 30
                month += 1
            }
        } else if (date2.contains("month")) {
            month += variance
            if (month >= 13) {
                month -= 12
                year += 1
            }
        } else if (date2.contains("year"))
            year += variance

        val expireDate = StringBuilder()
        expireDate.append(month)
        expireDate.append('/')
        expireDate.append(day)
        expireDate.append('/')
        expireDate.append(year)
        bestBeforeTextView.text = expireDate.toString()

        //Update warning icon
        val current = Timestamp.now().toDate()
        val currentFormatted = formatter.format(current)
        val currentDay = currentFormatted.substring(3, 5).toInt()
        val currentMonth = currentFormatted.substring(0, 2).toInt()
        val currentYear = currentFormatted.substring(6, 10).toInt()

        if (currentYear < year)
            this.warningView.setImageResource(android.R.color.white)
        else if (currentYear == year && currentMonth < month)
            this.warningView.setImageResource(android.R.color.white)
        else if (currentMonth == month && currentDay < day)
            this.warningView.setImageResource(android.R.color.white)
    }
}