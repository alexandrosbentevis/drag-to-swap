package com.popsa.interview.dragtoswap

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnimationUtils
import android.widget.ImageSwitcher
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.popsa.interview.dragtoswap.MainActivityCoordinator.Events.ImageDragged
import com.popsa.interview.dragtoswap.MainActivityCoordinator.Events.ImageDropped
import com.popsa.interview.dragtoswap.MainActivityCoordinator.Events.ImageSelected
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_scrolling.*

/**
 * Place for applying view data to views, and passing actions to coordinator
 */
class MainActivity : AppCompatActivity() {

    private val viewModel: MainActivityViewModel by viewModels()
    private lateinit var coordinator: MainActivityCoordinator

    private val imageSwitchers: List<ImageSwitcher> by lazy {
        listOf(
            image1,
            image2,
            image3,
            image4
        )
    }

    private val previewImageView: ShapeableImageView by lazy { previewImage }
    private val listLayout: ConstraintLayout by lazy { list }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        coordinator = MainActivityCoordinator(viewModel)
        setSupportActionBar(toolbar)
        toolbar.title = title

        observeDraggingIndex()
        observeImages()
        observeDraggingImage()
        observeEvents()

        list.setOnTouchListener { _, event ->
            val eventX = event.x.toInt()
            val eventY = event.y.toInt()

            when (event.action) {
                MotionEvent.ACTION_DOWN -> getImageSwitcherAt(eventX, eventY)?.let {
                        val index = it.tag as Int
                        coordinator.startedSwap(index, eventX, eventY)
                    }
                MotionEvent.ACTION_MOVE -> coordinator.imageDragging(eventX, eventY)
                MotionEvent.ACTION_UP -> coordinator.imageDropped(eventX, eventY)
            }
            true
        }
    }

    /**
     * Observes the view model events
     */
    private fun observeEvents() {
        viewModel.events.observe(this) {
            when (it) {
                is ImageSelected -> {
                    showPreviewImage()
                    dragPreviewImage(it.x, it.y)
                }
                is ImageDragged -> {
                    dragPreviewImage(it.x, it.y)
                }
                is ImageDropped -> {
                    animatePreviewImage(it.x, it.y)
                    animateReveal(it.x, it.y)
                    dropPreviewImage(it.x, it.y)
                }
            }
        }
    }

    /**
     * Observes the dragging image.
     */
    private fun observeDraggingImage() {
        viewModel.draggingImage.observe(this) { image ->
            Glide.with(this)
                .load(image?.imageUrl)
                .into(previewImageView)
        }
    }

    /**
     * Observes the list of images.
     */
    private fun observeImages() {
        viewModel.images.observe(this) { images ->
            // Load all the images from the viewModel into ImageViews
            imageSwitchers.forEachIndexed { index, imageSwitcher ->
                Glide.with(this)
                    .load(images[index].imageUrl)
                    .into(imageSwitcher.nextView as ImageView)
                imageSwitcher.tag =
                    index // Quick&dirty: stash the index of this image in the ImageView tag

                imageSwitcher.inAnimation =
                    AnimationUtils.loadAnimation(this, android.R.anim.fade_in)
                imageSwitcher.outAnimation =
                    AnimationUtils.loadAnimation(this, android.R.anim.fade_out)

                imageSwitcher.showNext()
            }
        }
    }

    /**
     * Observes the dragging index.
     */
    private fun observeDraggingIndex() {
        viewModel.draggingIndex.observe(this) { index ->
            if (index == null) {
                resetPreviewAnimation()
            }
        }
    }

    /**
     * Resets the preview circular view animation.
     */
    private fun resetPreviewAnimation() {
        // alpha
        previewImageView.animate().alpha(0f).duration = PREVIEW_ALPHA_ANIMATION_DURATION

        // stroke width
        val animator = ObjectAnimator.ofFloat(
            previewImageView,
            "strokeWidth",
            resources.getDimension(R.dimen.stroke_width)
        )
        animator.duration = PREVIEW_STROKE_WIDTH_ANIMATION_DURATION
        animator.start()
    }

    /**
     * Starts the animations of the preview image.,
     *
     * @param eventX The x coordinate
     * @param eventY The y coordinate
     */
    private fun animatePreviewImage(eventX: Int, eventY: Int) {
        val targetImage = getImageSwitcherAt(eventX, eventY)
        targetImage?.let {

            // center
            val destX = targetImage.x + targetImage.width / 2 - previewImageView.width / 2
            val destY = targetImage.y + targetImage.height / 2 - previewImageView.height / 2
            previewImageView.animate().x(destX).y(destY).duration =
                PREVIEW_CENTER_ANIMATION_DURATION

            // stroke width
            val animator = ObjectAnimator.ofFloat(previewImageView, "strokeWidth", 0f)
            animator.duration = PREVIEW_STROKE_WIDTH_ANIMATION_DURATION
            animator.start()
        }
    }

    /**
     * Starts the circular reveal animation.
     *
     * @param eventX The x coordinate
     * @param eventY The y coordinate
     */
    private fun animateReveal(eventX: Int, eventY: Int) {
        val targetImage = getImageSwitcherAt(eventX, eventY)
        targetImage?.let {
            val sourceImageIndex = viewModel.draggingIndex.value
            val targetImageIndex = targetImage.let { it.tag as Int }
            val imageSwitcher = imageSwitchers[targetImageIndex]
            if (sourceImageIndex != targetImageIndex) {
                imageSwitcher.post {
                    val animator = ViewAnimationUtils.createCircularReveal(
                        imageSwitcher.currentView,
                        imageSwitcher.width / 2,
                        imageSwitcher.height / 2,
                        0f,
                        imageSwitcher.width.toFloat()
                    ).apply {
                        interpolator = AccelerateDecelerateInterpolator()
                        duration = IMAGE_REVEAL_ANIMATION_DURATION
                    }
                    animator.start()
                }
            }
        }
    }

    /**
     * Shows the preview image.
     */
    private fun showPreviewImage() {
        previewImageView.visibility = View.VISIBLE
        previewImageView.alpha = 1f
    }

    /**
     * Drags the preview image.
     *
     * @param eventX The x coordinate
     * @param eventY The y coordinate
     */
    private fun dragPreviewImage(eventX: Int, eventY: Int) {
        previewImageView.x = eventX.toFloat() - previewImageView.width / 2
        previewImageView.y = eventY.toFloat() - previewImageView.height / 2
    }

    /**
     * Drops the preview image.
     *
     * @param eventX The x coordinate
     * @param eventY The y coordinate
     */
    private fun dropPreviewImage(eventX: Int, eventY: Int) {
        val sourceImageIndex = viewModel.draggingIndex.value
        val targetImage = getImageSwitcherAt(eventX, eventY)
        val targetImageIndex = targetImage
            ?.let { it.tag as Int }
        if (targetImageIndex != null && sourceImageIndex != null && targetImageIndex != sourceImageIndex)
            coordinator.swapImages(sourceImageIndex, targetImageIndex)
        else
            coordinator.cancelSwap()
    }

    /**
     * Finds the image switch at specific coordinates. If it does not exist returns null.
     *
     * @param x The x coordinate
     * @param y The y coordinate
     */
    private fun getImageSwitcherAt(x: Int, y: Int): ImageSwitcher? {
        val hitRect = Rect()
        return imageSwitchers.firstOrNull {
            it.getHitRect(hitRect)
            hitRect.contains(x, y)
        }
    }

    companion object {
        const val PREVIEW_STROKE_WIDTH_ANIMATION_DURATION = 200L
        const val PREVIEW_ALPHA_ANIMATION_DURATION = 200L
        const val PREVIEW_CENTER_ANIMATION_DURATION = 500L
        const val IMAGE_REVEAL_ANIMATION_DURATION = 500L
    }

}