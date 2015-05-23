package ch.epfl.planair.meta;

public final class PipelineConfig {

	public enum Step {
		HUE, BRIGHTNESS, SATURATION, SOBEL
	}

	private final float[] upperConf;
	private final float[] lowerConf;

	public PipelineConfig() {
		this.upperConf = new float[]{ 125/255f, 240/255f, 255/255f,  90/255f };
		this.lowerConf = new float[]{  80/255f,  30/255f,  80/255f,  20/255f };
		assert upperConf.length == lowerConf.length && lowerConf.length == Step.values().length;
	}

	public int lower(Step s) {
		return (int) (lowerConf[s.ordinal()] * 255.999);
	}

	public int upper(Step s) {
		return (int) (upperConf[s.ordinal()] * 255.999);
	}

	public float lowerUnit(Step s) {
		return lowerConf[s.ordinal()];
	}

	public float upperUnit(Step s) {
		return upperConf[s.ordinal()];
	}

	public void lower(Step s, float v) {
		lowerConf[s.ordinal()] = v;
	}

	public void upper(Step s, float v) {
		upperConf[s.ordinal()] = v;
	}

}
