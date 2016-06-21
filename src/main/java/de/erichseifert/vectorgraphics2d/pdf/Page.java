/*
 * VectorGraphics2D: Vector export for Java(R) Graphics2D
 *
 * (C) Copyright 2010-2016 Erich Seifert <dev[at]erichseifert.de>,
 * Michael Seifert <mseifert[at]error-reports.org>
 *
 * This file is part of VectorGraphics2D.
 *
 * VectorGraphics2D is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * VectorGraphics2D is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with VectorGraphics2D.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.erichseifert.vectorgraphics2d.pdf;

import java.awt.geom.Rectangle2D;

/**
 * Represents a page of a PDF document.
 */
class Page implements PDFObject {
	private final Rectangle2D mediaBox;
	private PageTreeNode parent;

	/**
	 * Initializes a {@code Page} with the specified parent node and MediaBox.
	 * @param mediaBox Boundaries of the page.
	 */
	public Page(Rectangle2D mediaBox) {
		this.mediaBox = mediaBox;
	}

	@Override
	public String getType() {
		return "Page";
	}

	public Rectangle2D getMediaBox() {
		return mediaBox;
	}

	/**
	 * Returns the immediate parent of this {@code Page}.
	 * @return Parent node.
	 */
	public PageTreeNode getParent() {
		return parent;
	}

	/**
	 * Sets the parent of this {@code Page} to the specified node.
	 * @param parent Immediate parent.
	 */
	protected void setParent(PageTreeNode parent) {
		this.parent = parent;
	}
}

