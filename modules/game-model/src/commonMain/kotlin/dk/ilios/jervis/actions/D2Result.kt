package dk.ilios.jervis.actions

import kotlin.random.Random

class D2Result(result: Int = Random.nextInt(1, 3)): DieResult(result, 1, 2)
