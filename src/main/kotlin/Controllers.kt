import de.ralleytn.plugins.jinput.xinput.XInputEnvironmentPlugin
import net.java.games.input.*
import java.util.*
import kotlin.math.abs


object Controllers : ControllerListener {
    val env = run {
        val x: ControllerEnvironment = XInputEnvironmentPlugin()
        println("Xinput override? ${x.isSupported}")
        if (x.isSupported) x else ControllerEnvironment.getDefaultEnvironment()
    }
    init {
        env.addControllerListener(this)
    }
    private val allInternal = env.controllers
        .onEach { println(it::class.simpleName) }
        .map(::Controller2)
        .toMutableList()
    val all: List<Controller2> get() = allInternal

    val gamepads get() = all.filter { it.type == Controller.Type.GAMEPAD }
    val keyboards get() = all.filter { it.type == Controller.Type.KEYBOARD }
    val mice get() = all.filter { it.type == Controller.Type.MOUSE }

    override fun controllerRemoved(ev: ControllerEvent) {
        allInternal.removeAll { it.controller === ev.controller }
    }

    override fun controllerAdded(ev: ControllerEvent) {
        allInternal.add(Controller2(ev.controller))
    }

    fun step() {
        interested.forEach {
            it.step()
        }
    }

    private val oldButtonStates = WeakHashMap<Component, Float>()
    private val interested = HashSet<Controller2>()
    fun interested(controller: Controller2) = interested.add(controller)
}

fun Controller2.interested() = Controllers.interested(this)


class Controller2(val controller: Controller): Controller by controller {
    @Suppress("UNCHECKED_CAST")
    override fun getComponents(): Array<Component> = components as Array<Component>
    val components = controller.components.map { ConcreteComponent(it) }.toTypedArray()
    val componentMap = components.associateBy { it.component }
    var active: Boolean = false

    fun button(number: Int) = when(number) {
        0 -> components.find { it.identifier == Component.Identifier.Button._0 }
        1 -> components.find { it.identifier == Component.Identifier.Button._1 }
        2 -> components.find { it.identifier == Component.Identifier.Button._2 }
        3 -> components.find { it.identifier == Component.Identifier.Button._3 }
        4 -> components.find { it.identifier == Component.Identifier.Button._4 }
        5 -> components.find { it.identifier == Component.Identifier.Button._5 }
        6 -> components.find { it.identifier == Component.Identifier.Button._6 }
        7 -> components.find { it.identifier == Component.Identifier.Button._7 }
        8 -> components.find { it.identifier == Component.Identifier.Button._8 }
        9 -> components.find { it.identifier == Component.Identifier.Button._9 }
        10 -> components.find { it.identifier == Component.Identifier.Button._10 }
        11 -> components.find { it.identifier == Component.Identifier.Button._11 }
        12 -> components.find { it.identifier == Component.Identifier.Button._12 }
        13 -> components.find { it.identifier == Component.Identifier.Button._13 }
        14 -> components.find { it.identifier == Component.Identifier.Button._14 }
        15 -> components.find { it.identifier == Component.Identifier.Button._15 }
        16 -> components.find { it.identifier == Component.Identifier.Button._16 }
        17 -> components.find { it.identifier == Component.Identifier.Button._17 }
        18 -> components.find { it.identifier == Component.Identifier.Button._18 }
        19 -> components.find { it.identifier == Component.Identifier.Button._19 }
        20 -> components.find { it.identifier == Component.Identifier.Button._20 }
        21 -> components.find { it.identifier == Component.Identifier.Button._21 }
        22 -> components.find { it.identifier == Component.Identifier.Button._22 }
        23 -> components.find { it.identifier == Component.Identifier.Button._23 }
        24 -> components.find { it.identifier == Component.Identifier.Button._24 }
        25 -> components.find { it.identifier == Component.Identifier.Button._25 }
        26 -> components.find { it.identifier == Component.Identifier.Button._26 }
        27 -> components.find { it.identifier == Component.Identifier.Button._27 }
        28 -> components.find { it.identifier == Component.Identifier.Button._28 }
        29 -> components.find { it.identifier == Component.Identifier.Button._29 }
        30 -> components.find { it.identifier == Component.Identifier.Button._30 }
        31 -> components.find { it.identifier == Component.Identifier.Button._31 }
        else -> components.asSequence().filter { it.type == ComponentType.Button }.drop(number).firstOrNull()
    }

    private val primaryVector = Vector()
    val primary: Vector get() = primaryVector.apply {
        x = primaryX?.valueWithoutDeadzone ?: 0f
        y = primaryY?.valueWithoutDeadzone ?: 0f
        if(x.squared() + y.squared() < (primaryX?.deadZone?.squared() ?: 0f)) {
            x = 0f
            y = 0f
        }
    }

    private val secondaryVector = Vector()
    val secondary: Vector get() = secondaryVector.apply { x = secondaryX?.value ?: 0f; y = secondaryY?.value ?: 0f }

