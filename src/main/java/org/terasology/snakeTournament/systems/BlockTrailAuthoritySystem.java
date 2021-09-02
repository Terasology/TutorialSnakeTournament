// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.snakeTournament.systems;

import org.joml.Vector3f;
import org.joml.Vector3i;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.math.Side;
import org.terasology.engine.physics.events.MovedEvent;
import org.terasology.engine.registry.In;
import org.terasology.snakeTournament.events.CharacterTrappedEvent;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.block.Blocks;

@RegisterSystem(RegisterMode.AUTHORITY)
public class BlockTrailAuthoritySystem extends BaseComponentSystem {
    @In
    WorldProvider worldProvider;
    @In
    BlockManager blockManager;

    @ReceiveEvent
    public void onCharacterMoved(MovedEvent event, EntityRef character) {
        // calculate our previous position by subtracting the delta
        Vector3f currentPosition = new Vector3f(event.getPosition());
        Vector3f previousPosition = currentPosition.sub(event.getDelta(), new Vector3f());

        // find out the current and previous block positions
        // offset by 0.5 to account for the fact that blocks are center aligned on the whole numbers
        Vector3i currentBlockPosition = Blocks.toBlockPos(currentPosition);
        Vector3i previousBlockPosition = Blocks.toBlockPos(previousPosition);

        // check if we are in a different block position
        if (!previousBlockPosition.equals(currentBlockPosition)) {
            // get the block that we are going to insert into the world
            Block trailBlock = blockManager.getBlock("CoreAssets:Stone");

            // put a block into the world at our previous block position
            worldProvider.setBlock(previousBlockPosition, trailBlock);
            // characters are approximately 2 blocks tall, add another block at the head level
            previousBlockPosition.add(0, 1, 0);
            worldProvider.setBlock(previousBlockPosition, trailBlock);

            if (isPositionTrapped(currentBlockPosition)) {
                character.send(new CharacterTrappedEvent());
            }
        }
    }

    private boolean isPositionTrapped(Vector3i position) {
        // check to see if all adjacent blocks are not air
        int surroundedSideCount = 0;
        for (Side side : Side.horizontalSides()) {
            Vector3i adjacentBlockPosition = side.getAdjacentPos(position, new Vector3i());
            if (worldProvider.getBlock(adjacentBlockPosition) != blockManager.getBlock(BlockManager.AIR_ID)) {
                surroundedSideCount++;
            }
        }
        return surroundedSideCount == 4;
    }
}
