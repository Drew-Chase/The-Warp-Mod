package chase.minecraft.architectury.warpmod.client.gui;

import chase.minecraft.architectury.warpmod.client.ClientWarps;
import chase.minecraft.architectury.warpmod.client.WarpModClient;
import chase.minecraft.architectury.warpmod.networking.WarpNetworking;
import com.mojang.blaze3d.vertex.PoseStack;
import io.netty.buffer.Unpooled;
import lol.bai.badpackets.api.PacketSender;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static chase.minecraft.architectury.warpmod.client.gui.GUIFactory.*;

public class EditWarpScreen extends Screen
{
	@Nullable
	private final Screen _parent;
	private final ClientWarps.ClientWarp _warp;
	@NotNull
	private final LocalPlayer player;
	private final String ogName;
	private EditBox _nameBox, _xBox, _yBox, _zBox, _pitchBox, _yawBox;
	private String dimension;
	private Button _saveButton;
	
	public EditWarpScreen(@Nullable Screen parent, @NotNull ClientWarps.ClientWarp warp)
	{
		super(Component.translatable("warpmod.edit.title"));
		_parent = parent;
		_warp = warp;
		assert Minecraft.getInstance().player != null;
		player = Minecraft.getInstance().player;
		dimension = warp.dimension().toString();
		ogName = warp.name();
	}
	
	public EditWarpScreen(@Nullable Screen parent)
	{
		super(Component.translatable("warpmod.create"));
		_parent = parent;
		assert Minecraft.getInstance().player != null;
		player = Minecraft.getInstance().player;
		_warp = new ClientWarps.ClientWarp("", player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot(), player.level.dimension().location());
		dimension = player.level.dimension().location().toString();
		ogName = "";
	}
	
	@Override
	protected void init()
	{
		int inputWidth = Mth.clamp(width / 3, 100, 200);
		int inputPadding = 20;
		_nameBox = createTextBox(font, (width / 2) - 200 / 2, height / 4, 200, 20, Component.translatable("warpmod.edit.name"));
		_nameBox.setValue(_warp.name());
		setInitialFocus(_nameBox);
		addRenderableWidget(_nameBox);
		
		_xBox = createNumbersTextBox(font, (width / 2) - inputWidth - inputPadding, (height / 4) + 40, inputWidth, 20, Component.translatable("warpmod.edit.x"));
		_xBox.setValue(Double.toString((int) (_warp.x() * 100) / 100d));
		addRenderableWidget(_xBox);
		
		_yBox = createNumbersTextBox(font, (width / 2) - inputWidth - inputPadding, (height / 4) + 80, inputWidth, 20, Component.translatable("warpmod.edit.y"));
		_yBox.setValue(Double.toString((int) (_warp.y() * 100) / 100d));
		addRenderableWidget(_yBox);
		
		_zBox = createNumbersTextBox(font, (width / 2) - inputWidth - inputPadding, (height / 4) + 120, inputWidth, 20, Component.translatable("warpmod.edit.z"));
		_zBox.setValue(Double.toString((int) (_warp.z() * 100) / 100d));
		addRenderableWidget(_zBox);
		
		_pitchBox = createNumbersTextBox(font, (width / 2) + inputPadding, (height / 4) + 40, inputWidth, 20, Component.translatable("warpmod.edit.pitch"));
		_pitchBox.setValue(Double.toString((int) (_warp.pitch() * 100) / 100d));
		addRenderableWidget(_pitchBox);
		
		_yawBox = createNumbersTextBox(font, (width / 2) + inputPadding, (height / 4) + 80, inputWidth, 20, Component.translatable("warpmod.edit.yaw"));
		_yawBox.setValue(Float.toString((int) (_warp.yaw() * 10000) / 10000f));
		addRenderableWidget(_yawBox);
		
		CycleButton<Object> _dimButton = addRenderableWidget(CycleButton.builder((mode) ->
				{
					for (String dimension : WarpModClient.dimensions)
					{
						ResourceLocation dim = new ResourceLocation(dimension);
						if (dim.toString().equals(mode))
						{
							dimension = dim.toString();
							return Component.literal(dim.getPath());
						}
					}
					
					dimension = player.level.dimension().location().toString();
					return Component.literal(player.level.dimension().location().getPath());
				})
				.withValues(WarpModClient.dimensions)
				.displayOnlyValue()
				.withInitialValue(dimension)
				.create((width / 2) + 20, (height / 4) + 120, inputWidth, 20, Component.translatable("warpmod.edit.dim"), (cycleButton, mode) ->
				{
					this.dimension = (String) mode;
				}));
		
		
		_saveButton = addRenderableWidget(createButton((width / 2) - 110, height - 30, 100, 20, Component.translatable("warpmod.edit.save"), button ->
		{
			boolean ok = true;
			String name = _nameBox.getValue();
			if (name.isEmpty()) ok = false;
			
			double x = 0;
			double y = 0;
			double z = 0;
			float pitch = 0;
			float yaw = 0;
			
			try
			{
				
				if (_nameBox.getValue().isEmpty())
				{
					_nameBox.setSuggestion("Totally Cool Warp Name");
				} else
				{
					_nameBox.setSuggestion("");
				}
				
				if (_yawBox.getValue().isEmpty()) yaw = player.getXRot();
				else yaw = Float.parseFloat(_yawBox.getValue());
				
				if (_pitchBox.getValue().isEmpty()) pitch = player.getYRot();
				else pitch = Float.parseFloat(_pitchBox.getValue());
				
				if (_zBox.getValue().isEmpty()) z = player.getZ();
				else z = Double.parseDouble(_zBox.getValue());
				
				if (_yBox.getValue().isEmpty()) y = player.getY();
				else y = Double.parseDouble(_yBox.getValue());
				
				if (_xBox.getValue().isEmpty()) x = player.getX();
				else x = Double.parseDouble(_xBox.getValue());
				
			} catch (NumberFormatException e)
			{
				ok = false;
			}
			
			if (ok)
			{
				// Create NBT from inputs
				CompoundTag tag = new CompoundTag();
				tag.putString("ogName", ogName);
				tag.putString("name", name);
				tag.putDouble("x", x);
				tag.putDouble("y", y);
				tag.putDouble("z", z);
				tag.putFloat("pitch", pitch);
				tag.putFloat("yaw", yaw);
				tag.putString("dim", dimension);
				
				// Create packet buffer and send to server
				FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
				buf.writeNbt(tag);
				PacketSender.c2s().send(WarpNetworking.CREATE, buf);
				
				// Return to parent screen
				assert minecraft != null;
				minecraft.setScreen(_parent);
			}
		}));
		addRenderableWidget(createButton((width / 2) + 10, height - 30, 100, 20, CommonComponents.GUI_CANCEL, button ->
		{
			assert minecraft != null;
			minecraft.setScreen(_parent);
		}));
		super.init();
	}
	
