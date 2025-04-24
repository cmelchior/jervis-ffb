package com.jervisffb.resources

import com.jervisffb.engine.ext.playerNo
import com.jervisffb.engine.model.SetupId
import com.jervisffb.engine.rules.builder.GameType
import com.jervisffb.engine.serialize.FILE_FORMAT_VERSION
import com.jervisffb.engine.serialize.JervisMetaData
import com.jervisffb.engine.serialize.JervisSetupFile
import com.jervisffb.engine.serialize.RelativeCoordinate

object DefaultSetups {
    val standardSetups = mapOf(
        "jervis-default-5-5-1.jrs" to JervisSetupFile(
            metadata = JervisMetaData(FILE_FORMAT_VERSION),
            id = SetupId("jervis-default-5-5-1"),
            name = "5-5-1",
            gameType = GameType.STANDARD,
            team = null,
            formation = mapOf(
                1.playerNo to RelativeCoordinate(5, 0),
                2.playerNo to RelativeCoordinate(6, 0),
                3.playerNo to RelativeCoordinate(7, 0),
                4.playerNo to RelativeCoordinate(8, 0),
                5.playerNo to RelativeCoordinate(9, 0),
                6.playerNo to RelativeCoordinate(1, 1),
                7.playerNo to RelativeCoordinate(3, 1),
                8.playerNo to RelativeCoordinate(11, 1),
                9.playerNo to RelativeCoordinate(13, 1),
                10.playerNo to RelativeCoordinate(7, 4),
                11.playerNo to RelativeCoordinate(7, 9),
            )
        ),
        "jervis-default-3-4-4.jrs" to JervisSetupFile(
            metadata = JervisMetaData(FILE_FORMAT_VERSION),
            id = SetupId("jervis-default-3-4-4"),
            name = "3-4-4",
            gameType = GameType.STANDARD,
            team = null,
            formation = mapOf(
                1.playerNo to RelativeCoordinate(6, 0),
                2.playerNo to RelativeCoordinate(7, 0),
                3.playerNo to RelativeCoordinate(8, 0),
                4.playerNo to RelativeCoordinate(1, 2),
                5.playerNo to RelativeCoordinate(4, 2),
                6.playerNo to RelativeCoordinate(10, 2),
                7.playerNo to RelativeCoordinate(12, 2),
                8.playerNo to RelativeCoordinate(1, 3),
                9.playerNo to RelativeCoordinate(4, 3),
                10.playerNo to RelativeCoordinate(10, 3),
                11.playerNo to RelativeCoordinate(12, 3),
            )
        )
    )

    val sevensSetups = mapOf(
        "jervis-default-bb7-5-2.jrs" to JervisSetupFile(
            metadata = JervisMetaData(FILE_FORMAT_VERSION),
            id = SetupId("jervis-default-5-2"),
            name = "5-2",
            gameType = GameType.BB7,
            team = null,
            formation = mapOf(
                1.playerNo to RelativeCoordinate(2, 0),
                2.playerNo to RelativeCoordinate(4, 0),
                3.playerNo to RelativeCoordinate(5, 0),
                4.playerNo to RelativeCoordinate(6, 0),
                5.playerNo to RelativeCoordinate(8, 0),
                6.playerNo to RelativeCoordinate(1, 1),
                7.playerNo to RelativeCoordinate(9, 1),
            )
        ),
        "jervis-default-bb7-3-4.jrs" to JervisSetupFile(
            metadata = JervisMetaData(FILE_FORMAT_VERSION),
            id = SetupId("jervis-default-3-4"),
            name = "3-4",
            gameType = GameType.BB7,
            team = null,
            formation = mapOf(
                1.playerNo to RelativeCoordinate(2, 0),
                2.playerNo to RelativeCoordinate(5, 0),
                3.playerNo to RelativeCoordinate(8, 0),
                4.playerNo to RelativeCoordinate(1, 1),
                5.playerNo to RelativeCoordinate(4, 1),
                6.playerNo to RelativeCoordinate(6, 1),
                7.playerNo to RelativeCoordinate(9, 1),
            )
        )
    )
}
