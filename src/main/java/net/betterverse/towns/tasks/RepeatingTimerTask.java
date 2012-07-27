package net.betterverse.towns.tasks;

import java.util.ArrayList;

import net.betterverse.towns.object.PlotBlockData;
import net.betterverse.towns.object.TownsRegenAPI;
import net.betterverse.towns.object.TownsUniverse;

public class RepeatingTimerTask extends TownsTimerTask {
	public RepeatingTimerTask(TownsUniverse universe) {
		super(universe);
	}

	@Override
	public void run() {
		if (TownsRegenAPI.hasPlotChunks()) {
			for (PlotBlockData plotChunk : new ArrayList<PlotBlockData>(TownsRegenAPI.getPlotChunks().values())) {
				if (! plotChunk.restoreNextBlock()) {
					TownsRegenAPI.deletePlotChunk(plotChunk);
					TownsRegenAPI.deletePlotChunkSnapshot(plotChunk);
				}
			}
		}
	}
}
