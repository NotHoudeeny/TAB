package me.neznamy.tab.shared.features;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import me.neznamy.tab.platforms.bukkit.PluginHooks;
import me.neznamy.tab.platforms.bukkit.placeholders.afk.AFKProvider;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.config.Configs;
import me.neznamy.tab.shared.cpu.CPUFeature;
import me.neznamy.tab.shared.features.interfaces.QuitEventListener;
import me.neznamy.tab.shared.features.interfaces.Refreshable;
import me.neznamy.tab.shared.placeholders.Placeholder;
import me.neznamy.tab.shared.placeholders.PlaceholderRegistry;
import me.neznamy.tab.shared.placeholders.Placeholders;
import me.neznamy.tab.shared.placeholders.PlayerPlaceholder;
import me.neznamy.tab.shared.placeholders.RelationalPlaceholder;
import me.neznamy.tab.shared.placeholders.ServerConstant;
import me.neznamy.tab.shared.placeholders.ServerPlaceholder;

@SuppressWarnings("unchecked")
public class PlaceholderManager implements QuitEventListener {

	public final int DEFAULT_COOLDOWN = 100;
	public final int DEFAULT_RELATIONAL_COOLDOWN = 500;

	//for metrics
	public List<String> unknownPlaceholders = new ArrayList<String>();

	public List<String> serverConstantList = new ArrayList<String>();
	public Map<String, Integer> serverPlaceholderRefreshIntervals = new HashMap<String, Integer>();
	public Map<String, Integer> playerPlaceholderRefreshIntervals = new HashMap<String, Integer>();
	public Map<String, Integer> relationalPlaceholderRefreshIntervals = new HashMap<String, Integer>();

	private AFKProvider afk;
	private List<PlaceholderRegistry> registry = new ArrayList<>();
	
	private static PlaceholderManager instance;

