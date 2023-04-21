package chase.minecraft.architectury.warpmod.client.gui.screen;

import chase.minecraft.architectury.warpmod.WarpMod;
import chase.minecraft.architectury.warpmod.client.WarpModClient;
import chase.minecraft.architectury.warpmod.client.gui.component.ColorButton;
import chase.minecraft.architectury.warpmod.client.gui.component.DropdownWidget;
import chase.minecraft.architectury.warpmod.client.gui.waypoint.WaypointColor;
import chase.minecraft.architectury.warpmod.data.Warp;
import chase.minecraft.architectury.warpmod.data.Warps;
import chase.minecraft.architectury.warpmod.data.WaypointIcons;
import chase.minecraft.architectury.warpmod.networking.WarpNetworking;
import chase.minecraft.architectury.warpmod.utils.WorldUtils;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import io.netty.buffer.Unpooled;
import lol.bai.badpackets.api.PacketSender;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;

import static chase.minecraft.architectury.warpmod.client.gui.GUIFactory.*;

public class EditWarpScreen extends Screen
{
	@Nullable
	private final Screen _parent;
	private final Warp warp;
	@NotNull
	private final Player player;
	private final String ogName;
	private EditBox _nameBox;
	private EditBox _xBox;
	private EditBox _yBox;
	private EditBox _zBox;
	private EditBox _pitchBox;
	private EditBox _yawBox;
	private String icon;
	private ResourceLocation dimension;
	private Button _saveButton;
	private ColorButton _colorButton;
	private DropdownWidget<ResourceLocation> _dimensionButton;
	private DropdownWidget<String> _iconButton;
	private WaypointColor color = WaypointColor.WHITE;
	private boolean visible = true;
	
	public EditWarpScreen(@Nullable Screen parent, @NotNull Warp warp)
	{
		super(Component.translatable("warpmod.edit.title"));
		_parent = parent;
		this.warp = warp;
		assert Minecraft.getInstance().player != null;
		player = Minecraft.getInstance().player;
		dimension = warp.getDimension();
		ogName = warp.getName();
		color = warp.getColor();
		this.icon = warp.getIcon().getPath();
		this.visible = warp.visible();
	}
	
	public EditWarpScreen(@Nullable Screen parent)
	{
		super(Component.translatable("warpmod.create"));
		_parent = parent;
		assert Minecraft.getInstance().player != null;
		player = Minecraft.getInstance().player;
		warp = new Warp("", player.getX(), player.getEyeY(), player.getZ(), player.getYRot(), player.getXRot(), player.level.dimension().location(), player);
		dimension = player.level.dimension().location();
		ogName = "";
		this.icon = warp.getIcon().getPath();
		
		Random random = new Random();
		int index = random.nextInt(0, WaypointColor.values().length - 1);
		this.color = WaypointColor.values()[index];
		this.visible = warp.visible();
	}
	
