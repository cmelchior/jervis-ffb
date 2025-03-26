# UI Todo list and Bugs

This document is used to track various todo lists for the UI as well as 
bugs encountered that are not fixed.

The TODO list are by no means exhaustive, it is just a place to dump
things as they come up.


## Todo List
- [ ] When selecting a team reroll, what to do about temporary rerolls.
      Right now the rules just select the most appropriate one, but most likely we need 
      to list all types of rerolls and let it be up to the UI to filter/show them somehow.
- [ ] Add "quick"-action when prone, where you can also select Jump if it would be 
      possible after standing up.
- [ ] Filter actions that cannot be used because they cannot select a target
  - Foul
  - Block if Prone (but yes combined with Jump Up)
  - Blitz if no players
- [ ] Custom cursors:
  - [ ] During move
  - [ ] When selecting block
  - [ ] When selecting pass target
  - [ ] When selecting foul target
- [ ] Add numbers to yellow squares to indicate the target number for rush/dodge.
- [ ] Keep player card open for the currently active player.
- [ ] Add support for Rerolls / Apothecary / etc. in the sidebars
- [ ] Create "dice"-like background for dice rolls.
- [ ] Only have 3 players pr. row in the Dugout (similar to FUMBBL).
- [ ] Add numbers where the player has moved already
- [ ] Passes going out of bounds currently land in exitAt, make the animation move the
      ball out of bounds.
- [ ] Set size of player icons and square decorators correctly. Right now they are bit off making them look blurry.
- [ ] Add UI indicator signaling limit of opponent players move (see BB3).
- [ ] Add scrollbar indicator to game log components.
- [ ] If possible, move dice roll dialogs away from the pitch.
- [ ] Rethink dialog design. They are currently way too big. As a minimum
      you need to be able to drag them away from the screen.
- [ ] Some fonts need to scale with the size of the window. Introduce the concept of 
      wsp (Window Scaled Pixels) and use them where appropriate (Player stats / Skills)
- [ ] Figure out a better way to trigger recomposition. Currently, there is a lot of
      object creation/copying going on with UiFieldSquare. It seems performant enough
      on my machine, but it also feels like it could be optimized. Currently there
      are two places changes are happening. In the rules engine and in 
      ManualActionProvider when setting up listeners.
- [ ] Could we add 3d dice rolling across the board? Probably difficult in pure Compose, but maybe using Lottie?
- [x] Hover on block shows dice indicator.
- [x] Better ball animation using Projectile Motion equations.
    - https://www.youtube.com/watch?v=qtsxHx1MpUI&ab_channel=PhysicsAlmanac
    - Size-Distance Scaling Equation: https://sites.millersville.edu/sgallagh/Publications/top2013.pdf
    - https://en.wikipedia.org/wiki/Visual_angle
    - https://www.quora.com/Do-objects-appear-exponentially-smaller-as-you-move-away-from-them
    - https://www.youtube.com/watch?v=_KLfj84SOh8&ab_channel=ErikRosolowsky
    - https://www.omnicalculator.com/physics/time-of-flight-projectile-motion
- [x] Standup and end movement as a single action (UI only).


## Design ideas

- [ ] Experiment with an action system similar to BB3 (round action circle around player)
- [ ] Think about an UI that can work across Desktop: 16:9 and iPad 4:3.
- [ ] Is it worth exploring an isometric view (to make it more immersive)? 
      - [ ] How feasible is it to use the current player graphics?


## Known bugs

- Block against Kroxigor with 1 asssist 4 < 5 only rolls 1 dice
- Cannot select Block action from prone.
- No pass animation is being triggered when Using the Pass action.
- UI does not restore moved used indicators correctly when undo'ing across player actions.
- Player with ball blocking across middle line drops ball that bounce, crash
  java.lang.IllegalArgumentException: Cannot determine position of: FieldCoordinateImpl(x=2147483647, y=2147483647)
  at com.jervisffb.engine.rules.Rules$DefaultImpls.throwIn(Rules.kt:141)
  at com.jervisffb.engine.rules.BB2020Rules.throwIn(StandardBB2020Rules.kt:5)
- Hover over player in dugout during setup doesn't show stat card.
- The UI flow for P2P Host/Client works in the happy case, but there are plenty of options 
  for messing it up. Probably need to rethink the logic to make it easier to manage. Especially
  when the other party jump back in the flow.
- When selecting dice rolls on the server, it should not be possible to select "All" for UNDO.
  "All" doesn't work, since if you undo a dice roll, the server will register it is at a point where
  it needs to roll and then immediately re-apply the dice roll.

- Detected while running random actions:
  Exception in thread "GameLoopScope" java.lang.ArrayIndexOutOfBoundsException: Index -2147483648 out of bounds for length 26
  at com.jervisffb.engine.model.Field.get(Field.kt:53)
  at com.jervisffb.engine.rules.bb2020.procedures.UtilsKt.calculateMoveTypesAvailable(utils.kt:52)
  at com.jervisffb.engine.rules.bb2020.procedures.actions.pass.PassAction$MoveOrPassOrEndAction.getAvailableActions(PassAction.kt:101)
  at com.jervisffb.engine.GameEngineController.getAvailableActions(GameEngineController.kt:102)
  at com.jervisffb.engine.GameEngineController.rollForwardToNextActionNode(GameEngineController.kt:306)
  at com.jervisffb.engine.GameEngineController.rollForwardToNextActionNode(GameEngineController.kt:333)
  at com.jervisffb.engine.GameEngineController.rollForwardToNextActionNode(GameEngineController.kt:314)
  at com.jervisffb.engine.GameEngineController.rollForwardToNextActionNode(GameEngineController.kt:333)

