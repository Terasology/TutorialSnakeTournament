/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.snakeTournament.systems;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.math.Vector3i;
import org.terasology.physics.events.MovedEvent;
import org.terasology.registry.In;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;

import javax.vecmath.Vector3f;

@RegisterSystem(RegisterMode.AUTHORITY)
public class BlockTrailAuthoritySystem extends BaseComponentSystem {
    @In
    WorldProvider worldProvider;
    @In
    BlockManager blockManager;

    @ReceiveEvent
    public void onCharacterMoved(MovedEvent event, EntityRef character) {
        // calculate our previous position by subtracting the delta
        Vector3f previousPosition = new Vector3f(event.getPosition());
        previousPosition.sub(event.getDelta());

        // find out the current and previous block positions
        // offset by 0.5 to account for the fact that blocks are center aligned on the whole numbers
        Vector3i previousBlockPosition = new Vector3i(previousPosition, 0.5f);
        Vector3i currentBlockPosition = new Vector3i(event.getPosition(), 0.5f);

        // check if we are in a different block position
        if (!previousBlockPosition.equals(currentBlockPosition)) {
            // get the block that we are going to insert into the world
            Block trailBlock = blockManager.getBlock("Core:Stone");

            // put a block into the world at our previous block position
            worldProvider.setBlock(previousBlockPosition, trailBlock);
            // characters are approximately 2 blocks tall, add another block at the head level
            previousBlockPosition.add(0, 1, 0);
            worldProvider.setBlock(previousBlockPosition, trailBlock);
        }
    }
}
