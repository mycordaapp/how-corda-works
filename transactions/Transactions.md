

The class structure is as below, starting at the bottom

The root class is BaseTransaction. It contains what 
we would expect, attributes defining the transaction, e.g. 
inputs, outputs, notary and some simple functions to 
help verify the transaction.

```kotlin
abstract class BaseTransaction : NamedByHash {
    /** A list of reusable reference data states which can be referred to by other contracts in this transaction. */
    abstract val references: List<*>
    /** The inputs of this transaction. Note that in BaseTransaction subclasses the type of this list may change! */
    abstract val inputs: List<*>
    /** Ordered list of states defined by this transaction, along with the associated notaries. */
    abstract val outputs: List<TransactionState<ContractState>>
    /**
     * If present, the notary for this transaction. If absent then the transaction is not notarised at all.
     * This is intended for issuance/genesis transactions that don't consume any other states and thus can't
     * double spend anything.
     */
    abstract val notary: Party?

    protected open fun checkBaseInvariants() {
        checkNotarySetIfInputsPresent()
        checkNoDuplicateInputs()
        checkForInputsAndReferencesOverlap()
    }

    private fun checkNotarySetIfInputsPresent() {
        if (inputs.isNotEmpty() || references.isNotEmpty()) {
            check(notary != null) { "The notary must be specified explicitly for any transaction that has inputs" }
        }
    }

    private fun checkNoDuplicateInputs() {
        check(inputs.size == inputs.toSet().size) { "Duplicate input states detected" }
        check(references.size == references.toSet().size) { "Duplicate reference states detected" }
    }

    private fun checkForInputsAndReferencesOverlap() {
        val intersection = inputs intersect references
        require(intersection.isEmpty()) {
            "A StateRef cannot be both an input and a reference input in the same transaction. Offending " +
                    "StateRefs: $intersection"
        }
    }

    /**
     * Returns a [StateAndRef] for the given output index.
     */
    fun <T : ContractState> outRef(index: Int): StateAndRef<T> = StateAndRef(uncheckedCast(outputs[index]), StateRef(id, index))

    /**
     * Returns a [StateAndRef] for the requested output state, or throws [IllegalArgumentException] if not found.
     */
    fun <T : ContractState> outRef(state: ContractState): StateAndRef<T> = outRef(outputStates.indexOfOrThrow(state))

    /**
     * Helper property to return a list of [ContractState] objects, rather than the often less convenient [TransactionState]
     */
    val outputStates: List<ContractState> get() = outputs.map { it.data }

    /**
     * Helper to simplify getting an indexed output.
     * @param index the position of the item in the output.
     * @return The ContractState at the requested index
     */
    fun getOutput(index: Int): ContractState = outputs[index].data

    /**
     * Helper to simplify getting all output states of a particular class, interface, or base class.
     * @param clazz The class type used for filtering via an [Class.isInstance] check.
     * Clazz must be an extension of [ContractState].
     * @return the possibly empty list of output states matching the clazz restriction.
     */
    fun <T : ContractState> outputsOfType(clazz: Class<T>): List<T> = outputs.mapNotNull { clazz.castIfPossible(it.data) }

    inline fun <reified T : ContractState> outputsOfType(): List<T> = outputsOfType(T::class.java)

    /**
     * Helper to simplify filtering outputs according to a [Predicate].
     * @param clazz The class type used for filtering via an [Class.isInstance] check.
     * Clazz must be an extension of [ContractState].
     * @param predicate A filtering function taking a state of type T and returning true if it should be included in the list.
     * The class filtering is applied before the predicate.
     * @return the possibly empty list of output states matching the predicate and clazz restrictions.
     */
    fun <T : ContractState> filterOutputs(clazz: Class<T>, predicate: Predicate<T>): List<T> {
        return outputsOfType(clazz).filter { predicate.test(it) }
    }

    inline fun <reified T : ContractState> filterOutputs(crossinline predicate: (T) -> Boolean): List<T> {
        return filterOutputs(T::class.java, Predicate { predicate(it) })
    }

    /**
     * Helper to simplify finding a single output matching a [Predicate].
     * @param clazz The class type used for filtering via an [Class.isInstance] check.
     * Clazz must be an extension of [ContractState].
     * @param predicate A filtering function taking a state of type T and returning true if this is the desired item.
     * The class filtering is applied before the predicate.
     * @return the single item matching the predicate.
     * @throws IllegalArgumentException if no item, or multiple items are found matching the requirements.
     */
    fun <T : ContractState> findOutput(clazz: Class<T>, predicate: Predicate<T>): T {
        return outputsOfType(clazz).single { predicate.test(it) }
    }

    inline fun <reified T : ContractState> findOutput(crossinline predicate: (T) -> Boolean): T {
        return findOutput(T::class.java, Predicate { predicate(it) })
    }

    /**
     * Helper to simplify getting all output [StateAndRef] items of a particular state class, interface, or base class.
     * @param clazz The class type used for filtering via an [Class.isInstance] check.
     * Clazz must be an extension of [ContractState].
     * @return the possibly empty list of output [StateAndRef<T>] states matching the clazz restriction.
     */
    fun <T : ContractState> outRefsOfType(clazz: Class<T>): List<StateAndRef<T>> {
        return outputs.mapIndexedNotNull { index, state ->
            clazz.castIfPossible(state.data)?.let { StateAndRef<T>(uncheckedCast(state), StateRef(id, index)) }
        }
    }

    inline fun <reified T : ContractState> outRefsOfType(): List<StateAndRef<T>> = outRefsOfType(T::class.java)

    /**
     * Helper to simplify filtering output [StateAndRef] items according to a [Predicate].
     * @param clazz The class type used for filtering via an [Class.isInstance] check.
     * Clazz must be an extension of [ContractState].
     * @param predicate A filtering function taking a state of type T and returning true if it should be included in the list.
     * The class filtering is applied before the predicate.
     * @return the possibly empty list of output [StateAndRef] states matching the predicate and clazz restrictions.
     */
    fun <T : ContractState> filterOutRefs(clazz: Class<T>, predicate: Predicate<T>): List<StateAndRef<T>> {
        return outRefsOfType(clazz).filter { predicate.test(it.state.data) }
    }

    inline fun <reified T : ContractState> filterOutRefs(crossinline predicate: (T) -> Boolean): List<StateAndRef<T>> {
        return filterOutRefs(T::class.java, Predicate { predicate(it) })
    }

    /**
     * Helper to simplify finding a single output [StateAndRef] matching a [Predicate].
     * @param clazz The class type used for filtering via an [Class.isInstance] check.
     * Clazz must be an extension of [ContractState].
     * @param predicate A filtering function taking a state of type T and returning true if this is the desired item.
     * The class filtering is applied before the predicate.
     * @return the single [StateAndRef] item matching the predicate.
     * @throws IllegalArgumentException if no item, or multiple items are found matching the requirements.
     */
    fun <T : ContractState> findOutRef(clazz: Class<T>, predicate: Predicate<T>): StateAndRef<T> {
        return outRefsOfType(clazz).single { predicate.test(it.state.data) }
    }

    inline fun <reified T : ContractState> findOutRef(crossinline predicate: (T) -> Boolean): StateAndRef<T> {
        return findOutRef(T::class.java, Predicate { predicate(it) })
    }

    override fun toString(): String = "${javaClass.simpleName}(id=$id)"
}
```

