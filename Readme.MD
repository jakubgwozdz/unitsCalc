My version of HelloWorld.kt, a simple program that I started writing to learn Kotlin language and a PoC towards IntelliJ IDEA Plugins.

My aim: write IDEA plugin that will display a tool window, where one can do all the math towards units conversion for XLS_FO and/or HTML, so:

- "25.4mm" -> "1in"
- "1in+1cm" -> "35.4mm"

Currently it works with parenthesis, sums and subtractions. No multiplications or divisions yet.
Also, the ratios are hardcoded, 1in = 25.4mm = 72pt = 300px
