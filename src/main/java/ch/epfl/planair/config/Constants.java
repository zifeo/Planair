package ch.epfl.planair.config;


public interface Constants {

	// scores.Scoreboard
	int SCOREBOARD_PADDING          = 5;
	int SCOREBOARD_FONT_SIZE        = 11;
	int SCOREBOARD_FONT_HEIGHT      = 15;
	int SCOREBOARD_TIME_CHART_BASE  = 45;
	int SCOREBOARD_SCROLL_HEIGHT    = 10;

	// specs.Accelerable
	float ACCELERABLE_NORMAL_FORCE  = 1;
	float ACCELERABLE_G             = 0.1f;
	float ACCELERABLE_MU            = 0.03f;

	// Planair
	boolean DEBUG              = true;
	int PLATE_SIZE             = 250;
	int PLATE_THICKNESS        = 10;
	int SPHERE_RADIUS          = 10;
	int CYLINDER_HEIGHT        = 20;
	int CYLINDER_RADIUS        = 15;
	int CYLINDER_RESOLUTION    = 8;
	int SCOREBOARD_HEIGHT      = 100;
	int WINDOWS_WIDTH          = 500;
	int WINDOWS_HEIGHT         = 600;
	int FRAMERATE              = 60;
	float PI_3                 = (float) Math.PI/3;
	int EYE_HEIGHT             = 200;

	// visual.Pipeline
	float PIPELINE_DISCRETIZATION_STEPS_PHI = 0.06f;
	float PIPELINE_DISCRETIZATION_STEPS_R = 2.5f;

}
