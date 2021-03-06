package org.egordorichev.lasttry.ui;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Rectangle;

import org.egordorichev.lasttry.input.DefaultInputProcessor;
import org.egordorichev.lasttry.input.InputManager;
import org.egordorichev.lasttry.input.Keys;

public class UiTextInput extends UiTextLabel {
	/**
	 * Current text string
	 */
	protected String text = "";
	/**
	 * Cursor position
	 */
	protected int cursorX = 0;
	/**
	 * Ignore input
	 */
	protected boolean ignoreInput = false;

	public UiTextInput(Rectangle rectangle, Origin origin) {
		super(rectangle, origin, "|");

		InputManager.multiplexer.addProcessor(new DefaultInputProcessor() {
			@Override
			public boolean keyDown(int keycode) {
				if (ignoreInput) {
					return false;
				}

				if (keycode == Keys.ERASE_TEXT && text.length() > 0 && cursorX > 0) {
					StringBuilder builder = new StringBuilder(text);
					builder.deleteCharAt(cursorX - 1);
					text = builder.toString();
					cursorX -= 1;
					updateLabel();
				} else if (keycode == Input.Keys.FORWARD_DEL && cursorX < text.length()) {
					StringBuilder builder = new StringBuilder(text);
					builder.deleteCharAt(cursorX);
					text = builder.toString();
					updateLabel();
				} else if (keycode == Input.Keys.ENTER) {
					onEnter();
				} else if (keycode == Input.Keys.LEFT) {
					cursorX = Math.max(0, cursorX - 1);
					updateLabel();
				} else if (keycode == Input.Keys.RIGHT) {
					cursorX = Math.min(text.length(), cursorX + 1);
					updateLabel();
				}

				return false;
			}

			@Override
			public boolean keyTyped(char character) {
				if (ignoreInput) {
					return false;
				}

				if (Character.isIdentifierIgnorable(character)) {
					return false;
				}

				type(character + "");

				return false;
			}
		});
	}

	public UiTextInput(Rectangle rectangle, Origin origin, DefaultInputProcessor processor) {
		super(rectangle, origin, "|");
		if (processor != null) {
			InputManager.multiplexer.addProcessor(processor);
		}
	}

	public String getText() {
		return this.text;
	}

	public void onEnter() {

	}

	public void setIgnoreInput(boolean ignoreInput) {
		this.ignoreInput = ignoreInput;
	}

	public void type(String text) {
		StringBuilder builder = new StringBuilder(this.text);
		builder.insert(cursorX, text);
		cursorX += 1;
		this.text = builder.toString();

		updateLabel();
	}

	public void clear() {
		this.text = "";
		this.cursorX = 0;
		updateLabel();
	}

	public void setCursorX(int cursorX) {
		this.cursorX = cursorX;
	}

	protected void updateLabel() {
		StringBuilder builder = new StringBuilder(this.text);
		builder.insert(cursorX, '|');
		this.setLabel(builder.toString());
	}
}