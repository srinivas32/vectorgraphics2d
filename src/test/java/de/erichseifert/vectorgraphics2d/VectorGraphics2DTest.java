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
package de.erichseifert.vectorgraphics2d;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import de.erichseifert.vectorgraphics2d.intermediate.commands.Command;
import de.erichseifert.vectorgraphics2d.intermediate.commands.CreateCommand;
import de.erichseifert.vectorgraphics2d.intermediate.commands.DisposeCommand;
import de.erichseifert.vectorgraphics2d.util.GraphicsUtils;
import de.erichseifert.vectorgraphics2d.util.PageSize;

@RunWith(Theories.class)
public class VectorGraphics2DTest {
	private static class MockProcessor implements Processor {
		private final List<Command<?>> commands;

		public MockProcessor() {
			commands = new LinkedList<Command<?>>();
		}

		@Override
		public Document getDocument() {
			return null;
		}

		@Override
		public void add(Command<?> command) {
			commands.add(command);
		}

		@Override
		public Iterable<Command<?>> getCommands() {
			return commands;
		}
	}

	private static class DummyVectorGraphics2D extends VectorGraphics2D {
		public DummyVectorGraphics2D() {
			super(new MockProcessor());
		}
	}

	@Test
	public void testVectorGraphics2DEmitsCreateCommand() {
		VectorGraphics2D g = new DummyVectorGraphics2D();
		Iterable<Command<?>> commands = g.getProcessor().getCommands();
		Iterator<Command<?>> commandIterator = commands.iterator();
		assertTrue(commandIterator.hasNext());

		Command<?> firstCommand = commandIterator.next();
		assertTrue(firstCommand instanceof CreateCommand);
		assertEquals(g, ((CreateCommand) firstCommand).getValue());
	}

	@Test
	public void testCreateEmitsCreateCommand() {
		VectorGraphics2D g = new DummyVectorGraphics2D();
		Iterable<Command<?>> gCommands = g.getProcessor().getCommands();
		Iterator<Command<?>> gCommandIterator = gCommands.iterator();
		CreateCommand gCreateCommand = (CreateCommand) gCommandIterator.next();

		VectorGraphics2D g2 = (VectorGraphics2D) g.create();
		CreateCommand g2CreateCommand = null;
		for (Command<?> g2Command : g2.getProcessor().getCommands()) {
			if (g2Command instanceof CreateCommand) {
				g2CreateCommand = (CreateCommand) g2Command;
			}
		}
		assertNotEquals(gCreateCommand, g2CreateCommand);
		assertEquals(g2, g2CreateCommand.getValue());
	}

	@Test
	public void testDisposeCommandEmitted() {
		Graphics2D g = new DummyVectorGraphics2D();
		g.setColor(Color.RED);

		Graphics2D g2 = (Graphics2D) g.create();
		g2.setColor(Color.BLUE);
		g2.dispose();

		Iterable<Command<?>> commands = ((VectorGraphics2D) g).getProcessor().getCommands();
		Command<?> lastCommand = null;
		for (Command<?> command : commands) {
			lastCommand = command;
		}

		assertTrue(lastCommand instanceof DisposeCommand);
		assertEquals(Color.BLUE, ((DisposeCommand) lastCommand).getValue().getColor());
	}

	@Test
	public void testClipIntersectsClipRectangle() {
		VectorGraphics2D vg2d = new DummyVectorGraphics2D();
		Rectangle2D currentClipShape = new Rectangle2D.Double(5, 10, 20, 30);
		vg2d.setClip(currentClipShape);
		Rectangle2D newClipShape = new Rectangle2D.Double(10, 20, 30, 40);

		vg2d.clip(newClipShape);

		Rectangle2D intersection = currentClipShape.createIntersection(newClipShape);
		assertTrue(GraphicsUtils.equals(vg2d.getClip(), intersection));
	}

	@Test
	public void testClipClearsClippingShapeWhenNullIsPassed() {
		VectorGraphics2D vg2d = new DummyVectorGraphics2D();
		Rectangle2D clipShape = new Rectangle2D.Double(5, 10, 20, 30);
		vg2d.setClip(clipShape);

		vg2d.clip(null);

		assertThat(vg2d.getClip(), is(nullValue()));
	}

	@Test
	public void testSetBackgroundSetsBackgroundColor() {
		VectorGraphics2D vg2d = new DummyVectorGraphics2D();
		Color backgroundColor = Color.DARK_GRAY;

		vg2d.setBackground(backgroundColor);

		assertThat(vg2d.getBackground(), is(backgroundColor));
	}

	@Test(expected = NullPointerException.class)
	public void testThrowsNullPointerExceptionWhenPageSizeIsNull() {
		String format = "svg";
		PageSize pageSize = null;

		new VectorGraphics2D.Builder(format, pageSize);
	}

	@Test(expected = NullPointerException.class)
	public void testThrowsNullPointerExceptionWhenFormatIsNull() {
		String format = null;
		PageSize pageSize = PageSize.A4;

		new VectorGraphics2D.Builder(format, pageSize);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testThrowsIllegalArgumentExceptionWhenFormatIsUnkown() {
		String format = "UnknownFormat";
		PageSize pageSize = PageSize.A4;

		new VectorGraphics2D.Builder(format, pageSize);
	}

	@DataPoints
	public static List<String> KNOWN_FORMATS = Arrays.asList("eps", "pdf", "svg");

	@Theory
	public void testInitializesBuilderWhenFormatIsKnown(String format) {
		PageSize pageSize = PageSize.A4;

		VectorGraphics2D.Builder builder = new VectorGraphics2D.Builder(format, pageSize);

		assertNotNull(builder);
	}

	@Theory
	public void testBuildReturnsVG2DObjectThatIsNotNull(String format) {
		PageSize pageSize = PageSize.A4;
		VectorGraphics2D.Builder builder = new VectorGraphics2D.Builder(format, pageSize);

		VectorGraphics2D vg2d = builder.build();

		assertNotNull(vg2d);
	}

	@Test(expected = IllegalStateException.class)
	public void testBuildThrowsIllegalStateExceptionWhenCompressionIsEnabledForEPS() {
		String format = "eps";
		PageSize pageSize = PageSize.A4;
		VectorGraphics2D.Builder builder = new VectorGraphics2D.Builder(format, pageSize).compressed(true);

		builder.build();
	}

	@Test(expected = IllegalStateException.class)
	public void testBuildThrowsIllegalStateExceptionWhenCompressionIsEnabledForSVG() {
		String format = "svg";
		PageSize pageSize = PageSize.A4;
		VectorGraphics2D.Builder builder = new VectorGraphics2D.Builder(format, pageSize).compressed(true);

		builder.build();
	}
}