Next comes CoreTransaction. It provides concrete types (StateRef),
and a copy of the network params hash. TODO - I am a little confused as to why 
it is needed and can't all be collapsed into BaseTransaction.


```kotlin
@CordaSerializable
abstract class CoreTransaction : BaseTransaction() {
    /** The inputs of this transaction, containing state references only. **/
    abstract override val inputs: List<StateRef>
    /** The reference inputs of this transaction, containing the state references only. **/
    abstract override val references: List<StateRef>
    /**
     * Hash of the network parameters that were in force when the transaction was notarised. Null means, that the transaction
     * was created on older version of Corda (before 4), resolution will default to initial parameters.
     */
    abstract val networkParametersHash: SecureHash?
}
```

Next comes TraversableTransaction 

```kotlin

/**
 * Implemented by [WireTransaction] and [FilteredTransaction]. A TraversableTransaction allows you to iterate
 * over the flattened components of the underlying transaction structure, taking into account that some
 * may be missing in the case of this representing a "torn" transaction. Please see the user guide section
 * "Transaction tear-offs" to learn more about this feature.
 */
abstract class TraversableTransaction(open val componentGroups: List<ComponentGroup>, val digestService: DigestService) : CoreTransaction() {

    /**
     * Old version of [TraversableTransaction] constructor for ABI compatibility.
     */
    @DeprecatedConstructorForDeserialization(1)
    constructor(componentGroups: List<ComponentGroup>) : this(componentGroups, DigestService.sha2_256)

    /** Hashes of the ZIP/JAR files that are needed to interpret the contents of this wire transaction. */
    val attachments: List<SecureHash> = deserialiseComponentGroup(componentGroups, SecureHash::class, ATTACHMENTS_GROUP)

    /** Pointers to the input states on the ledger, identified by (tx identity hash, output index). */
    override val inputs: List<StateRef> = deserialiseComponentGroup(componentGroups, StateRef::class, INPUTS_GROUP)

    /** Pointers to reference states, identified by (tx identity hash, output index). */
    override val references: List<StateRef> = deserialiseComponentGroup(componentGroups, StateRef::class, REFERENCES_GROUP)

    override val outputs: List<TransactionState<ContractState>> = deserialiseComponentGroup(componentGroups, TransactionState::class, OUTPUTS_GROUP)

    /** Ordered list of ([CommandData], [PublicKey]) pairs that instruct the contracts what to do. */
    val commands: List<Command<*>> = deserialiseCommands(componentGroups, digestService = digestService)

    override val notary: Party? = let {
        val notaries: List<Party> = deserialiseComponentGroup(componentGroups, Party::class, NOTARY_GROUP)
        check(notaries.size <= 1) { "Invalid Transaction. More than 1 notary party detected." }
        notaries.firstOrNull()
    }

    val timeWindow: TimeWindow? = let {
        val timeWindows: List<TimeWindow> = deserialiseComponentGroup(componentGroups, TimeWindow::class, TIMEWINDOW_GROUP)
        check(timeWindows.size <= 1) { "Invalid Transaction. More than 1 time-window detected." }
        timeWindows.firstOrNull()
    }

    override val networkParametersHash: SecureHash? = let {
        val parametersHashes = deserialiseComponentGroup(componentGroups, SecureHash::class, PARAMETERS_GROUP)
        check(parametersHashes.size <= 1) { "Invalid Transaction. More than 1 network parameters hash detected." }
        parametersHashes.firstOrNull()
    }

    /**
     * Returns a list of all the component groups that are present in the transaction, excluding the privacySalt,
     * in the following order (which is the same with the order in [ComponentGroupEnum]:
     * - list of each input that is present
     * - list of each output that is present
     * - list of each command that is present
     * - list of each attachment that is present
     * - The notary [Party], if present (list with one element)
     * - The time-window of the transaction, if present (list with one element)
     * - list of each reference input that is present
     * - network parameters hash if present
     */
    val availableComponentGroups: List<List<Any>>
        get() {
            val result = mutableListOf(inputs, outputs, commands, attachments, references)
            notary?.let { result += listOf(it) }
            timeWindow?.let { result += listOf(it) }
            networkParametersHash?.let { result += listOf(it) }
            return result
        }
}
```

All we have here are raw inputs 


