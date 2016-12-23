# ImageWings
A small plugin that let's you load Images and Displays them as particle wings

# Examples
I used [some image](http://www.clipartkid.com/wings-free-images-at-clker-com-vector-clip-art-online-royalty-p5nNvq-clipart/) I found online for the Wing.  
For the animated wing I rotated both wings to 24° in 6° increments.

## Result static:
![Bad wing](http://i.imgur.com/HfnuRzy.png)

## Result animated:
![AnimatedWing](https://github.com/I-Al-Istannen/ImageWings/blob/master/media/AnimatedWing.gif)

# Dependencies
## PerceiveCore
It uses the RotationMatrices which it brings with it, which you can find [here](https://github.com/PerceiveDev/PerceiveCore/blob/dev/src/main/java/com/perceivedev/perceivecore/util/math/RotationMatrices.java).  
The other parts are the Gui and Command system

# Limitations
Due to some `"bug"` the rotation value for the player body seems to not update correctly. This means you may need to manually spin your head a bit until the wings align properly. I compared my functions with the client source code and they perfrom the same calculations.  
It makes no sense, but it is true sadly.
