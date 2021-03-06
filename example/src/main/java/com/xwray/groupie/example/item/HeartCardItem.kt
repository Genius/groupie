package com.xwray.groupie.example.item

import android.graphics.drawable.Animatable
import androidx.annotation.ColorInt
import com.xwray.groupie.example.INSET
import com.xwray.groupie.example.INSET_TYPE_KEY
import com.xwray.groupie.example.R
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import kotlinx.android.synthetic.main.item_heart_card.*

const val FAVORITE = "FAVORITE"

class HeartCardItem(
    @ColorInt private val colorInt: Int,
    id: Long,
    private val onFavoriteListener: (item: HeartCardItem, favorite: Boolean) -> Unit
) : Item(id) {

    private var checked = false
    private var inProgress = false
    private var holder: com.xwray.groupie.GroupieViewHolder? = null

    init {
        extras[INSET_TYPE_KEY] = INSET
    }

    override fun getLayout() = R.layout.item_heart_card

    private fun bindHeart(holder: GroupieViewHolder) {
        if (inProgress) {
            animateProgress(holder)
        } else {
            (holder.favorite.drawable as? Animatable)?.stop()
            holder.favorite.setImageResource(R.drawable.favorite_state_list)
        }
        holder.favorite.isChecked = checked
    }

    private fun animateProgress(holder: GroupieViewHolder) {
        holder.favorite.apply {
            setImageResource(R.drawable.avd_favorite_progress)
            (drawable as Animatable).start()
        }
    }

    fun setFavorite(favorite: Boolean) {
        inProgress = false
        checked = favorite
    }

    override fun isClickable() = false

    override fun bind(holder: GroupieViewHolder, position: Int) {
        holder.root.setBackgroundColor(colorInt)
        bindHeart(holder)
        holder.text.text = (id + 1).toString()

        holder.favorite.setOnClickListener {
            if (!inProgress) {
                inProgress = true
                animateProgress(holder)
                onFavoriteListener(this@HeartCardItem, !checked)
            }
        }
    }

    override fun bind(holder: GroupieViewHolder, position: Int, payloads: List<Any>) {
        if (payloads.contains(FAVORITE)) {
            bindHeart(holder)
        } else {
            bind(holder, position)
        }
    }
}
