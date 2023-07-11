package com.aubot.agv.traffic;

import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Block;
import org.opentcs.data.model.Point;

import java.util.Set;

public class BlockPoints {

  private TCSObjectReference<Block> blockRef;

  private Set<TCSObjectReference<Point>> entryPoints;

  private Set<TCSObjectReference<Point>> exitPoints;

  public BlockPoints(TCSObjectReference<Block> blockRef,
                     Set<TCSObjectReference<Point>> entryPoints,
                     Set<TCSObjectReference<Point>> exitPoints) {
    this.blockRef = blockRef;
    this.entryPoints = entryPoints;
    this.exitPoints = exitPoints;
  }

  public TCSObjectReference<Block> getBlock() {
    return blockRef;
  }

  public Set<TCSObjectReference<Point>> getEntryPoints() {
    return entryPoints;
  }

  public Set<TCSObjectReference<Point>> getExitPoints() {
    return exitPoints;
  }
}
