package ch.epfl.planair.meta;

public interface Consts {

	// styles
	String LOGO = "Planair";
	int COLOR1  = 0xFF2C3E50;
	int COLOR2  = 0xFF34495E;
	int COLOR3  = 0xFF7F8C8D;
	int COLOR4  = 0xFF95A5A6;
	int COLORBG = 0xFFCCCCCC;
	int RED     = 0xFFC0392B;
	int WHITE   = 0xFFFFFFFF;
	int BLACK   = 0xFF000000;
	int NOALPHA = 0x00000000;
	String FONT = "fonts/SF-Archery-Black/SF_Archery_Black.ttf";

	// Planair
	boolean DEBUG              = true;
	int FRAMERATE              = 60;
	int EYE_HEIGHT             = 200;
	int CAMERA_WIDTH           = 640;
	int CAMERA_HEIGHT          = 480;
	int CAMERA_FPS             = 30;

	// mods.menu
	int MENU_WIDTH          = 400;
	int MENU_HEIGHT         = 500;
	int MENU_ITEM_HEIGHT    = 35;
	int MENU_ITEM_MARGIN    = 15;
	int MENU_HEIGHT_CENTER  = 100;

	// mods.play
	int PLATE_SIZE             = 600;
	int PLATE_THICKNESS        = 10;
	int SCOREBOARD_HEIGHT      = 100;
	float MOTION_FACTOR        = 1.5f;
	int TREE_RADIUS            = 15;

	// mods.obstacles
	int SPHERE_RADIUS          = 5;
	int HOLDER_RADIUS          = 15;

	// visual.pipeline
	float PIPELINE_DISCRETIZATION_STEPS_PHI = 0.03f;
	float PIPELINE_DISCRETIZATION_STEPS_R = 2f;
	int PIPELINE_DETECT_OFFSET = 150;
	int PIPELINE_LINES_COUNT = 6;
	float GRAPH_NON_FLAT_QUAD_MIN_COS = 1;

	// scores.Scoreboard
	int SCOREBOARD_PADDING          = 5;
	int SCOREBOARD_FONT_SIZE        = 11;
	int SCOREBOARD_FONT_HEIGHT      = 15;
	int SCOREBOARD_TIME_CHART_BASE  = 45;
	int SCROLL_HEIGHT = 20;

	// specs.Accelerable
	float ACCELERABLE_NORMAL_FORCE  = 1f;
	float ACCELERABLE_G             = 0.1f;
	float ACCELERABLE_MU            = 0.03f;


}
