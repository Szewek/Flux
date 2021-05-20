@file:JvmName("Templates")
@file:Suppress("unused")
package szewek.mcgen.template

import com.google.gson.JsonElement
import szewek.mcgen.util.JsonFileWriter
import szewek.mcgen.util.JsonFunc

private val colors = arrayOf(
        "white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray",
        "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black"
)
private val dirs = arrayOf("north", "east", "south", "west")
private val lit = arrayOf("false", "true")
private val mapShapes = mapOf(
    "sword" to arrayOf("X", "X", "#"),
    "shovel" to arrayOf("X", "#", "#"),
    "pickaxe" to arrayOf("XXX", " # ", " # "),
    "axe" to arrayOf("XX", "X#", " #"),
    "hoe" to arrayOf("XX", " #", " #")
)

private fun machineBlockStates(v: JsonElement, out: JsonFileWriter) {
    val item = v.asString
    val ns = out.namespace
    out(item, variants {
        for (i in lit.indices) for (j in dirs.indices) {
            "facing=${dirs[j]},lit=${lit[i]}" obj {
                "model" set (if (i == 0) "$ns:block/$item" else "$ns:block/${item}_on")
                if (j > 0) "y" set (90*j)
            }
        }
    })
}
private fun activeBlockStates(v: JsonElement, out: JsonFileWriter) {
    val item = v.asString
    val ns = out.namespace
    out(item, variants {
        "lit=false".singleObj("model", "$ns:block/$item")
        "lit=true".singleObj("model", "$ns:block/${item}_on")
    })
}
private fun defaultBlockStates(v: JsonElement, out: JsonFileWriter) {
    val item = v.asString
    val ns = out.namespace
    out(item, variants { "".singleObj("model", "$ns:block/$item") })
}

private fun metalRecipes(v: JsonElement, out: JsonFileWriter) {
    val item = v.asString
    val ns = out.namespace
    val itemIngot = "$ns:${item}_ingot"
    val itemNugget = "$ns:${item}_nugget"
    val itemBlock = "$ns:${item}_block"
    if (!isVanilla(item)) {
        out("${item}_block", craftingCompress(itemIngot, itemBlock))
        out("${item}_ingot", craftingCompress(itemNugget, itemIngot))
        out("${item}_nugget", craftingUncompress("${item}_nugget", itemIngot, itemNugget))
        out("${item}_ingot_from_block", craftingUncompress("${item}_ingot", itemBlock, itemIngot))
        if (!isAlloy(item)) out("${item}_ingot_smelting_ore", smelting(
                "${item}_ingot",
                "#forge:ores/${item}",
                1 of itemIngot
        ))
    }
    if (!isAlloy(item)) {
        val oreLazyTag = lazyTag("forge:ores/${item}")
        out("${item}_dust_grinding_ore",
                fluxMachine("grinding", 2 of "$ns:${item}_dust", oreLazyTag)
        )
        out("${item}_grit_washing_ore",
                fluxMachine("washing", 3 of "$ns:${item}_grit", oreLazyTag)
        )
        out("${item}_dust_grinding_grit",
                fluxMachine("grinding", 1 of "$ns:${item}_dust", lazyTag("forge:grits/${item}"))
        )
    }
    out("${item}_dust_grinding_ingot",
            fluxMachine("grinding", 1 of "$ns:${item}_dust", lazyTag("forge:ingots/${item}"))
    )
    out("${item}_ingot_smelting_dust", smelting(
            "${item}_ingot",
            "#forge:dusts/${item}",
            1 of (if (isVanilla(item)) "minecraft" else ns) + ":${item}_ingot"
    ))
    if (item != "netherite") out("${item}_gear", craftingShaped(
            arrayOf("X#X", "# #", "X#X"),
            mapOf("#" to "#forge:ingots/$item", "X" to "#forge:nuggets/$item"),
            2 of "$ns:${item}_gear"
    ))
    out("${item}_plate", craftingShaped(
            arrayOf("#", "#"),
            mapOf("#" to "#forge:ingots/$item"),
            1 of "$ns:${item}_plate"
    ))
    out("${item}_plate_compacting_ingot",
            fluxMachine("compacting", 1 of "$ns:${item}_plate", lazyTag("forge:ingots/${item}"))
    )
}

private fun metalRecipesTagged(v: JsonElement, out: JsonFileWriter) {
    val item = v.asString
    val dustsTag = "#forge:dusts/$item"
    out("${item}_dust_grinding_ore",
            fluxMachine("grinding", 2 of dustsTag, lazyTag("forge:ores/$item"))
    )
    out("${item}_dust_grinding_grit",
            fluxMachine("grinding", 1 of dustsTag, lazyTag("forge:grits/$item"))
    )
    out("${item}_dust_grinding_ingot",
            fluxMachine("grinding", 1 of dustsTag, lazyTag("forge:ingots/$item"))
    )
    out("${item}_grit_washing_ore",
            fluxMachine("washing", 3 of "#forge:grits/$item", lazyTag("forge:ores/$item"))
    )
}

