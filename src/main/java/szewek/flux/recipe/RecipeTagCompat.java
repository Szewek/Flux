package szewek.flux.recipe;

import com.google.gson.JsonObject;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import szewek.flux.FluxCfg;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public final class RecipeTagCompat {

	public static ItemStack findItemTag(JsonObject json) {
		String tagName = JSONUtils.getAsString(json, "tag");
		ITag<Item> tag = ItemTags.getAllTags().getTag(new ResourceLocation(tagName));
		if (tag != null && !tag.getValues().isEmpty()) {
			Item foundItem = itemFromTagCompat(tag.getValues());
			if (foundItem != null) {
				return new ItemStack(foundItem, JSONUtils.getAsInt(json, "count", 1));
			}
		}
		return ItemStack.EMPTY;
	}

	@Nullable
	public static Item itemFromTagCompat(Collection<Item> items) {
		if (items.isEmpty()) {
			return null;
		}
		final List<? extends String> modCompat = FluxCfg.COMMON.preferModCompat.get();
		if (modCompat.contains("jaopca")) {
			// JAOPCA creates recipes so duplicates cannot be allowed
			return null;
		}
		if (modCompat.isEmpty() || items.size() == 1) {
			return items.iterator().next();
		}
		Item foundItem = null;
		int compatIndex = modCompat.size();
		for (Item item : items) {
			String ns = Objects.requireNonNull(item.getRegistryName()).getNamespace();
			int n = modCompat.indexOf(ns);
			if (n != -1 && n < compatIndex) {
				foundItem = item;
				compatIndex = n;
			}
			if (compatIndex == 0) {
				break;
			}
		}
		if (foundItem == null) {
			foundItem = items.iterator().next();
		}
		return foundItem;
	}

	private RecipeTagCompat() {}
}
