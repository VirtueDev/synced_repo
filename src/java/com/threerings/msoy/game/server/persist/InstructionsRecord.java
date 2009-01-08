//
// $Id$

package com.threerings.msoy.game.server.persist;

import com.samskivert.depot.Key;
import com.samskivert.depot.PersistentRecord;
import com.samskivert.depot.annotation.Column;
import com.samskivert.depot.annotation.Entity;
import com.samskivert.depot.annotation.Id;
import com.samskivert.depot.expression.ColumnExp;

import com.threerings.msoy.game.gwt.GameDetail;

/**
 * Contains a game's instructions.
 */
@Entity
public class InstructionsRecord extends PersistentRecord
{
    // AUTO-GENERATED: FIELDS START
    public static final Class<InstructionsRecord> _R = InstructionsRecord.class;
    public static final ColumnExp GAME_ID = colexp(_R, "gameId");
    public static final ColumnExp INSTRUCTIONS = colexp(_R, "instructions");
    // AUTO-GENERATED: FIELDS END

    /** Increment this value if you modify the definition of this persistent object in a way that
     * will result in a change to its SQL counterpart. */
    public static final int SCHEMA_VERSION = 2;

    /** The game for which we track instructions. */
    @Id public int gameId;

    /** The creator supplied instructions for this game. */
    @Column(length=GameDetail.MAX_INSTRUCTIONS_LENGTH)
    public String instructions;

    // AUTO-GENERATED: METHODS START
    /**
     * Create and return a primary {@link Key} to identify a {@link InstructionsRecord}
     * with the supplied key values.
     */
    public static Key<InstructionsRecord> getKey (int gameId)
    {
        return new Key<InstructionsRecord>(
                InstructionsRecord.class,
                new ColumnExp[] { GAME_ID },
                new Comparable[] { gameId });
    }
    // AUTO-GENERATED: METHODS END
}