	public PlaceholderManager(){
		instance = this;
		for (String placeholder : Placeholders.allUsedPlaceholderIdentifiers) {
			if (placeholder.startsWith("%bungee_")) serverPlaceholderRefreshIntervals.put(placeholder, 1000);
		}

		serverConstantList.add("%server_max_players%");							//%maxplayers%

		serverPlaceholderRefreshIntervals.put("%server_online%", 1000); 		//%online%
		serverPlaceholderRefreshIntervals.put("%server_uptime%", 1000);
		serverPlaceholderRefreshIntervals.put("%server_ram_used%", 200);		//%memory-used%
		serverPlaceholderRefreshIntervals.put("%server_tps%", 1000);			//%tps%
		serverPlaceholderRefreshIntervals.put("%server_tps_1%", 1000);			//%tps%
		serverPlaceholderRefreshIntervals.put("%server_tps_1_colored%", 1000);
		serverPlaceholderRefreshIntervals.put("%supervanish_playercount%", 1000); //%canseeonline%
		serverPlaceholderRefreshIntervals.put("%premiumvanish_bungeeplayercount%", 1000);

		playerPlaceholderRefreshIntervals.put("%cmi_user_afk%", 1000); 			//%afk%
		playerPlaceholderRefreshIntervals.put("%cmi_user_afk_symbol%", 1000); 	//%afk%
		playerPlaceholderRefreshIntervals.put("%cmi_user_display_name%", 1000);
		playerPlaceholderRefreshIntervals.put("%cmi_user_ping%", 1000);			//%ping%
		playerPlaceholderRefreshIntervals.put("%cmi_user_vanished_symbol%", 1000);
		playerPlaceholderRefreshIntervals.put("%deluxetags_tag%", 1000);		//%deluxetag%
		playerPlaceholderRefreshIntervals.put("%eglow_glowcolor%", 100);
		playerPlaceholderRefreshIntervals.put("%essentials_nickname%", 1000);	//%essentialsnick%
		playerPlaceholderRefreshIntervals.put("%factionsuuid_faction_name%", 1000);
		playerPlaceholderRefreshIntervals.put("%luckperms_prefix%", 1000); 		//%luckperms-prefix%
		playerPlaceholderRefreshIntervals.put("%luckperms_primary_group_name%", 1000); //%rank%
		playerPlaceholderRefreshIntervals.put("%luckperms_suffix%", 1000); 		//%luckperms-suffix%
		playerPlaceholderRefreshIntervals.put("%multiverse_world_alias%", 2000);
		playerPlaceholderRefreshIntervals.put("%player_colored_ping%", 1000);
		playerPlaceholderRefreshIntervals.put("%player_displayname%", 1000);	//%displayname%
		playerPlaceholderRefreshIntervals.put("%player_health%", 100); 			//%health%
		playerPlaceholderRefreshIntervals.put("%player_health_rounded%", 100);	//%health%
		playerPlaceholderRefreshIntervals.put("%player_name%", 10000); 			//nick plugins changing player name, so not a constant
		playerPlaceholderRefreshIntervals.put("%player_ping%", 1000);			//%ping%
		playerPlaceholderRefreshIntervals.put("%player_world%", 1000);			//%world%
		playerPlaceholderRefreshIntervals.put("%player_x%", 200);				//%xPos%
		playerPlaceholderRefreshIntervals.put("%player_y%", 200);				//%yPos%
		playerPlaceholderRefreshIntervals.put("%player_z%", 200);				//%zPos%
		playerPlaceholderRefreshIntervals.put("%statistic_deaths%", 1000);		//%deaths%
		playerPlaceholderRefreshIntervals.put("%statistic_hours_played%", 1000);
		playerPlaceholderRefreshIntervals.put("%statistic_player_kills%", 1000);
		playerPlaceholderRefreshIntervals.put("%statistic_time_played%", 1000);
		playerPlaceholderRefreshIntervals.put("%uperms_prefix%", 1000);			//%vault-prefix%
		playerPlaceholderRefreshIntervals.put("%uperms_rank%", 1000);			//%rank%
		playerPlaceholderRefreshIntervals.put("%uperms_suffix%", 1000);			//%vault-suffix%
		playerPlaceholderRefreshIntervals.put("%vault_eco_balance%", 1000);
		playerPlaceholderRefreshIntervals.put("%vault_eco_balance_commas%", 1000);
		playerPlaceholderRefreshIntervals.put("%vault_eco_balance_fixed%", 1000);
		playerPlaceholderRefreshIntervals.put("%vault_eco_balance_formatted%", 1000);
		playerPlaceholderRefreshIntervals.put("%vault_prefix%", 1000);			//%vault-prefix%
		playerPlaceholderRefreshIntervals.put("%vault_rank%", 1000);			//%rank%
		playerPlaceholderRefreshIntervals.put("%vault_rankprefix%", 1000);		//%vault-prefix%
		playerPlaceholderRefreshIntervals.put("%vault_suffix%", 1000);			//%vault-suffix%
		playerPlaceholderRefreshIntervals.put("%viaversion_player_protocol_version%", 999999); //%player-version%

		relationalPlaceholderRefreshIntervals.put("%rel_factionsuuid_relation_color%", 500);
		relationalPlaceholderRefreshIntervals.put("%rel_factions_relation_color%", 500);

		for (Entry<String, Integer> placeholder : ((Map<String, Integer>)Configs.config.getConfigurationSection("papi-placeholder-cooldowns.server")).entrySet()) {
			serverPlaceholderRefreshIntervals.put(placeholder.getKey(), placeholder.getValue());
			Shared.debug("Loaded cooldown " + placeholder.getValue() + " for SERVER placeholder " + placeholder.getKey());
		}
		for (Entry<String, Integer> placeholder : ((Map<String, Integer>)Configs.config.getConfigurationSection("papi-placeholder-cooldowns.player")).entrySet()) {
			playerPlaceholderRefreshIntervals.put(placeholder.getKey(), placeholder.getValue());
			Shared.debug("Loaded cooldown " + placeholder.getValue() + " for PLAYER placeholder " + placeholder.getKey());
		}
		for (Entry<String, Integer> placeholder : ((Map<String, Integer>)Configs.config.getConfigurationSection("papi-placeholder-cooldowns.relational")).entrySet()) {
			relationalPlaceholderRefreshIntervals.put(placeholder.getKey(), placeholder.getValue());
			Shared.debug("Loaded cooldown " + placeholder.getValue() + " for RELATIONAL placeholder " + placeholder.getKey());
		}

		AtomicInteger atomic = new AtomicInteger();
		Shared.featureCpu.startRepeatingMeasuredTask(50, "refreshing placeholders", CPUFeature.PLACEHOLDER_REFRESHING, new Runnable() {

			@Override
			public void run() {
				int loopTime = atomic.addAndGet(50);
				Collection<ITabPlayer> allPlayers = Shared.getPlayers();
				Collection<ITabPlayer> players = new ArrayList<ITabPlayer>();
				for (ITabPlayer p : allPlayers) {
					if (p.onJoinFinished) players.add(p);
				}
				Map<ITabPlayer, Set<Refreshable>> update = new HashMap<ITabPlayer, Set<Refreshable>>();
				Map<ITabPlayer, Set<Refreshable>> forceUpdate = new HashMap<ITabPlayer, Set<Refreshable>>();
				boolean somethingChanged = false;
				for (RelationalPlaceholder relPlaceholder : Placeholders.registeredRelationalPlaceholders.values()) {
					if (loopTime % relPlaceholder.refresh != 0) continue;
					long startTime = System.nanoTime();
					for (ITabPlayer p1 : players) {
						for (ITabPlayer p2 : players) {
							if (relPlaceholder.update(p1, p2)) {
								if (!forceUpdate.containsKey(p2)) forceUpdate.put(p2, new HashSet<Refreshable>());
								forceUpdate.get(p2).addAll(getPlaceholderUsage(relPlaceholder.identifier));
								somethingChanged = true;
							}
							if (relPlaceholder.update(p2, p1)) {
								if (!forceUpdate.containsKey(p1)) forceUpdate.put(p1, new HashSet<Refreshable>());
								forceUpdate.get(p1).addAll(getPlaceholderUsage(relPlaceholder.identifier));
								somethingChanged = true;
							}
						}
					}
					Shared.placeholderCpu.addTime(relPlaceholder.identifier, System.nanoTime()-startTime);
				}
				for (Placeholder placeholder : new HashSet<>(Placeholders.usedPlaceholders)) { //avoiding concurrent modification on reload
					if (loopTime % placeholder.cooldown != 0) continue;
//					System.out.println(placeholder.getIdentifier() + " - " + placeholder.cooldown);
					if (placeholder instanceof PlayerPlaceholder) {
						long startTime = System.nanoTime();
						for (ITabPlayer all : players) {
							if (((PlayerPlaceholder)placeholder).update(all)) {
								if (!update.containsKey(all)) update.put(all, new HashSet<Refreshable>());
								update.get(all).addAll(getPlaceholderUsage(placeholder.getIdentifier()));
								somethingChanged = true;
							}
						}
						Shared.placeholderCpu.addTime(placeholder.getIdentifier(), System.nanoTime()-startTime);
					}
					if (placeholder instanceof ServerPlaceholder) {
						long startTime = System.nanoTime();
						if (((ServerPlaceholder)placeholder).update()) {
							Set<Refreshable> usage = getPlaceholderUsage(placeholder.getIdentifier());
							somethingChanged = true;
							for (ITabPlayer all : players) {
								if (!update.containsKey(all)) update.put(all, new HashSet<Refreshable>());
								update.get(all).addAll(usage);
							}
						}
						Shared.placeholderCpu.addTime(placeholder.getIdentifier(), System.nanoTime()-startTime);
					}
				}
				if (somethingChanged) {
					for (Entry<ITabPlayer, Set<Refreshable>> entry : update.entrySet()) {
						if (forceUpdate.containsKey(entry.getKey())) {
							entry.getValue().removeAll(forceUpdate.get(entry.getKey()));
						}
					}
					Shared.featureCpu.runTask("refreshing", new Runnable() {

						@Override
						public void run() {
							for (Entry<ITabPlayer, Set<Refreshable>> entry : forceUpdate.entrySet()) {
								for (Refreshable r : entry.getValue()) {
									long startTime = System.nanoTime();
									r.refresh(entry.getKey(), true);
									Shared.featureCpu.addTime(r.getRefreshCPU(), System.nanoTime()-startTime);
								}
							}
							for (Entry<ITabPlayer, Set<Refreshable>> entry : update.entrySet()) {
								for (Refreshable r : entry.getValue()) {
									long startTime = System.nanoTime();
									r.refresh(entry.getKey(), false);
									Shared.featureCpu.addTime(r.getRefreshCPU(), System.nanoTime()-startTime);
								}
							}
						}
					});
				}
			}
		});
	}
	public static PlaceholderManager getInstance() {
		return instance;
	}
	public static Set<Refreshable> getPlaceholderUsage(String identifier){
		Set<Refreshable> set = new HashSet<Refreshable>();
		for (Refreshable r : Shared.refreshables) {
			if (r.getUsedPlaceholders().contains(identifier)) set.add(r);
		}
		return set;
	}

