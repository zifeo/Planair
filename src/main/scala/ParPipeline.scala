import processing.core.PApplet

final class ParPipeline(
                    val p: PApplet,
	                 val hueL: Int, val hueH: Int,
	                 val briL: Int, val briH: Int,
	                 val satL: Int, val satH: Int,
	                 val sobel: Int
	                 ) {

	type ITI = Int => Int
	val BLACK = 0xFF000000

	def apply(i: Array[Int], width: Int): Array[Int] = {

		def select(low: Int, value: Int, high: Int)(s: Int => Float): Int = {
			val v = s(value)
			if (low <= v && v <= high) value
			else BLACK
		}

		def hue(): ITI = select(hueL, _, hueH)(p.hue)
		def brigthness(): ITI = select(briL, _, briH)(p.brightness)
		def saturation(): ITI = select(satL, _, satH)(p.saturation)

		???
	}

}