    val primaryX: Component2? = components.find { it.identifier == Component.Identifier.Axis.X }
        ?: components.find { it.type == ComponentType.POV }?.let {
            object: Component2 {
                override val valueWithoutDeadzone: Float get() = it.pov.x
                override val lastValueWithoutDeadzone: Float get() = it.lastPov.x
                override fun getDeadZone(): Float = 0f
                override fun getIdentifier(): Component.Identifier = Component.Identifier.Axis.X
                override fun getName(): String = "${it.name} X"
                override fun getPollData(): Float = it.pov.x
                override fun isAnalog(): Boolean = true
                override fun isRelative(): Boolean = false
            }
        }
    val primaryY: Component2? = when {
        name.contains("xbox", true) -> components.find { it.identifier == Component.Identifier.Axis.Y }?.inverted
        else -> components.find { it.identifier == Component.Identifier.Axis.Y } ?: components.find { it.type == ComponentType.POV }?.let {
            object: Component2 {
                override val valueWithoutDeadzone: Float get() = it.pov.y
                override val lastValueWithoutDeadzone: Float get() = it.lastPov.y
                override fun getDeadZone(): Float = 0f
                override fun getIdentifier(): Component.Identifier = Component.Identifier.Axis.Y
                override fun getName(): String = "${it.name} Y"
                override fun getPollData(): Float = it.pov.y
                override fun isAnalog(): Boolean = false
                override fun isRelative(): Boolean = false
            }
        }
    }
    val primaryThumb: Component2? = when {
        name.contains("xbox", true) -> components.find { it.name == "lthumb" }
        name.contains("nintendo", true) && name.contains("fight", true) -> button(10)
        else -> null
    }

    val dPad: Component2? = components.find { it.type == ComponentType.POV }

    val secondaryX: Component2? = when {
        name.contains("xbox", true) -> components.find { it.identifier == Component.Identifier.Axis.RX }
        name.contains("nintendo", true) && name.contains("fight", true) -> components.find { it.identifier == Component.Identifier.Axis.Z }
        else -> components.find { it.identifier == Component.Identifier.Axis.RX }
    }
    val secondaryY: Component2? = when {
        name.contains("xbox", true) -> components.find { it.identifier == Component.Identifier.Axis.RY }?.inverted
        name.contains("nintendo", true) && name.contains("fight", true) -> components.find { it.identifier == Component.Identifier.Axis.RZ }
        else -> components.find { it.identifier == Component.Identifier.Axis.RY }
    }
    val secondaryThumb: Component2? = when {
        name.contains("xbox", true) -> components.find { it.name == "rthumb" }
        name.contains("nintendo", true) && name.contains("fight", true) -> button(11)
        else -> null
    }

    val menu: Component2? = when {
        name.contains("xbox", true) -> components.find { it.name == "start" }
        name.contains("nintendo", true) && name.contains("fight", true) -> button(9)
        else -> button(11)
    }
    val menuAlt: Component2? = when {
        name.contains("xbox", true) -> components.find { it.name == "back" }
        name.contains("nintendo", true) && name.contains("fight", true) -> button(8)
        else -> button(10)
    }
    val system: Component2? = when {
        name.contains("xbox", true) -> null
        name.contains("nintendo", true) && name.contains("fight", true) -> button(12)
        else -> button(12)
    }
    val media: Component2? = when {
        name.contains("xbox", true) -> null
        name.contains("nintendo", true) && name.contains("fight", true) -> button(13)
        else -> button(13)
    }

    val leftBumper: Component2? = when {
        name.contains("xbox", true) -> components.find { it.name == "lb" }
        name.contains("nintendo", true) && name.contains("fight", true) -> button(4)
        else -> button(6)
    }
    val rightBumper: Component2? = when {
        name.contains("xbox", true) -> components.find { it.name == "rb" }
        name.contains("nintendo", true) && name.contains("fight", true) -> button(5)
        else -> button(8)
    }

    val leftTrigger: Component2? = when {
        name.contains("xbox", true) -> components.find { it.name == "lt" }
        name.contains("nintendo", true) && name.contains("fight", true) -> button(6)
        else -> button(7)
    }
    val rightTrigger: Component2? = when {
        name.contains("xbox", true) -> components.find { it.name == "rt" }
        name.contains("nintendo", true) && name.contains("fight", true) -> button(7)
        else -> button(9)
    }

