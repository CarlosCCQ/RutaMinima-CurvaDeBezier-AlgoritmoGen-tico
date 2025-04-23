package com.estudying.mykpc01application

import android.graphics.*
import android.os.Bundle
import android.view.MotionEvent
import android.widget.ImageView
import retrofit2.Callback
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.estudying.mykpc01application.databinding.ActivityMainBinding
import androidx.core.graphics.createBitmap
import retrofit2.Call
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val puntos = mutableListOf<Punto>()
    private var rutaOptima = listOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.imageView.post {
            val width = binding.imageView.width
            val height = binding.imageView.height

            val bitmap = createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            binding.imageView.setImageBitmap(bitmap)

            val paint = Paint().apply {
                color = Color.BLUE
                strokeWidth = 5f
                style = Paint.Style.STROKE
            }

            binding.imageView.setOnTouchListener { v, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    val x = event.x.toInt()
                    val y = event.y.toInt()
                    puntos.add(Punto(x, y))

                    canvas.drawCircle(x.toFloat(), y.toFloat(), 10f, Paint().apply { color = Color.RED })
                    binding.imageView.invalidate()
                    v.performClick()
                }
                true
            }

            binding.btnaccion.setOnClickListener {
                if (puntos.size < 2) {
                    Toast.makeText(this, "Debe tocar al menos 2 puntos", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val parametros = listOf(
                    puntos.size.toFloat(),
                    50f,
                    0.05f,
                    100f
                )

                val requestData = RequestData(parametros, arregloPuntos(puntos))
                val call = RetrofitRuta.api.predict(requestData)

                call.enqueue(object : Callback<ResponseData> {
                    override fun onResponse(call: Call<ResponseData>, response: Response<ResponseData>) {
                        if (response.isSuccessful) {
                            rutaOptima = response.body()?.prediction ?: listOf()
                            binding.lblmejordistancia.text =
                                getString(R.string.ruta_resultado, rutaOptima.joinToString(" - "))
                            dibujarRuta(canvas, paint)
                            binding.imageView.invalidate()
                        } else {
                            Toast.makeText(applicationContext, "Error respuesta", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<ResponseData>, t: Throwable) {
                        Toast.makeText(applicationContext, "Error conexión: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }
    }

    private fun arregloPuntos(puntos: List<Punto>): IntArray {
        val coords = IntArray(puntos.size * 2)
        puntos.forEachIndexed { i, p ->
            coords[i * 2] = p.x
            coords[i * 2 + 1] = p.y
        }
        return coords
    }

    private fun dibujarRuta(canvas: Canvas, paint: Paint) {
        canvas.drawColor(Color.WHITE)

        for (p in puntos) {
            canvas.drawCircle(p.x.toFloat(), p.y.toFloat(), 10f, Paint().apply { color = Color.RED })
        }

        val puntosRuta = rutaOptima.map { puntos[it] }

        for (i in 0 until puntosRuta.size - 1) {
            val p1 = puntosRuta[i]
            val p2 = puntosRuta[i + 1]
            canvas.drawLine(
                p1.x.toFloat(), p1.y.toFloat(),
                p2.x.toFloat(), p2.y.toFloat(),
                paint // este es azul con grosor 5
            )
        }

        dibujarBezierGeneral(canvas, puntosRuta)
    }

    private fun dibujarBezierGeneral(canvas: Canvas, ruta: List<Punto>) {
        if (ruta.isEmpty()) return

        val path = Path()
        path.moveTo(ruta[0].x.toFloat(), ruta[0].y.toFloat())

        val steps = 100
        for (t in 1..steps) {
            val tNorm = t / steps.toFloat()
            val punto = calcularBezier(ruta, tNorm)
            path.lineTo(punto.x.toFloat(), punto.y.toFloat())
        }

        val paintBezier = Paint().apply {
            color = Color.MAGENTA
            strokeWidth = 3f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }

        canvas.drawPath(path, paintBezier)
    }

    private fun calcularBezier(puntos: List<Punto>, t: Float): Punto {
        val n = puntos.size
        val copia = puntos.map { Punto(it.x, it.y) }.toMutableList()

        for (r in 1 until n) {
            for (i in 0 until n - r) {
                copia[i].x = ((1 - t) * copia[i].x + t * copia[i + 1].x).toInt()
                copia[i].y = ((1 - t) * copia[i].y + t * copia[i + 1].y).toInt()
            }
        }

        return copia[0]
    }
}