	@Override
	public void render(@NotNull PoseStack matrixStack, int x, int y, float partialTicks)
	{
		renderBackground(matrixStack);
		drawCenteredString(matrixStack, this.font, getTitle(), width / 2, font.lineHeight, ChatFormatting.WHITE.getColor());
		drawString(matrixStack, font, _nameBox.getMessage(), _nameBox.getX(), _nameBox.getY() - font.lineHeight - 5, ChatFormatting.GRAY.getColor());
		drawString(matrixStack, font, _xBox.getMessage(), _xBox.getX(), _xBox.getY() - font.lineHeight - 5, ChatFormatting.GRAY.getColor());
		drawString(matrixStack, font, _yBox.getMessage(), _yBox.getX(), _yBox.getY() - font.lineHeight - 5, ChatFormatting.GRAY.getColor());
		drawString(matrixStack, font, _zBox.getMessage(), _zBox.getX(), _zBox.getY() - font.lineHeight - 5, ChatFormatting.GRAY.getColor());
		drawString(matrixStack, font, _pitchBox.getMessage(), _pitchBox.getX(), _pitchBox.getY() - font.lineHeight - 5, ChatFormatting.GRAY.getColor());
		drawString(matrixStack, font, _yawBox.getMessage(), _yawBox.getX(), _yawBox.getY() - font.lineHeight - 5, ChatFormatting.GRAY.getColor());
		
		if (_nameBox.getValue().isEmpty())
		{
			_saveButton.active = false;
			_nameBox.setSuggestion("Totally Cool Warp Name");
		} else
		{
			_saveButton.active = true;
			_nameBox.setSuggestion("");
		}
		
		if (_yawBox.getValue().isEmpty()) _yawBox.setSuggestion(Float.toString((int) (player.getXRot() * 10000) / 10000f));
		else _yawBox.setSuggestion("");
		
		if (_pitchBox.getValue().isEmpty()) _pitchBox.setSuggestion(Float.toString((int) (player.getY() * 10000) / 10000f));
		else _pitchBox.setSuggestion("");
		
		if (_zBox.getValue().isEmpty()) _zBox.setSuggestion(Double.toString((int) (player.getZ() * 100) / 100d));
		else _zBox.setSuggestion("");
		
		if (_yBox.getValue().isEmpty()) _yBox.setSuggestion(Double.toString((int) (player.getY() * 100) / 100d));
		else _yBox.setSuggestion("");
		
		if (_xBox.getValue().isEmpty()) _xBox.setSuggestion(Double.toString((int) (player.getX() * 100) / 100d));
		else _xBox.setSuggestion("");
		
		super.render(matrixStack, x, y, partialTicks);
	}
	
	
}
