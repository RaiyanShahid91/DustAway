import com.raiyanshahid.dustaway.*
import java.awt.AlphaComposite
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

// args: scene.png outDir frames pad direction wind scatter [distance] [sweep]
fun main(a: Array<String>) {
    val img = ImageIO.read(File(a[0]))
    val out = File(a[1]).apply { mkdirs() }
    val frames = a[2].toInt(); val pad = a[3].toInt()
    val cfg = DustConfig(
        direction = SweepDirection.valueOf(a[4]), windAngleDegrees = a[5].toFloat(), scatter = a[6].toFloat(),
        distanceFraction = a.getOrNull(7)?.toFloat() ?: 0.45f, sweepFraction = a.getOrNull(8)?.toFloat() ?: 0.6f,
    )
    val w = img.width; val h = img.height
    val px = IntArray(w * h); img.getRGB(0, 0, w, h, px, 0, w)
    val dust = DustEngine.build(px, w, h, cfg)
    val s = DustSample()
    for (f in 0 until frames) {
        val p = f / (frames - 1f)
        val canvas = BufferedImage(w + 2 * pad, h + 2 * pad, BufferedImage.TYPE_INT_ARGB)
        val g = canvas.createGraphics()
        if (p == 0f) g.drawImage(img, pad, pad, null) else for (i in 0 until dust.count) {
            if (!dust.evaluate(i, p, s)) continue
            val c = Color(s.color, true)
            g.composite = AlphaComposite.SrcOver
            g.color = Color(c.red, c.green, c.blue, (c.alpha * s.alpha).toInt().coerceIn(0, 255))
            g.fillRect((s.x + pad).toInt(), (s.y + pad).toInt(), Math.ceil(s.size.toDouble()).toInt(), Math.ceil(s.size.toDouble()).toInt())
        }
        g.dispose()
        ImageIO.write(canvas, "png", File(out, "f%03d.png".format(f)))
    }
    println("${dust.count} particles, cell ${dust.cellSize}")
}
