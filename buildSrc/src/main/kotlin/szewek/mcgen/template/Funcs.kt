package szewek.mcgen.template

import szewek.mcgen.util.JsonCreator
import szewek.mcgen.util.JsonFunc

internal infix fun Int.of(that: String) = Pair(that, this)

internal fun JsonCreator.item(name: String) = obj { "item" set name }
internal fun JsonCreator.tag(name: String) = obj { "tag" set name }
internal inline fun JsonCreator.ingredients(crossinline fn: JsonFunc) = "ingredients" arr fn
internal inline fun JsonCreator.typed(type: String, crossinline fn: JsonFunc) = obj {
    typed(type)
    fn()
}
internal fun JsonCreator.keyResult(pair: Pair<String, Int>) = "result" obj { result(pair) }
internal fun JsonCreator.result(pair: Pair<String, Int>) {
    itemOrTag(pair.first)
    if (pair.second > 1) "count" set pair.second
}
internal fun JsonCreator.keyItemOrTag(key: String, name: String) = key obj { itemOrTag(name) }
internal fun JsonCreator.itemOrTag(name: String) {
    if (name[0] == '#') {
        "tag" set name.substring(1)
    } else {
        "item" set name
    }
}

internal fun JsonCreator.pool(rolls: Int = 1, entries: JsonFunc, conditions: JsonFunc? = null) = obj {
    "rolls" set rolls
    "entries" arr entries
    if (conditions != null) "conditions" arr conditions
}

internal fun JsonCreator.typed(name: String) {
    "type" set name
}

val condition_survivesExplosion: JsonFunc = { obj { "condition" set "minecraft:survives_explosion" } }