- Detected while running random actions:
  Caused by: java.lang.ArrayIndexOutOfBoundsException: Index -2147483648 out of bounds for length 26
  at com.jervisffb.engine.model.Field.get(Field.kt:53)
  at com.jervisffb.engine.rules.bb2020.procedures.UtilsKt.calculateMoveTypesAvailable(utils.kt:52)
  at com.jervisffb.engine.rules.bb2020.procedures.actions.move.MoveAction$SelectMoveType.getAvailableActions(MoveAction.kt:47)
  at com.jervisffb.engine.GameEngineController.getAvailableActions(GameEngineController.kt:102)
  at com.jervisffb.engine.GameEngineController.rollForwardToNextActionNode(GameEngineController.kt:306)
  at com.jervisffb.engine.GameEngineController.rollForwardToNextActionNode(GameEngineController.kt:314)
  at com.jervisffb.engine.GameEngineController.rollForwardToNextActionNode(GameEngineController.kt:333)
  at com.jervisffb.engine.GameEngineController.rollForwardToNextActionNode(GameEngineController.kt:333)

- Saw "No player eligible for High Kick" even though there clearly was. Forgot to capture events leading up it. 
  Try to reproduce.

- Saw this when GFI
  Exception in thread "AWT-EventQueue-0" java.util.ConcurrentModificationException
  at java.base/java.util.ArrayList$Itr.checkForComodification(ArrayList.java:1095)
  at java.base/java.util.ArrayList$Itr.next(ArrayList.java:1049)
  at com.jervisffb.engine.fsm.ProcedureStack.containsNode(ProcedureStack.kt:142)
  at com.jervisffb.ui.game.viewmodel.SidebarViewModel$special$$inlined$map$1$2.emit(Emitters.kt:66)
  at kotlinx.coroutines.flow.SharedFlowImpl.collect$suspendImpl(SharedFlow.kt:397)
  at kotlinx.coroutines.flow.SharedFlowImpl$collect$1.invokeSuspend(SharedFlow.kt)
  at kotlin.coroutines.jvm.internal.BaseContinuationImpl.resumeWith(ContinuationImpl.kt:33)
  at kotlinx.coroutines.DispatchedTask.run(DispatchedTask.kt:100)
  at androidx.compose.ui.platform.FlushCoroutineDispatcher$dispatch$2$1.invoke(FlushCoroutineDispatcher.skiko.kt:63)
  at androidx.compose.ui.platform.FlushCoroutineDispatcher$dispatch$2$1.invoke(FlushCoroutineDispatcher.skiko.kt:58)
  at androidx.compose.ui.platform.FlushCoroutineDispatcher.performRun(FlushCoroutineDispatcher.skiko.kt:102)
  at androidx.compose.ui.platform.FlushCoroutineDispatcher.access$performRun(FlushCoroutineDispatcher.skiko.kt:37)
  at androidx.compose.ui.platform.FlushCoroutineDispatcher$dispatch$2.invokeSuspend(FlushCoroutineDispatcher.skiko.kt:58)
  at kotlin.coroutines.jvm.internal.BaseContinuationImpl.resumeWith(ContinuationImpl.kt:33)
  at kotlinx.coroutines.DispatchedTask.run(DispatchedTask.kt:100)
  at java.desktop/java.awt.event.InvocationEvent.dispatch(InvocationEvent.java:318)
  at java.desktop/java.awt.EventQueue.dispatchEventImpl(EventQueue.java:773)
  at java.desktop/java.awt.EventQueue$4.run(EventQueue.java:720)
  at java.desktop/java.awt.EventQueue$4.run(EventQueue.java:714)
  at java.base/java.security.AccessController.doPrivileged(AccessController.java:400)
  at java.base/java.security.ProtectionDomain$JavaSecurityAccessImpl.doIntersectionPrivilege(ProtectionDomain.java:87)
  at java.desktop/java.awt.EventQueue.dispatchEvent(EventQueue.java:742)
  at java.desktop/java.awt.EventDispatchThread.pumpOneEventForFilters(EventDispatchThread.java:203)
  at java.desktop/java.awt.EventDispatchThread.pumpEventsForFilter(EventDispatchThread.java:124)
  at java.desktop/java.awt.EventDispatchThread.pumpEventsForHierarchy(EventDispatchThread.java:113)
  at java.desktop/java.awt.EventDispatchThread.pumpEvents(EventDispatchThread.java:109)
  at java.desktop/java.awt.EventDispatchThread.pumpEvents(EventDispatchThread.java:101)
  at java.desktop/java.awt.EventDispatchThread.run(EventDispatchThread.java:90)
  Suppressed: kotlinx.coroutines.internal.DiagnosticCoroutineContextException: [androidx.compose.ui.scene.ComposeContainer$DesktopCoroutineExceptionHandler@1e9d7ed0, androidx.compose.runtime.BroadcastFrameClock@5b8fadb6, StandaloneCoroutine{Cancelling}@284b31d8, FlushCoroutineDispatcher@59bf5aa7]

- If a ball bounce over the center during kick-off and caught by a player there, it should still be returned to the 
  the other team (I think, double check the rules)