	public void registerPAPIPlaceholder(String identifier) {
		if (serverPlaceholderRefreshIntervals.containsKey(identifier)) {
			Shared.debug("Registering SERVER PlaceholderAPI placeholder " + identifier + " with cooldown " + serverPlaceholderRefreshIntervals.get(identifier));
			Placeholders.registerPlaceholder(new ServerPlaceholder(identifier, serverPlaceholderRefreshIntervals.get(identifier)){
				public String get() {
					return PluginHooks.setPlaceholders((UUID)null, identifier);
				}
			}, true);
			return;
		}
		if (serverConstantList.contains(identifier)) {
			Shared.debug("Registering SERVER PlaceholderAPI constant " + identifier);
			Placeholders.registerPlaceholder(new ServerConstant(identifier){
				public String get() {
					return PluginHooks.setPlaceholders((UUID)null, identifier);
				}
			}, true);
			return;
		}
		if (playerPlaceholderRefreshIntervals.containsKey(identifier)) {
			Shared.debug("Registering PLAYER PlaceholderAPI placeholder " + identifier + " with cooldown " + playerPlaceholderRefreshIntervals.get(identifier));
			Placeholders.registerPlaceholder(new PlayerPlaceholder(identifier, playerPlaceholderRefreshIntervals.get(identifier)){
				public String get(ITabPlayer p) {
					return PluginHooks.setPlaceholders(p.getBukkitEntity(), identifier);
				}
			}, true);
			return;
		}
		if (relationalPlaceholderRefreshIntervals.containsKey(identifier)) {
			Shared.debug("Registering RELATIONAL PlaceholderAPI placeholder " + identifier + " with cooldown " + relationalPlaceholderRefreshIntervals.get(identifier));
			Placeholders.registerPlaceholder(new RelationalPlaceholder(identifier, relationalPlaceholderRefreshIntervals.get(identifier)) {

				@Override
				public String get(ITabPlayer viewer, ITabPlayer target) {
					return PluginHooks.setRelationalPlaceholders(viewer, target, identifier);
				}
			});
			return;
		}
		unknownPlaceholders.add(identifier);
		if (identifier.contains("%rel_")) {
			Shared.debug("Registering unlisted RELATIONAL PlaceholderAPI placeholder " + identifier + " with cooldown " + DEFAULT_RELATIONAL_COOLDOWN);
			Placeholders.registerPlaceholder(new RelationalPlaceholder(identifier, DEFAULT_RELATIONAL_COOLDOWN) {

				@Override
				public String get(ITabPlayer viewer, ITabPlayer target) {
					return PluginHooks.setRelationalPlaceholders(viewer, target, identifier);
				}
			});
		} else {
			if (identifier.startsWith("%server_")) {
				Shared.debug("Registering unlisted SERVER PlaceholderAPI placeholder " + identifier + " with cooldown " + DEFAULT_COOLDOWN);
				Placeholders.registerPlaceholder(new ServerPlaceholder(identifier, DEFAULT_COOLDOWN){
					public String get() {
						return PluginHooks.setPlaceholders((UUID)null, identifier);
					}
				}, true);
			} else {
				int cooldown = identifier.startsWith("%cmi_") ? DEFAULT_COOLDOWN * 10 : DEFAULT_COOLDOWN; //inefficient plugin
				Shared.debug("Registering unlisted PLAYER PlaceholderAPI placeholder " + identifier + " with cooldown " + cooldown);
				Placeholders.registerPlaceholder(new PlayerPlaceholder(identifier, cooldown){
					public String get(ITabPlayer p) {
						return PluginHooks.setPlaceholders(p == null ? null : p.getBukkitEntity(), identifier);
					}
				}, true);
			}
		}
	}

