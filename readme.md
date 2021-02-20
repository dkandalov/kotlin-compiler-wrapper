[![Build Status](https://github.com/dkandalov/kotlin-compiler-wrapper/workflows/CI/badge.svg)](https://github.com/dkandalov/kotlin-compiler-wrapper/actions)

This is a wrapper for kotlin compiler API to be used inside of [LivePlugin](https://github.com/dkandalov/live-plugin) (see `liveplugin.pluginrunner.kotlin.KotlinPluginRunner`).
The main reason for pulling this code out of LivePlugin was that Kotlin has some classes 
with **exactly the same fully qualified names** as classes in IntelliJ.
So with this code inside IJ plugin, it was really hard to know which classes 
are going to be loaded ("namespaced" by classloader) IntelliJ class or class from Kotlin jar. 
And using the wrong class could lead to subtle classloading errors which are tricky to debug (e.g. errors like "expected type kotlin.String but was kotlin.String").

This project is not intended for public use outside of LivePlugin but of course you can do what you want :)

```
repositories {
    maven { setUrl("https://dl.bintray.com/dkandalov/maven") }
}
dependencies {
    implementation "live-plugin:kotlin-compiler-wrapper:0.3"
}
```