package dev.diwer.equipment_rebalance;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EquipmentRebalance implements ModInitializer {
	public static final String MOD_ID = "equipment_rebalance";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Equipment Rebalance initializing (26.3, loader 0.20.1)");
	}
}
