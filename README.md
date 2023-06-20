# Drag to swap

This is a simple Android project to showcase the design and implementation of a drag to drop animation in an android app.

## Features
Although the app is a sample app, it supports some really nice features that are essential for the best user experience:
* **Drag view**: Drags a preview circular view from a source to a target in order to swap the images between the source and the target.
* **Swap animations**: Smooth animations to swap the images between the source and the target.

## Approach
In order to achieve the behaviour of the Popsa app, an `ImageSwitcher` was used for the four image containers.

When the user drags an image, a circular preview image appears that can be dropped in one of the remaining targets. When a drop is performed the preview image animates to the center of the target and two animations are happening in parallel:

a. The source image is swapping with the target with a crossfade animation
b. The target is swapping with the source using a circular reveal animation

## Future improvements

This project is just a small project to demonstrate the design and implementation of the drag to swap animation. Because of the limited amount of time given to finish the task, the animations are not exactly matching the Popsa app and could be improved.

An improvement to this solution would be to make the preview circular image perform the circular reveal itself and look more similar to the Popsa app fluid animation.

Also tests could be written for the logic in the coordinator.