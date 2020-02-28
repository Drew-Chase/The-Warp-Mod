package com.drewchaseproject.forge.WarpMod.Objects;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.PlayerEntity;

public class Warps {

	private List<Warp> warps = new ArrayList<Warp>();
	private String[] subcommands = new String[] { "set", "random", "map", "list", "help", "reload", "me", "remove", "rename", "invite", "accept" };

	public List<Warp> getWarps(PlayerEntity player) {
		List<Warp> value = new ArrayList<Warp>();
		for (Warp warp : warps) {
			if (warp.getPlayer().equals(player))
				value.add(warp);
		}
		return value;
	}

	public List<Warp> getWarps(String player) {
		List<Warp> value = new ArrayList<Warp>();
		for (Warp warp : warps) {
			if (warp.getPlayer().getDisplayName().getString().equalsIgnoreCase(player))
				value.add(warp);
		}
		return value;
	}

	/**
	 * 
	 * @return a List of Warps
	 */
	public List<Warp> getWarps() {
		return warps;
	}

	/**
	 * 
	 * @param index
	 * @return a warp based on and index
	 */
	public Warp getWarp(int index) {
		if (index > warps.size())
			return null;
		return getWarps().get(index);
	}

	/**
	 * 
	 * @param name
	 * @return a warp based on warp name
	 */
	public Warp getWarp(String name) {
		for (Warp warp : getWarps()) {
			if (warp.getName().equalsIgnoreCase(name))
				return warp;
		}
		return null;
	}

	public Warp get(Warp w) {
		for (Warp warp : getWarps()) {
			if (w.equals(warp)) {
				return warp;
			}
		}
		return null;
	}

	public boolean exists(Warp w) {
		for (Warp warp : getWarps()) {
			if (w.equals(warp)) {
				return true;
			}
		}
		return false;
	}

	public void empty(PlayerEntity player, List<Warp> list) {
		for (Warp warp : list) {
			if (warp.getPlayer().equals(player)) {
				removeWarp(warp);
			}
		}
	}

	public boolean isEmpty(PlayerEntity player) {
		return getWarps(player).size() == 0;
	}

	/**
	 * 
	 * @param w
	 */
	public boolean addWarp(Warp w) {
		for (String s : subcommands)
			if (w.getName().equalsIgnoreCase(s)) {
				return false;
			}
		for (Warp warp : getWarps()) {
			if (warp.getName().equals(w.getName())) {
				warp.setDimension(w.getDimension());
				warp.setPos(w.getPos());
				warp.setPlayer(w.getPlayer());
				warp.setPitch(warp.getPitch());
				warp.setYaw(w.getYaw());
				return true;
			}
		}
		warps.add(w);
		return true;
	}

	public List<PlayerEntity> getPlayers() {
		List<PlayerEntity> players = new ArrayList<PlayerEntity>();

		for (Warp warp : getWarps()) {
			if (!players.contains(warp.getPlayer())) {
				players.add(warp.getPlayer());
			}
		}

		return players;
	}

	public void removeWarp(Warp w) {
		getWarps().remove(w);
	}

	public void renameWarp(Warp w, String name) {
		w.setName(name);
	}
}
