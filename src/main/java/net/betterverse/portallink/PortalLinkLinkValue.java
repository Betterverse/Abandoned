package net.betterverse.portallink;

public class PortalLinkLinkValue
{
  private String string;
  private int whichNether;

  public PortalLinkLinkValue(String str, int which)
  {
    this.string = str;
    this.whichNether = which;
  }

  public String getString() {
    return this.string;
  }

  public int getWhichNether() {
    return this.whichNether;
  }
}