package dev.jaxydog.astral.utility;

import dev.jaxydog.astral.utility.register.Registerable;
import java.util.HashMap;
import java.util.function.BiFunction;
import net.minecraft.util.DyeColor;

/**
 * Utility class for auto-generating values for each dye color
 * ---
 * This is similar to how dyed amethyst blocks are generated within the old Moonlight mod, only made to be much more generic
 */
public class DyeableSet<T extends Registerable> implements Registerable.Client, Registerable.Main, Registerable.Server {

	/** The set's internal map that contains the created value for each dye color */
	private final HashMap<DyeColor, T> MAP = new HashMap<>(DyeColor.values().length);

	public DyeableSet(String baseRawId, BiFunction<String, DyeColor, T> constructor) {
		for (var color : DyeColor.values()) {
			var id = color.asString() + "_" + baseRawId;
			var value = constructor.apply(id, color);

			this.MAP.put(color, value);
		}
	}

	/** Returns the value associated with the given color */
	public T get(DyeColor color) {
		return this.MAP.get(color);
	}

	@Override
	public String getRawId() {
		throw new UnsupportedOperationException("the 'DyeableSet' class does not have its own identifier");
	}

	@Override
	public void registerClient() {
		for (var value : this.MAP.values()) {
			if (value instanceof Registerable.Client client) {
				client.registerClient();
			}
		}
	}

	@Override
	public void registerMain() {
		for (var value : this.MAP.values()) {
			if (value instanceof Registerable.Main main) {
				main.registerMain();
			}
		}
	}

	@Override
	public void registerServer() {
		for (var value : this.MAP.values()) {
			if (value instanceof Registerable.Server server) {
				server.registerServer();
			}
		}
	}
}
