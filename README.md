# ImageWings
A small plugin that let's you load Images and Displays them as particle wings

# Heavily Work in Progress

# Usage
You will need to create a folder "images" inside the data folder of this plugin.

## File

### Name
Then create a file in there, ending with ".wingMeta". Just a normal, renamed textfile.

### Layout
```yaml
# The path to the image
image_path: "S:/Minecraft/Test/coolWingSmall.png"
# The direction of the entity is taken and multiplied by this value and then added to the center
# This allows us to offset the wings a bit. The default offsets them by 0.1 blocks
player_vector_multiplier: -0.1
# The pitch in radian. You will most certainly want to leave it.
# It is evaluated using an ExpressionParser I found somewhere (MIT-License). I will probably change that to just an ordinary number
pitch_rad: "PI"
# The addition to the yaw in radian. You will also want to leave this as it is
yaw_rad_addition: "PI"
# The parser. This is interesting
parser:
  # The xScale. Every x value will be multiplied by this amount
  xScale: 0.05
  # The yScale. Same as above, just y
  yScale: 0.05
  
  # The x offset in pixels
  xOffsetAbsolute: 90.0
  # The y offset in pixels
  yOffsetAbsolute: 85.0

  # This is the amount of pixels it skips in the for loop reading the image
  # Smaller ==> accurate, but more particles
  # Bigger ==> Less accurate, but less particles
  xGranularity: 1
  yGranularity: 1

  # How the colours should be treated
  colourMapper:
  # You can add as many as you want
  # Any pixel between (0,19,127) and (0,19,127) [red, green, blue] inclusive will be the particle "SUSPENDED_DEPTH"
  - 0,19,127 to 0,19,127 is SUSPENDED_DEPTH
  # Any pixel between (10,10,13) and (10,10,13) [red, green, blue] inclusive will be the particle "ENCHANTMENT_TABLE"
  - 10,10,13 to 10,10,13 is ENCHANTMENT_TABLE
```

# Examples
I used [some image](http://www.clipartkid.com/wings-free-images-at-clker-com-vector-clip-art-online-royalty-p5nNvq-clipart/) I found online for the Wing.
I resized it to `117 * 47` pixels and painted the red wing blue.
Then I used the file I posted above and reloaded (which currently displays it).

## Result:
![Bad wing](http://i.imgur.com/HfnuRzy.png)

Yes, the two particles and the angle do not make it very clear, but it is actually a bit behind the entity.


# Dependencies
## PerceiveCore
Yea, I don't know why either :)  
It uses the RotationMatrices which it brings with it, which you can find [here](https://github.com/PerceiveDev/PerceiveCore/blob/dev/src/main/java/com/perceivedev/perceivecore/particle/math/RotationMatrices.java).

## EvalEx
Currently used for evaluating the `yaw_rad` and `pitch_rad`.  
You can find it [here](https://github.com/uklimaschewski/EvalEx).
