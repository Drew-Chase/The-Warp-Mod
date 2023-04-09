package chase.minecraft.architectury.warpmod.client.gui;


import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

public class GUIFactory
{
	/**
	 * This function creates a button with specified dimensions, label, and press action.
	 *
	 * @param x           The x-coordinate of the button's top-left corner.
	 * @param y           The y parameter represents the vertical position of the button on the screen or component. It is usually measured in pixels from the top of the screen or component.
	 * @param width       The width parameter is an integer value that represents the desired width of the button in pixels.
	 * @param height      The height parameter is an integer value that represents the height of the button in pixels.
	 * @param label       The label parameter is a Component object that represents the text or image that will be displayed on the button. It can be a simple text string or a more complex graphical element.
	 * @param pressAction `pressAction` is a functional interface that defines the action to be performed when the button is pressed. It typically takes no arguments and returns no value. The implementation of this interface is passed as a lambda expression or method reference when creating the button.
	 * @return A Button object is being returned.
	 */
	public static Button createButton(int x, int y, int width, int height, Component label, Button.OnPress pressAction)
	{
		return Button.builder(label, pressAction).bounds(x, y, width, height).build();
	}
	
	public static EditBox createTextBox(Font font, int x, int y, int width, int height, Component label)
	{
		return new EditBox(font, x, y, width, height, label);
	}
	public static EditBox createNumbersTextBox(Font font, int x, int y, int width, int height, Component label)
	{
		EditBox box = new EditBox(font, x, y, width, height, label);
		box.setFilter(f->{
			if(f.isEmpty())
				return true;
			try{
				Double.parseDouble(f);
				return true;
			}catch(NumberFormatException e){
				return false;
			}
		});
		return box;
	}
	
}
