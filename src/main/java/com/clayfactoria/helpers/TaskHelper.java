package com.clayfactoria.helpers;

import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.StateData;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;

public class TaskHelper {

  public static @Nullable Vector3i findNearbyContainer(NPCEntity npcEntity) {
    World world = npcEntity.getWorld();
    assert world != null;
    Vector3i pos = npcEntity.getOldPosition().toVector3i();

    // Check surrounding blocks
    Vector3i[] directions = {
        new Vector3i(0,0,-1),
        new Vector3i(1,0,0),
        new Vector3i(0, 0, 1),
        new Vector3i(-1, 0, 0)
    };

    // Shuffle order to prevent order of check being predictable
    List<Vector3i> shuffled = Arrays.asList(directions);
    Collections.shuffle(shuffled);
    for (Vector3i dir : shuffled) {
      BlockType type = world.getBlockType(pos.clone().add(dir));
      if (type == null) {continue;}
      StateData blockState = type.getState();
      if (blockState == null) {continue;}
      if (blockState.getId() == null) {continue;}
      if (blockState.getId().equals("container")) {
        return pos.add(dir);
      }
    }
    return null;
  }
}
