
package com.mytools.app

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.*
import android.text.InputType

class MainActivity : Activity() {
    private val prefs by lazy {
        getSharedPreferences("mytools", MODE_PRIVATE)
    }
    private lateinit var root: LinearLayout
    private val blue = Color.rgb(45, 105, 220)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showHome()
    }

    private fun setup(title: String) {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 20, 20, 20)
            setBackgroundColor(Color.rgb(246, 248, 252))
        }
        val scroll = ScrollView(this)
        scroll.addView(root)
        setContentView(scroll)

        addText(title, 26f, true)
        addText("Всё нужное — в одном месте", 14f, false)
    }

    private fun addText(
        text: String, size: Float, bold: Boolean
    ) {
        root.addView(TextView(this).apply {
            this.text = text
            textSize = size
            setTextColor(Color.rgb(35, 45, 65))
            if (bold) setTypeface(null, 1)
            setPadding(4, 12, 4, 12)
        })
    }

    private fun button(text: String, action: () -> Unit) {
        root.addView(Button(this).apply {
            this.text = text
            isAllCaps = false
            setOnClickListener { action() }
        })
    }

    private fun field(hint: String, multi: Boolean = false): EditText {
        return EditText(this).apply {
            this.hint = hint
            textSize = 16f
            setTextColor(Color.BLACK)
            setHintTextColor(Color.GRAY)
            setPadding(12, 8, 12, 8)
            if (multi) {
                minLines = 3
                gravity = Gravity.TOP
            }
            root.addView(this)
        }
    }

    private fun showHome() {
        setup("Мои инструменты")
        addText("Выбери, что нужно сделать:", 18f, true)
        button("📝  Заметки") { showNotes() }
        button("✅  Список дел") { showTasks() }
        button("🧮  Калькулятор") { showCalculator() }
        addText("Версия 1.0 • Всё просто и удобно", 13f, false)
    }

    private fun backButton() {
        button("← На главную") { showHome() }
    }

    private fun showNotes() {
        setup("📝 Заметки")
        val editor = field("Напиши свою заметку...", true)
        editor.setText(prefs.getString("notes", ""))

        button("Сохранить заметки") {
            prefs.edit().putString(
                "notes", editor.text.toString()
            ).apply()
            Toast.makeText(
                this, "Заметки сохранены!", Toast.LENGTH_SHORT
            ).show()
        }
        backButton()
    }

    private fun showTasks() {
        setup("✅ Список дел")
        val input = field("Новая задача")
        val list = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        root.addView(list)

        fun refresh() {
            list.removeAllViews()
            val tasks = prefs.getStringSet(
                "tasks", emptySet()
            )!!.toMutableSet()

            tasks.sorted().forEach { task ->
                val check = CheckBox(this).apply {
                    text = task
                    textSize = 16f
                    setTextColor(Color.DKGRAY)
                }
                list.addView(check)
            }
        }

        button("＋ Добавить задачу") {
            val task = input.text.toString().trim()
            if (task.isNotEmpty()) {
                val tasks = prefs.getStringSet(
                    "tasks", emptySet()
                )!!.toMutableSet()
                tasks.add(task)
                prefs.edit().putStringSet("tasks", tasks).apply()
                input.text.clear()
                refresh()
            }
        }

        button("Удалить отмеченные") {
            val tasks = prefs.getStringSet(
                "tasks", emptySet()
            )!!.toMutableSet()
            val checks = (0 until list.childCount)
                .map { list.getChildAt(it) as CheckBox }
            checks.filter { it.isChecked }.forEach {
                tasks.remove(it.text.toString())
            }
            prefs.edit().putStringSet("tasks", tasks).apply()
            refresh()
        }

        refresh()
        backButton()
    }

    private fun showCalculator() {
        setup("🧮 Калькулятор")
        val first = field("Первое число")
        first.inputType = InputType.TYPE_CLASS_NUMBER or
            InputType.TYPE_NUMBER_FLAG_DECIMAL or
            InputType.TYPE_NUMBER_FLAG_SIGNED

        val second = field("Второе число")
        second.inputType = first.inputType

        val result = TextView(this).apply {
            text = "Результат появится здесь"
            textSize = 21f
            setTextColor(blue)
            setPadding(4, 20, 4, 20)
        }
        root.addView(result)

        val operations = listOf(
            "+" to { a: Double, b: Double -> a + b },
            "-" to { a: Double, b: Double -> a - b },
            "×" to { a: Double, b: Double -> a * b },
            "÷" to { a: Double, b: Double ->
                if (b == 0.0) Double.NaN else a / b
            }
        )

        operations.forEach { (symbol, calculate) ->
            button(symbol) {
                val a = first.text.toString().toDoubleOrNull()
                val b = second.text.toString().toDoubleOrNull()

                if (a == null || b == null) {
                    result.text = "Введи оба числа"
                } else if (symbol == "÷" && b == 0.0) {
                    result.text = "На ноль делить нельзя!"
                } else {
                    result.text = "Результат: ${calculate(a, b)}"
                }
            }
        }

        backButton()
    }
}
