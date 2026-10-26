package io.github.ranjandas.kimono.core.design.text

/** A number with its noun, singular for one: "1 app", "8 apps". */
fun count(n: Int, singular: String, plural: String = "${singular}s"): String = "$n ${if (n == 1) singular else plural}"
