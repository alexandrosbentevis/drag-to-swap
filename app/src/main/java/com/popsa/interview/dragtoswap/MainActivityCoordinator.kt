package com.popsa.interview.dragtoswap

/**
 * Place for logic
 */
class MainActivityCoordinator(
    private val viewModel: MainActivityViewModel,
    private val imageRepository: ImageRepository = FakeDi.imageRepository
) {

    init {
        viewModel.images.value = imageRepository.list
    }

    /**
     * Swaps the images.
     *
     * @param position1 The position of the first image.
     * @param position2 The position of the second image.
     */
    fun swapImages(position1: Int, position2: Int) {
        val item1 = imageRepository.list[position1]
        val item2 = imageRepository.list[position2]
        imageRepository.list[position1] = item2
        imageRepository.list[position2] = item1
        viewModel.images.value = imageRepository.list
        cancelSwap()
    }

    /**
     * Indicates that a swap has started
     *
     * @param index The index of the swapping source.
     * @param eventX The x coordinate of the starting swap.
     * @param eventY The y coordinateof the starting swap.
     */
    fun startedSwap(index: Int, eventX: Int, eventY: Int) {
        viewModel.draggingIndex.value = index
        viewModel.draggingImage.value = imageRepository.list[index]
        viewModel.events.value = Events.ImageSelected(eventX, eventY)
    }

    /**
     * Cancels the swap.
     */
    fun cancelSwap() {
        viewModel.draggingIndex.value = null
    }

    /**
     * Updates the view model for a drop event.
     *
     * @param eventX The x coordinate of the drop event.
     * @param eventY The y coordinate of the drop event.
     */
    fun imageDropped(eventX: Int, eventY: Int) {
        viewModel.events.value = Events.ImageDropped(eventX, eventY)
    }

    /**
     * Updates the view model for a dragging event.
     *
     * @param eventX The x coordinate of the dragging event.
     * @param eventX The y coordinate of the dragging event.
     */
    fun imageDragging(eventX: Int, eventY: Int) {
        viewModel.events.value = Events.ImageDragged(eventX, eventY)
    }

    sealed class Events {
        data class ImageDropped(val x: Int, val y: Int) : Events()
        data class ImageDragged(val x: Int, val y: Int) : Events()
        data class ImageSelected(val x: Int, val y: Int): Events()
    }
}