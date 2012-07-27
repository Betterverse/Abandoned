package net.betterverse.towns.war;

public class CellAttackThread extends Thread {
	CellUnderAttack cell;
	boolean running = false;

	public CellAttackThread(CellUnderAttack cellUnderAttack) {
		this.cell = cellUnderAttack;
	}

	@Override
	public void run() {
		running = true;
		cell.drawFlag();
		while (running) {
			try {
				Thread.sleep(TownsWarConfig.getTimeBetweenFlagColorChange());
			} catch (InterruptedException e) {
				return;
			}
			if (running) {
				cell.changeFlag();
				if (cell.hasEnded()) {
					TownsWar.attackWon(cell);
				}
			}
		}
	}

	protected void setRunning(boolean running) {
		this.running = running;
	}
}
