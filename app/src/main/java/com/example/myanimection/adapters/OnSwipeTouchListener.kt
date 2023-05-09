package com.example.myanimection.adapters

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.myanimection.R
import com.example.myanimection.views.AnimeDetailFragment
import kotlin.math.abs

open class OnSwipeTouchListener(private final val context: Context, private val recyclerView: RecyclerView): OnTouchListener {

    val gestureDetector = GestureDetector(context, GestureListener())

    private fun loadFragment(animeMediaId: Int) {
        val fragment = AnimeDetailFragment()
        val bundle = Bundle()
        bundle.putInt("animeId", animeMediaId)
        fragment.arguments = bundle
        (context as AppCompatActivity).supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainerView, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        return gestureDetector.onTouchEvent(event!!)
    }

    private inner class GestureListener() : GestureDetector.SimpleOnGestureListener() {
        private val SWIPE_THRESHOLD = 100
        private val SWIPE_VELOCITY_THRESHOLD = 100
        private var click = false

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            if (e.action == MotionEvent.ACTION_UP) {
                Log.d("Click", click.toString())
                val child = recyclerView.findChildViewUnder(e.x, e.y)
                if (child != null) {
                    val position = recyclerView.getChildAdapterPosition(child)
                    val item = (recyclerView.adapter as RecyclerHomeAnimeAdapter).data[position]
                    loadFragment(item!!.id)
                }
            }
            return true
        }

        override fun onFling(
            e1: MotionEvent,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            var result = false
            try {
                val diffY = e2.y - e1.y
                val diffX = e2.x - e1.x
                if (abs(diffX) > abs(diffY)) {
                    if (abs(diffX) > SWIPE_THRESHOLD && abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            onSwipeRight()
                            animateRecyclerView(recyclerView.width.toFloat())
                        } else {
                            onSwipeLeft()
                            animateRecyclerView(-recyclerView.width.toFloat())
                        }
                        result = true
                    }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
            return result
        }
    }
    open fun onSwipeRight() {}

    open fun onSwipeLeft() {}

    private fun animateRecyclerView(offset: Float) {
        val animator = ValueAnimator.ofFloat(0f, offset)
        animator.addUpdateListener { valueAnimator ->
            val animatedValue = valueAnimator.animatedValue as Float
            recyclerView.translationX = animatedValue
        }
        animator.duration = 300
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                val animatorReverse = ValueAnimator.ofFloat(offset, 0f)
                animatorReverse.addUpdateListener { valueAnimator ->
                    val animatedValue = valueAnimator.animatedValue as Float
                    recyclerView.translationX = animatedValue
                }
                animatorReverse.duration = 300
                animatorReverse.interpolator = AccelerateDecelerateInterpolator()
                animatorReverse.start()
            }
        })
        animator.start()
    }
}

