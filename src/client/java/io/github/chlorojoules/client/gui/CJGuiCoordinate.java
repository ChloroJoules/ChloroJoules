package io.github.chlorojoules.client.gui;

public class CJGuiCoordinate extends CJGuiElement {
	public String label;

	public CJGuiCoordinate(int x, int y, String label) {
		super(x, y);

		this.label = label;
	}
}
