# Bug List

This file is just a temporary way to capture any bugs seen that I didn't have time to investigate yet.

## Known bugs

- Block against Kroxigor with 1 asssist 4 < 5 only rolls 1 dice
- Cannot select Block action from prone.
- No pass animation is being triggered when Using the Pass action.
- UI does not restore moved used indicators correctly when undo'ing across player actions.
- Player with ball blocking across middle line drops ball that bounce, crash
- Hover over player in dugout during setup doesn't show stat card.
- The UI flow for P2P Host/Client works in the happy case, but there are plenty of options
  for messing it up. Probably need to rethink the logic to make it easier to manage. Especially
  when the other party jump back in the flow.
- When selecting dice rolls on the server, it should not be possible to select "All" for UNDO.
  "All" doesn't work, since if you undo a dice roll, the server will register it is at a point where
  it needs to roll and then immediately re-apply the dice roll.

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

- In a HotSeat game beween 2 random players and Blitz is rolled, the game deadlocks