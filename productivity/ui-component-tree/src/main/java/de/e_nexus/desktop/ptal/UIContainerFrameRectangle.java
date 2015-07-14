package de.e_nexus.desktop.ptal;

import java.awt.Container;
import java.awt.Rectangle;

public abstract class UIContainerFrameRectangle extends Rectangle {

	public UIContainerFrameRectangle(int x, int y, int w, int h) {
		super(x, y, w, h);
	}

	public abstract Container getTarget();
	
	public abstract boolean autoRefresh();
}