    val primaryAction: Component2? = when {
        name.contains("xbox", true) -> components.find { it.name == "a" }
        name.contains("nintendo", true) && name.contains("fight", true) -> button(2)
        else -> button(0)
    }
    val secondaryAction: Component2? = when {
        name.contains("xbox", true) -> components.find { it.name == "x" }
        name.contains("nintendo", true) && name.contains("fight", true) -> button(1)
        else -> button(1)
    }
    val primaryAlt: Component2? = when {
        name.contains("xbox", true) -> components.find { it.name == "b" }
        name.contains("nintendo", true) && name.contains("fight", true) -> button(3)
        else -> button(2)
    }
    val secondaryAlt: Component2? = when {
        name.contains("xbox", true) -> components.find { it.name == "y" }
        name.contains("nintendo", true) && name.contains("fight", true) -> button(0)
        else -> button(3)
    }
    val ok: Component2? = primaryAction
    val cancel: Component2? = when {
        name.contains("xbox", true) -> listOfNotNull(
            components.find { it.name == "b" },
            components.find { it.name == "x" }
        ).takeUnless { it.isEmpty() }?.let {
            object: Component2 {
                override val valueWithoutDeadzone: Float get() = it.maxOf { it.valueWithoutDeadzone }
                override val lastValueWithoutDeadzone: Float get() = it.maxOf { it.lastValueWithoutDeadzone }
                override fun getDeadZone(): Float = it.maxOf { it.deadZone }
                override fun getIdentifier(): Component.Identifier = it.first().identifier
                override fun getName(): String = "B/X"
                override fun getPollData(): Float = it.maxOf { it.pollData }
                override fun isAnalog(): Boolean = false
                override fun isRelative(): Boolean = false
            }
        }
        else -> secondaryAction
    }

    val event = Event()
    private var ignoreBecauseFirstFrame = true
    fun step() {
        components.forEach { it.lastValueWithoutDeadzone = it.valueWithoutDeadzone }
        controller.poll()
        val q = controller.eventQueue
        while(q.getNextEvent(event)) {
            if(event.component.isRelative)
                componentMap[event.component]!!.valueWithoutDeadzone += event.value
            else
                componentMap[event.component]!!.valueWithoutDeadzone = event.value
        }
        if(ignoreBecauseFirstFrame) ignoreBecauseFirstFrame = false
        else if(!active) active = components.any { abs(it.lastValueWithoutDeadzone - it.valueWithoutDeadzone) > 0.5f }
    }

    init { printInfo() }

    fun printInfo() {
        println("name = ${this.name} (${this.controller::class.simpleName})")
        println("type = ${this.type}")
        println("portType = ${this.portType}")
        println("portNumber = ${this.portNumber}")
        this.rumblers.onEach {
            println("  axisIdentifier = ${it.axisIdentifier?.name}")
            println("  axisName = ${it.axisName}")
        }
        this.components.onEach {
            println("  name = ${it.name}")
            println("    type = ${it.identifier::class.simpleName}")
            println("    identifierName = ${it.identifier?.name}")
            println("    deadZone = ${it.deadZone}")
            println("    pollData = ${it.pollData}")
            println("    isAnalog = ${it.isAnalog}")
            println("    isRelative = ${it.isRelative}")
        }
    }
}

enum class ComponentType {
    Axis, Button, Key, POV, Other
}

interface Component2: Component {
    val valueWithoutDeadzone: Float
    val lastValueWithoutDeadzone: Float
}

class ConcreteComponent(val component: Component): Component2, Component by component {
    override var valueWithoutDeadzone: Float = 0f
    override var lastValueWithoutDeadzone: Float = 0f
}

val Component.type get() = when(this.identifier) {
    is Component.Identifier.Axis -> if(identifier == Component.Identifier.Axis.POV) ComponentType.POV else ComponentType.Axis
    is Component.Identifier.Button -> ComponentType.Button
    is Component.Identifier.Key -> ComponentType.Key
    else -> ComponentType.Other
}
val Component2.down get() = valueWithoutDeadzone > 0.5f
val Component2.lastDown get() = lastValueWithoutDeadzone > 0.5f
val Component2.justPressed get() = !lastDown && down
val Component2.justReleased get() = lastDown && !down

val Component2.value: Float get() = if(abs(valueWithoutDeadzone) < deadZone) 0f else valueWithoutDeadzone
val Component2.lastValue: Float get() = if(abs(lastValueWithoutDeadzone) < deadZone) 0f else lastValueWithoutDeadzone

val Component2.pov get() = PovDirections.fromFloat(valueWithoutDeadzone)
val Component2.lastPov get() = PovDirections.fromFloat(valueWithoutDeadzone)

val Component2.inverted: Component2 get() = object: Component2 by this {
    override val valueWithoutDeadzone: Float get() = -this@inverted.valueWithoutDeadzone
    override val lastValueWithoutDeadzone: Float get() = -this@inverted.lastValueWithoutDeadzone
}

enum class PovDirections(val x: Float, val y: Float) {
    None(0f, 0f),
    NW(-1f, -1f),
    N(0f, -1f),
    NE(1f, -1f),
    E(1f, 0f),
    SE(1f, 1f),
    S(0f, 1f),
    SW(-1f, 1f),
    W(-1f, 0f),
    ;
    companion object {
        fun fromFloat(float: Float): PovDirections = values()[((float + 0.01f) * 8).toInt()]
    }
}