	@Override
	public void onQuit(ITabPlayer disconnectedPlayer) {
		for (Placeholder pl : Placeholders.getAllPlaceholders()) {
			if (pl instanceof PlayerPlaceholder) {
				((PlayerPlaceholder)pl).lastValue.remove(disconnectedPlayer.getName());
			}
		}
		for (RelationalPlaceholder pl : Placeholders.registeredRelationalPlaceholders.values()) {
			for (ITabPlayer all : Shared.getPlayers()) {
				pl.lastValue.remove(all.getName() + "-" + disconnectedPlayer.getName());
				pl.lastValue.remove(disconnectedPlayer.getName() + "-" + all.getName());
			}
		}
	}
	public AFKProvider getAFKProvider() {
		return afk;
	}
	public void setAFKProvider(AFKProvider afk) {
		Shared.debug("Loaded AFK provider: " + afk.getClass().getSimpleName());
		this.afk = afk;
	}
	public void addRegistry(PlaceholderRegistry registry) {
		this.registry.add(registry);
	}
	public void registerPlaceholders() {
		registry.forEach(r -> r.registerPlaceholders());
		for (String placeholder : Placeholders.allUsedPlaceholderIdentifiers) {
			Placeholders.categorizeUsedPlaceholder(placeholder);
		}
	}
}