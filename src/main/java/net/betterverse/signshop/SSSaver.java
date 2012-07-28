package net.betterverse.signshop;

public class SSSaver implements Runnable {

	SSMain main;


	SSSaver(SSMain m) {
		this.main = m;
	}

	public void run() {
		this.main.save();
	}
}