private fun colorRecipes(v: JsonElement, out: JsonFileWriter) {
    val o = v.asJsonObject
    val from = o["from"].asString
    o.remove("from")
    val into = o["into"].asString
    o.remove("into")
    val type = o["type"].asString
    o.remove("type")
    for(col in colors) out("${col}_${into}_$type", typedRecipe("${out.namespace}:$type") {
        ingredients(lazyItem("${col}_$from"))
        keyResult(1 of "${col}_$into")
        for ((k, je) in o.entrySet()) k set je
    })
}
private fun colorCopyingRecipes(v: JsonElement, out: JsonFileWriter) {
    val o = v.asJsonObject
    val from = o["from"].asString
    o.remove("from")
    val into = o["into"].asString
    o.remove("into")
    for(col in colors) out("copying_${col}_$into", typedRecipe("flux:copying") {
        keyItemOrTag("source", "${col}_$into")
        keyItemOrTag("material", "${col}_$from")
        for ((k, je) in o.entrySet()) k set je
    })
}

private fun toolRecipes(v: JsonElement, out: JsonFileWriter) {
    val item = v.asString
    val ns = out.namespace
    val mapKeys = mapOf("X" to "$ns:${item}_ingot", "#" to "minecraft:stick")
    for((name, pat) in mapShapes) out("${item}_$name", craftingShaped(pat, mapKeys,1 of "$ns:${item}_$name"))
}

private fun metalTags(v: JsonElement, out: JsonFileWriter) {
    val item = v.asString
    val nsItem = "${out.namespace}:$item"
    if (!isVanilla(item)) {
        if (!isAlloy(item)) {
            val ore = "${nsItem}_ore"
            out.itemBlockTags("ores/$item", singleTag(ore))
        }
        val block = "${nsItem}_block"
        out("items/ingots/$item", singleTag("${nsItem}_ingot"))
        out("items/nuggets/$item", singleTag("${nsItem}_nugget"))
        out.itemBlockTags("storage_blocks/$item", singleTag(block))
    }
    out("items/dusts/$item", singleTag("${nsItem}_dust"))
    if (!isAlloy(item)) out("items/grits/$item", singleTag("${nsItem}_grit"))
    out("items/gears/$item", singleTag("${nsItem}_gear"))
    out("items/plates/$item", singleTag("${nsItem}_plate"))
}

private fun typeTags(v: JsonElement, out: JsonFileWriter) {
    val o = v.asJsonObject
    val tag = o["tag"].asString
    val blocks = o["blocks"].asBoolean
    val items = o["items"].asJsonArray.map{ "#forge:$tag/${it.asString}" }.toTypedArray()
    val tagListFunc = tagList(items)

    out("items/$tag", tagListFunc)
    if (blocks) out("blocks/$tag", tagListFunc)
    when (tag) {
        "storage_blocks" -> {
            out("blocks/supports_beacon", tagListFunc)
            out("../../minecraft/tags/blocks/beacon_base_blocks", tagListFunc)
        }
        "ingots" -> {
            out("items/beacon_payment", tagListFunc)
            out("../../minecraft/tags/items/beacon_payment_items", tagListFunc)
        }
    }
}

private fun containerLootTables(v: JsonElement, out: JsonFileWriter) {
    val item = v.asString
    out("blocks/$item", typedLoot("minecraft:block") {
        pool(
                entries = {
                    typed("minecraft:item") {
                        "functions" arr {
                            obj {
                                "function" set "minecraft:copy_name"
                                "source" set "block_entity"
                            }
                        }
                        "name" set "${out.namespace}:${item}"
                    }
                },
                conditions = condition_survivesExplosion
        )
    })
}

private fun metalLootTables(v: JsonElement, out: JsonFileWriter) {
    val item = v.asString
    val types = if (isAlloy(item)) arrayOf("block") else arrayOf("ore", "block")
    for (typ in types) out("blocks/${item}_$typ", typedLoot("minecraft:block") {
        pool(
                entries = singleEntryItem("${out.namespace}:${item}_$typ"),
                conditions = condition_survivesExplosion
        )
    })
}

private fun blockLootTables(v: JsonElement, out: JsonFileWriter) {
    val item = v.asString
    out("blocks/$item", typedLoot("minecraft:block") {
        pool(
                entries = singleEntryItem("${out.namespace}:$item"),
                conditions = condition_survivesExplosion
        )
    })
}

private fun fluxGifts(v: JsonElement, out: JsonFileWriter) {
    val o = v.asJsonObject
    val name = o["name"].asString
    val table = o["table"].asString
    val box = o["box"].asString.toInt(16)
    val ribbon = o["ribbon"].asString.toInt(16)
    out("gifts/$name", typedLoot("minecraft:gift") {
        pool(
                entries = {
                    typed("minecraft:item") {
                        "name" set "flux:gift"
                        "functions" arr {
                            obj {
                                "function" set "minecraft:set_nbt"
                                "tag" set "{\"Box\":${box},\"Ribbon\":${ribbon},\"LootTable\":\"${table}\"}"
                            }
                        }
                    }
                }
        )
    })
}

private fun JsonFileWriter.itemBlockTags(name: String, fn: JsonFunc) {
    invoke("items/$name", fn)
    invoke("blocks/$name", fn)
}

private fun isVanilla(name: String) = name == "iron" || name == "gold" || name == "netherite" // name == "copper"
private fun isAlloy(name: String) = name == "bronze" || name == "steel" || name == "netherite"
