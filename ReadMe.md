# StateMachine Theory

**Region**: A Region denotes a behavior fragment that may execute concurrently with its orthogonal Regions.

---

**Vertex**: Vertex is an abstract class that captures the common characteristics for a variety of different concrete kinds of nodes in the StateMachine graph
(States, Pseudostates, or ConnectionPointReferences). A Vertex can be the source and/or target of any number of Transitions.

---

**State**: A State models a situation in the execution of a StateMachine Behavior during which some invariant condition holds. In most cases this condition is
not explicitly defined, but is implied, usually through the name associated with the State e.g: 'Idle', 'Active'.

**Kinds of State**:

A **simple** State has no internal Vertices or Transitions. <br>
A **composite** State contains at least one Region. <br>
A **submachine** State refers to an entire StateMachine, which is, conceptually, deemed to be “nested” within the State.

---

**PseudoState**: A Pseudostate is an abstraction that encompasses different types of transient Vertices in the StateMachine graph. Pseudostates are
generally used to chain multiple Transitions into more complex compound transitions. For example, by combining a Transition entering a fork Pseudostate with a
set of Transitions exiting that Pseudostate, we get a compound Transition that can enter a set of orthogonal Regions.

**Kinds of PseudoStates**:

**join** – This type of Pseudostate serves as a common target Vertex for two or more Transitions originating from Vertices in different orthogonal Regions.
Transitions terminating on a join Pseudostate cannot have a guard or a trigger. <br>
**fork** – fork Pseudostates serve to split an incoming Transition into two or more Transitions terminating on Vertices in orthogonal Regions of a composite
State. The Transitions outgoing from a fork Pseudostate cannot have a guard or a trigger. <br>
**junction** – This type of Pseudostate is used to connect multiple Transitions into compound paths between States. For example, a junction Pseudostate can be
used to merge multiple incoming Transitions into a single outgoing Transition representing a shared continuation path. Or, it can be used to split an
incoming Transition into multiple outgoing Transition segments with different guard Constraints. <br>
**choice** – This type of Pseudostate is similar to a junction Pseudostate (see above) and serves similar purposes, with the difference that the guard
Constraints on all outgoing Transitions are evaluated dynamically, when the compound transition traversal reaches this Pseudostate. Consequently, choice is
used to realize a dynamic conditional branch. It allows splitting of compound transitions into multiple alternative paths such that the decision on which
path to take may depend on the results of Behavior executions performed in the same compound transition prior to reaching the choice point. If more than one
guard evaluates to true, one of the corresponding Transitions is selected. <br>
**terminate** – Entering a terminated Pseudostate implies that the execution of the StateMachine is terminated immediately. <br>
**history** - A history state is a pseudostate, meaning that a state machine can’t rest in a history state. When a transition that leads to a history state 
happens, the history state itself doesn’t become active, rather the “most recently visited state” becomes active. It is a way for a compound state to remember (when it exits) which state was active, so that if the compound state ever becomes active again, it can go back to the same active substate, 
instead of blindly following the initial transition. There are two types of history states, deep history states and shallow history states. A deep history remembers the deepest active state(s) while a shallow history only remembers the immediate child’s state.

---

**Transition**:
Three kinds of transitions are defined.

**external**: The Transition exits its source Vertex. If the Vertex is a State, then executing this Transition will result in the execution of any
associated exit Behavior of that State. <br>
**local**: It is the opposite of external, meaning that the Transition does not exit its containing State (and, hence, the exit Behavior of the containing
State will not be executed). However, for local Transitions the target Vertex must be different from its source Vertex. A local Transition can only exist
within a composite State. <br>
**internal**: It is a special case of a local Transition that is a self-transition (i.e., with the same source and target States), such that the State is never
exited (and, thus, not re-entered), which means that no exit or entry Behaviors are executed when this Transition is executed. This kind of Transition can
only be defined if the source Vertex is a State. <br>