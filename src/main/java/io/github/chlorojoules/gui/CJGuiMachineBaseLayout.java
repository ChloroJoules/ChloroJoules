package io.github.chlorojoules.gui;

public class CJGuiMachineBaseLayout {
	// Game slot hitbox size.
	private static final int SLOT_WIDTH = 16;
	private static final int SLOT_HEIGHT = 16;

	//public static final int BASE_WIDTH = 176;
	public static final int BASE_HEIGHT = 166;

	public static final int PLAYER_INVENTORY_ROWS = 3;
	public static final int PLAYER_INVENTORY_COLUMNS = 9;
	public static final int PLAYER_INVENTORY_X = 8;
	public static final int PLAYER_INVENTORY_Y = 84;
	public static final int PLAYER_INVENTORY_HOTBAR_Y = 142;

	public static final int TOOLTIP_OFFSET_X = 12;
	public static final int TOOLTIP_OFFSET_Y = -12;

	public static final int TOOLTIP_BORDER = 3;
	public static final int TOOLTIP_HEIGHT = 12;
	public static final int TOOLTIP_DESCRIPTION_HEIGHT = 14;
	public static final int TOOLTIP_DESCRIPTION_OFFSET = 15;

	public static final int INVENTORY_LABEL_X = 8;
	public static final int INVENTORY_LABEL_Y = BASE_HEIGHT - 96 + 2;

	// NOTE: Positional information from `resources/cj_machinebase.xcf` image
	//       Source split by layer.

	// Area where machine GUI elements should be placed.
	public static final int WORKING_WIDTH = 176;
	public static final int WORKING_HEIGHT = 86;

	public static final int PROGRESS_FULL_X = 176;
	public static final int PROGRESS_FULL_Y = 0;

	public static final int PROGRESS_EMPTY_X = 176;
	public static final int PROGRESS_EMPTY_Y = 17;

	public static final int PROGRESS_WIDTH = 24;
	public static final int PROGRESS_HEIGHT = 17;

	public static final int RIFT_VIEW_X = 176;
	public static final int RIFT_VIEW_Y = 151;

	public static final int RIFT_VIEW_WIDTH = 63;
	public static final int RIFT_VIEW_HEIGHT = 63;

	public static final int SLOT_IN_X = 176;
	public static final int SLOT_IN_Y = 34;

	public static final int SLOT_JEWEL_X = 202;
	public static final int SLOT_JEWEL_Y = 52;

	public static final int SLOT_FERTILIZER_X = 202;
	public static final int SLOT_FERTILIZER_Y = 70;

	public static final int SLOT_PASTE_X = 202;
	public static final int SLOT_PASTE_Y = 88;

	public static final int SLOT_OTHERWORLD_X = 202;
	public static final int SLOT_OTHERWORLD_Y = 106;

	public static final int SLOT_IN_WIDTH = 18;
	public static final int SLOT_IN_HEIGHT = 18;

	public static final int SLOT_IN_OFFSET_X =
			(SLOT_IN_WIDTH - SLOT_WIDTH) / 2;

	public static final int SLOT_IN_OFFSET_Y =
			(SLOT_IN_HEIGHT - SLOT_HEIGHT) / 2;

	public static final int SLOT_OUT_X = 176;
	public static final int SLOT_OUT_Y = 52;

	public static final int SLOT_OUT_WIDTH = 26;
	public static final int SLOT_OUT_HEIGHT = 26;

	public static final int SLOT_OUT_OFFSET_X =
			(SLOT_OUT_WIDTH - SLOT_WIDTH) / 2;

	public static final int SLOT_OUT_OFFSET_Y =
			(SLOT_OUT_HEIGHT - SLOT_HEIGHT) / 2;

	public static final int FUEL_TANK_INSET = 18;
	public static final int JEWEL_SLOT_INSET_X = 30;
	public static final int JEWEL_SLOT_INSET_Y = 33;

	public static final int FLUID_FULL_X = 176;
	public static final int FLUID_FULL_Y = 78;

	public static final int FLUID_EMPTY_X = 188;
	public static final int FLUID_EMPTY_Y = 78;

	public static final int FLUID_WIDTH = 12;
	public static final int FLUID_HEIGHT = 53;

	public static final int FLUID_OFFSET_X = 1;
	public static final int FLUID_OFFSET_Y = 1;

	public static final int STATUS_OK_X = 194;
	public static final int STATUS_OK_Y = 34;

	public static final int STATUS_ERROR_X = 194;
	public static final int STATUS_ERROR_Y = 43;

	public static final int STATUS_WARNING_X = 203;
	public static final int STATUS_WARNING_Y = 34;

	public static final int STATUS_INFO_X = 203;
	public static final int STATUS_INFO_Y = 43;

	public static final int STATUS_WIDTH = 9;
	public static final int STATUS_HEIGHT = 9;

	public static final int BUTTON_INACTIVE_X = 176;
	public static final int BUTTON_INACTIVE_Y = 131;

	public static final int BUTTON_ACTIVE_X = 196;
	public static final int BUTTON_ACTIVE_Y = 131;

	public static final int BUTTON_WIDTH = 20;
	public static final int BUTTON_HEIGHT = 20;

	public static final int BUTTON_LABEL_INSET = 2;

	public static final int INFO_X = INVENTORY_LABEL_X;
	public static final int INFO_Y = 5;

	public static final int STATUS_X = INFO_X + (STATUS_WIDTH * 3) / 2;
	public static final int STATUS_Y = INFO_Y;
}
