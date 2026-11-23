package com.example.engine

import org.json.JSONObject

data class TouchButtonPlacement(
    val id: String,
    val label: String,
    val xFraction: Float,
    val yFraction: Float,
    val scale: Float = 1f,
    val alpha: Float = 0.92f,
    val mappedKeyCode: String = "",
)

data class ControlLayoutConfig(
    val joystickX: Float = 0.22f,
    val joystickY: Float = 0.56f,
    val joystickScale: Float = 1.0f,
    val joystickAlpha: Float = 0.90f,
    val buttons: List<TouchButtonPlacement> = defaultButtons(),
    val gamepadMap: Map<String, String> = defaultGamepadMap(),
) {
    fun toJson(): String {
        val root = JSONObject()
        root.put("joystickX", joystickX.toDouble())
        root.put("joystickY", joystickY.toDouble())
        root.put("joystickScale", joystickScale.toDouble())
        root.put("joystickAlpha", joystickAlpha.toDouble())

        val btnArray = org.json.JSONArray()
        buttons.forEach { btn ->
            val obj = JSONObject()
            obj.put("id", btn.id)
            obj.put("label", btn.label)
            obj.put("xFraction", btn.xFraction.toDouble())
            obj.put("yFraction", btn.yFraction.toDouble())
            obj.put("scale", btn.scale.toDouble())
            obj.put("alpha", btn.alpha.toDouble())
            obj.put("mappedKeyCode", btn.mappedKeyCode)
            btnArray.put(obj)
        }
        root.put("buttons", btnArray)

        val gpObj = JSONObject()
        gamepadMap.forEach { (k, v) -> gpObj.put(k, v) }
        root.put("gamepadMap", gpObj)
        return root.toString()
    }

    companion object {
        fun defaultButtons(): List<TouchButtonPlacement> = listOf(
            TouchButtonPlacement("A", "A", 0.78f, 0.40f, 1.0f, 0.95f, "KEY_SPACE"),
            TouchButtonPlacement("X", "X", 0.69f, 0.56f, 1.0f, 0.95f, "KEY_SHIFT"),
            TouchButtonPlacement("B", "B", 0.87f, 0.56f, 1.0f, 0.95f, "KEY_ESC"),
            TouchButtonPlacement("Y", "Y", 0.78f, 0.72f, 1.0f, 0.95f, "KEY_ENTER"),
        )

        fun defaultGamepadMap(): Map<String, String> = mapOf(
            "BUTTON_A" to "Accelerate / Confirm (Space)",
            "BUTTON_B" to "Brake / Back (Esc)",
            "BUTTON_X" to "Nitro / Boost (Shift)",
            "BUTTON_Y" to "Menu / Enter (Enter)",
            "DPAD_LEFT" to "Steer Left (A / Left)",
            "DPAD_RIGHT" to "Steer Right (D / Right)",
            "BUTTON_START" to "Pause Drawer",
        )

        fun fromJson(json: String): ControlLayoutConfig {
            return try {
                val root = JSONObject(json)
                val btnList = mutableListOf<TouchButtonPlacement>()
                val arr = root.optJSONArray("buttons")
                if (arr != null) {
                    for (i in 0 until arr.length()) {
                        val o = arr.getJSONObject(i)
                        btnList.add(
                            TouchButtonPlacement(
                                id = o.optString("id", "A"),
                                label = o.optString("label", "A"),
                                xFraction = o.optDouble("xFraction", 0.75).toFloat(),
                                yFraction = o.optDouble("yFraction", 0.5).toFloat(),
                                scale = o.optDouble("scale", 1.0).toFloat(),
                                alpha = o.optDouble("alpha", 0.92).toFloat(),
                                mappedKeyCode = o.optString("mappedKeyCode", "KEY_SPACE"),
                            )
                        )
                    }
                }
                val gpMap = mutableMapOf<String, String>()
                val gpObj = root.optJSONObject("gamepadMap")
                if (gpObj != null) {
                    val keys = gpObj.keys()
                    while (keys.hasNext()) {
                        val k = keys.next()
                        gpMap[k] = gpObj.optString(k, "")
                    }
                }
                ControlLayoutConfig(
                    joystickX = root.optDouble("joystickX", 0.22).toFloat(),
                    joystickY = root.optDouble("joystickY", 0.56).toFloat(),
                    joystickScale = root.optDouble("joystickScale", 1.0).toFloat(),
                    joystickAlpha = root.optDouble("joystickAlpha", 0.9).toFloat(),
                    buttons = if (btnList.isEmpty()) defaultButtons() else btnList,
                    gamepadMap = if (gpMap.isEmpty()) defaultGamepadMap() else gpMap,
                )
            } catch (_: Exception) {
                ControlLayoutConfig()
            }
        }
    }
}
