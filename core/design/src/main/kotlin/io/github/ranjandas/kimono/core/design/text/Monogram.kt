package io.github.ranjandas.kimono.core.design.text

/**
 * Two letters that stand for an app, the way an element has a symbol: the initials of the first two words
 * ("Apple TV" is "AT"), or for a single word its first letter and the next consonant ("Netflix" is "Nt",
 * "YouTube" is "Yt", "Plex" is "Pl"). A word with no later consonant falls back to its second letter, and a
 * one-letter name is just that letter. Digits count as letters, so "4K Player" is "4P".
 */
fun monogram(label: String): String {
    val words = label.split(' ', '-', '_', '.').map { word -> word.filter { it.isLetterOrDigit() } }
        .filter { it.isNotEmpty() }
    return when {
        words.isEmpty() -> "?"
        words.size >= 2 -> "${words[0].first().uppercaseChar()}${words[1].first().uppercaseChar()}"
        else -> singleWord(words[0])
    }
}

private fun singleWord(word: String): String {
    val first = word.first().uppercaseChar()
    val rest = word.drop(1)
    val second = rest.firstOrNull { it.isLetter() && it.lowercaseChar() !in Vowels } ?: rest.firstOrNull()
    return if (second == null) "$first" else "$first${second.lowercaseChar()}"
}

private val Vowels = setOf('a', 'e', 'i', 'o', 'u', 'y')
