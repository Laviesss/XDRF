package dev.diwer.equipment_rebalance.client;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EquipmentRebalanceClient implements ClientModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("equipment_rebalance.client");

	@Override
	public void onInitializeClient() {
		LOGGER.info("Equipment Rebalance client init");
	}
}
