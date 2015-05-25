package ch.epfl.planair.meta;

public final class PipelineConfig {

	public enum Step {
		HUE, BRIGHTNESS, SATURATION, SOBEL
	}

	private final float[] lowerConf;
	private final float[] upperConf;

	public PipelineConfig() {
		this.lowerConf = new float[]{  80/255f,  30/255f,  80/255f,  50/255f };
		this.upperConf = new float[]{ 125/255f, 240/255f, 255/255f,  90/255f };
		assert upperConf.length == lowerConf.length && lowerConf.length == Step.values().length;
	}

	private PipelineConfig(PipelineConfig that) {
		this.lowerConf = new float[that.lowerConf.length];
		this.upperConf = new float[that.lowerConf.length];
		System.arraycopy(that.lowerConf, 0, this.lowerConf, 0, that.lowerConf.length);
		System.arraycopy(that.upperConf, 0, this.upperConf, 0, that.upperConf.length);
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

	public PipelineConfig snapshot() {
		return new PipelineConfig(this);
	}

}