	@Override
	protected void init()
	{
		int inputWidth = 50;
		int inputPadding = 20;
		_nameBox = createTextBox(font, (width / 2) - 200 / 2, height / 4, 200, 20, Component.translatable("warpmod.edit.name"));
		_nameBox.setValue(warp.getName());
		setInitialFocus(_nameBox);
		addRenderableWidget(_nameBox);
		
		
		_xBox = createNumbersTextBox(font, 0, 0, inputWidth, 20, Component.translatable("warpmod.edit.x"));
		_xBox.setValue(Double.toString((int) (warp.getX() * 100) / 100d));
		
		_yBox = createNumbersTextBox(font, 0, 0, inputWidth, 20, Component.translatable("warpmod.edit.y"));
		_yBox.setValue(Double.toString((int) (warp.getY() * 100) / 100d));
		
		
		_zBox = createNumbersTextBox(font, 0, 0, inputWidth, 20, Component.translatable("warpmod.edit.z"));
		_zBox.setValue(Double.toString((int) (warp.getZ() * 100) / 100d));
		
		_pitchBox = createNumbersTextBox(font, 0, 0, inputWidth, 20, Component.translatable("warpmod.edit.pitch"));
		_pitchBox.setValue(Double.toString((int) (warp.getPitch() * 100) / 100d));
		
		_yawBox = createNumbersTextBox(font, 0, 0, inputWidth, 20, Component.translatable("warpmod.edit.yaw"));
		_yawBox.setValue(Float.toString((int) (warp.getYaw() * 10000) / 10000f));
		
		
		ImmutableList<AbstractWidget> row = ImmutableList.of(_xBox, _yBox, _zBox, _pitchBox, _yawBox);
		
		int currentX = (width / 2) - (inputWidth * 3) - inputPadding;
		int currentY = (height / 4) + 40;
		for (AbstractWidget col : row)
		{
			col.setX(currentX);
			col.setY(currentY);
			addRenderableWidget(col);
			currentX += inputWidth + inputPadding;
		}
		_nameBox.setX(_xBox.getX());
		
		addRenderableWidget(createButton(_nameBox.getX() + _nameBox.getWidth() + 5, _nameBox.getY(), 100, _nameBox.getHeight(), Component.translatable("warpmod.edit.reset"), button ->
		{
			_xBox.setValue(Double.toString((int) (player.getX() * 100) / 100d));
			_yBox.setValue(Double.toString((int) (player.getEyeY() * 100) / 100d));
			_zBox.setValue(Double.toString((int) (player.getZ() * 100) / 100d));
			_pitchBox.setValue(Double.toString((int) (player.getYRot() * 100) / 100d));
			_yawBox.setValue(Float.toString((int) (player.getXRot() * 1000) / 1000f));
		}));
		
		initCycleButtons();
		
		row = ImmutableList.of(_dimensionButton, _iconButton, _colorButton);
		inputWidth = 100;
		currentY = (height / 4) + 80;
		currentX = (int) ((width / 2) - (inputWidth * 1.5) - inputPadding);
		for (AbstractWidget col : row)
		{
			col.setX(currentX);
			col.setY(currentY);
			col.setWidth(inputWidth);
			addRenderableWidget(col);
			currentX += inputWidth + inputPadding;
		}
		
		_saveButton = addRenderableWidget(createButton((width / 2) - 110, height - 30, 100, 20, Component.translatable("warpmod.edit.save"), button ->
		{
			save();
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
		drawString(matrixStack, font, Component.translatable("warpmod.edit.dim"), _dimensionButton.getX(), _dimensionButton.getY() - font.lineHeight - 5, ChatFormatting.GRAY.getColor());
		drawString(matrixStack, font, Component.translatable("warpmod.edit.icon"), _iconButton.getX(), _iconButton.getY() - font.lineHeight - 5, ChatFormatting.GRAY.getColor());
		drawString(matrixStack, font, Component.translatable("warpmod.edit.color"), _colorButton.getX(), _colorButton.getY() - font.lineHeight - 5, ChatFormatting.GRAY.getColor());
		
		if (_nameBox.getValue().isEmpty())
		{
			_saveButton.active = false;
			_nameBox.setSuggestion("Totally Cool Warp Name");
		} else
		{
			_saveButton.active = true;
			_nameBox.setSuggestion("");
		}
		
		if (_yawBox.getValue().isEmpty()) _yawBox.setSuggestion(Float.toString((int) (player.getXRot() * 10000) / 100f));
		else _yawBox.setSuggestion("");
		
		if (_pitchBox.getValue().isEmpty()) _pitchBox.setSuggestion(Float.toString((int) (player.getEyeY() * 10000) / 100f));
		else _pitchBox.setSuggestion("");
		
		if (_zBox.getValue().isEmpty()) _zBox.setSuggestion(Double.toString((int) (player.getZ() * 100) / 100d));
		else _zBox.setSuggestion("");
		
		if (_yBox.getValue().isEmpty()) _yBox.setSuggestion(Double.toString((int) (player.getY() * 100) / 100d));
		else _yBox.setSuggestion("");
		
		if (_xBox.getValue().isEmpty()) _xBox.setSuggestion(Double.toString((int) (player.getX() * 100) / 100d));
		else _xBox.setSuggestion("");
		
		super.render(matrixStack, x, y, partialTicks);
	}
	
	private void initCycleButtons()
	{
		HashMap<ResourceLocation, String> dimensions = new HashMap<>();
		for (int i = 0; i < WarpModClient.dimensions.length; i++)
		{
			ResourceLocation dimension = new ResourceLocation(WarpModClient.dimensions[i]);
			
			dimensions.put(dimension, WorldUtils.getLevelName(dimension));
		}
		_dimensionButton = new DropdownWidget<ResourceLocation>(0, 0, 100, 20, this.dimension, dimensions, value ->
		{
			this.dimension = value;
		});
		
		_iconButton = new DropdownWidget<>(0, 0, 100, 20, WaypointIcons.getName(WarpMod.id(this.icon)), WaypointIcons.names(), value ->
		{
			this.icon = Objects.requireNonNull(WaypointIcons.getByName(value)).getPath();
		});
		
		
		// Create color cycle button
		_colorButton = new ColorButton(0, 0, 100, 20, this.color, color ->
		{
			this.color = color;
		});
	}
	
	private Component getDimensionTooltip()
	{
		MutableComponent tooltip = Component.translatable("warpmod.edit.dim");
		for (String dimensions : WarpModClient.dimensions)
		{
			String dim = new ResourceLocation(dimensions).getPath().replaceAll("_", " ").toUpperCase();
			tooltip.append("\n");
			tooltip.append("%s%s%s".formatted(ChatFormatting.GREEN, dim, ChatFormatting.GOLD));
			if (player.level.dimension().location().toString().equalsIgnoreCase(dimensions))
			{
				tooltip.append(" - AUTO");
			}
		}
		return tooltip;
	}
	
	private Component getColorTooltip()
	{
		String[] colors = WaypointColor.getColorNames();
		MutableComponent tooltip = Component.translatable("warpmod.edit.color");
		for (String color : colors)
		{
			tooltip.append("\n");
			tooltip.append("%s%s%s".formatted(ChatFormatting.getByName(color), color.toUpperCase().replaceAll("_", " "), ChatFormatting.GOLD));
			if (Objects.equals(ChatFormatting.getByName(color), this.color))
			{
				tooltip.append(" - SELECTED");
			}
		}
		return tooltip;
	}
	
	private Component getIconTooltip()
	{
		
		MutableComponent tooltip = Component.translatable("warpmod.edit.icon");
		for (String icon : WaypointIcons.names())
		{
			tooltip.append("\n");
			tooltip.append("%s%s%s".formatted(ChatFormatting.GREEN, icon, ChatFormatting.GOLD));
			if (WaypointIcons.getName(WarpMod.id(this.icon)).equalsIgnoreCase(icon))
			{
				tooltip.append(" - SELECTED");
			}
		}
		return tooltip;
	}
	
	private void save()
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
			Warp warp = new Warp(name, x, y, z, pitch, yaw, dimension, player, false, WarpMod.id(icon), this.color, this.visible);
			if (WarpModClient.onServer)
			{
				// Create packet buffer and send to server
				FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
				buf.writeInt(ogName.length());
				buf.writeCharSequence(ogName, Charset.defaultCharset());
				buf.writeNbt(warp.toNbt());
				PacketSender.c2s().send(WarpNetworking.CREATE, buf);
				
			} else
			{
				Warps warps = Warps.fromPlayer(player);
				if (warps.exists(ogName))
				{
					warps.rename(ogName, name);
				}
				warps.createAddOrUpdate(warp);
			}
			
			// Return to parent screen
			assert minecraft != null;
			minecraft.setScreen(_parent);
		}
	}
	
	
}
