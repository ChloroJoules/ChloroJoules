package io.github.chlorojoules.gui;

public class CJGuiCoordinate extends CJGuiElement {
	public String label;

	public CJGuiCoordinate(int x, int y, String label) {
		super(x, y);

		this.label = label;
	}
}
