package com.clayfactoria.codecs;

import com.clayfactoria.codecs.task.DepositTaskExecutor;
import com.clayfactoria.codecs.task.HarvestTaskExecutor;
import com.clayfactoria.codecs.task.PositionTaskExecutor;
import com.clayfactoria.codecs.task.TakeTaskExecutor;
import com.clayfactoria.codecs.task.TaskExecutor;
import com.clayfactoria.codecs.task.WorkTaskExecutor;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.math.vector.Vector3f;
import java.util.function.Supplier;

public enum Task implements Supplier<String> {
  DEPOSIT(
      "Deposit",
      "Deposit held item in an adjacent container",
      new Vector3f(0.59F, 0.29F, 0.89F), // Purple
      "ImageAssets/Deposit.png",
      false,
      false,
      new DepositTaskExecutor()),
  TAKE(
      "Take",
      "Take an item from an adjacent container",
      new Vector3f(0.92F, 0.27F, 0.84F), // Pink
      "ImageAssets/Take.png",
      false,
      false,
      new TakeTaskExecutor()),
  POSITION(
      "Position",
      "Do nothing (After moving to a position)",
      new Vector3f(0.93F, 0.22F, 0.35F), // Red
      "ImageAssets/Position.png",
      false,
      true,
      new PositionTaskExecutor()),
  WORK(
      "Work",
      "Work at an adjacent workstation",
      new Vector3f(0.33F, 0.45F, 0.9F), // Blue
      "ImageAssets/Work.png",
      false,
      false,
      new WorkTaskExecutor()),
  HARVEST(
      "Harvest",
      "Harvest any crop in the given area",
      new Vector3f(0.92F, 0.86F, 0.2F), // Yellow
      "ImageAssets/Harvest.png",
      true,
      false,
      new HarvestTaskExecutor());

  public static final Codec<Task> CODEC = new EnumCodec<>(Task.class);
  public final String name;
  public final String description;
  public final Vector3f color;
  public final String iconAssetPath;
  public final boolean locationEqualsWalkLocation;
  public final boolean usesBounds;
  public final TaskExecutor taskExecutor;

  Task(
      String name,
      String description,
      Vector3f color,
      String iconAssetPath,
      boolean usesBounds,
      boolean locationEqualsWalkLocation,
      TaskExecutor taskExecutor) {
    this.name = name;
    this.description = description;
    this.color = color;
    this.iconAssetPath = iconAssetPath;
    this.usesBounds = usesBounds;
    this.locationEqualsWalkLocation = locationEqualsWalkLocation;
    this.taskExecutor = taskExecutor;
  }

  public String get() {
    return this.description;
  }

  @Override
  public String toString() {
    return this.name;
  }
}
